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

import com.google.android.`as`.oss.common.config.AbstractConfigReader
import com.google.android.`as`.oss.common.config.FlagListener
import com.google.android.`as`.oss.common.config.FlagManager

/** Config reader for DUI Beacon Feature */
class BeaconConfigReader(private val flagManager: FlagManager) :
  AbstractConfigReader<BeaconConfig>() {
  init {
    refreshConfig()
    flagManager
      .listenable()
      .addListener(
        FlagListener {
          if (FlagListener.anyHasPrefix(it, BeaconFlags.PREFIX)) {
            refreshConfig()
          }
        }
      )
  }

  override fun computeConfig(): BeaconConfig {
    return BeaconConfig(
      beaconUiConfigs =
        beaconUiConfigs {
          generalCardDetailedTextCollapsedLineCount =
            flagManager.get(BeaconFlags.GENERAL_CARD_DETAILED_TEXT_COLLAPSED_LINE_COUNT)
          generalCardDetailedTextExpandedLineCount =
            flagManager.get(BeaconFlags.GENERAL_CARD_DETAILED_TEXT_EXPANDED_LINE_COUNT)
          generalCardTitleTextCollapsedLineCount =
            flagManager.get(BeaconFlags.GENERAL_CARD_TITLE_TEXT_COLLAPSED_LINE_COUNT)
          generalCardTitleTextExpandedLineCount =
            flagManager.get(BeaconFlags.GENERAL_CARD_TITLE_TEXT_EXPANDED_LINE_COUNT)
          generalCardsMaximumDisplayUiVersion =
            flagManager.get(BeaconFlags.GENERAL_CARDS_MAXIMUM_DISPLAY_UI_VERSION)
        }
    )
  }
}
