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

package com.google.android.as.oss.pd.config.impl;

import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManagerFactory;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.common.ClientConfig;
import com.google.android.as.oss.pd.config.ClientBuildVersionReader;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * A {@link ClientBuildVersionReader} implementation that returns build version id for client's
 * protected download.
 */
final class ClientBuildVersionReaderImpl implements ClientBuildVersionReader {
  private final ImmutableMap<Client, FlagReader<Long>> flagReaders;

  @Override
  public Optional<Long> getBuildId(Client client) {
    FlagReader<Long> flagReader = flagReaders.get(client);
    return Optional.ofNullable(flagReader).map(FlagReader::getConfig);
  }

  public static ClientBuildVersionReaderImpl create(
      FlagManagerFactory flagManagerFactory,
      Executor executor,
      Map<Client, ClientConfig> clientConfigMap) {
    ImmutableMap.Builder<Client, FlagReader<Long>> flagReadersBuilder = ImmutableMap.builder();
    for (Map.Entry<Client, ClientConfig> entry : clientConfigMap.entrySet()) {
      if (entry.getValue().buildIdFlag().isPresent()
          && !entry.getValue().buildIdFlag().get().flagName().isEmpty()) {
        FlagManager flagManager =
            flagManagerFactory.create(
                entry.getValue().buildIdFlag().get().flagNamespace(), executor);
        flagReadersBuilder.put(
            entry.getKey(),
            FlagReader.forLong(flagManager, entry.getValue().buildIdFlag().get().flagName()));
      }
    }

    return new ClientBuildVersionReaderImpl(flagReadersBuilder.buildOrThrow());
  }

  private ClientBuildVersionReaderImpl(ImmutableMap<Client, FlagReader<Long>> flagReaders) {
    this.flagReaders = flagReaders;
  }
}
