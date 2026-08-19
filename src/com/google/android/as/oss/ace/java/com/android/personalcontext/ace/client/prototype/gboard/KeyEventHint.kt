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

package com.android.personalcontext.ace.client.prototype.gboard

import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.BundleCompat
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.KeyEventHintId

/** A hint for passing down a verified key event. */
data class KeyEventHint(val keyEvent: KeyEvent) : PrototypeHint(KeyEventHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putParcelable(KEY_KEY_EVENT, keyEvent)
  }

  companion object : Creator {
    private const val KEY_KEY_EVENT = "key_event"

    override fun create(bundle: Bundle): PrototypeHint =
      KeyEventHint(
        keyEvent =
          BundleCompat.getParcelable(bundle, KEY_KEY_EVENT, KeyEvent::class.java)
            ?: error("KeyEvent is null")
      )
  }
}
