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

import com.google.android.as.oss.common.ExecutorAnnotations.GeneralExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.config.FlagManagerFactory;
import com.google.android.as.oss.common.config.FlagNamespace;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.common.ClientConfig;
import com.google.android.as.oss.pd.config.ClientBuildVersionReader;
import com.google.android.as.oss.pd.config.ProtectedDownloadConfig;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.Map;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

/** A module to register a ConfigReader for {@link ProtectedDownloadConfig}. */
@Module
@InstallIn(SingletonComponent.class)
interface ProtectedDownloadConfigModule {

  @Binds
  ConfigReader<ProtectedDownloadConfig> bindConfigReader(ProtectedDownloadConfigReader reader);

  @Binds
  ClientBuildVersionReader bindClientBuildVersionReader(ClientBuildVersionReaderImpl reader);

  @Provides
  @Singleton
  static ProtectedDownloadConfigReader provideConfigReader(
      FlagManagerFactory flagManagerFactory, @GeneralExecutorQualifier Executor executor) {
    return ProtectedDownloadConfigReader.create(
        flagManagerFactory.create(FlagNamespace.DEVICE_PERSONALIZATION_SERVICES, executor));
  }

  @Provides
  @Singleton
  static ClientBuildVersionReaderImpl provideClientBuildVersionReaderImpl(
      FlagManagerFactory flagManagerFactory,
      @GeneralExecutorQualifier Executor executor,
      Map<Client, ClientConfig> clientConfigMap) {
    return ClientBuildVersionReaderImpl.create(flagManagerFactory, executor, clientConfigMap);
  }
}
