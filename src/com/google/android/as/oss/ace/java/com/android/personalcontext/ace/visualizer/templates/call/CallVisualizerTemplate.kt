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

package com.android.personalcontext.ace.visualizer.templates.call

import android.service.personalcontext.hint.CallHint
import android.util.Log
import androidx.compose.runtime.Composable
import com.android.personalcontext.ace.common.FindHintUtils.findContextHint
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.visualizer.compat.FlexFontCompat
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat
import com.android.personalcontext.ace.visualizer.compat.VisualMetadataCompat
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import com.android.personalcontext.ace.visualizer.templates.call.compose.CallTemplate
import com.android.personalcontext.ace.visualizer.templates.call.data.CallInsightConverter
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerWidget
import javax.inject.Inject

/** A [VisualizerTemplate] that renders the Magic Cue Call UI. */
class CallVisualizerTemplate
@Inject
internal constructor(
  private val callInsightConverter: CallInsightConverter,
  private val insightGridCompat: InsightGridCompat,
  private val visualMetadataCompat: VisualMetadataCompat,
  private val flexFontCompat: FlexFontCompat,
) : VisualizerTemplate {

  override fun handleInsight(
    publishedInsight: IPublishedContextInsight
  ): (@Composable () -> Unit)? {
    Log.i(TAG, "[CallEmbedded] handleInsight init")
    val insight = publishedInsight.insight
    if (insight.findContextHint<CallHint>() == null) {
      Log.v(TAG, "[CallEmbedded] No CallHint found")
      return null
    }
    Log.i(TAG, "[CallEmbedded] CallHint found, converting to widget")
    val widget: CallVisualizerWidget = callInsightConverter.convert(insight)

    val isFullScreen = with(visualMetadataCompat) { insight.isVariant() }

    Log.i(TAG, "[CallEmbedded] Returning CallTemplate")
    return {
      CallTemplate(
        widget = widget,
        insightGridCompat = insightGridCompat,
        isFullScreen = isFullScreen,
        flexFontCompat = flexFontCompat,
      )
    }
  }

  companion object {
    private const val TAG = "CallVisualizerTemplate"
  }
}
