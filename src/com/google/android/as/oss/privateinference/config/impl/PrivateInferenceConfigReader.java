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

package com.google.android.as.oss.privateinference.config.impl;

import android.os.SystemProperties;
import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.common.config.FlagManager.EnumFlag;
import com.google.android.as.oss.common.config.FlagManager.IntegerFlag;
import com.google.android.as.oss.common.config.FlagManager.StringFlag;
import com.google.android.as.oss.privateinference.config.PrivateInferenceConfig;
import com.google.android.as.oss.privateinference.library.bsa.token.cache.TokenCacheFlag;
import com.google.android.as.oss.privateinference.library.oakutil.DeviceAttestationFlag;
import com.google.android.as.oss.privateinference.transport.ProxyConfigProviderType;
import com.google.android.as.oss.privateinference.transport.ProxyConfiguration;
import com.google.common.flogger.GoogleLogger;

/** Reads Private Compute Backend configuration from DeviceConfig flags. */
class PrivateInferenceConfigReader extends AbstractConfigReader<PrivateInferenceConfig> {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String FLAG_PREFIX = "PrivateInference__";

  static final BooleanFlag ENABLED_FLAG =
      BooleanFlag.create(FLAG_PREFIX + "enabled", PrivateInferenceConfig.DEFAULT_ENABLED);

  // The system property for hardware revision & build fingerprint.
  static final String HARDWARE_REVISION_SYSTEM_PROPERTY = "ro.boot.hardware.revision";
  static final String BUILD_FINGERPRINT_SYSTEM_PROPERTY = "ro.build.fingerprint";

  static final EnumFlag<DeviceAttestationFlag.Mode> DEVICE_ATTESTATION_MODE_FLAG =
      EnumFlag.create(
          DeviceAttestationFlag.Mode.class,
          FLAG_PREFIX + "device_attestation_mode",
          PrivateInferenceConfig.DEFAULT_DEVICE_ATTESTATION_MODE);

  static final BooleanFlag ENABLE_WAIT_FOR_GRPC_CHANNEL_READY_FLAG =
      BooleanFlag.create(
          FLAG_PREFIX + "wait_for_grpc_channel_ready",
          PrivateInferenceConfig.DEFAULT_ENABLE_WAIT_FOR_GRPC_CHANNEL_READY);

  static final BooleanFlag ATTACH_CERTIFICATE_HEADER_FLAG =
      BooleanFlag.create(
          FLAG_PREFIX + "attach_cert_header",
          PrivateInferenceConfig.DEFAULT_ATTACH_CERTIFICATE_HEADER);

  static final BooleanFlag ENABLE_ARATEA_TOKEN_CACHE =
      BooleanFlag.create(
          FLAG_PREFIX + "enable_aratea_token_cache",
          PrivateInferenceConfig.DEFAULT_ENABLE_ARATEA_TOKEN_CACHE);

  static final IntegerFlag PROXY_TOKEN_BATCH_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_batch_size",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_BATCH_SIZE);

  static final EnumFlag<TokenCacheFlag.Mode> PROXY_TOKEN_CACHE_MODE_FLAG =
      EnumFlag.create(
          TokenCacheFlag.Mode.class,
          FLAG_PREFIX + "proxy_token_cache_mode",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_CACHE_MODE);

  static final IntegerFlag PROXY_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_cache_refresh_interval_minutes",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES);

  static final IntegerFlag PROXY_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_memory_cache_min_pool_size",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE);

  static final IntegerFlag PROXY_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_memory_cache_preferred_pool_size",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE);

  static final IntegerFlag PROXY_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_durable_cache_min_pool_size",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE);

