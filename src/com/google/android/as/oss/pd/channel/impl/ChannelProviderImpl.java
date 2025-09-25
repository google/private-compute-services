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

package com.google.android.as.oss.pd.channel.impl;

import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.channel.ChannelProvider;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;

/**
 * A simple {@link com.google.android.as.oss.pd.channel.ChannelProvider} that caches the returned
 * channels by host name.
 */
class ChannelProviderImpl implements ChannelProvider {

  private static final int SERVER_PORT = 443;
  private static final int MAX_RESPONSE_SIZE_IN_BYTES = 32 * 1024 * 1024; // Max download size: 32MB

  private final LoadingCache<Client, Channel> channelCache;
  private final Optional<String> apiKeyOverride;

  ChannelProviderImpl(
      ImmutableMap<Client, String> hostNames,
      String defaultHostName,
      Optional<String> apiKeyOverride) {
    this.apiKeyOverride = apiKeyOverride;
    this.channelCache =
        CacheBuilder.newBuilder()
            .build(CacheLoader.from(client -> buildChannelFor(hostNames, client, defaultHostName)));
  }

  @Override
  public Channel getChannel(Client client) {
    // Using getUnchecked since no checked exception is thrown from the loading function
    return channelCache.getUnchecked(client);
  }

  @Override
  public Optional<String> getServiceApiKeyOverride() {
    return apiKeyOverride;
  }

  // Implemented as a static method instead of an instance method to avoid "under-initialization"
  // errors by the static analysis tools.
  private static Channel buildChannelFor(
      ImmutableMap<Client, String> hostNames, Client client, String defaultHostName) {
    String hostName = hostNames.getOrDefault(client, defaultHostName);
    return ManagedChannelBuilder.forAddress(hostName, SERVER_PORT)
        .maxInboundMessageSize(MAX_RESPONSE_SIZE_IN_BYTES)
        .build();
  }
}
