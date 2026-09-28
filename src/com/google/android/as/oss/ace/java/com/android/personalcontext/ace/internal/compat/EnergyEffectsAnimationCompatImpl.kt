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

package com.android.personalcontext.ace.internal.compat

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils.GeminiAnimationSpec
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationSpec
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import javax.inject.Inject

/** An implementation of [EnergyEffectsAnimationCompat] that provides energy effects animation. */
class EnergyEffectsAnimationCompatImpl @Inject constructor(private val themeCompat: ThemeCompat) :
  EnergyEffectsAnimationCompat {

  @Composable
  override fun Modifier.applyEnergyEffectsAnimation(
    spec: EnergyEffectsAnimationSpec,
    fallback: @Composable Modifier.() -> Modifier,
  ): Modifier {
    if (spec !is GeminiAnimationSpec) {
      Log.e(TAG, "Unsupported animation spec type")
      return this.fallback()
    }
    val publishedInsight = LocalPublishedContextInsight.current
    // Check if the top-most insight has the animation v2 hint set. This follows the
    // contract that the ThemeHint must be set on the top-most insight.
    val showAnimationV2 = with(themeCompat) { publishedInsight.insight.shouldShowAnimationV2() }

    if (!showAnimationV2) {
      return this.fallback()
    }

    return with(EnergyEffectsAnimationUtils) {
      applyEnergyEffectsAnimation(geminiAnimationSpec = spec)
    }
  }

  override fun getAndStartEffectsDrawable(view: View, spec: EnergyEffectsAnimationSpec): Drawable? {
    if (spec !is GeminiAnimationSpec) {
      Log.e(TAG, "Unsupported animation spec type")
      return null
    }
    return EnergyEffectsAnimationUtils.getAndStartEffectsDrawable(view.context, spec)
  }

  companion object {
    private const val TAG = "EnergyEffectsAnimCompat"
  }
}
