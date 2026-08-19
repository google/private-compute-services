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
import com.google.android.`as`.oss.common.flavor.BuildFlavor
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.AppInfo
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.FailureReason
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.IntentQueries
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.MemoryEntities
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.ModelOutputs
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.NotificationContent
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.QuartzModelOutputs
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.SelectedEntityContent
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.TriggeringMessages

/** Config reader for Feedback. */
class FeedbackConfigReader(
  private val flagManager: FlagManager,
  private val buildFlavor: BuildFlavor,
) : AbstractConfigReader<FeedbackConfig>() {
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
    val defaultGeneralSpoonDonationOptInEnabled =
      flagManager.get(FeedbackFlags.ENABLE_DEFAULT_GENERAL_SPOON_DONATION_OPT_IN)
    val defaultDonationOptInL1Enabled =
      flagManager.get(FeedbackFlags.ENABLE_DEFAULT_DONATION_OPT_IN_L1)
    val defaultDonationOptInL0Enabled =
      flagManager.get(FeedbackFlags.ENABLE_DEFAULT_DONATION_OPT_IN_L0)
    val defaultOptInMap =
      createDefaultOptInMap(
        defaultGeneralSpoonDonationOptInEnabled,
        defaultDonationOptInL1Enabled,
        defaultDonationOptInL0Enabled,
      )

    return FeedbackConfig(
      enableSelectedEntityContent = flagManager.get(FeedbackFlags.ENABLE_SELECTED_ENTITY_CONTENT),
      enableViewDataDialogV2SingleEntity =
        flagManager.get(FeedbackFlags.ENABLE_VIEW_DATA_DIALOG_V2_SINGLE_ENTITY),
      enableOptInUiV2 = flagManager.get(FeedbackFlags.ENABLE_OPT_IN_UI_V2),
      enableGroundTruthSelectorSingleEntity =
        flagManager.get(FeedbackFlags.ENABLE_GROUND_TRUTH_SELECTOR_SINGLE_ENTITY),
      enableGroundTruthSelectorMultiEntity =
        flagManager.get(FeedbackFlags.ENABLE_GROUND_TRUTH_SELECTOR_MULTI_ENTITY),
      dataCollectionCategoryDefaultOptIn = defaultOptInMap,
      enableFineGrainedViewDataDialog =
        flagManager.get(FeedbackFlags.ENABLE_FINE_GRAINED_VIEW_DATA_DIALOG),
      enableDefaultDonationOptInL1 = defaultDonationOptInL1Enabled,
      enableDefaultDonationOptInL0 = defaultDonationOptInL0Enabled,
      enableBlueflaxFeedback = flagManager.get(FeedbackFlags.ENABLE_BLUEFLAX_FEEDBACK),
    )
  }

  private fun createDefaultOptInMap(
    defaultGeneralSpoonDonationOptInEnabled: Boolean,
    defaultDonationOptInL1Enabled: Boolean,
    defaultDonationOptInL0Enabled: Boolean,
  ): Map<String, List<DataCollectionCategory>> = buildMap {
    if (buildFlavor == BuildFlavor.INTERNAL) {
      put(
        QuartzCUJ.QUARTZ_CUJ_KEY_TYPE.name,
        listOf(NotificationContent, QuartzModelOutputs, AppInfo),
      )
      put(
        QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION.name,
        listOf(NotificationContent, QuartzModelOutputs, AppInfo),
      )
      put(
        QuartzCUJ.QUARTZ_CUJ_ADVANCED_ORGANIZER.name,
        listOf(NotificationContent, QuartzModelOutputs, AppInfo),
      )
    }

    val categoriesToApply: List<DataCollectionCategory> = buildList {
      // Add general categories if the flag is enabled
      if (defaultGeneralSpoonDonationOptInEnabled) {
        add(TriggeringMessages)
        add(IntentQueries)
        add(ModelOutputs)
        add(FailureReason)
        add(SelectedEntityContent)
      }

      // Add MemoryEntities if either L1 or L0 flag is enabled
      if (defaultDonationOptInL1Enabled || defaultDonationOptInL0Enabled) {
        add(MemoryEntities)
      }
    }

    if (categoriesToApply.isNotEmpty()) {
      for (cuj in SPOON_CUJS) {
        put(cuj.name, categoriesToApply)
      }
    }
  }

  companion object {
    private val SPOON_CUJS =
      SpoonCUJ.values().filter { cuj ->
        cuj != SpoonCUJ.SPOON_CUJ_UNKNOWN && cuj != SpoonCUJ.UNRECOGNIZED
      }
  }
}
