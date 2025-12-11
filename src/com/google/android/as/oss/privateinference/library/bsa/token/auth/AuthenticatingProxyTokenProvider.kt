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
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.util.runSuspendCatching
import kotlinx.coroutines.CoroutineScope

/**
 * Implementation of [BsaTokenProvider] which fetches batches of [ProxyToken] instances of a size
 * configured by [PrivateInferenceConfig.proxyTokenBatchSize].
 */
class AuthenticatingProxyTokenProvider(
  override val coroutineScope: CoroutineScope,
  private val blindSignAuth: BlindSignAuth,
  private val configReader: ConfigReader<PrivateInferenceConfig>,
) : BsaTokenProvider<ProxyToken> {
  override val maxBatchSize: Int
    get() = configReader.config.proxyTokenBatchSize()

  override suspend fun fetchTokens(
    params: BsaTokenParams<ProxyToken>,
    batchSize: Int,
  ): Result<List<ProxyToken>> = runSuspendCatching {
    require(batchSize in 1..maxBatchSize) { "Invalid batchSize" }
    require(params is ProxyTokenParams) {
      "Only ProxyTokenParams are allowed when fetching ProxyTokens"
    }
    blindSignAuth.createProxyTokens(batchSize)
  }
}
