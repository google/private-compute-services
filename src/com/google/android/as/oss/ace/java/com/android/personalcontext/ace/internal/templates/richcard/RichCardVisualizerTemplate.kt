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

package com.android.personalcontext.ace.internal.templates.richcard

import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.richcard.RichCardErrorHint
import com.android.personalcontext.ace.client.prototype.richcard.RichCardHint
import com.android.personalcontext.ace.client.prototype.richcard.RichCardLiveDataHint
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.richcard.common.RichCardTheme
import com.android.personalcontext.ace.internal.templates.richcard.common.rememberInsightEventReporter
import com.android.personalcontext.ace.internal.templates.richcard.decoder.CardDecoderManager
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRendererManager
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import javax.inject.Inject

/** A [VisualizerTemplate] that renders RichCards. */
class RichCardVisualizerTemplate
@Inject
internal constructor(
  private val cardDecoderManager: CardDecoderManager,
  private val cardRendererManager: CardRendererManager,
) : VisualizerTemplate {

  override fun handleInsight(
    publishedInsight: IPublishedContextInsight
  ): (@Composable () -> Unit)? {
    Log.d(TAG, "Handling insight...")
    val insight = publishedInsight.insight

    val richCardHint = insight.findPrototypeHint<RichCardHint>()
    val richCardLiveDataHint = insight.findPrototypeHint<RichCardLiveDataHint>()
    val richCardErrorHint = insight.findPrototypeHint<RichCardErrorHint>()
    if (richCardHint == null && richCardLiveDataHint == null && richCardErrorHint == null) {
      Log.d(
        TAG,
        "No RichCardHint, RichCardLiveDataHint or RichCardErrorHint found, ignoring insight.",
      )
      return null
    }

    if (insight !is InsightCollection) {
      error("Insight must be an InsightCollection, but was ${insight::class.java.simpleName}")
    }

    val cardInsights = insight.insights.mapNotNull { it.toPrototypeInsight<CardInsight>() }
    val innerContent: @Composable () -> Unit = {
      if (cardInsights.isNotEmpty()) {
        RichCardContent(
          cardUiDatas = cardInsights.map { cardDecoderManager.fromInsight(it) },
          cardInsight = cardInsights.first(),
        )
      } else {
        val displayInsight =
          insight.insights.filterIsInstance<DisplayInsight>().firstOrNull()
            ?: error(
              "RichCard visualizer hint found, but no valid CardInsights or DisplayInsights in collection"
            )
        DisplayInsightSnackbar(displayInsight)
      }
    }

    return { RichCardTheme { innerContent() } }
  }

  // Renders the rich card content as a single card
  @Composable
  private fun RichCardContent(
    cardUiDatas: List<CardUiData<DeprecatedUiCardContext>>,
    cardInsight: CardInsight,
  ) {
    val reportEvent = rememberInsightEventReporter()

    DisposableEffect(Unit) {
      onDispose {
        Log.d(TAG, "Card disposed, reporting EVENT_USER_DISMISS")
        reportEvent(cardInsight.toContextInsight(), InsightEvent.EVENT_USER_DISMISS)
      }
    }

    with(cardRendererManager) {
      Render(cardUiData = cardUiDatas[0], modifier = Modifier.fillMaxWidth())
    }
  }

  // TODO: shuqianz - add feedback support if needed.
  @Composable
  private fun DisplayInsightSnackbar(displayInsight: DisplayInsight) {
    Snackbar(
      modifier = Modifier.padding(12.dp),
      containerColor = MaterialTheme.colorScheme.inverseSurface,
      contentColor = MaterialTheme.colorScheme.inverseOnSurface,
      shape = MaterialTheme.shapes.extraLarge,
    ) {
      Text(
        text = displayInsight.details.title.toString(),
        fontSize = 14.sp,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
      )
    }
  }

  companion object {
    const val TAG = "RichCardVisualizerTemplate"
  }
}
