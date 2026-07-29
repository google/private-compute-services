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

package com.android.personalcontext.ace.internal.templates.richcard.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.internal.flexfont.FlexFontUtils.withFlexFont
import com.android.personalcontext.ace.internal.templates.richcard.CardTitle

/** A composable that renders the card title. */
@Suppress("NewApi", "FlaggedApi")
@Composable
fun CardTitle(cardTitle: CardTitle?, modifier: Modifier = Modifier) {
  if (cardTitle == null) return
  when (cardTitle) {
    is CardTitle.Loading ->
      LoadingBox(
        modifier = modifier.fillMaxWidth().height(18.dp),
        shape = RoundedCornerShape(50.dp),
      )
    is CardTitle.Present ->
      Text(
        text = cardTitle.text,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        style = MaterialTheme.typography.titleMedium.withFlexFont(weight = 600, round = 100f),
        modifier = modifier,
      )
  }
}
