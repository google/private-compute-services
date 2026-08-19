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

package com.android.personalcontext.ace.internal.templates.richcard.decoder

import android.graphics.drawable.Icon
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import androidx.compose.foundation.layout.WindowInsets
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.isPrototypeInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.loading.LoadingInsight
import com.android.personalcontext.ace.client.prototype.richcard.RichCardHint
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.common.DisplayableInsight
import com.android.personalcontext.ace.common.asDisplayableInsight
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.richcard.Attribution
import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.internal.templates.richcard.CardTitle
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext
import com.android.personalcontext.ace.internal.templates.richcard.common.utils.toCardActions

/**
 * Converts between [CardUiData] and [CardInsight].
 *
 * @param T The type of the card context.
 */
@Suppress("NewApi")
abstract class CardUiDataDecoder<T : DeprecatedUiCardContext> {

  /**
   * Converts a [CardInsight] to a [CardUiData].
   *
   * @return The converted [CardUiData].
   * @receiver The [CardInsight] to convert.
   */
  fun CardInsight.toCardUiData(): CardUiData<T> {
    val attribution = getAttribution()
    val titleDisplayableInsight = getTitleDisplayableInsight()
    val cardActionDetails = getCardActionDetails(titleDisplayableInsight)
    val dismissInsight = getDismissInsight()
    val cardTitle = getCardTitle(titleDisplayableInsight)
    val icon = getCardIcon(titleDisplayableInsight)
    val cardContext = body.toCardContext()
    val cardContextAction = getCardContextAction()
    val actions = (actions as? InsightCollection)?.toCardActions()
    val richCardHint = toContextInsight().findPrototypeHint<RichCardHint>()
    val insets = richCardHint?.bottomInsetPx?.let { WindowInsets(bottom = it) } ?: WindowInsets()

    return CardUiData(
      cardActionDetails = cardActionDetails,
      cardTitle = cardTitle,
      icon = icon,
      titleInsight = titleDisplayableInsight,
      dismissInsight = dismissInsight,
      attribution = attribution,
      cardContext = cardContext,
      cardContextAction = cardContextAction,
      actions = actions,
      insets = insets,
    )
  }

  /** Extracts the header insight string. */
  private fun CardInsight.getHeaderInsightString(): String? {
    val headerInsights = header as? InsightCollection
    val displayInsights =
      headerInsights?.insights?.mapNotNull { it as? DisplayInsight } ?: emptyList()
    return displayInsights.firstOrNull()?.details?.title?.toString()?.ifEmpty { null }
  }

  /** Extracts the attribution details. */
  private fun CardInsight.getAttribution(): Attribution? {
    val headerInsights = header as? InsightCollection
    val displayInsights =
      headerInsights?.insights?.mapNotNull { it as? DisplayInsight } ?: emptyList()

    return displayInsights
      .takeIf { it.size > 1 }
      ?.let { insights ->
        val attributionTitle = insights[1].details.title?.toString()
        val attributionIcons = insights.drop(2).mapNotNull { it.details.icon }
        if (
          attributionTitle != null &&
            (attributionTitle.isNotEmpty() || attributionIcons.isNotEmpty())
        ) {
          Attribution(title = attributionTitle, sourceAppIcons = attributionIcons)
        } else {
          null
        }
      }
  }

  /** Extracts the title insight. */
  private fun CardInsight.getTitleDisplayableInsight(): DisplayableInsight? {
    val titleCollection = title as? InsightCollection
    val rawTitleInsight = titleCollection?.insights?.firstOrNull() ?: title
    return rawTitleInsight?.asDisplayableInsight()
  }

  /** Extracts the card action details. */
  private fun CardInsight.getCardActionDetails(titleInsight: DisplayableInsight?) =
    (container as? ActionableInsight)?.actionDetails
      ?: (titleInsight?.originalInsight as? ActionableInsight)?.actionDetails

  /** Extracts the dismiss insight. */
  private fun CardInsight.getDismissInsight(): ServerSideCloseInsight? {
    val titleCollection = title as? InsightCollection
    return titleCollection?.insights?.firstNotNullOfOrNull {
      it.toPrototypeInsight<ServerSideCloseInsight>()
    }
  }

  /** Extracts the card title. */
  private fun CardInsight.getCardTitle(titleInsight: DisplayableInsight?): CardTitle? {
    var insightString = titleInsight?.displayDetails?.title?.toString()

    // TODO: Remove fallback to header insight string for backwards compatibility.
    if (insightString.isNullOrEmpty()) {
      insightString = getHeaderInsightString()
    }

    val currentTitle = title
    // Is loading if the current title is LoadingInsight or contains a LoadingInsight
    val isLoading =
      when {
        currentTitle?.isPrototypeInsight<LoadingInsight>() == true -> true
        currentTitle is InsightCollection ->
          currentTitle.insights.any { it.isPrototypeInsight<LoadingInsight>() }
        else -> false
      }

    return when {
      isLoading -> CardTitle.Loading
      !insightString.isNullOrEmpty() -> CardTitle.Present(insightString)
      else -> null
    }
  }

  /** Extracts the card icon. */
  private fun CardInsight.getCardIcon(titleInsight: DisplayableInsight?): Icon? =
    container?.asDisplayableInsight()?.displayDetails?.icon ?: titleInsight?.displayDetails?.icon

  /** Extracts the card context action. */
  protected fun CardInsight.getCardContextAction(): CardContextAction? =
    (container as? ActionableInsight)?.let { actionableInsight ->
      actionableInsight.actionDetails.remoteAction?.let { remoteAction ->
        CardContextAction(insight = actionableInsight, remoteAction = remoteAction)
      }
    }

  /**
   * Converts a [ContextInsight] to a [DeprecatedUiCardContext].
   *
   * @return The converted [DeprecatedUiCardContext].
   * @receiver The [ContextInsight] to convert, usually from [CardInsight.body].
   */
  abstract fun ContextInsight.toCardContext(): T
}
