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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.client.prototype.visualizerstate

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.VisualizerStateHintId

/**
 * A hint representing interactive visualizer state modifications republished by the client.
 *
 * @property visualizerStates The [VisualizerStateData]s to persist into the visualizer session's
 *   state store.
 */
data class VisualizerStateHint(val visualizerStates: List<VisualizerStateData>) :
  PrototypeHint(VisualizerStateHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putParcelableArrayList(VISUALIZER_STATES_KEY, visualizerStates.toBundleList())
  }

  companion object : Creator {
    private const val VISUALIZER_STATES_KEY = "visualizer_states"

    override fun create(bundle: Bundle): PrototypeHint =
      VisualizerStateHint(
        visualizerStates =
          bundle
            .getParcelableArrayList(VISUALIZER_STATES_KEY, Bundle::class.java)
            .toVisualizerStateDataList()
      )
  }
}
