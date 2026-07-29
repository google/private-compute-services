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

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily

val GoogleSansFlex = FontFamily(Font(DeviceFontFamilyName("google-sans-flex")))
val GoogleSansText = FontFamily(Font(DeviceFontFamilyName("google-sans-text")))

fun Typography.withDefaultFontFamily(fontFamily: FontFamily): Typography {
  return this.copy(
    displayLarge = this.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = this.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = this.displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = this.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = this.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = this.headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = this.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = this.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = this.titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = this.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = this.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = this.bodySmall.copy(fontFamily = fontFamily),
    labelLarge = this.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = this.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = this.labelSmall.copy(fontFamily = fontFamily),
  )
}
