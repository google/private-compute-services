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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache

import android.content.Context
import androidx.work.WorkerFactory
import com.google.android.`as`.oss.common.CoroutineQualifiers
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.initializer.PcsInitializer
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import java.util.Optional
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
internal object WorkerModule {
  @Provides
  @Singleton
  @IntoSet
  fun provideProxyTokenWorkerFactory(
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
    @BsaTokenProvider.MemoryCached
    memoryTokenProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
    @BsaTokenProvider.DiskCached
    diskTokenProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
    @BsaTokenProvider.MultilevelCached
    multilevelCached: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
  ): WorkerFactory {
    fun getCacheControlPlane(): BsaTokenCacheControlPlane? {
      return when (configReader.config.proxyTokenCacheMode()) {
        TokenCacheFlag.Mode.NO_CACHE -> null
        TokenCacheFlag.Mode.MEMORY_ONLY -> memoryTokenProvider.getOrNull()
        TokenCacheFlag.Mode.DURABLE_ONLY -> diskTokenProvider.getOrNull()
        TokenCacheFlag.Mode.DURABLE_AND_MEMORY -> multilevelCached.getOrNull()
      }
        as? BsaTokenCacheControlPlane
    }

    return BsaTokenCacheRefreshWorker.Factory(
      tokenClass = ProxyToken::class.java,
      cacheControl =
        object : BsaTokenCacheControlPlane {
          override suspend fun invalidate() {
            getCacheControlPlane()?.invalidate()
          }

          override suspend fun invalidateAndRefill() {
            getCacheControlPlane()?.invalidateAndRefill()
          }
        },
    )
  }

  @Provides
  @Singleton
  @IntoSet
  fun provideArateaTokenWorkerFactory(
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
    @BsaTokenProvider.MemoryCached
    memoryTokenProvider:
      Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaTokenWithoutChallenge>>,
    @BsaTokenProvider.DiskCached
    diskTokenProvider:
      Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaTokenWithoutChallenge>>,
    @BsaTokenProvider.MultilevelCached
    multilevelCached: Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaTokenWithoutChallenge>>,
  ): WorkerFactory {
    fun getCacheControlPlane(): BsaTokenCacheControlPlane? {
      return when (configReader.config.arateaTokenCacheMode()) {
        TokenCacheFlag.Mode.NO_CACHE -> null
        TokenCacheFlag.Mode.MEMORY_ONLY -> memoryTokenProvider.getOrNull()
        TokenCacheFlag.Mode.DURABLE_ONLY -> diskTokenProvider.getOrNull()
        TokenCacheFlag.Mode.DURABLE_AND_MEMORY -> multilevelCached.getOrNull()
      }
        as? BsaTokenCacheControlPlane
    }
    return BsaTokenCacheRefreshWorker.Factory(
      tokenClass = ArateaTokenWithoutChallenge::class.java,
      cacheControl =
        object : BsaTokenCacheControlPlane {
          override suspend fun invalidate() {
            getCacheControlPlane()?.invalidate()
          }

          override suspend fun invalidateAndRefill() {
            getCacheControlPlane()?.invalidateAndRefill()
          }
        },
    )
  }

  @Provides
  @Singleton
  @IntoSet
  fun provideProxyTokenWorkManagerInitializer(
    @ApplicationContext context: Context,
    configReader: ConfigReader<PrivateInferenceConfig>,
    @CoroutineQualifiers.ApplicationScope coroutineScope: CoroutineScope,
  ): PcsInitializer =
    BsaTokenCacheRefreshWorker.Initializer(
      appContext = context,
      configReader = configReader,
      tokenClass = ProxyToken::class.java,
      coroutineScope = coroutineScope,
    )

  @Provides
  @Singleton
  @IntoSet
  fun provideArateaTokenWorkManagerInitializer(
    @ApplicationContext context: Context,
    configReader: ConfigReader<PrivateInferenceConfig>,
    @CoroutineQualifiers.ApplicationScope coroutineScope: CoroutineScope,
  ): PcsInitializer =
    BsaTokenCacheRefreshWorker.Initializer(
      appContext = context,
      configReader = configReader,
      tokenClass = ArateaTokenWithoutChallenge::class.java,
      coroutineScope = coroutineScope,
    )
}
