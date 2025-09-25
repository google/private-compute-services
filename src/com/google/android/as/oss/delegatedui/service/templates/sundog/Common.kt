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

package com.google.android.`as`.oss.delegatedui.service.templates.sundog

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.Common.CARD_SHAPE

internal object Common {
  val CARD_SHAPE = RoundedCornerShape(100.dp)
  const val FIXED_VELOCITY = 8f
}

@Composable
internal fun PrimaryRoundedIcon(
  iconBackgroundWidth: Dp = 56.dp,
  iconWidth: Dp = 24.dp,
  iconColor: Color,
  iconBackgroundColor: Color,
  imageBitmap: Bitmap?,
) {
  if (imageBitmap == null) return
  Box(
    modifier = Modifier.width(iconBackgroundWidth).fillMaxHeight(),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(
      modifier = Modifier.size(iconBackgroundWidth),
      onDraw = { drawCircle(color = iconBackgroundColor) },
    )
    Icon(
      imageBitmap.asImageBitmap(),
      contentDescription = null,
      modifier = Modifier.size(iconWidth),
      tint = iconColor,
    )
  }
}

@Composable
internal fun DeletionRow(dragDirection: Float) =
  Row(
    modifier =
      Modifier.fillMaxSize()
        .zIndex(-1f)
        .clip(CARD_SHAPE)
        .background(color = MaterialTheme.colorScheme.onErrorContainer)
        .padding(horizontal = 16.dp),
    horizontalArrangement = if (dragDirection > 0) Arrangement.Start else Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    DeletionButton()
  }

@Composable
private fun DeletionButton() =
  Icon(
    modifier = Modifier.size(24.dp),
    imageVector = Icons.Outlined.Delete,
    tint = MaterialTheme.colorScheme.onError,
    contentDescription = null,
  )
