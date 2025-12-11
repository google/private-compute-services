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

package com.google.android.`as`.oss.conversationid.util

import java.security.MessageDigest

/** Utility class which hashes conversation id into strings using a salted hash. */
class ConversationIdHasher(val salt: String) {

  private val digest = MessageDigest.getInstance("SHA-256")

  fun hash(conversationId: String): String {
    digest.reset()
    digest.update(salt.toByteArray())
    digest.update(conversationId.toByteArray())
    return bytesToHex(digest.digest())
  }

  private fun bytesToHex(bytes: ByteArray): String {
    return buildString {
      for (byte in bytes) {
        append(String.format("%02x", byte))
      }
    }
  }
}
