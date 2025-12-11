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

package com.google.android.`as`.oss.delegatedui.service.templates.motion

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MotionScheme

object ExpressiveMotionUtils {

  /** Returns an expressive Material motion scheme. */
  @ExperimentalMaterial3ExpressiveApi
  fun expressiveMotionScheme(): MotionScheme =
    object : MotionScheme {
      override fun <T> defaultSpatialSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 0.8f, stiffness = 380.0f)
      }

      override fun <T> fastSpatialSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 0.6f, stiffness = 800.0f)
      }

      override fun <T> slowSpatialSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 0.8f, stiffness = 200.0f)
      }

      override fun <T> defaultEffectsSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 1.0f, stiffness = 1600.0f)
      }

      override fun <T> fastEffectsSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 1.0f, stiffness = 3800.0f)
      }

      override fun <T> slowEffectsSpec(): FiniteAnimationSpec<T> {
        return spring(dampingRatio = 1.0f, stiffness = 800.0f)
      }
    }
}
