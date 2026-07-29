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

package com.android.personalcontext.ace.visualizer.compat

import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils.GeminiAnimationSpec

interface EnergyEffectsAnimationCompat {
  /**
   * Modifier that applies the energy effects animation to the composable if enabled, otherwise
   * applies the [fallback] modifier.
   *
   * @param geminiAnimationSpec The spec of the animation.
   * @param fallback A modifier to apply if the energy effects animation is not enabled.
   */
  @Composable
  fun Modifier.applyEnergyEffectsAnimation(
    geminiAnimationSpec: GeminiAnimationSpec,
    fallback: @Composable Modifier.() -> Modifier,
  ): Modifier = this

  /**
   * Modifier that applies the energy effects animation to the composable if enabled, otherwise
   * returns the original modifier.
   *
   * @param geminiAnimationSpec The spec of the animation.
   */
  @Composable
  fun Modifier.applyEnergyEffectsAnimation(geminiAnimationSpec: GeminiAnimationSpec): Modifier =
    applyEnergyEffectsAnimation(geminiAnimationSpec = geminiAnimationSpec, fallback = { this })

  /**
   * Returns a themed [Drawable] that applies the effects animation and starts the animation
   * sequence, or null if not supported.
   *
   * @param view The view to attach the animation to and resolve theme colors/resources.
   * @param geminiAnimationSpec The spec of the animation.
   */
  fun getAndStartEffectsDrawable(view: View, geminiAnimationSpec: GeminiAnimationSpec): Drawable? =
    null
}
