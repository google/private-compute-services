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

package com.google.android.`as`.oss.feedback.config

import com.google.android.`as`.oss.common.config.AbstractConfigReader
import com.google.android.`as`.oss.common.config.FlagListener
import com.google.android.`as`.oss.common.config.FlagManager

/** Config reader for Feedback. */
class FeedbackConfigReader(private val flagManager: FlagManager) :
  AbstractConfigReader<FeedbackConfig>() {
  init {
    refreshConfig()
    flagManager
      .listenable()
      .addListener(
        FlagListener {
          if (FlagListener.anyHasPrefix(it, FeedbackFlags.PREFIX)) {
            refreshConfig()
          }
        }
      )
  }

  override fun computeConfig(): FeedbackConfig {
    return FeedbackConfig(
      enableSelectedEntityContent = flagManager.get(FeedbackFlags.ENABLE_SELECTED_ENTITY_CONTENT),
      enableViewDataDialogV2SingleEntity =
        flagManager.get(FeedbackFlags.ENABLE_VIEW_DATA_DIALOG_V2_SINGLE_ENTITY),
      enableViewDataDialogV2MultiEntity =
        flagManager.get(FeedbackFlags.ENABLE_VIEW_DATA_DIALOG_V2_MULTI_ENTITY),
      enableOptInUiV2 = flagManager.get(FeedbackFlags.ENABLE_OPT_IN_UI_V2),
      enableGroundTruthSelectorSingleEntity =
        flagManager.get(FeedbackFlags.ENABLE_GROUND_TRUTH_SELECTOR_SINGLE_ENTITY),
      enableGroundTruthSelectorMultiEntity =
        flagManager.get(FeedbackFlags.ENABLE_GROUND_TRUTH_SELECTOR_MULTI_ENTITY),
    )
  }
}
