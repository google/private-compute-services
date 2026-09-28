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

package com.android.personalcontext.ace.client.prototype.session

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.EmbeddedSessionHintId
import java.util.UUID

/**
 * A [PrototypeHint] representing the embedded session state.
 *
 * @property uuid A unique identifier for the embedded session.
 */
data class EmbeddedSessionHint(val uuid: UUID) : PrototypeHint(EmbeddedSessionHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(UUID_KEY, uuid.toString())
  }

  companion object : Creator {
    const val UUID_KEY = "UUID_KEY"

    override fun create(bundle: Bundle): PrototypeHint =
      EmbeddedSessionHint(
        uuid =
          bundle.getString(UUID_KEY)?.let { runCatching { UUID.fromString(it) }.getOrNull() }
            ?: UUID.randomUUID()
      )
  }
}
