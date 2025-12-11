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

package com.google.android.`as`.oss.privateinference.library.bsa.token

import com.google.android.`as`.oss.common.CoroutineQualifiers.ApplicationScope
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaAnnotations.ArateaTokenErrorLogMapper
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaAnnotations.ProxyTokenErrorLogMapper
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.Authenticating
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.DiskCached
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.MemoryCached
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.MultilevelCached
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import javax.inject.Singleton
import kotlin.jvm.optionals.getOrNull
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
internal object BsaTokenProviderModule {
  @Provides
  @Singleton
  fun provideProxyTokenProvider(
    @ApplicationScope scope: CoroutineScope,
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
    @Authenticating authenticatingProvider: BsaTokenProvider<@JvmSuppressWildcards ProxyToken>,
    @MemoryCached memoryCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
    @DiskCached diskCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
    @MultilevelCached
    multilevelCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ProxyToken>>,
    pcsStatsLogger: PcsStatsLogger,
    @ProxyTokenErrorLogMapper
    tokenFetchErrorLogMapper: @JvmSuppressWildcards (Throwable) -> CountMetricId?,
  ): BsaTokenProvider<ProxyToken> =
    ConfigurableBsaTokenProvider(
      coroutineScope = scope,
      configReader = configReader,
      tokenClass = ProxyToken::class.java,
      authenticatingProvider = authenticatingProvider,
      memoryCacheProvider = memoryCacheProvider.getOrNull(),
      diskCacheProvider = diskCacheProvider.getOrNull(),
      multilevelCacheProvider = multilevelCacheProvider.getOrNull(),
      pcsStatsLogger = pcsStatsLogger,
      tokenFetchErrorLogMapper = tokenFetchErrorLogMapper,
    )

  @Provides
  @Singleton
  fun provideArateaTokenProvider(
    @ApplicationScope scope: CoroutineScope,
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
    @Authenticating authenticatingProvider: BsaTokenProvider<@JvmSuppressWildcards ArateaToken>,
    @MemoryCached
    memoryCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaToken>>,
    @DiskCached diskCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaToken>>,
    @MultilevelCached
    multilevelCacheProvider: Optional<BsaTokenProvider<@JvmSuppressWildcards ArateaToken>>,
    pcsStatsLogger: PcsStatsLogger,
    @ArateaTokenErrorLogMapper
    tokenFetchErrorLogMapper: @JvmSuppressWildcards (Throwable) -> CountMetricId?,
  ): BsaTokenProvider<ArateaToken> =
    ConfigurableBsaTokenProvider(
      coroutineScope = scope,
      configReader = configReader,
      tokenClass = ArateaToken::class.java,
      authenticatingProvider = authenticatingProvider,
      memoryCacheProvider = memoryCacheProvider.getOrNull(),
      diskCacheProvider = diskCacheProvider.getOrNull(),
      multilevelCacheProvider = multilevelCacheProvider.getOrNull(),
      pcsStatsLogger = pcsStatsLogger,
      tokenFetchErrorLogMapper = tokenFetchErrorLogMapper,
    )
}
