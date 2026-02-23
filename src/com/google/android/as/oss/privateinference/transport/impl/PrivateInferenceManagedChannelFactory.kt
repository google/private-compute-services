/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.`as`.oss.privateinference.transport.impl

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresExtension
import com.google.android.`as`.oss.common.ExecutorAnnotations.PiExecutorQualifier
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.Annotations.PiServerChannelIdleTimeoutMinutes
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEndpointUrl
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceProxyConfiguration
import com.google.android.`as`.oss.privateinference.config.impl.ProxyAuthFlag
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.logging.MetricIdMap
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.android.`as`.oss.privateinference.transport.ManagedChannelFactory
import com.google.android.`as`.oss.privateinference.transport.ProxyConfigManager
import com.google.android.`as`.oss.privateinference.transport.ProxyConfiguration
import com.google.android.`as`.oss.privateinference.transport.TransportConstants.HTTPS_PORT
import com.google.android.`as`.oss.privateinference.transport.TransportConstants.MAX_INBOUND_MESSAGE_SIZE_BYTES
import com.google.android.`as`.oss.privateinference.transport.TransportFlag
import com.google.android.`as`.oss.privateinference.util.timers.Annotations.PrivateInferenceClientTimers
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import com.google.android.`as`.oss.privateinference.util.timers.Timers
import com.google.common.flogger.GoogleLogger
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.privacy.ppn.proto.PrivacyPassTokenData
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.cronet.CronetChannelBuilder
import io.grpc.okhttp.OkHttpChannelBuilder
import java.util.Optional
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.chromium.net.Proxy
import org.chromium.net.ProxyOptions
import org.chromium.net.impl.HttpEngineNativeProvider
import org.chromium.net.impl.NativeCronetProvider

