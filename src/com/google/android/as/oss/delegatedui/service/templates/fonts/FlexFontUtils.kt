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

package com.google.android.`as`.oss.delegatedui.service.templates.fonts

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation

/** Utility class for the font style. */
object FlexFontUtils {

  /**
   * Modifies the [Typography] to use Google Sans Flex with the given variable format.
   *
   * @param slant the slant of the font.
   * @param width the width of the font.
   * @param grade the grade of the font.
   * @param round the round of the font.
   */
  @OptIn(ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class)
  fun Typography.withFlexFont(
    slant: Float = 0.0f,
    width: Float = 100.0f,
    grade: Int = 0,
    round: Float = 100.0f,
  ): Typography =
    this.copy(
      displayLarge = this.displayLarge.withFlexFont(weight = 400),
      displayMedium = this.displayMedium.withFlexFont(weight = 400),
      displaySmall = this.displaySmall.withFlexFont(weight = 400),
      headlineLarge = this.headlineLarge.withFlexFont(weight = 400),
      headlineMedium = this.headlineMedium.withFlexFont(weight = 400),
      headlineSmall = this.headlineSmall.withFlexFont(weight = 400),
      titleLarge = this.titleLarge.withFlexFont(weight = 400),
      titleMedium = this.titleMedium.withFlexFont(weight = 500),
      titleSmall = this.titleSmall.withFlexFont(weight = 500),
      labelLarge = this.labelLarge.withFlexFont(weight = 500),
      labelMedium = this.labelMedium.withFlexFont(weight = 500),
      labelSmall = this.labelSmall.withFlexFont(weight = 500),
      bodyLarge = this.bodyLarge.withFlexFont(weight = 400),
      bodyMedium = this.bodyMedium.withFlexFont(weight = 400),
      bodySmall = this.bodySmall.withFlexFont(weight = 400),
      displayLargeEmphasized = this.displayLargeEmphasized.withFlexFont(weight = 500),
      displayMediumEmphasized = this.displayMediumEmphasized.withFlexFont(weight = 500),
      displaySmallEmphasized = this.displaySmallEmphasized.withFlexFont(weight = 500),
      headlineLargeEmphasized = this.headlineLargeEmphasized.withFlexFont(weight = 500),
      headlineMediumEmphasized = this.headlineMediumEmphasized.withFlexFont(weight = 500),
      headlineSmallEmphasized = this.headlineSmallEmphasized.withFlexFont(weight = 500),
      titleLargeEmphasized = this.titleLargeEmphasized.withFlexFont(weight = 500),
      titleMediumEmphasized = this.titleMediumEmphasized.withFlexFont(weight = 600),
      titleSmallEmphasized = this.titleSmallEmphasized.withFlexFont(weight = 600),
      labelLargeEmphasized = this.labelLargeEmphasized.withFlexFont(weight = 600),
      labelMediumEmphasized = this.labelMediumEmphasized.withFlexFont(weight = 600),
      labelSmallEmphasized = this.labelSmallEmphasized.withFlexFont(weight = 600),
      bodyLargeEmphasized = this.bodyLargeEmphasized.withFlexFont(weight = 500),
      bodyMediumEmphasized = this.bodyMediumEmphasized.withFlexFont(weight = 500),
      bodySmallEmphasized = this.bodySmallEmphasized.withFlexFont(weight = 500),
    )

  /**
   * Returns the Google Sans Flex text style with the given variable format.
   *
   * @param slant the slant of the font.
   * @param width the width of the font.
   * @param weight the weight of the font.
   * @param grade the grade of the font.
   * @param round the round of the font.
   */
  @OptIn(ExperimentalTextApi::class)
  fun TextStyle.withFlexFont(
    weight: Int,
    slant: Float = 0.0f,
    width: Float = 100.0f,
    grade: Int = 0,
    round: Float = 100.0f,
  ): TextStyle = this.copy(fontFamily = FlexFont(weight, slant, width, grade, round))

  @OptIn(ExperimentalTextApi::class)
  fun FlexFont(
    weight: Int,
    slant: Float = 0.0f,
    width: Float = 100.0f,
    grade: Int = 0,
    round: Float = 100.0f,
  ): FontFamily =
    FontFamily(
      Font(
        R.font.google_sans_flex_pcs,
        variationSettings =
          FontVariation.Settings(
            FontVariation.slant(slant),
            FontVariation.width(width),
            FontVariation.weight(weight),
            FontVariation.grade(grade),
            FontVariation.round(round),
          ),
      )
    )

  private fun FontVariation.round(round: Float): FontVariation.Setting {
    require(round in 0.0f..100.0f) { "'ROND' must be in 0..100" }
    return Setting("ROND", round)
  }
}
