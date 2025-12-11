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

import androidx.annotation.VisibleForTesting
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.config.asFlow
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.BsaTokenCacheControlPlane
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.TokenCacheFlag
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.common.flogger.GoogleLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * [PrivateInferenceConfig]-sensitive implementation of [BsaTokenProvider] which, depending on the
 * current value of [TokenCacheFlag.Mode], picks from one of a number of delegate providers to
 * satisfy fetch requests.
 */
class ConfigurableBsaTokenProvider<T : BsaToken>(
  override val coroutineScope: CoroutineScope,
  private val configReader: ConfigReader<PrivateInferenceConfig>,
  private val tokenClass: Class<in T>,
  private val authenticatingProvider: BsaTokenProvider<T>,
  private val memoryCacheProvider: BsaTokenProvider<T>?,
  private val diskCacheProvider: BsaTokenProvider<T>?,
  private val multilevelCacheProvider: BsaTokenProvider<T>?,
  private val pcsStatsLogger: PcsStatsLogger,
  private val tokenFetchErrorLogMapper: (Throwable) -> CountMetricId?,
) : BsaTokenProvider<T> {
  @VisibleForTesting
  val currentProvider = MutableStateFlow<BsaTokenProvider<T>>(authenticatingProvider)

  init {
    coroutineScope.launch {
      configReader
        .asFlow()
        .map { config ->
          when (tokenClass) {
            ArateaToken::class.java -> config.arateaTokenCacheMode()
            ProxyToken::class.java -> config.proxyTokenCacheMode()
            else -> throw IllegalArgumentException("Bad tokenClass value")
          }
        }
        .distinctUntilChanged()
        .collectLatest(::updateComposedProvider)
    }
  }

  override val maxBatchSize: Int
    get() = currentProvider.value.maxBatchSize

  override suspend fun fetchTokens(params: BsaTokenParams<T>, batchSize: Int): Result<List<T>> {
    val result = currentProvider.value.fetchTokens(params, batchSize)
    if (result.isFailure) {
      result.exceptionOrNull()?.let {
        tokenFetchErrorLogMapper(it)?.let { metricsId -> pcsStatsLogger.logEventCount(metricsId) }
      }
    }
    return result
  }

  private suspend fun updateComposedProvider(cacheMode: TokenCacheFlag.Mode) {
    logger.atInfo().log("Changing %s to cacheMode: %s", tokenClass.stableTokenClassName, cacheMode)
    val nextProvider =
      when (cacheMode) {
        TokenCacheFlag.Mode.NO_CACHE -> authenticatingProvider
        TokenCacheFlag.Mode.MEMORY_ONLY -> memoryCacheProvider
        TokenCacheFlag.Mode.DURABLE_ONLY -> diskCacheProvider
        TokenCacheFlag.Mode.DURABLE_AND_MEMORY -> multilevelCacheProvider
      }

    if (nextProvider == null) {
      logger
        .atWarning()
        .log(
          "Cache mode: %s is not supported by this provider (%s), falling back",
          cacheMode,
          tokenClass.stableTokenClassName,
        )
    }

    currentProvider.update { previous ->
      (previous as? BsaTokenCacheControlPlane)?.invalidate()
      nextProvider ?: authenticatingProvider
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
