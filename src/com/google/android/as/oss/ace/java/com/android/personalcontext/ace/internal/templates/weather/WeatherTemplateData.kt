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

package com.android.personalcontext.ace.internal.templates.weather

import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsight
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint.SuggestionType
import com.android.personalcontext.ace.client.prototype.weather.WeatherInsight
import com.android.personalcontext.ace.client.prototype.weather.WeatherInsight.ChipContent
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat

/** A semantic data structure for the Weather CUJs. */
data class WeatherTemplateData(
  val suggestionType: SuggestionType,
  val chipContents: List<ChipContent>,
  val shouldShowBrandedIcon: Boolean,
) {

  companion object {

    /** Convert from hints and insights into [WeatherTemplateData]. */
    fun ContextInsight.toWeatherTemplateData(
      hint: WeatherHint,
      themeCompat: ThemeCompat,
    ): WeatherTemplateData {
      val insight =
        findWeatherInsight()
          ?: error("Expected a top-level WeatherInsight, actual: ${this.javaClass.simpleName}")

      return WeatherTemplateData(
        suggestionType = hint.suggestionType,
        chipContents = insight.chipContents,
        shouldShowBrandedIcon = with(themeCompat) { shouldShowBrandedIcon() },
      )
    }

    private fun ContextInsight.findWeatherInsight(): WeatherInsight? =
      toPrototypeInsight<WeatherInsight>()
  }
}
