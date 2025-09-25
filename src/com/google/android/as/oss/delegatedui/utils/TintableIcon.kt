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

package com.google.android.`as`.oss.delegatedui.utils

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap

/** Represents an icon that can be tinted. */
data class TintableIcon(val bitmap: Bitmap, val tintable: Boolean)

/** Converts a Bitmap to a TintableIcon. */
fun Bitmap.asTintableIcon(tintable: Boolean): TintableIcon = TintableIcon(this, tintable)

/**
 * A composable of either an [Icon] or [Image], depending on whether the [TintableIcon] is tintable
 * or not.
 */
@Composable
fun IconOrImage(
  icon: TintableIcon,
  modifier: Modifier = Modifier,
  tint: Color = LocalContentColor.current,
  contentDescription: String? = null,
) {
  if (icon.tintable) {
    Icon(
      bitmap = icon.bitmap.asImageBitmap(),
      tint = tint,
      contentDescription = contentDescription,
      modifier = modifier,
    )
  } else {
    Image(
      bitmap = icon.bitmap.asImageBitmap(),
      contentDescription = contentDescription,
      modifier = modifier,
    )
  }
}
