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

package com.android.personalcontext.ace.internal.templates.richcard.common.utils

import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsightV1
import com.android.personalcontext.ace.client.prototype.clientaction.ClientActionInsight
import com.android.personalcontext.ace.client.prototype.clientaction.params.textpaste.TextPasteParams
import com.android.personalcontext.ace.internal.templates.richcard.ActionableCardAction
import com.android.personalcontext.ace.internal.templates.richcard.CardAction
import com.android.personalcontext.ace.internal.templates.richcard.EgressableCardAction

/**
 * Converts a [InsightCollection] to a list of [CardAction]. The [CardUiDataConverter] need this to
 * convert the insight collection to card ui data.
 *
 * @return The list of [CardAction].
 */
fun InsightCollection.toCardActions(): List<CardAction> = insights.mapNotNull { insight ->
  when (insight) {
    is ActionableInsight -> insight.toActionableCardAction()
    is DisplayInsight -> insight.toEgressableCardAction()
    else -> {
      val prototypeInsight = insight.toPrototypeInsightV1()
      if (prototypeInsight is ClientActionInsight) {
        prototypeInsight.toEgressableCardAction(insight)
      } else {
        null
      }
    }
  }
}

/**
 * Converts an [ActionableInsight] to a [ActionableCardAction].
 *
 * @return The [ActionableCardAction].
 */
private fun ActionableInsight.toActionableCardAction(): ActionableCardAction? =
  actionDetails.remoteAction?.let { remoteAction ->
    ActionableCardAction(
      displayDetails = displayDetails,
      insight = this,
      remoteAction = remoteAction,
    )
  }

/**
 * Converts a [DisplayInsight] to a [EgressableCardAction].
 *
 * @return The [EgressableCardAction].
 */
private fun DisplayInsight.toEgressableCardAction(): EgressableCardAction =
  EgressableCardAction(displayDetails = details, insight = this)

/**
 * Converts a [ClientActionInsight] to a [EgressableCardAction].
 *
 * @return The [EgressableCardAction].
 */
private fun ClientActionInsight.toEgressableCardAction(
  insight: ContextInsight
): EgressableCardAction? =
  when (val params = clientActionParams) {
    is TextPasteParams -> {
      EgressableCardAction(displayDetails = insightDisplayDetails, insight = insight)
    }
    else -> {
      null
    }
  }
