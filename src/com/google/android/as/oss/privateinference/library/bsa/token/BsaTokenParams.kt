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

import okio.ByteString
import okio.ByteString.Companion.toByteString

/**
 * Parameters for use when fetching [BsaToken] instances using [BsaTokenProvider] implementations.
 */
sealed interface BsaTokenParams<T> {
  /**
   * If true, fetch operations must fetch a _new_ token and are not allowed to use any
   * cached-values.
   */
  val mustBeFresh: Boolean
    get() = false
}

/** Parameters to be passed when fetching [ProxyToken] instances from a [BsaTokenProvider]. */
data class ProxyTokenParams(override val mustBeFresh: Boolean = false) : BsaTokenParams<ProxyToken>

/** Parameters to be passed when fetching [ArateaToken] instances from a [BsaTokenProvider]. */
@ConsistentCopyVisibility
data class ArateaTokenParams private constructor(private val challengeBytes: ByteString) :
  BsaTokenParams<ArateaToken> {
  override val mustBeFresh: Boolean = true

  val challenge: ByteArray
    get() = challengeBytes.toByteArray()

  val challengeBase64: String
    get() = challengeBytes.base64()

  constructor(challengeBytes: ByteArray) : this(challengeBytes.toByteString())
}

/**
 * Parameters to be passed when fetching [ArateaTokenWithoutChallenge] instances from a
 * [BsaTokenProvider].
 */
data class CacheableArateaTokenParams(override val mustBeFresh: Boolean = false) :
  BsaTokenParams<ArateaTokenWithoutChallenge>
