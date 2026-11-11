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

package com.android.personalcontext.ace.visualizer.templates.call.data

import android.app.RemoteAction
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightActionDetails
import android.service.personalcontext.insight.InsightCollection
import android.service.personalcontext.insight.InsightDisplayDetails
import android.util.Log
import com.android.personalcontext.ace.visualizer.compat.CardInsightCompat
import com.android.personalcontext.ace.visualizer.compat.ClientActionInsightCompat
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat
import java.util.UUID
import javax.inject.Inject

class CallInsightConverter
@Inject
constructor(
  private val cardInsightCompat: CardInsightCompat,
  private val insightGridCompat: InsightGridCompat,
  private val clientActionInsightCompat: ClientActionInsightCompat,
) {
  private val TAG = "CallInsightConverter"

  /**
   * Converts a top-level [ContextInsight] to a [CallVisualizerWidget].
   *
   * The top-level [ContextInsight] is expected to be a [CardInsight]. The top-level [CardInsight]
   * is expected to have the following properties:
   * 1. [Required] A body [InsightCollection] representing the dynamic data from OSI
   * 2. [Optional] A footer [DisplayInsight], representing the AI disclaimer
   * 3. [Optional] An actions [InsightCollection] containing a [DisplayInsight], representing the
   *    "Show more" button
   */
  fun convert(insight: ContextInsight): CallVisualizerWidget = insight.toCallVisualizerWidget()

  private fun ContextInsight.toCallVisualizerWidget(): CallVisualizerWidget =
    cardInsightCompat.ifCardInsight(this) { cardInsight ->
      Log.i(TAG, "[CallEmbedded] #toCallVisualizerWidget getting top level widget CardInsight")

      val dynamicDataInsightCollection =
        (cardInsight.body as? InsightCollection)
          ?: error(
            "[CallEmbedded] #toCallVisualizerWidget Expected mainInsight.body to be an InsightCollection, actual: ${cardInsight.body.javaClass.simpleName}."
          )
      val dynamicDataInsights: List<ContextInsight> = dynamicDataInsightCollection.insights

      val detailedCards: List<CallVisualizerDetailedCard> = dynamicDataInsights.mapNotNull {
        it.toSingleDetailedCard()
      }

      val generalCards: List<List<CallVisualizerGeneralCard>> = dynamicDataInsights.mapNotNull {
        it.toCallVisualizerGeneralCards()
      }

      val aiDisclaimer: DisplayInsight? = cardInsight.footer?.toAiDisclaimer()
      val ctaDisplayMoreResults: CtaDisplayMoreResults? =
        cardInsight.actions?.toCtaDisplayMoreResults()

      if (detailedCards.isEmpty() && generalCards.sumOf { it.size } == 0) {
        error(
          "[CallEmbedded] #toCallVisualizerWidget No actual cards were included in the InsightCollection."
        )
      }

      Log.i(
        TAG,
        "[CallEmbedded] #toCallVisualizerWidget Returning CallVisualizerWidget with ${detailedCards.size} detailed cards, ${generalCards.sumOf { it.size }} general cards",
      )

      CallVisualizerWidget(
        detailedCards = detailedCards,
        generalCards = generalCards,
        aiDisclaimer = aiDisclaimer,
        ctaDisplayMoreResults = ctaDisplayMoreResults,
      )
    }
      ?: error(
        "[CallEmbedded] #toCallVisualizerWidget Expected mainInsight to be a CardInsight, actual: ${this.javaClass.simpleName}."
      )

  /**
   * Converts an [InsightCollection] to a single [CallVisualizerDetailedCard].
   *
   * The [InsightCollection] is expected to have 3 insights in the following order:
   * 1. A header [DisplayInsight], representing the title of the detailed card
   * 2. A rows [InsightCollection], representing a single row of "details"
   * 3. An action [ActionableInsight] for deeplinks
   * 4. A feedback [DisplayInsight] for providing feedback
   *
   * If any of the insights are not in the expected order, or are not the expected type, null will
   * be returned.
   */
  private fun ContextInsight.toSingleDetailedCard(): CallVisualizerDetailedCard? =
    cardInsightCompat.ifCardInsight(this) { cardInsight ->
      val detailedCardInsight = this@toSingleDetailedCard

      val header = (cardInsight.header as? DisplayInsight)
      val items =
        insightGridCompat.ifInsightGrid(cardInsight.body) { insightGrid -> insightGrid.items }
      val feedback = (cardInsight.footer as? DisplayInsight)
      val deeplink = (cardInsight.actions as? ActionableInsight)
      val dataSource = deeplink?.actionDetails?.toDataSource(deeplink)

      // Check required fields and disqualify if not present
      if (header == null) {
        Log.e(
          TAG,
          "[CallEmbedded] #toSingleDetailedCard detailedCardInsight.header is not a DisplayInsight",
        )
        return@ifCardInsight null
      }
      if (items == null) {
        Log.e(
          TAG,
          "[CallEmbedded] #toSingleDetailedCard detailedCardInsight.body is not an InsightGrid",
        )
        return@ifCardInsight null
      }
      if (items.isEmpty()) {
        Log.e(
          TAG,
          "[CallEmbedded] #toSingleDetailedCard detailedCardInsight.body is an empty InsightGrid",
        )
        return@ifCardInsight null
      }

      CallVisualizerDetailedCard(
        title = header,
        items = items,
        feedback = feedback,
        deeplink = deeplink,
        originalDataSource = dataSource,
        originalInsight = detailedCardInsight,
        listUuid = UUID.randomUUID().toString(),
      )
    }
      ?: run {
        Log.e(
          TAG,
          "[CallEmbedded] #toSingleDetailedCard detailedCardInsight is not a CardInsight in the expected format",
        )
        null
      }

  /**
   * Converts a [ContextInsight] to a list of [CallVisualizerGeneralCard]
   *
   * @return null if the [ContextInsight] is not a valid list of general cards. Returned list will
   *   never be empty.
   */
  private fun ContextInsight.toCallVisualizerGeneralCards(): List<CallVisualizerGeneralCard>? {
    val insightCollection = (this as? InsightCollection) ?: return null

    // Check if it's an InsightCollection of ActionableInsights or DisplayInsights. If not, return
    // null
    if (!insightCollection.isActionableOrDisplayInsightCollection()) {
      Log.w(
        TAG,
        "[CallEmbedded] #toCallVisualizerGeneralCards dropped InsightCollection: not all children are ActionableInsight or DisplayInsight (size=${insightCollection.insights.size})",
      )
      return null
    }

    // At this point, InsightCollection is confirmed to be a list of ActionableInsights or
    // DisplayInsights. Map them to [CallVisualizerGeneralCard]

    val generalCards =
      insightCollection.insights.mapNotNull { generalCardInsight: ContextInsight ->
        (generalCardInsight as? ActionableInsight)?.toCallVisualizerGeneralCard()
          ?: (generalCardInsight as? DisplayInsight)?.toCallVisualizerGeneralCard()
      }

    if (generalCards.isEmpty()) {
      Log.w(
        TAG,
        "[CallEmbedded] #toCallVisualizerGeneralCards dropped InsightCollection: mapped child generalCards is empty (inputSize=${insightCollection.insights.size})",
      )
      return null
    }

    return generalCards
  }

  /**
   * Returns true if the [InsightCollection] is an InsightCollection of ActionableInsights or
   * DisplayInsights.
   */
  private fun InsightCollection.isActionableOrDisplayInsightCollection(): Boolean =
    this.insights.all { it is ActionableInsight || it is DisplayInsight }

  /** Converts an [ActionableInsight] to a [CallVisualizerGeneralCard]. */
  private fun ActionableInsight.toCallVisualizerGeneralCard(): CallVisualizerGeneralCard? {
    val displayDetails = this.displayDetails
    val dataSource = this.actionDetails.toDataSource(this)
    if (dataSource == null) {
      Log.i(TAG, "[CallEmbedded] #toCallVisualizerGeneralCard No data source found")
      return null
    }

    return CallVisualizerGeneralCard(
      title = displayDetails.title?.toString() ?: "",
      date = displayDetails.subtitle?.toString() ?: "",
      detailedText = displayDetails.contentDescription?.toString() ?: "",
      detailedTextIcon = displayDetails.icon,
      dataSource = dataSource.remoteAction,
      originalDataSource = dataSource,
      cardExpandButtonAccessibilityContentDescription = "",
      listUuid = UUID.randomUUID().toString(),
      originalInsight = this,
    )
  }

  /**
   * Converts a [DisplayInsight] to a [CallVisualizerGeneralCard]. Only used if the
   * [ActionableInsight] is not available (ie. a deeplink is missing).
   */
  private fun DisplayInsight.toCallVisualizerGeneralCard(): CallVisualizerGeneralCard {
    val displayDetails = this.details

    return CallVisualizerGeneralCard(
      title = displayDetails.title?.toString() ?: "",
      date = displayDetails.subtitle?.toString() ?: "",
      detailedText = displayDetails.contentDescription?.toString() ?: "",
      detailedTextIcon = displayDetails.icon,
      dataSource = null,
      originalDataSource = null,
      cardExpandButtonAccessibilityContentDescription = "",
      listUuid = UUID.randomUUID().toString(),
      originalInsight = this,
    )
  }

  /** Converts [InsightActionDetails] to [CallVisualizerResponseSource]. */
  private fun InsightActionDetails.toRemoteAction(): RemoteAction? =
    if (this.remoteAction?.actionIntent == null) {
      null
    } else {
      this.remoteAction
    }

  /**
   * The [InsightCollection] is expected to have a single [DisplayInsight] representing the "Show
   * more" button
   */
  private fun ContextInsight.toCtaDisplayMoreResults(): CtaDisplayMoreResults? =
    with(clientActionInsightCompat) {
      val insight = this@toCtaDisplayMoreResults
      val results: CtaDisplayMoreResults? =
        ifClientActionInsight(insight) { clientActionInsight ->
          clientActionInsight.insightDisplayDetails.toCtaDisplayMoreResults(insight)
        }
          ?: run {
            // Fallback to DisplayInsight if OSI did not send a client action insight, or if
            // clientActionInsightCompat is not implemented
            (insight as? DisplayInsight)?.details?.toCtaDisplayMoreResults(insight)
          }

      return results
    }

  private fun InsightDisplayDetails.toCtaDisplayMoreResults(
    originalInsight: ContextInsight
  ): CtaDisplayMoreResults? {
    val insightDisplayDetails = this@toCtaDisplayMoreResults

    // Required fields and disqualify if not present
    val title = insightDisplayDetails.title?.toString()

    if (title.isNullOrBlank()) {
      Log.i(TAG, "[CallEmbedded] #toCtaDisplayMoreResults title is null or blank")
      return null
    }

    // Optional fields
    val contentDescription = insightDisplayDetails.contentDescription?.toString()
    val icon = insightDisplayDetails.icon

    return CtaDisplayMoreResults(
      title = title,
      contentDescription = contentDescription,
      icon = icon,
      originalInsight = originalInsight,
    )
  }

  private fun InsightActionDetails.toDataSource(originalInsight: ContextInsight): DataSource? {
    val remoteAction = this.toRemoteAction() ?: return null

    return DataSource(
      title = remoteAction.title.toString(),
      appName = remoteAction.contentDescription.toString(),
      appIcon = remoteAction.icon,
      remoteAction = remoteAction,
      originalInsight = originalInsight,
    )
  }

  /** Converts a [ContextInsight] to an AI disclaimer string. */
  private fun ContextInsight.toAiDisclaimer(): DisplayInsight? {
    val insight = (this as? DisplayInsight) ?: return null

    if (insight.details.title?.toString().isNullOrEmpty()) {
      return null
    }

    return insight
  }
}
