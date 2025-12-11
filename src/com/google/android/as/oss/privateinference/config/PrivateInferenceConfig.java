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

package com.google.android.as.oss.privateinference.config;

import com.google.android.as.oss.privateinference.config.impl.ArateaAuthFlag;
import com.google.android.as.oss.privateinference.config.impl.ProxyAuthFlag;
import com.google.android.as.oss.privateinference.library.bsa.token.cache.TokenCacheFlag;
import com.google.android.as.oss.privateinference.library.bsa.token.cache.TokenCacheFlag.Mode;
import com.google.android.as.oss.privateinference.library.oakutil.AttestationPublisherFlag;
import com.google.android.as.oss.privateinference.library.oakutil.DeviceAttestationFlag;
import com.google.android.as.oss.privateinference.transport.ProxyConfigProviderType;
import com.google.android.as.oss.privateinference.transport.ProxyConfiguration;
import com.google.android.as.oss.privateinference.transport.TransportFlag;
import com.google.auto.value.AutoValue;

/** Configuration for controlling the behavior of Private Inference. */
@AutoValue
public abstract class PrivateInferenceConfig {

  public static Builder builder() {
    return new AutoValue_PrivateInferenceConfig.Builder();
  }

  public static Builder defaultBuilder() {
    return builder()
        .setProxyTokenBatchSize(DEFAULT_PROXY_TOKEN_BATCH_SIZE)
        .setProxyTokenCacheMode(DEFAULT_PROXY_TOKEN_CACHE_MODE)
        .setProxyTokenCacheRefreshIntervalMinutes(
            DEFAULT_PROXY_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES)
        .setProxyTokenMemoryCacheMinPoolSize(DEFAULT_PROXY_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE)
        .setProxyTokenMemoryCachePreferredPoolSize(
            DEFAULT_PROXY_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE)
        .setProxyTokenDurableCacheMinPoolSize(DEFAULT_PROXY_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE)
        .setProxyTokenDurableCachePreferredPoolSize(
            DEFAULT_PROXY_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE)
        .setArateaTokenBatchSize(DEFAULT_ARATEA_TOKEN_BATCH_SIZE)
        .setArateaTokenCacheMode(DEFAULT_ARATEA_TOKEN_CACHE_MODE)
        .setArateaTokenCacheRefreshIntervalMinutes(
            DEFAULT_ARATEA_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES)
        .setProxyConfiguration(
            new ProxyConfiguration(
                DEFAULT_PROXY_URL, DEFAULT_PROXY_PORT, DEFAULT_PROXY_AUTH_HEADER))
        .setProxyConfigProviderType(DEFAULT_PROXY_CONFIG_PROVIDER_TYPE)
        .setProxyConfigRefreshIntervalMinutes(DEFAULT_PROXY_CONFIG_REFRESH_INTERVAL_MINUTES)
        .setAttestationPublisherMode(DEFAULT_ATTESTATION_PUBLISHER_MODE)
        .setEnabled(DEFAULT_ENABLED)
        .setEndpointUrl(DEFAULT_PRIVATE_INFERENCE_ENDPOINT_URL)
        .setDeviceAttestationMode(DEFAULT_DEVICE_ATTESTATION_MODE)
        .setWaitForGrpcChannelReady(DEFAULT_ENABLE_WAIT_FOR_GRPC_CHANNEL_READY)
        .setAttachCertificateHeader(DEFAULT_ATTACH_CERTIFICATE_HEADER)
        .setTransportMode(DEFAULT_TRANSPORT_MODE)
        .setTokenIssuanceEndpointUrl(DEFAULT_TOKEN_ISSUANCE_ENDPOINT_URL)
        .setArateaAuthMode(DEFAULT_ARATEA_AUTH_MODE)
        .setProxyAuthMode(DEFAULT_PROXY_AUTH_MODE);
  }

  /** Returns the current attestation publisher mode. */
  public abstract AttestationPublisherFlag.Mode attestationPublisherMode();

  /** Determines whether the Private Inference feature is enabled. */
  public abstract boolean enabled();

  /** Returns the endpoint URL for the Private Inference service. */
  public abstract String endpointUrl();

  // TODO: Remove this flag before launch if needed.
  /** Set to true to send device authentication to the server. */
  public abstract DeviceAttestationFlag.Mode deviceAttestationMode();

