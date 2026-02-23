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

import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory

data class FeedbackConfig(
  // Whether to enable the selected entity content in the feedback dialog.
  val enableSelectedEntityContent: Boolean,
  // Whether to enable the view data dialog v2 for single entity feedback.
  val enableViewDataDialogV2SingleEntity: Boolean,
  // Whether to enable the opt in ui v2 for multi entity feedback.
  val enableOptInUiV2: Boolean,
  // Whether to enable the ground truth selector for single entity feedback.
  val enableGroundTruthSelectorSingleEntity: Boolean,
  // Whether to enable the ground truth selector for multi entity feedback.
  val enableGroundTruthSelectorMultiEntity: Boolean,
  // Default opt-in state for each CUJ's data collection category. .
  val dataCollectionCategoryDefaultOptIn: Map<String, List<DataCollectionCategory>>,
)
