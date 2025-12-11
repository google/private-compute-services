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

import java.time.Instant
import javax.inject.Qualifier

/** Defines a wrapper around a BlindSignedAuth token generated using [BlindSignedAuth]. */
sealed interface BsaToken {
  /** Raw token bytes as returned from [BlindSignedAuth]'s JNI wrapper for Quiche. */
  val bytes: BsaTokenBytes

  /**
   * If cached, this value defines the instant after which the cached token should be considered
   * invalid.
   */
  val expirationTime: Instant?
    get() = null
}

/** Whether or not the [BsaToken] instance can be cached. */
val BsaToken.isCacheable: Boolean
  get() = expirationTime != null

/** [BsaToken] intended for use when establishing an IP-protecting proxy. */
data class ProxyToken(override val bytes: BsaTokenBytes, override val expirationTime: Instant) :
  BsaToken {
  constructor(
    bytes: ByteArray,
    expirationTime: Instant,
  ) : this(BsaTokenBytes(bytes), expirationTime)

  /**
   * Qualifier for [ProxyToken]-based things, when type-parameters aren't enough to disambiguate.
   */
  @javax.inject.Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class Qualifier
}

/** [BsaToken] intended for use when sending inference requests to Aratea. */
data class ArateaToken(override val bytes: BsaTokenBytes) : BsaToken {
  constructor(bytes: ByteArray) : this(BsaTokenBytes(bytes))

  /**
   * Qualifier for [ArateaToken]-based things, when type-parameters aren't enough to disambiguate.
   */
  @javax.inject.Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class Qualifier
}

/**
 * Provides a stable string name for the receiving [Class].
 *
 * This value can be used in persisted data where the actual class name could change between
 * application launches due to the application being updated and proguard providing
 * differently-obfuscated names.
 */
val Class<*>.stableTokenClassName: String
  get() =
    when (this) {
      ArateaToken::class.java -> "ArateaToken"
      ProxyToken::class.java -> "ProxyToken"
      else -> throw IllegalArgumentException("Bad tokenClass value")
    }
