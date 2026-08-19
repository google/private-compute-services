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
import android.os.ext.SdkExtensions
import androidx.annotation.RequiresExtension
import com.google.android.`as`.oss.common.ExecutorAnnotations.PiExecutorQualifier
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.Annotations.PiServerChannelIdleTimeoutMinutes
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEnableConfigurableIpBlindingMode
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEndpointUrl
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceForceIpTunnelCreationForEverySession
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceProxyConfiguration
import com.google.android.`as`.oss.privateinference.config.impl.ProxyAuthFlag
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.logging.MetricIdMap
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.android.`as`.oss.privateinference.service.api.proto.IpBlindingMode
import com.google.android.`as`.oss.privateinference.service.api.proto.SessionConfiguration
import com.google.android.`as`.oss.privateinference.service.api.proto.sessionConfiguration
import com.google.android.`as`.oss.privateinference.transport.IpRelayFallbackFlag
import com.google.android.`as`.oss.privateinference.transport.ManagedChannelFactory
import com.google.android.`as`.oss.privateinference.transport.ProxyConfigManager
import com.google.android.`as`.oss.privateinference.transport.ProxyConfiguration
import com.google.android.`as`.oss.privateinference.transport.TransportConstants.HTTPS_PORT
import com.google.android.`as`.oss.privateinference.transport.TransportConstants.MAX_INBOUND_MESSAGE_SIZE_BYTES
import com.google.android.`as`.oss.privateinference.transport.TransportFlag
import com.google.android.`as`.oss.privateinference.transport.unusable.UnusableManagedChannel
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
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.chromium.net.CronetEngine
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
  @PrivateInferenceForceIpTunnelCreationForEverySession
  private val forceIpTunnelCreationForEverySession: Boolean,
  @PrivateInferenceProxyConfiguration private val proxyConfigManager: Optional<ProxyConfigManager>,
  private val transportFlag: TransportFlag,
  private val ipRelayFallbackFlag: IpRelayFallbackFlag,
  private val bsaProxyTokenProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
  @PiExecutorQualifier private val backgroundExecutor: ListeningExecutorService,
  @param:PrivateInferenceClientTimers private val timers: TimerSet,
  private val proxyAuthFlag: ProxyAuthFlag,
  private val pcsStatsLogger: PcsStatsLogger,
  @PrivateInferenceEnableConfigurableIpBlindingMode
  private val enableConfigurableIpBlindingMode: Boolean,
) : ManagedChannelFactory {

  private val mutex = Mutex()
  private val managedChannelInstances: MutableMap<IpBlindingMode, ManagedChannel> = mutableMapOf()
  private var cronetEngineInstances: MutableMap<IpBlindingMode, CronetEngine> = mutableMapOf()

  override suspend fun getInstance(sessionConfiguration: SessionConfiguration): ManagedChannel {
    val config =
      if (sessionConfiguration.ipBlindingMode == IpBlindingMode.IP_BLINDING_MODE_UNSPECIFIED) {
        logger
          .atInfo()
          .log(
            "IpBlindingMode in SessionInitializationRequest is unspecified, falling back to " +
              "IP_BLINDING_MODE_ENABLED."
          )
        sessionConfiguration { ipBlindingMode = IpBlindingMode.IP_BLINDING_MODE_ENABLED }
      } else {
        sessionConfiguration
      }
    return getManagedChannelInstance(config)
  }

  private suspend fun getManagedChannelInstance(
    sessionConfiguration: SessionConfiguration
  ): ManagedChannel = mutex.withLock {
    val ipBlindingMode =
      if (enableConfigurableIpBlindingMode) {
        sessionConfiguration.ipBlindingMode
      } else {
        if (sessionConfiguration.ipBlindingMode == IpBlindingMode.IP_BLINDING_MODE_DISABLED) {
          logger
            .atInfo()
            .log(
              "IpBlindingMode in SessionInitializationRequest is ignored because " +
                "EnableConfigurableIpBlindingMode flag is disabled."
            )
        }
        IpBlindingMode.IP_BLINDING_MODE_ENABLED
      }
    // TODO: [forceIpTunnelCreationForEverySession] is a debug only flag to force IP
    // blinding tunnel creation for every session. Refactor it to be generic to non-IP blinding
    // tunnels too.
    if (
      forceIpTunnelCreationForEverySession &&
        ipBlindingMode == IpBlindingMode.IP_BLINDING_MODE_ENABLED
    ) {
      logger.atInfo().log("Shutting down any previous IP relay ManagedChannel or CronetEngine.")
      managedChannelInstances.get(ipBlindingMode)?.shutdown()
      cronetEngineInstances.get(ipBlindingMode)?.shutdown()
      managedChannelInstances.remove(ipBlindingMode)
      cronetEngineInstances.remove(ipBlindingMode)
    }
    managedChannelInstances.get(ipBlindingMode)
      ?: create(sessionConfiguration).also {
        if (it is UnusableManagedChannel) {
          logger.atWarning().log("Created UnusableManagedChannel, will not cache it.")
        } else {
          managedChannelInstances.put(ipBlindingMode, it)
        }
      }
  }

  private fun proxyApisAvailable(): Boolean =
    // Protects the call against SdkExtensions, which was introduced in API level 30 (R)
    // http://[redacted]
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
      // HttpEngine's proxy APIs are available from API level 31 (S) with extension version 22.
      // See
      // http://[redacted]
      SdkExtensions.getExtensionVersion(Build.VERSION_CODES.S) >= 22

  private suspend fun create(sessionConfiguration: SessionConfiguration): ManagedChannel {
    val mode = transportFlag.mode()
    logger
      .atInfo()
      .log(
        "Creating gRPC channel with IP blinding mode %s for Private Inference server at %s with " +
          "transport mode %s",
        sessionConfiguration.ipBlindingMode,
        endpointUrl,
        mode,
      )

    if (sessionConfiguration.ipBlindingMode == IpBlindingMode.IP_BLINDING_MODE_DISABLED) {
      return createCronetMainlineChannelBuilder().build()
    }

    val isIpRelayMode =
      mode == TransportFlag.Mode.CRONET_MAINLINE_IP_RELAY ||
        mode == TransportFlag.Mode.CRONET_STATIC_IP_RELAY

    if (!isIpRelayMode) {
      logger.atWarning().log("Private Inference is not going over an IP Blinding tunnel.")
    }

    val proxyConfig =
      if (isIpRelayMode) {
        if (!proxyConfigManager.isPresent) {
          logger
            .atWarning()
            .log("ProxyConfigManager is not present in PrivateInferenceManagedChannelFactory.")
        }
        val config = proxyConfigManager.orElse(null)?.getProxyConfig() ?: emptyList()
        if (config.isEmpty()) {
          logger
            .atWarning()
            .log("Proxy configuration is empty for %s. Returning UnusableManagedChannel.", mode)
          return UnusableManagedChannel("Proxy configuration is empty for $mode")
        }
        config
      } else {
        emptyList()
      }

    val managedChannelBuilder: ManagedChannelBuilder<*> =
      when (mode) {
        TransportFlag.Mode.OK_HTTP -> OkHttpChannelBuilder.forAddress(endpointUrl, HTTPS_PORT)
        TransportFlag.Mode.CRONET_MAINLINE -> createCronetMainlineChannelBuilder()
        TransportFlag.Mode.CRONET_MAINLINE_IP_RELAY ->
          when {
            ipRelayFallbackFlag.mode == IpRelayFallbackFlag.Mode.FORCE_STATIC -> {
              logger
                .atInfo()
                .log(
                  "Forcing CRONET_STATIC_IP_RELAY for IP relay support due to experiment override."
                )
              createIpRelayChannelBuilder(CronetProviderType.STATIC, proxyConfig = proxyConfig)
            }
            !proxyApisAvailable() -> {
              logger
                .atInfo()
                .log(
                  "Falling back to CRONET_STATIC_IP_RELAY for IP relay support as Proxy APIs are not available."
                )
              createIpRelayChannelBuilder(CronetProviderType.STATIC, proxyConfig = proxyConfig)
            }
            else ->
              createIpRelayChannelBuilder(
                CronetProviderType.MAINLINE,
                fallbackOnError = true,
                proxyConfig = proxyConfig,
              )
          }
        TransportFlag.Mode.CRONET_STATIC -> {
          // TODO: This is a corner case expected to be unreachable. We no longer cache
          // the CronetEngine instances. Will refactor the logic to separate CronetEngine and IP
          // blinding mode.
          val engine = NativeCronetProvider(context).createBuilder().build()
          CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engine)
        }
        TransportFlag.Mode.CRONET_STATIC_IP_RELAY ->
          createIpRelayChannelBuilder(CronetProviderType.STATIC, proxyConfig = proxyConfig)
        else -> ManagedChannelBuilder.forTarget(endpointUrl)
      }
    return managedChannelBuilder
      .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE_BYTES)
      .idleTimeout(channelIdleTimeoutMinutes, TimeUnit.MINUTES)
      .build()
  }

  private fun createCronetMainlineChannelBuilder(): ManagedChannelBuilder<*> {
    val engine =
      cronetEngineInstances.get(IpBlindingMode.IP_BLINDING_MODE_DISABLED)
        ?: HttpEngineNativeProvider(context).createBuilder().build().also {
          cronetEngineInstances.put(IpBlindingMode.IP_BLINDING_MODE_DISABLED, it)
        }
    return CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engine)
      .maxInboundMessageSize(MAX_INBOUND_MESSAGE_SIZE_BYTES)
      .idleTimeout(channelIdleTimeoutMinutes, TimeUnit.MINUTES)
  }

  private enum class CronetProviderType(
    val metricId: CountMetricId,
    val builderFactory: (Context) -> org.chromium.net.CronetEngine.Builder,
  ) {
    MAINLINE(
      CountMetricId.PCS_PI_CRONET_MAINLINE_LIB_USAGE,
      { context -> HttpEngineNativeProvider(context).createBuilder() },
    ),
    STATIC(
      CountMetricId.PCS_PI_CRONET_NATIVE_LIB_USAGE,
      { context -> NativeCronetProvider(context).createBuilder() },
    ),
  }

  private suspend fun createIpRelayChannelBuilder(
    providerType: CronetProviderType,
    fallbackOnError: Boolean = false,
    proxyConfig: List<ProxyConfiguration>,
  ): CronetChannelBuilder {
    return try {
      val engineBuilder = providerType.builderFactory(context)
      engineBuilder.applyProxyConfig(proxyConfig)
      val engine =
        engineBuilder.build().also {
          cronetEngineInstances.put(IpBlindingMode.IP_BLINDING_MODE_ENABLED, it)
        }
      pcsStatsLogger.logEventCount(providerType.metricId)
      CronetChannelBuilder.forAddress(endpointUrl, HTTPS_PORT, engine)
    } catch (e: UnsupportedOperationException) {
      if (fallbackOnError) {
        logger
          .atInfo()
          .log(
            "Falling back to CRONET_STATIC_IP_RELAY for IP relay support as Proxy APIs are not available."
          )
        createIpRelayChannelBuilder(
          CronetProviderType.STATIC,
          fallbackOnError = false,
          proxyConfig = proxyConfig,
        )
      } else {
        throw e
      }
    }
  }

  private suspend fun org.chromium.net.CronetEngine.Builder.applyProxyConfig(
    proxyConfig: List<ProxyConfiguration>
  ) {
    logger.atInfo().log("Setting proxy options: %s", proxyConfig.toLogString)
    setProxyOptions(
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
        },
        ProxyOptions.ALL_PROXIES_FAILED_BEHAVIOR_DISALLOW_DIRECT,
      )
    )
  }

  private val List<ProxyConfiguration>.toLogString: String
    get() = map { "${it.host}:${it.port}" }.joinToString(",")

  private fun getProxyCallback(proxyConfiguration: ProxyConfiguration) =
    object : Proxy.HttpConnectCallback() {
      var masqueTunnelSetupTimer: Timers.Timer? = null
      var startTime: Long? = null

      override fun onBeforeRequest(request: Request) {
        request.use {
          startTime = System.nanoTime()
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
            (System.nanoTime() - it).nanoseconds.inWholeMilliseconds,
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
