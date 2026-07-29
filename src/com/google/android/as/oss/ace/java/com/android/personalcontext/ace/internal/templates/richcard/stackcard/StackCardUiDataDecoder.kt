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

package com.android.personalcontext.ace.internal.templates.richcard.stackcard

import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.metadata.VisualMetadataHint
import com.android.personalcontext.ace.client.prototype.metadata.VisualStyle
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.richcard.decoder.CardUiDataDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StackCardUiDataDecoder @Inject constructor() : CardUiDataDecoder<StackCardUiData>() {

  override fun ContextInsight.toCardContext(): StackCardUiData {
    return this.toPrototypeInsight<CardInsight>()?.let { cardInsight ->
      val headerInsights = cardInsight.header.toDisplayInsights()
      val bodyInsights = cardInsight.body.toDisplayInsights()

      val headerInsight = headerInsights.firstOrNull()
      val headerData = headerInsight?.let {
        it.details.title?.toString()?.let { title ->
          HeaderData(
            title = title,
            subtitle = it.details.subtitle?.toString(),
            contentDescription = it.details.contentDescription?.toString(),
          )
        }
      }

      val items = bodyInsights.mapNotNull { insight ->
        insight.details.title?.toString()?.let { title ->
          val metadata = insight.findPrototypeHint<VisualMetadataHint>()
          val style =
            if (metadata?.visualStyle == VisualStyle.VARIANT) {
              Style.VARIANT
            } else {
              Style.STANDARD
            }

          StackItem(
            title = title,
            subtitle = insight.details.subtitle?.toString()?.takeIf { it.isNotEmpty() },
            style = style,
          )
        }
      }

      val action = cardInsight.getCardContextAction()

      StackCardUiData(header = headerData, items = items, action = action)
    } ?: throw IllegalArgumentException("Failed to convert ContextInsight to StackCardUiData")
  }

  private fun ContextInsight?.toDisplayInsights(): List<DisplayInsight> =
    when (this) {
      is DisplayInsight -> listOf(this)
      is InsightCollection -> this.insights.filterIsInstance<DisplayInsight>()
      else -> emptyList()
    }

  private fun ContextInsight?.toActionableInsights(): List<ActionableInsight> =
    when (this) {
      is ActionableInsight -> listOf(this)
      is InsightCollection -> this.insights.filterIsInstance<ActionableInsight>()
      else -> emptyList()
    }
}
