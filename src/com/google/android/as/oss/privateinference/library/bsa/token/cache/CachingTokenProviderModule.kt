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

import com.google.android.`as`.oss.common.CoroutineQualifiers.ApplicationScope
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.db.BsaTokenDatabase
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.db.DatabaseTokenPool
import com.google.android.`as`.oss.privateinference.library.bsa.token.crypto.BsaTokenCipher
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
internal object CachingTokenProviderModule {
  @Provides
  @Singleton
  fun provideProxyTokenValidityPredicate(
    timeSource: TimeSource
  ): TokenValidityPredicate<ProxyToken> = { token ->
    token.expirationTime.isAfter(timeSource.now())
  }

  @Provides
  @Singleton
  fun provideProxyTokenMemoryPool(
    predicate: TokenValidityPredicate<@JvmSuppressWildcards ProxyToken>,
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
  ): MemoryTokenPool<ProxyToken> =
    MemoryTokenPool(
      refreshParams = listOf(ProxyTokenParams()),
      minPoolSize = configReader.config.proxyTokenMemoryCacheMinPoolSize(),
      preferredPoolSize = configReader.config.proxyTokenMemoryCachePreferredPoolSize(),
      tokenValidityPredicate = predicate,
    )

  @Provides
  @Singleton
  @BsaTokenProvider.MemoryCached
  fun provideMemoryProxyTokenProvider(
    @ApplicationScope coroutineScope: CoroutineScope,
    @BsaTokenProvider.Authenticating
    authenticatingProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
    tokenPool: MemoryTokenPool<@JvmSuppressWildcards ProxyToken>,
  ): BsaTokenProvider<ProxyToken> =
    CachingBsaTokenProvider(
      coroutineScope = coroutineScope,
      refillDelegate = authenticatingProvider,
      tokenPool = tokenPool,
    )

  @Provides
  @Singleton
  @IntoSet
  @ProxyToken.Qualifier
  fun provideMemoryProxyTokenControlPlane(
    @BsaTokenProvider.MemoryCached provider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>
  ): BsaTokenCacheControlPlane = provider as CachingBsaTokenProvider

  @Provides
  @Singleton
  @BsaTokenProvider.DiskCached
  fun provideDiskProxyTokenProvider(
    @ApplicationScope coroutineScope: CoroutineScope,
    @BsaTokenProvider.Authenticating
    authenticatingProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
    cipher: BsaTokenCipher,
    database: Lazy<BsaTokenDatabase>,
    timeSource: TimeSource,
  ): BsaTokenProvider<ProxyToken> =
    CachingBsaTokenProvider(
      coroutineScope = coroutineScope,
      refillDelegate = authenticatingProvider,
      tokenPool =
        DatabaseTokenPool(
          refreshParams = listOf(ProxyTokenParams()),
          minPoolSize = configReader.config.proxyTokenDurableCacheMinPoolSize(),
          preferredPoolSize = configReader.config.proxyTokenDurableCachePreferredPoolSize(),
          cipher = cipher,
          daoProvider = { database.get().bsaTokenDao() },
          timeSource = timeSource,
        ),
    )

  @Provides
  @Singleton
  @IntoSet
  @ProxyToken.Qualifier
  fun provideDiskProxyTokenControlPlane(
    @BsaTokenProvider.DiskCached provider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>
  ): BsaTokenCacheControlPlane = provider as CachingBsaTokenProvider

  @Provides
  @Singleton
  @BsaTokenProvider.MultilevelCached
  fun provideMultilevelProxyTokenProvider(
    @ApplicationScope coroutineScope: CoroutineScope,
    @BsaTokenProvider.DiskCached
    diskCachedProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
    memoryTokenPool: MemoryTokenPool<@JvmSuppressWildcards ProxyToken>,
  ): BsaTokenProvider<ProxyToken> =
    CachingBsaTokenProvider(
      coroutineScope = coroutineScope,
      refillDelegate = diskCachedProvider,
      tokenPool = memoryTokenPool,
    )
}
