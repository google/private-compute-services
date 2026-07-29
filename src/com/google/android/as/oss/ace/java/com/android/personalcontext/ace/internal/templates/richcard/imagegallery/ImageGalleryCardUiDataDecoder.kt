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

package com.android.personalcontext.ace.internal.templates.richcard.imagegallery

import android.graphics.drawable.Icon
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.isPrototypeInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.loading.LoadingInsight
import com.android.personalcontext.ace.internal.templates.richcard.decoder.CardUiDataDecoder
import javax.inject.Inject
import javax.inject.Singleton

/** Converter between [ImageGalleryCardUiData] and [ContextInsight]. */
@Singleton
class ImageGalleryCardUiDataDecoder @Inject constructor() :
  CardUiDataDecoder<ImageGalleryCardUiData>() {

  override fun ContextInsight.toCardContext(): ImageGalleryCardUiData {
    return this.toPrototypeInsight<CardInsight>()?.let { cardInsight ->
      if (cardInsight.isContentLoading()) {
        ImageGalleryCardUiData.LoadingData
      } else {
        ImageGalleryCardUiData.LoadedData(
          header = cardInsight.getHeaderStr(),
          subtitle = cardInsight.getSubtitleStr(),
          subtitleIcon = cardInsight.getSubtitleIcon(),
          subtitleSuffix = cardInsight.getSubtitleSuffix(),
          subtitleContentDescription = cardInsight.getSubtitleContentDescription(),
          tertiaryText = cardInsight.getTertiaryText(),
          images = cardInsight.getImages(),
          action = cardInsight.getCardContextAction(),
        )
      }
    }
      ?: throw IllegalArgumentException(
        "Failed to convert ContextInsight to ImageGalleryCardUiData"
      )
  }

  /** Checks whether the card insight represents an initial content loading state. */
  private fun CardInsight.isContentLoading(): Boolean =
    header.hasLoadingInsight() || isLegacyLoading()

  /** Checks whether this insight collection contains a [LoadingInsight]. */
  private fun ContextInsight?.hasLoadingInsight(): Boolean =
    this?.isPrototypeInsight<LoadingInsight>() == true ||
      (this as? InsightCollection)?.insights?.any { it.isPrototypeInsight<LoadingInsight>() } ==
        true

  /**
   * Helper function to ensure backwards compatibility with older provider APKs.
   *
   * TODO: Remove this helper function once older provider APK fully ages out
   */
  private fun CardInsight.isLegacyLoading(): Boolean =
    !getHeaderStr().isNullOrEmpty() &&
      getSubtitleStr() == null &&
      (body.hasLoadingInsight() || getImages().isEmpty()) &&
      getTertiaryText() == null

  /** Returns display insights from the header, if present. */
  private val CardInsight.displayInsights: List<DisplayInsight>
    get() =
      (header as? InsightCollection)?.insights?.filterIsInstance<DisplayInsight>() ?: emptyList()

  /** Extracts the title from the first header display insight. */
  private fun CardInsight.getHeaderStr(): String? =
    displayInsights.getOrNull(0)?.details?.title?.toString()

  /** Extracts the title from the second header display insight. */
  private fun CardInsight.getSubtitleStr(): String? =
    displayInsights.getOrNull(1)?.details?.title?.toString()

  /** Extracts the icon from the second header display insight. */
  private fun CardInsight.getSubtitleIcon(): Icon? = displayInsights.getOrNull(1)?.details?.icon

  /** Extracts the subtitle suffix from the second header display insight. */
  private fun CardInsight.getSubtitleSuffix(): String? =
    displayInsights.getOrNull(1)?.details?.subtitle?.toString()

  /** Extracts the content description from the second header display insight. */
  private fun CardInsight.getSubtitleContentDescription(): String? =
    displayInsights.getOrNull(1)?.details?.contentDescription?.toString()

  /** Extracts the tertiary text from the third header display insight. */
  private fun CardInsight.getTertiaryText(): String? =
    displayInsights.getOrNull(2)?.details?.subtitle?.toString()

  /** Extracts icons from body display insights to form the image collage list. */
  private fun CardInsight.getImages(): List<Icon> {
    val bodyInsights = body as? InsightCollection
    val imageInsights = bodyInsights?.insights?.filterIsInstance<DisplayInsight>() ?: emptyList()
    return imageInsights.mapNotNull { it.details.icon }
  }
}