  /** Set to true to wait for the gRPC channel to be ready before sending requests. */
  public abstract boolean waitForGrpcChannelReady();

  /** Set to true to attach the certificate header to the gRPC requests. */
  public abstract boolean attachCertificateHeader();

  /** Returns the current transport mode requests will use. */
  public abstract TransportFlag.Mode transportMode();

  /** Returns the amount of proxy tokens which should be generated each time a batch is needed. */
  public abstract int proxyTokenBatchSize();

  /** Returns the caching mode intended for ProxyTokens */
  public abstract TokenCacheFlag.Mode proxyTokenCacheMode();

  /** Returns the duration between cache refresh operations for ProxyTokens. */
  public abstract int proxyTokenCacheRefreshIntervalMinutes();

  /** Returns the low-water point for in-memory cached proxy tokens. */
  public abstract int proxyTokenMemoryCacheMinPoolSize();

  /** Returns the preferred cache size for in-memory cached proxy tokens. */
  public abstract int proxyTokenMemoryCachePreferredPoolSize();

  /** Returns the low-water point for durably-cached proxy tokens. */
  public abstract int proxyTokenDurableCacheMinPoolSize();

  /** Returns the preferred cache size for durably-cached proxy tokens. */
  public abstract int proxyTokenDurableCachePreferredPoolSize();

  /** Returns the amount of aratea tokens which should be generated each time a batch is needed. */
  public abstract int arateaTokenBatchSize();

  /** Returns the caching mode intended for ArateaTokens */
  public abstract TokenCacheFlag.Mode arateaTokenCacheMode();

  /** Returns the duration between cache refresh operations for ArateaTokens. */
  public abstract int arateaTokenCacheRefreshIntervalMinutes();

  /** Returns the proxy configuration to use for IP Blinding. */
  public abstract ProxyConfiguration proxyConfiguration();

  /** Returns the mode to use for getting the proxy config. */
  public abstract ProxyConfigProviderType.Mode proxyConfigProviderType();

  /** Returns the duration between cache refresh operations for the proxy config in minutes. */
  public abstract int proxyConfigRefreshIntervalMinutes();

  /** Returns the URL of the token issuance endpoint to use for IP Blinding. */
  public abstract String tokenIssuanceEndpointUrl();

  public abstract ArateaAuthFlag.Mode arateaAuthMode();

  public abstract ProxyAuthFlag.Mode proxyAuthMode();

  public static final String PRIVATE_INFERENCE_PROD_ENDPOINT_URL =
      "privatearatea.pa.googleapis.com";
  public static final String TOKEN_ISSUANCE_PROD_ENDPOINT_URL = "phosphor-pa.googleapis.com";

  // Default values
  public static final AttestationPublisherFlag.Mode DEFAULT_ATTESTATION_PUBLISHER_MODE =
      AttestationPublisherFlag.Mode.DISABLED;
  public static final boolean DEFAULT_ENABLED = true;
  public static final String DEFAULT_PRIVATE_INFERENCE_ENDPOINT_URL =
      PRIVATE_INFERENCE_PROD_ENDPOINT_URL;
  public static final DeviceAttestationFlag.Mode DEFAULT_DEVICE_ATTESTATION_MODE =
      DeviceAttestationFlag.Mode.ENABLED_NO_PROPERTIES;
  public static final boolean DEFAULT_ENABLE_WAIT_FOR_GRPC_CHANNEL_READY = true;
  public static final boolean DEFAULT_ATTACH_CERTIFICATE_HEADER = false;
  public static final TransportFlag.Mode DEFAULT_TRANSPORT_MODE =
      TransportFlag.Mode.CRONET_STATIC_IP_RELAY;

  public static final String DEFAULT_PROXY_URL = "ToBeProvidedByFlags";
  public static final int DEFAULT_PROXY_PORT = 0;
  public static final String DEFAULT_PROXY_AUTH_HEADER = "ToBeProvidedByFlags";

  /**
   * Possible value for {@link PrivateInferenceConfig#proxyTokenCacheRefreshIntervalMinutes()},
   * {@link PrivateInferenceConfig#arateaTokenCacheRefreshIntervalMinutes()} and {@link
   * PrivateInferenceConfig#proxyConfigRefreshIntervalMinutes()}. If this value is returned, the
   * system should not schedule automatic cache refresh operations for the associated token type.
   */
  public static final int CACHE_REFRESH_INTERVAL_NEVER = -1;

