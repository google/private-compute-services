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

package com.google.android.`as`.oss.delegatedui.service.templates.bugle

import android.app.PendingIntent
import com.google.android.`as`.oss.delegatedui.api.integration.templates.bugle.BugleSuggestion
import com.google.android.`as`.oss.delegatedui.api.integration.templates.bugle.BugleTemplateData

internal data class BugleSuggestionModel(
  val suggestion: BugleSuggestion,
  val pendingIntentList: List<PendingIntent?>,
) {
  init {
    require(suggestion.attributionDialogData.attributionsCount == pendingIntentList.size) {
      "pendingIntentList's size must match suggestion.attributionDialogData.attributionsCount. " +
        "Expected size: ${suggestion.attributionDialogData.attributionsCount}, " +
        "but actual size was: ${pendingIntentList.size}."
    }
  }
}

/**
 * Gets the [BugleSuggestionModel]s, which contains the [BugleSuggestion] and the [PendingIntent]s.
 *
 * @param bugleTemplateData The [BugleTemplateData] containing the [BugleSuggestion]s and the
 *   mapping between [PendingIntent]s and `AttributionCardData`s.
 * @param pendingIntentList The list of [PendingIntent]s.
 */
internal fun getBugleSuggestionModelList(
  bugleTemplateData: BugleTemplateData,
  pendingIntentList: List<PendingIntent>?,
): List<BugleSuggestionModel> {
  val pendingIntentToAttributionMap = bugleTemplateData.pendingIntentToAttributionMapList
  var pendingIntentIndex = 0
  var attributionIndex = 0
  return bugleTemplateData.bugleSuggestionsList.map { bugleSuggestion ->
    val suggestionPendingIntentList =
      bugleSuggestion.attributionDialogData.attributionsList.map {
        if (pendingIntentToAttributionMap.getOrNull(pendingIntentIndex) == attributionIndex++) {
          pendingIntentList?.getOrNull(pendingIntentIndex++)
        } else {
          null // No pending intent for this attribution, null is required for placeholder in list.
        }
      }

    BugleSuggestionModel(bugleSuggestion, suggestionPendingIntentList)
  }
}
