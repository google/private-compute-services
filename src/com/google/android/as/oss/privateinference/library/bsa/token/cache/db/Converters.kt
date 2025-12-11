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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache.db

import androidx.room.TypeConverter
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.crypto.EncryptedBsaTokenBytes
import java.time.Instant
import okio.ByteString.Companion.decodeBase64

object Converters {
  @TypeConverter
  fun convertEncryptedBytesToString(value: EncryptedBsaTokenBytes?): String? = value?.toString()

  @TypeConverter
  fun convertStringToEncryptedBytes(value: String?): EncryptedBsaTokenBytes? =
    value?.let(EncryptedBsaTokenBytes::fromString)

  @TypeConverter
  fun convertBsaTokenParamsToString(value: BsaTokenParams<*>?): String? =
    when (value) {
      is ArateaTokenParams -> "aratea(${value.challengeBase64})"
      is ProxyTokenParams -> "proxy"
      null -> null
    }

  @TypeConverter
  fun convertStringToBsaTokenParams(value: String?): BsaTokenParams<*>? {
    return if (value == null) {
      null
    } else if (value.startsWith("aratea(")) {
      val challenge =
        requireNotNull(value.substring(7, value.length - 1).decodeBase64()?.toByteArray())
      ArateaTokenParams(challenge)
    } else if (value.startsWith("proxy")) {
      ProxyTokenParams()
    } else {
      null
    }
  }

  @TypeConverter fun convertInstantToLong(value: Instant?): Long? = value?.toEpochMilli()

  @TypeConverter
  fun convertLongToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}
