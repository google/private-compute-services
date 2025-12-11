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
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.ProtoSerializer
import androidx.datastore.dataStoreFile
import androidx.work.WorkerFactory
import com.google.android.apps.common.inject.annotation.ApplicationContext
import com.google.android.`as`.oss.common.CoroutineQualifiers
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.initializer.PcsInitializer
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceProxyConfiguration
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/** Module that provides a [DataStore] for the proxy configs. */
@Module
@InstallIn(SingletonComponent::class)
internal interface ProxyConfigModule {

  @Binds
  @Singleton
  @PrivateInferenceProxyConfiguration
  fun bindProxyConfigControlPlane(impl: ProxyConfigManagerImpl): ProxyConfigControlPlane

  companion object {
    @Provides
    @Singleton
    fun providesProxyConfigDataStore(
      @ApplicationContext context: Context,
      @CoroutineQualifiers.IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): DataStore<TimestampedProxyConfigs> {
      return DataStoreFactory.create(
        serializer = ProtoSerializer(timestampedProxyConfigs {}),
        scope = CoroutineScope(ioDispatcher + SupervisorJob()),
        produceFile = { context.dataStoreFile("proxy_configs_data_store.pb") },
      )
    }

    @Provides
    @Singleton
    @IntoSet
    fun provideProxyConfigRefreshWorkerFactory(
      @PrivateInferenceProxyConfiguration proxyConfigControlPlane: ProxyConfigControlPlane
    ): WorkerFactory {
      return ProxyConfigRefreshWorker.Factory(proxyConfigControlPlane)
    }

    @Provides
    @Singleton
    @IntoSet
    fun provideProxyTokenWorkManagerInitializer(
      @ApplicationContext context: Context,
      configReader: ConfigReader<PrivateInferenceConfig>,
      @CoroutineQualifiers.ApplicationScope coroutineScope: CoroutineScope,
    ): PcsInitializer =
      ProxyConfigRefreshWorker.Initializer(
        appContext = context,
        configReader = configReader,
        coroutineScope = coroutineScope,
      )
  }
}
