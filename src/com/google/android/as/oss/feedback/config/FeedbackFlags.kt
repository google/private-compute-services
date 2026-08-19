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

import com.google.android.`as`.oss.common.config.FlagManager.BooleanFlag

object FeedbackFlags {
  const val PREFIX = "PcsFeedback__"

  val ENABLE_SELECTED_ENTITY_CONTENT =
    BooleanFlag.create("${PREFIX}enable_selected_entity_content", false)
  val ENABLE_VIEW_DATA_DIALOG_V2_SINGLE_ENTITY =
    BooleanFlag.create("${PREFIX}enable_view_data_dialog_v2_single_entity", false)
  val ENABLE_OPT_IN_UI_V2 = BooleanFlag.create("${PREFIX}enable_opt_in_ui_v2", false)
  val ENABLE_GROUND_TRUTH_SELECTOR_SINGLE_ENTITY =
    BooleanFlag.create("${PREFIX}enable_ground_truth_selector_single_entity", false)
  val ENABLE_GROUND_TRUTH_SELECTOR_MULTI_ENTITY =
    BooleanFlag.create("${PREFIX}enable_ground_truth_selector_multi_entity", false)
  val ENABLE_FINE_GRAINED_VIEW_DATA_DIALOG =
    BooleanFlag.create("${PREFIX}enable_fine_grained_view_data_dialog", false)
  val ENABLE_DEFAULT_GENERAL_SPOON_DONATION_OPT_IN =
    BooleanFlag.create("${PREFIX}enable_default_general_spoon_donation_opt_in", false)
  val ENABLE_DEFAULT_DONATION_OPT_IN_L1 =
    BooleanFlag.create("${PREFIX}enable_default_donation_opt_in_l1", false)
  val ENABLE_DEFAULT_DONATION_OPT_IN_L0 =
    BooleanFlag.create("${PREFIX}enable_default_donation_opt_in_l0", false)
  val ENABLE_BLUEFLAX_FEEDBACK = BooleanFlag.create("${PREFIX}enable_blueflax_feedback", false)
}
