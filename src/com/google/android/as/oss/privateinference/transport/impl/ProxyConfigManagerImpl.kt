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

import androidx.datastore.core.DataStore
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.Annotations.TokenIssuanceServerGrpcChannel
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.logging.MetricIdMap
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper.IPProtectionRequestType
import com.google.android.`as`.oss.privateinference.transport.ProxyConfigManager
import com.google.android.`as`.oss.privateinference.transport.ProxyConfigProviderType
import com.google.android.`as`.oss.privateinference.transport.ProxyConfiguration
import com.google.android.`as`.oss.privateinference.util.timers.Annotations.PrivateInferenceClientTimers
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import com.google.common.flogger.GoogleLogger
import com.google.common.net.HostAndPort
import com.google.internal.ppn.phosphor.v1.ArateaIPBlindingServiceGrpcKt.ArateaIPBlindingServiceCoroutineStub
import com.google.privacy.ppn.proto.getProxyConfigRequest
import com.google.protobuf.util.kotlin.toJavaInstant
import com.google.protobuf.util.kotlin.toProtoTimestamp
import dagger.Lazy
import io.grpc.ManagedChannel
import java.time.Duration
import javax.inject.Inject
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ProxyConfigManagerImpl
@Inject
internal constructor(
  val configReader: ConfigReader<PrivateInferenceConfig>,
  val timeSource: TimeSource,
  @TokenIssuanceServerGrpcChannel private val phosphorChannelLazy: Lazy<ManagedChannel>,
  private val pcsStatsLogger: PcsStatsLogger,
  val networkUsageLogHelper: PrivateInferenceNetworkUsageLogHelper,
  @param:PrivateInferenceClientTimers private val timers: TimerSet,
  private val proxyConfigsDataStore: DataStore<TimestampedProxyConfigs>,
) : ProxyConfigManager, ProxyConfigControlPlane {

  private val stub: ArateaIPBlindingServiceCoroutineStub by lazy {
    ArateaIPBlindingServiceCoroutineStub(phosphorChannelLazy.get())
  }

  override suspend fun getProxyConfig(): List<ProxyConfiguration> =
    timers.start(PrivateInferenceClientTimerNames.GET_PROXY_CONFIG).use {
      when (configReader.config.proxyConfigProviderType()) {
        ProxyConfigProviderType.Mode.DEVICE_CONFIG -> {
          listOf(configReader.config.proxyConfiguration())
        }
        ProxyConfigProviderType.Mode.SERVER_WITH_MEMORY_CACHE,
        ProxyConfigProviderType.Mode.SERVER_WITH_LOCAL_CACHE -> {
          getProxyConfigFromDataStore() ?: fetchProxyConfigFromServerAndCache()
        }
        else -> {
          throw IllegalArgumentException(
            "Unsupported proxy config mode: ${configReader.config.proxyConfigProviderType()}"
          )
        }
      }
    }

  override suspend fun refresh() {
    val unused = fetchProxyConfigFromServerAndCache()
    logger.atInfo().log("Proxy config control plane refreshed successfully")
  }

  /**
   * Gets the proxy config from the server and caches it. If the request fails, an empty list is
   * returned.
   */
  suspend fun fetchProxyConfigFromServerAndCache(): List<ProxyConfiguration> {
    val request = getProxyConfigRequest { serviceType = SERVICE_TYPE }
    try {
      val response =
        pcsStatsLogger.getResultAndLogStatusAsync(METRIC_ID_MAP) {
          timers.start(PrivateInferenceClientTimerNames.FETCH_PROXY_CONFIG).use {
            stub.getProxyConfig(request)
          }
        }
      val localProxyConfigs =
        response.proxyChainList.map { proxyConfig ->
          val hostAndPort = HostAndPort.fromString(proxyConfig.proxyB)
          val host = hostAndPort.host
          val port =
            when {
              hostAndPort.hasPort() -> hostAndPort.port
              host.contains("fastly") -> FASTLY_PROXY_PORT
              else -> DEFAULT_PROXY_PORT
            }
          ProxyConfiguration(host, port, authHeader = "")
        }
      logger.atInfo().log("Got proxy configs from server: %s", localProxyConfigs)
      logNetworkUsage(
        isSuccess = true,
        request.serializedSize.toLong(),
        response.serializedSize.toLong(),
      )

      // Update the data store in a coroutine to avoid blocking the current thread.
      coroutineScope {
        launch {
          logger.atInfo().log("Updating proxy configs data store")
          proxyConfigsDataStore.updateData {
            timestampedProxyConfigs {
              proxyConfigs +=
                localProxyConfigs.map { config ->
                  proxyConfig {
                    url = config.host
                    port = config.port
                  }
                }
              lastUpdated = timeSource.now().toProtoTimestamp()
            }
          }
        }
      }

      return localProxyConfigs
    } catch (e: Exception) {
      logger.atWarning().withCause(e).log("Failed to get proxy configs from server")
      logNetworkUsage(isSuccess = false, request.serializedSize.toLong(), 0L)
      return listOf()
    }
  }

  internal fun logNetworkUsage(isSuccess: Boolean, requestSize: Long, responseSize: Long) {
    networkUsageLogHelper.logIPProtectionRequest(
      IPProtectionRequestType.IPP_GET_PROXY_CONFIG,
      isSuccess,
      requestSize,
      responseSize,
    )
  }

  private suspend fun getProxyConfigFromDataStore(): List<ProxyConfiguration>? {
    // DataStore caches the data in memory, so this could be fast if it's not the first read.
    val timestampedProxyConfigs = proxyConfigsDataStore.data.first()
    if (timestampedProxyConfigs == timestampedProxyConfigs {}) {
      return null
    }

    val proxyConfigs = timestampedProxyConfigs.proxyConfigsList
    val timestamp = timestampedProxyConfigs.lastUpdated
    val now = timeSource.now()
    val timeDiff = Duration.between(timestamp.toJavaInstant(), now)
    if (
      timeDiff.compareTo(
        Duration.ofMinutes(configReader.config.proxyConfigRefreshIntervalMinutes().toLong())
      ) > 0
    ) {
      logger.atInfo().log("Proxy configs are expired, will fetch new configs from server")
      return null
    }
    logger.atInfo().log("Proxy configs are not expired, returning cached configs")
    return proxyConfigs.map { proxyConfig ->
      ProxyConfiguration(proxyConfig.url, port = proxyConfig.port, authHeader = "")
    }
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()

    val METRIC_ID_MAP =
      MetricIdMap(
        CountMetricId.PCS_PI_IPP_GET_PROXY_CONFIG_SUCCESS,
        CountMetricId.PCS_PI_IPP_GET_PROXY_CONFIG_FAILURE,
        ValueMetricId.PCS_PI_IPP_GET_PROXY_CONFIG_SUCCESS_LATENCY_MS,
        ValueMetricId.PCS_PI_IPP_GET_PROXY_CONFIG_FAILURE_LATENCY_MS,
      )

    const val SERVICE_TYPE = "privatearatea"
    const val DEFAULT_PROXY_PORT = 443
    const val FASTLY_PROXY_PORT = 2498
  }
}