  public static final int DEFAULT_PROXY_TOKEN_BATCH_SIZE = 50;
  public static final TokenCacheFlag.Mode DEFAULT_PROXY_TOKEN_CACHE_MODE = Mode.DURABLE_AND_MEMORY;
  public static final int DEFAULT_PROXY_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES = 6 * 60;
  public static final int DEFAULT_PROXY_TOKEN_MEMORY_CACHE_MIN_POOL_SIZE = 2;
  public static final int DEFAULT_PROXY_TOKEN_MEMORY_CACHE_PREFERRED_POOL_SIZE = 10;
  public static final int DEFAULT_PROXY_TOKEN_DURABLE_CACHE_MIN_POOL_SIZE = 2;
  public static final int DEFAULT_PROXY_TOKEN_DURABLE_CACHE_PREFERRED_POOL_SIZE = 100;
  public static final int DEFAULT_ARATEA_TOKEN_BATCH_SIZE = 1;
  public static final TokenCacheFlag.Mode DEFAULT_ARATEA_TOKEN_CACHE_MODE = Mode.NO_CACHE;
  public static final int DEFAULT_ARATEA_TOKEN_CACHE_REFRESH_INTERVAL_MINUTES =
      CACHE_REFRESH_INTERVAL_NEVER;
  public static final ProxyConfigProviderType.Mode DEFAULT_PROXY_CONFIG_PROVIDER_TYPE =
      ProxyConfigProviderType.Mode.SERVER_WITH_MEMORY_CACHE;

  public static final String DEFAULT_TOKEN_ISSUANCE_ENDPOINT_URL = TOKEN_ISSUANCE_PROD_ENDPOINT_URL;

  public static final ArateaAuthFlag.Mode DEFAULT_ARATEA_AUTH_MODE =
      ArateaAuthFlag.Mode.ANONYMOUS_TOKEN;

  public static final ProxyAuthFlag.Mode DEFAULT_PROXY_AUTH_MODE =
      ProxyAuthFlag.Mode.ANONYMOUS_TOKEN;

  public static final int DEFAULT_PROXY_CONFIG_REFRESH_INTERVAL_MINUTES = 24 * 60;

  /** Builder for {@link PrivateInferenceConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setAttestationPublisherMode(AttestationPublisherFlag.Mode value);

    public abstract Builder setEnabled(boolean value);

    public abstract Builder setEndpointUrl(String value);

    public abstract Builder setDeviceAttestationMode(DeviceAttestationFlag.Mode value);

    public abstract Builder setWaitForGrpcChannelReady(boolean value);

    public abstract Builder setAttachCertificateHeader(boolean value);

    public abstract Builder setTransportMode(TransportFlag.Mode value);

    public abstract Builder setProxyTokenBatchSize(int batchSize);

    public abstract Builder setProxyTokenCacheMode(TokenCacheFlag.Mode mode);

    public abstract Builder setProxyTokenCacheRefreshIntervalMinutes(int hours);

    public abstract Builder setProxyTokenMemoryCacheMinPoolSize(int size);

    public abstract Builder setProxyTokenMemoryCachePreferredPoolSize(int size);

    public abstract Builder setProxyTokenDurableCacheMinPoolSize(int size);

    public abstract Builder setProxyTokenDurableCachePreferredPoolSize(int size);

    public abstract Builder setArateaTokenBatchSize(int batchSize);

    public abstract Builder setArateaTokenCacheMode(TokenCacheFlag.Mode mode);

    public abstract Builder setArateaTokenCacheRefreshIntervalMinutes(int hours);

    public abstract Builder setProxyConfiguration(ProxyConfiguration value);

    public abstract Builder setProxyConfigProviderType(ProxyConfigProviderType.Mode mode);

    public abstract Builder setProxyConfigRefreshIntervalMinutes(int minutes);

    public abstract Builder setTokenIssuanceEndpointUrl(String value);

    public abstract Builder setArateaAuthMode(ArateaAuthFlag.Mode mode);

    public abstract Builder setProxyAuthMode(ProxyAuthFlag.Mode mode);

    public abstract PrivateInferenceConfig build();
  }
}
