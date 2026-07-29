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

package com.android.personalcontext.ace.internal.templates.richcard.grid

import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.grid.InsightGrid
import com.android.personalcontext.ace.client.prototype.metadata.VisualMetadataHint
import com.android.personalcontext.ace.client.prototype.metadata.VisualStyle
import com.android.personalcontext.ace.common.asDisplayableInsight
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.richcard.decoder.CardUiDataDecoder
import javax.inject.Inject
import javax.inject.Singleton

/** Decoder for [GridCardUiData]. */
@Singleton
class GridCardUiDataDecoder @Inject constructor() : CardUiDataDecoder<GridCardUiData>() {

  override fun ContextInsight.toCardContext(): GridCardUiData {
    val cardInsight = this.toPrototypeInsight<CardInsight>() ?: error("Expected CardInsight")
    val action = cardInsight.getCardContextAction()

    val titleInsight = cardInsight.title as? DisplayInsight
    val mainTitle = titleInsight?.details?.title?.toString()
    val mainTitleContentDescription = titleInsight?.details?.contentDescription?.toString()
    val mainSubtitle = titleInsight?.details?.subtitle?.toString()
    val statusDisplayInsight = cardInsight.actions as? DisplayInsight
    val gridInsight = cardInsight.body.toPrototypeInsight<InsightGrid>()

    val gridItems =
      gridInsight?.items?.map { item ->
        val displayableInsight = (item.insight as? DisplayInsight)?.asDisplayableInsight()
        if (displayableInsight != null) {
          GridCardItem.Loaded(
            title = displayableInsight.displayDetails.title?.toString(),
            subtitle = displayableInsight.displayDetails.subtitle?.toString(),
            span = item.span,
          )
        } else {
          GridCardItem.Loading(span = item.span)
        }
      }

    val statusText = statusDisplayInsight?.details?.title?.toString()

    val accessoryData = statusText?.let { text ->
      val visualMetadataHint = statusDisplayInsight?.findPrototypeHint<VisualMetadataHint>()
      val isVariant = visualMetadataHint?.visualStyle == VisualStyle.VARIANT
      AccessoryData(text = text, isVariant = isVariant)
    }

    val titleData =
      if (mainTitle != null || mainSubtitle != null || accessoryData != null) {
        TitleData(
          mainTitle = mainTitle,
          mainTitleContentDescription = mainTitleContentDescription,
          mainSubtitle = mainSubtitle,
          accessory = accessoryData,
        )
      } else {
        null
      }

    return GridCardUiData(title = titleData, gridItems = gridItems, action = action)
  }
}
