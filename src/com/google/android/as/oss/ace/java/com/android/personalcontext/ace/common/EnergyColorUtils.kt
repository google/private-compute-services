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

package com.android.personalcontext.ace.common

import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.systemui.graphics.energycolorslib.EnergyColors

object EnergyColorUtils {
  /** Returns energy colors derived from the dynamic surface container color resource. */
  @RequiresApi(34)
  @Composable
  fun getEnergyColors(): Array<Color> {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    return remember(isDark, context) {
      val resId =
        if (isDark) {
          android.R.color.system_surface_container_dark
        } else {
          android.R.color.system_surface_container_light
        }
      val colors = EnergyColors.from(resId, context)
      arrayOf(Color(colors[0]), Color(colors[1]))
    }
  }
}