  static final IntegerFlag PROXY_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_token_durable_cache_preferred_pool_size",
          PrivateInferenceConfig.DEFAULT_PROXY_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE);

  static final IntegerFlag ARATEA_TOKEN_BATCH_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_batch_size",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_BATCH_SIZE);

  static final IntegerFlag ARATEA_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_memory_cache_min_pool_size",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE);

  static final IntegerFlag ARATEA_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_memory_cache_preferred_pool_size",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE);

  static final IntegerFlag ARATEA_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_durable_cache_min_pool_size",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE);

  static final IntegerFlag ARATEA_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_durable_cache_preferred_pool_size",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE);

  static final EnumFlag<TokenCacheFlag.Mode> ARATEA_TOKEN_CACHE_MODE_FLAG =
      EnumFlag.create(
          TokenCacheFlag.Mode.class,
          FLAG_PREFIX + "aratea_token_cache_mode",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_CACHE_MODE);

  static final IntegerFlag ARATEA_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "aratea_token_cache_refresh_interval_minutes",
          PrivateInferenceConfig.DEFAULT_ARATEA_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES);

  static final StringFlag PROXY_URL_FLAG =
      StringFlag.create(FLAG_PREFIX + "proxy_url", PrivateInferenceConfig.DEFAULT_PROXY_URL);
  static final IntegerFlag PROXY_PORT_FLAG =
      IntegerFlag.create(FLAG_PREFIX + "proxy_port", PrivateInferenceConfig.DEFAULT_PROXY_PORT);
  static final StringFlag PROXY_AUTH_HEADER_FLAG =
      StringFlag.create(
          FLAG_PREFIX + "proxy_auth_header", PrivateInferenceConfig.DEFAULT_PROXY_AUTH_HEADER);

  static final EnumFlag<ProxyConfigProviderType.Mode> PROXY_CONFIG_PROVIDER_TYPE_FLAG =
      EnumFlag.create(
          ProxyConfigProviderType.Mode.class,
          FLAG_PREFIX + "proxy_config_provider_type",
          PrivateInferenceConfig.DEFAULT_PROXY_CONFIG_PROVIDER_TYPE);

  static final IntegerFlag PROXY_CONFIG_REFRESH_INTERVAL_MINUTES_FLAG =
      IntegerFlag.create(
          FLAG_PREFIX + "proxy_config_refresh_interval_minutes",
          PrivateInferenceConfig.DEFAULT_PROXY_CONFIG_REFRESH_INTERVAL_MINUTES);

  private final FlagManager flagManager;

  static PrivateInferenceConfigReader create(FlagManager flagManager) {
    PrivateInferenceConfigReader instance = new PrivateInferenceConfigReader(flagManager);

    instance
        .flagManager
        .listenable()
        .addListener(
            (flagNames) -> {
              if (FlagListener.anyHasPrefix(flagNames, FLAG_PREFIX)) {
                instance.refreshConfig();
              }
            });

    return instance;
  }

  @Override
  protected PrivateInferenceConfig computeConfig() {
    boolean isEvt = SystemProperties.get(HARDWARE_REVISION_SYSTEM_PROPERTY).contains("EVT");
    boolean isUserDebug =
        SystemProperties.get(BUILD_FINGERPRINT_SYSTEM_PROPERTY).contains("userdebug");

    logger.atInfo().log("computeConfig: isEvt: %s, isUserDebug: %s", isEvt, isUserDebug);
    return PrivateInferenceConfig.builder()
        .setAttestationPublisherMode(PrivateInferenceConfig.DEFAULT_ATTESTATION_PUBLISHER_MODE)
        .setTransportMode(PrivateInferenceConfig.DEFAULT_TRANSPORT_MODE)
        .setEndpointUrl(getPrivateInferenceEndpointUrl(isEvt, isUserDebug))
        .setTokenIssuanceEndpointUrl(getTokenIssuanceEndpointUrl(isEvt, isUserDebug))
        .setArateaAuthMode(PrivateInferenceConfig.DEFAULT_ARATEA_AUTH_MODE)
        .setProxyAuthMode(PrivateInferenceConfig.DEFAULT_PROXY_AUTH_MODE)
        // Flags that can be overridden via Device Config flags.
        .setEnabled(flagManager.get(ENABLED_FLAG))
        .setEnableArateaTokenCache(flagManager.get(ENABLE_ARATEA_TOKEN_CACHE))
        .setProxyConfigProviderType(flagManager.get(PROXY_CONFIG_PROVIDER_TYPE_FLAG))
        .setWaitForGrpcChannelReady(flagManager.get(ENABLE_WAIT_FOR_GRPC_CHANNEL_READY_FLAG))
        .setAttachCertificateHeader(flagManager.get(ATTACH_CERTIFICATE_HEADER_FLAG))
        .setDeviceAttestationMode(getDeviceAttestationMode(isEvt))
        .setProxyTokenBatchSize(flagManager.get(PROXY_TOKEN_BATCH_SIZE_FLAG))
        .setProxyTokenCacheMode(flagManager.get(PROXY_TOKEN_CACHE_MODE_FLAG))
        .setProxyTokenCacheRefreshIntervalMinutes(
            flagManager.get(PROXY_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES_FLAG))
        .setProxyTokenMemoryCacheMinPoolSize(
            flagManager.get(PROXY_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE_FLAG))
        .setProxyTokenMemoryCachePreferredPoolSize(
            flagManager.get(PROXY_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE_FLAG))
        .setProxyTokenDurableCacheMinPoolSize(
            flagManager.get(PROXY_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE_FLAG))
        .setProxyTokenDurableCachePreferredPoolSize(
            flagManager.get(PROXY_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE_FLAG))
        .setArateaTokenBatchSize(flagManager.get(ARATEA_TOKEN_BATCH_SIZE_FLAG))
        .setArateaTokenMemoryCacheMinPoolSize(
            flagManager.get(ARATEA_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE_FLAG))
        .setArateaTokenMemoryCachePreferredPoolSize(
            flagManager.get(ARATEA_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE_FLAG))
        .setArateaTokenDurableCacheMinPoolSize(
            flagManager.get(ARATEA_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE_FLAG))
        .setArateaTokenDurableCachePreferredPoolSize(
            flagManager.get(ARATEA_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE_FLAG))
        .setArateaTokenCacheMode(flagManager.get(ARATEA_TOKEN_CACHE_MODE_FLAG))
        .setArateaTokenCacheRefreshIntervalMinutes(
            flagManager.get(ARATEA_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES_FLAG))
        .setProxyConfiguration(
            new ProxyConfiguration(
                flagManager.get(PROXY_URL_FLAG),
                flagManager.get(PROXY_PORT_FLAG),
                flagManager.get(PROXY_AUTH_HEADER_FLAG)))
        .setProxyConfigRefreshIntervalMinutes(
            flagManager.get(PROXY_CONFIG_REFRESH_INTERVAL_MINUTES_FLAG))
        .build();
  }

  // Defaults to ENABLED_NO_PROPERTIES. Attestation measurement is skipped for EVT devices because
  // they are unable to generate a valid key pair. Otherwise, use the value specified in
  // the flag.
  private DeviceAttestationFlag.Mode getDeviceAttestationMode(boolean isEvt) {
    if (isEvt) {
      return DeviceAttestationFlag.Mode.DISABLED;
    }
    return flagManager.get(DEVICE_ATTESTATION_MODE_FLAG);
  }

  private static String getPrivateInferenceEndpointUrl(boolean isEvt, boolean isUserdebug) {
    return PrivateInferenceConfig.PRIVATE_INFERENCE_PROD_ENDPOINT_URL;
  }

  private String getTokenIssuanceEndpointUrl(boolean isEvt, boolean isUserdebug) {
    return PrivateInferenceConfig.TOKEN_ISSUANCE_PROD_ENDPOINT_URL;
  }

  private PrivateInferenceConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
