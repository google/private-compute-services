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

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat

val LocalInsightGridCompat =
  compositionLocalOf<InsightGridCompat> {
    error("[CallEmbedded] #LocalInsightGridCompat InsightGridCompat not provided")
  }

data class CallWidgetBackgrounds(
  val widgetBackground: Color = Color.Unspecified,
  val cardBackground: Color = Color.Unspecified,
)

val LocalCallWidgetBackgrounds = compositionLocalOf { CallWidgetBackgrounds() }
