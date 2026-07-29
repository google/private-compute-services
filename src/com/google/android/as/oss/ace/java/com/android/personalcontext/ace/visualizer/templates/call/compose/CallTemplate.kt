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

package com.android.personalcontext.ace.visualizer.templates.call.compose

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.android.personalcontext.ace.visualizer.compat.FlexFontCompat
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerWidget

@Composable
fun CallTemplate(
  widget: CallVisualizerWidget,
  insightGridCompat: InsightGridCompat,
  isFullScreen: Boolean,
  flexFontCompat: FlexFontCompat,
) {
  CallTheme(flexFontCompat = flexFontCompat) {
    val backgrounds =
      if (isFullScreen) {
        CallWidgetBackgrounds(
          widgetBackground = MaterialTheme.colorScheme.surfaceContainer,
          cardBackground = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
      } else {
        CallWidgetBackgrounds(
          widgetBackground = MaterialTheme.colorScheme.surfaceContainerHighest,
          cardBackground = Color.Unspecified,
        )
      }

    CompositionLocalProvider(
      LocalInsightGridCompat provides insightGridCompat,
      LocalCallWidgetBackgrounds provides backgrounds,
    ) {
      if (isFullScreen) {
        FullScreenCallWidget(widget)
      } else {
        CallWidgetContainer(widget)
      }
    }
  }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
fun CallTheme(flexFontCompat: FlexFontCompat, content: @Composable () -> Unit) {
  val darkTheme = isSystemInDarkTheme()
  val context = LocalContext.current
  val colorScheme =
    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

  val dynamicTypography = flexFontCompat.flexFont(typography = Typography(), round = 0f)

  MaterialTheme(colorScheme = colorScheme, typography = dynamicTypography) { content() }
}
