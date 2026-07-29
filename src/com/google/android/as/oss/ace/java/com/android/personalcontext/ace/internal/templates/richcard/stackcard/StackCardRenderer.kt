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

package com.android.personalcontext.ace.internal.templates.richcard.stackcard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.common.cardContextActionClickable
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import javax.inject.Inject

class StackCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<StackCardUiData> {

  @Composable
  override fun Render(cardUiData: CardUiData<StackCardUiData>, modifier: Modifier) {
    val uiContext = cardUiData.cardContext ?: return

    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      Row(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .cardContextActionClickable(uiContext.action)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        // 1. Leading Column (Header)
        uiContext.header?.let { LeadingColumn(it) }

        // 2. Body Column (Items)
        BodyColumn(uiContext.items, modifier = Modifier.weight(1f))
      }
    }
  }

  @Composable
  private fun LeadingColumn(headerData: HeaderData, modifier: Modifier = Modifier) {
    var columnModifier = modifier.width(64.dp)
    if (headerData.contentDescription != null) {
      columnModifier = columnModifier.clearAndSetSemantics {
        contentDescription = headerData.contentDescription
      }
    }
    Column(modifier = columnModifier, verticalArrangement = Arrangement.Top) {
      if (headerData.subtitle != null) {
        Text(
          text = headerData.subtitle,
          style =
            MaterialTheme.typography.labelMedium.copy(
              fontSize = 12.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.W500,
              letterSpacing = 0.sp,
            ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Text(
        text = headerData.title,
        style =
          MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
          ),
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }

  @Composable
  private fun BodyColumn(items: List<StackItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
      for (item in items) {
        when (item.style) {
          Style.VARIANT -> VariantListItem(item)
          Style.STANDARD -> StandardListItem(item)
        }
      }
    }
  }

  @Composable
  private fun StandardListItem(item: StackItem, modifier: Modifier = Modifier) {
    Column(
      modifier =
        modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp))
          .padding(8.dp)
    ) {
      Text(
        text = item.title,
        style =
          MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.sp,
          ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (item.subtitle != null) {
        Text(
          text = item.subtitle,
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.W400,
              letterSpacing = 0.sp,
            ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }

  @Composable
  private fun VariantListItem(item: StackItem, modifier: Modifier = Modifier) {
    Column(
      modifier =
        modifier
          .fillMaxWidth()
          .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), RoundedCornerShape(12.dp))
          .padding(8.dp)
    ) {
      Text(
        text = item.title,
        style =
          MaterialTheme.typography.labelMedium.copy(
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.sp,
          ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (item.subtitle != null) {
        Text(
          text = item.subtitle,
          style =
            MaterialTheme.typography.bodySmall.copy(
              fontSize = 12.sp,
              lineHeight = 16.sp,
              fontWeight = FontWeight.W400,
              letterSpacing = 0.sp,
            ),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}
