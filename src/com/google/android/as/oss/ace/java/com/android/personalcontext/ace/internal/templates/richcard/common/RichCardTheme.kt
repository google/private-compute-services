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

package com.android.personalcontext.ace.internal.templates.richcard.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/** The theme for RichCard template. */
@Composable
fun RichCardTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val colorScheme =
    if (isSystemInDarkTheme()) {
      dynamicDarkColorScheme(context)
    } else {
      dynamicLightColorScheme(context)
    }

  val cardTypography = MaterialTheme.typography.withDefaultFontFamily(GoogleSansFlex)
  MaterialTheme(colorScheme = colorScheme, typography = cardTypography, content = content)
}
