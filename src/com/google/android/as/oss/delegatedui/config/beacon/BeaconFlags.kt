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

package com.google.android.`as`.oss.delegatedui.config.beacon

import com.google.android.`as`.oss.common.config.FlagManager.IntegerFlag

/** Flags for Beacon UI. */
object BeaconFlags {
  const val PREFIX = "DelegatedUiBeacon__"
  val GENERAL_CARD_DETAILED_TEXT_COLLAPSED_LINE_COUNT =
    IntegerFlag.create("${PREFIX}general_card_detailed_text_collapsed_line_count", 1)
  val GENERAL_CARD_DETAILED_TEXT_EXPANDED_LINE_COUNT =
    IntegerFlag.create("${PREFIX}general_card_detailed_text_expanded_line_count", 4)
  val GENERAL_CARD_TITLE_TEXT_COLLAPSED_LINE_COUNT =
    IntegerFlag.create("${PREFIX}general_card_title_text_collapsed_line_count", 1)
  val GENERAL_CARD_TITLE_TEXT_EXPANDED_LINE_COUNT =
    IntegerFlag.create("${PREFIX}general_card_title_text_expanded_line_count", 2)
  val GENERAL_CARDS_MAXIMUM_DISPLAY_UI_VERSION =
    IntegerFlag.create("${PREFIX}general_cards_maximum_display_ui_version", 0)
}
