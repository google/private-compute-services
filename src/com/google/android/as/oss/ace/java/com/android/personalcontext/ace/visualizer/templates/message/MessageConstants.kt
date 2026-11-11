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

package com.android.personalcontext.ace.visualizer.templates.message

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Constants for the underlay template. */
object MessageConstants {
  val ButtonHorizontalPadding: Dp = 12.dp
  val ButtonVerticalPadding: Dp = 4.dp
  val ButtonEndPadding: Dp = 18.dp
  val CornerRadius: Dp = 100.dp
  val MinHeight: Dp = 52.dp
  val IconSize: Dp = 20.dp
  val BorderStrokeWidth: Dp = 1.dp
  val InnerBorderStrokeWidth: Dp = 8.dp
  val InnerBorderBlurRadius: Dp = 2.dp
  val Spacing: Dp = 8.dp
  val RowStartPadding: Dp = 16.dp
  val RowEndPadding: Dp = 16.dp
  val RowTopPadding: Dp = 20.dp
  val RowBottomPadding: Dp = 12.dp
  val TextLineHeight: Dp = 20.dp
  val BackgroundColor: Color = Color.Black
  val IconColor: Color = Color.White

  const val ROTATION_DURATION_MILLIS: Int = 1500
  const val FADE_DURATION_MILLIS: Int = 500
  const val FADE_DELAY_MILLIS: Int = 1000
  const val INITIAL_ROTATION_DEGREES: Float = 20f // Start rotation at 20 degrees
  const val GRADIENT_START_FRACTION = 0.2f
  const val GRADIENT_MIDDLE_FRACTION = 0.5f
  const val GRADIENT_END_FRACTION = 0.8f
  const val ANIMATION_REVEAL_DURATION_MILLIS = 250
  const val ANIMATION_REVEAL_DELAY_MILLIS = 267
  const val CHIP_BOX_MIN_FADE_HEIGHT_DP = 2f
  const val CHIP_BOX_MAX_FADE_HEIGHT_DP = 16f
  const val CHIP_CONTENT_MIN_FADE_PROGRESS = 0.538f
  const val CHIP_CONTENT_MAX_FADE_PROGRESS = 0.923f

  const val MESSAGE_DATA_IS_STANDALONE_ROW_ENABLED_KEY =
    "MESSAGE_DATA_IS_STANDALONE_ROW_ENABLED_KEY"
}
