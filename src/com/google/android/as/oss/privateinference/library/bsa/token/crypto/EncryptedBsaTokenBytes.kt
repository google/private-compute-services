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

package com.google.android.`as`.oss.privateinference.library.bsa.token.crypto

import okio.ByteString
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.toByteString

/**
 * Container for encrypted data representing a
 * [com.google.android.as.oss.privateinference.library.bsa.token.BsaTokenBytes] object.
 */
data class EncryptedBsaTokenBytes(val encryptedData: ByteString, val associatedData: ByteString) {
  constructor(
    encryptedData: ByteArray,
    associatedData: ByteArray,
  ) : this(encryptedData.toByteString(), associatedData.toByteString())

  override fun toString(): String = encryptedData.base64() + "|" + associatedData.base64()

  companion object {
    fun fromString(value: String): EncryptedBsaTokenBytes? {
      val parts = value.split("|")
      if (parts.size != 2) return null
      val encryptedData = parts[0].decodeBase64() ?: return null
      val initializationVector = parts[1].decodeBase64() ?: return null
      return EncryptedBsaTokenBytes(encryptedData, initializationVector)
    }
  }
}
