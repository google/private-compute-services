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

package com.android.personalcontext.ace.internal.templates.richcard.placeholder

import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.internal.templates.richcard.decoder.CardUiDataDecoder
import javax.inject.Inject
import javax.inject.Singleton

/** Converter between [PlaceHolderCardUiData] and [ContextInsight]. */
@Singleton
@Suppress("NewApi")
class PlaceHolderCardUiDataDecoder @Inject constructor() :
  CardUiDataDecoder<PlaceHolderCardUiData>() {

  override fun ContextInsight.toCardContext(): PlaceHolderCardUiData {
    return this.toPrototypeInsight<CardInsight>()?.let { cardInsight ->
      val bodyInsights = cardInsight.body as? InsightCollection
      val displayInsight = bodyInsights?.insights?.filterIsInstance<DisplayInsight>()?.firstOrNull()
      val message = displayInsight?.details?.title?.toString()
      val action = cardInsight.getCardContextAction()

      PlaceHolderCardUiData(message = message, action = action)
    } ?: throw IllegalArgumentException("Failed to convert ContextInsight to PlaceHolderCardUiData")
  }
}
