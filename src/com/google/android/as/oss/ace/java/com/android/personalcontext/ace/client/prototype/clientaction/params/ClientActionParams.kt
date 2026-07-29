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

package com.android.personalcontext.ace.client.prototype.clientaction.params

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.android.personalcontext.ace.common.ClientActionParameters

/** Unique identifier for each type of ClientActionParams. */
enum class ClientActionParamId(val uid: Int) {
  UNKNOWN(0),
  SHARE_PHOTO(1),
  SHOW_CARDS(2),
  FULL_SCREEN_REQUEST(3),
  TEXT_PASTE(4),
  SHARE_LIVE_LOCATION(5),
  HIDE_KEYBOARD(6),
  BLUEFLAX(7),
}

/** Base class for Bundle-backed ClientActionParams. */
abstract class ClientActionParams : ClientActionParameters {
  /** Unique identifier for each type of ClientActionParams. */
  abstract val id: ClientActionParamId

  fun exportDataToBundle(bundle: Bundle) {
    bundle.putInt(PARAM_ID_KEY, id.uid)
    writeToBundle(bundle)
  }

  /** Subclasses can override this to add their own data to the bundle. */
  protected open fun writeToBundle(bundle: Bundle) {}

  interface Creator {
    fun create(bundle: Bundle): ClientActionParams
  }

  companion object {
    @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
    const val PARAM_ID_KEY = "PARAM_ID"
  }
}

/** Fallback params for unknown client actions. */
class UnknownParams : ClientActionParams() {
  override val id = ClientActionParamId.UNKNOWN

  companion object : ClientActionParams.Creator {
    override fun create(bundle: Bundle): UnknownParams = UnknownParams()
  }
}
