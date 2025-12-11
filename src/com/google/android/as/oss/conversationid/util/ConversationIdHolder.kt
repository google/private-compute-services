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

import com.google.common.time.TimeSource
import java.time.Duration
import java.time.Instant

/**
 * A holder for the conversation id. It keeps conversation id until it expires in 2 hours. Also
 * provides anonymous conversation id.
 */
class ConversationIdHolder(private val timeSource: TimeSource) {
  private var value: String? = null
  private var expiryTime: Instant? = null
  private var hasher: ConversationIdHasher = ConversationIdHasher("")

  /** @return true if the new value is different from the old value. */
  @Synchronized
  fun setValueWithSalt(value: String, salt: String): Boolean {
    val changed =
      if (salt != hasher.salt) {
        hasher = ConversationIdHasher(salt)
        true // a salt update changes the result regardless of value
      } else {
        this.value != value
      }
    expiryTime = timeSource.instant() + EXPIRE_DURATION
    this.value = value
    return changed
  }

  @Synchronized
  fun getAnonymousId(): String? {
    expiryTime?.let {
      val now = timeSource.instant()
      if (now > it) {
        clear()
      }
    }
    return value?.let { hasher.hash(it) }
  }

  @Synchronized
  fun clear() {
    value = null
    expiryTime = null
  }

  companion object {
    private val EXPIRE_DURATION = Duration.ofHours(2)
  }
}
