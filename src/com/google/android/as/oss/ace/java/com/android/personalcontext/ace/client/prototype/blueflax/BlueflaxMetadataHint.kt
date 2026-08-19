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

package com.android.personalcontext.ace.client.prototype.blueflax

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.BlueflaxMetadataHintId

/**
 * A hint for the Blueflax use case. Supplements BlueflaxHint with additional metadata.
 *
 * @property surface The surface type where the suggestion is shown.
 */
data class BlueflaxMetadataHint(val surface: SurfaceType, val limit: Int? = null) :
  PrototypeHint(BlueflaxMetadataHintId, this) {

  enum class SurfaceType(val value: String) {
    UNKNOWN("unknown"),
    HOME("home"),
    CONVERSATION("conversation");

    companion object {
      fun fromString(value: String?): SurfaceType {
        return entries.firstOrNull { it.value == value } ?: UNKNOWN
      }
    }
  }

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(KEY_SURFACE, surface.value)
    limit?.let { bundle.putInt(KEY_LIMIT, it) }
  }

  companion object : Creator {
    private const val KEY_SURFACE = "surface"
    private const val KEY_LIMIT = "limit"

    override fun create(bundle: Bundle): PrototypeHint {
      val surfaceStr = bundle.getString(KEY_SURFACE)
      val limit = if (bundle.containsKey(KEY_LIMIT)) bundle.getInt(KEY_LIMIT) else null
      return BlueflaxMetadataHint(surface = SurfaceType.fromString(surfaceStr), limit = limit)
    }
  }
}
