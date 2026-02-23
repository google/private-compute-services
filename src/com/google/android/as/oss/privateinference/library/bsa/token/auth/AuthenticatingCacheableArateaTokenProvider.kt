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

package com.google.android.`as`.oss.privateinference.library.bsa.token.auth

import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.CacheableArateaTokenParams
import com.google.android.`as`.oss.privateinference.util.runSuspendCatching
import kotlinx.coroutines.CoroutineScope

/**
 * A [BsaTokenProvider] for fetching [ArateaTokenWithoutChallenge]s using [BlindSignAuth].
 *
 * This provider ensures that the Aratea token cache is enabled in the configuration and delegates
 * token creation to [BlindSignAuth.createArateaTokensWithoutChallenge]. It expects [BsaTokenParams]
 * to be of type [CacheableArateaTokenParams].
 */
class AuthenticatingCacheableArateaTokenProvider(
  override val coroutineScope: CoroutineScope,
  private val blindSignAuth: BlindSignAuth,
  private val configReader: ConfigReader<PrivateInferenceConfig>,
) : BsaTokenProvider<ArateaTokenWithoutChallenge> {
  override val maxBatchSize: Int
    get() = configReader.config.arateaTokenBatchSize()

  override suspend fun fetchTokens(
    params: BsaTokenParams<ArateaTokenWithoutChallenge>,
    batchSize: Int,
  ): Result<List<ArateaTokenWithoutChallenge>> = runSuspendCatching {
    require(batchSize in 1..maxBatchSize) { "Invalid batchSize" }
    require(configReader.config.enableArateaTokenCache()) { "ArateaTokenCache is not enabled" }
    require(params is CacheableArateaTokenParams) {
      "Only CacheableArateaTokenParams are allowed when fetching ArateaTokenWithoutChallenge"
    }
    blindSignAuth.createArateaTokensWithoutChallenge(batchSize)
  }
}