@Singleton
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
class PrivateInferenceManagedChannelFactory
@Inject
constructor(
  @ApplicationContext private val context: Context,
  @PrivateInferenceEndpointUrl private val endpointUrl: String,
  @PiServerChannelIdleTimeoutMinutes private val channelIdleTimeoutMinutes: Long,
  @PrivateInferenceProxyConfiguration private val proxyConfigManager: Optional<ProxyConfigManager>,
  private val transportFlag: TransportFlag,
  private val bsaProxyTokenProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
  @PiExecutorQualifier private val backgroundExecutor: ListeningExecutorService,
  @param:PrivateInferenceClientTimers private val timers: TimerSet,
  private val proxyAuthFlag: ProxyAuthFlag,
  private val pcsStatsLogger: PcsStatsLogger,
) : ManagedChannelFactory {

  private val mutex = Mutex()
  private var managedChannelInstance: ManagedChannel? = null

  override suspend fun getInstance(): ManagedChannel {
    return getManagedChannelInstance()
  }

  private suspend fun getManagedChannelInstance(): ManagedChannel =
    mutex.withLock { managedChannelInstance ?: create().also { managedChannelInstance = it } }

  private suspend fun create(): ManagedChannel {
    logger
      .atInfo()
      .log(
        "Creating gRPC channel for Private Inference server at %s with transport mode %s",
        endpointUrl,
        transportFlag.mode(),
      )
    val managedChannelBuilder: ManagedChannelBuilder<*> =
      when (transportFlag.mode()) {
        TransportFlag.Mode.OK_HTTP -> OkHttpChannelBuilder.forAddress(endpointUrl, HTTPS_PORT)
        TransportFlag.Mode.CRONET_MAINLINE -> {
          val engine = HttpEngineNativeProvider(context).createBuilder().build()
          CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engine)
        }
        TransportFlag.Mode.CRONET_STATIC -> {
          val engine = NativeCronetProvider(context).createBuilder().build()
          CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engine)
        }
        TransportFlag.Mode.CRONET_STATIC_IP_RELAY -> {
          val engineBuilder = NativeCronetProvider(context).createBuilder()
          if (proxyConfigManager.isPresent) {
            val proxyConfig = proxyConfigManager.get().getProxyConfig()
            logger.atInfo().log("Setting proxy options: %s", proxyConfig.toLogString)
            engineBuilder.setProxyOptions(
              ProxyOptions.fromProxyList(
                proxyConfig.map { config ->
                  Proxy.createHttpProxy(
                    Proxy.SCHEME_HTTPS,
                    config.host,
                    config.port,
                    // Executor where proxy callbacks will be invoked.
                    { r -> r.run() },
                    getProxyCallback(config),
                  )
                }
              )
            )
          }
          CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engineBuilder.build())
        }
        else -> ManagedChannelBuilder.forTarget(endpointUrl)
      }
    return managedChannelBuilder
      .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE_BYTES)
      .idleTimeout(channelIdleTimeoutMinutes, TimeUnit.MINUTES)
      .build()
  }

  private val List<ProxyConfiguration>.toLogString: String
    get() = map { "${it.host}:${it.port}" }.joinToString(",")

  private fun getProxyCallback(proxyConfiguration: ProxyConfiguration) =
    object : Proxy.HttpConnectCallback() {
      var masqueTunnelSetupTimer: Timers.Timer? = null
      var startTime: Long? = null

      override fun onBeforeRequest(request: Request) {
        request.use {
          startTime = System.currentTimeMillis()
          masqueTunnelSetupTimer =
            timers.start(PrivateInferenceClientTimerNames.IPP_MASQUE_TUNNEL_SETUP)
          val authHeader = getProxyTokenAuthHeader(proxyConfiguration)
          logger.atFine().log("onBeforeRequest: %s", authHeader)
          // Cancel the request if proxy token is not available. This avoids sending the request
          // with empty auth header and getting an auth failure from the proxy server.
          if (authHeader == null) {
            return
          }
          it.proceed(
            listOf<android.util.Pair<String, String>>(
              android.util.Pair("authorization", authHeader)
            )
          )
        }
      }

      override fun onResponseReceived(
        responseHeaders: List<android.util.Pair<String, String>>,
        statusCode: Int,
      ): Int {
        logger.atInfo().log("onResponseReceived: %s statusCode: %s", responseHeaders, statusCode)
        masqueTunnelSetupTimer?.stop()
        startTime?.let {
          logMasqueTunnelSetupEventMetrics(
            statusCode,
            proxyConfiguration.host,
            System.currentTimeMillis() - it,
          )
        }
        return Proxy.HttpConnectCallback.RESPONSE_ACTION_PROCEED
      }
    }

  private fun logMasqueTunnelSetupEventMetrics(statusCode: Int, host: String, latencyMs: Long) {
    val isSuccess = statusCode == 200
    val isFastly = "fastly" in host
    val isCloudflare = "cloudflare" in host

    val metricsPair: Pair<CountMetricId, ValueMetricId>? =
      when {
        isSuccess && isFastly ->
          Pair(
            CountMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_FASTLY_SETUP_SUCCESS,
            ValueMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_FASTLY_SETUP_SUCCESS_LATENCY_MS,
          )
        isSuccess && isCloudflare ->
          Pair(
            CountMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_CLOUDFLARE_SETUP_SUCCESS,
            ValueMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_CLOUDFLARE_SETUP_SUCCESS_LATENCY_MS,
          )
        !isSuccess && isFastly ->
          Pair(
            CountMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_FASTLY_SETUP_FAILURE,
            ValueMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_FASTLY_SETUP_FAILURE_LATENCY_MS,
          )
        !isSuccess && isCloudflare ->
          Pair(
            CountMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_CLOUDFLARE_SETUP_FAILURE,
            ValueMetricId.PCS_PI_IPP_MASQUE_TUNNEL_VIA_CLOUDFLARE_SETUP_FAILURE_LATENCY_MS,
          )
        else -> null
      }

    metricsPair?.let { (countMetricId, latencyMetricId) ->
      pcsStatsLogger.logEventCount(countMetricId)
      pcsStatsLogger.logEventLatency(latencyMetricId, latencyMs)
    }
  }

  // TODO: Make this a suspend function once we have a async support for proxy
  // callback functions.
  private fun getProxyTokenAuthHeader(proxyConfiguration: ProxyConfiguration): String? {
    if (proxyAuthFlag.mode() == ProxyAuthFlag.Mode.BASIC) {
      return proxyConfiguration.authHeader
    }
    try {
      val tokenData =
        pcsStatsLogger.getResultAndLogStatus(METRIC_ID_MAP) {
          timers.start(PrivateInferenceClientTimerNames.IPP_GET_PROXY_TOKEN).use {
            PrivacyPassTokenData.parseFrom(
              bsaProxyTokenProvider
                .fetchTokenFuture(backgroundExecutor, ProxyTokenParams())
                .get()
                .bytes
                .toByteArray()
            )
          }
        }
      return "PrivateToken token=\"${tokenData.token}\" extensions=\"${tokenData.encodedExtensions}\""
    } catch (e: ExecutionException) {
      logger.atSevere().withCause(e.cause).log("Failed to fetch proxy token")
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to fetch proxy token")
    }
    return null
  }

  companion object {
    init {
      System.loadLibrary("pi_client_session_config_jni")
    }

    private val METRIC_ID_MAP =
      MetricIdMap(
        CountMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_SUCCESS,
        CountMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_FAILURE,
        ValueMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_SUCCESS_LATENCY_MS,
        ValueMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_FAILURE_LATENCY_MS,
      )

    private val logger = GoogleLogger.forEnclosingClass()
  }
}
