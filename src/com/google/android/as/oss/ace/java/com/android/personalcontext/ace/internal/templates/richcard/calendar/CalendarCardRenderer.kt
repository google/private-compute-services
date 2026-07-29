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

package com.android.personalcontext.ace.internal.templates.richcard.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.internal.templates.richcard.Attribution
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardAppContextBlock
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import javax.inject.Inject

/** [CardRenderer] for Calendar cards. */
class CalendarCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<DeprecatedUiCalendarCardContext> {

  @Composable
  override fun Render(cardUiData: CardUiData<DeprecatedUiCalendarCardContext>, modifier: Modifier) {
    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      val attribution = cardUiData.attribution
      val cardContext = cardUiData.cardContext

      if (attribution?.isValid == true && cardContext != null) {
        CardAppContextBlock(attribution) {
          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            when (cardContext) {
              is DeprecatedUiCalendarCardContext.Entries -> {
                for (entry in cardContext.items) {
                  when (entry) {
                    is DeprecatedUiCalendarCardContext.CalendarItem.Event -> EventItem(entry)
                    is DeprecatedUiCalendarCardContext.CalendarItem.FreeSlot -> FreeSlotItem(entry)
                  }
                }
              }
              is DeprecatedUiCalendarCardContext.NoEvents -> NoEventsItem(cardContext)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EventItem(entry: DeprecatedUiCalendarCardContext.CalendarItem.Event) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .background(MaterialTheme.colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalAlignment = Alignment.Top,
  ) {
    Column {
      Text(
        text = entry.title,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.W500,
        color = MaterialTheme.colorScheme.onSurface,
      )
      val subtitle = entry.subtitle
      if (subtitle != null) {
        Text(
          text = subtitle,
          fontSize = 14.sp,
          lineHeight = 16.sp,
          fontWeight = FontWeight.W400,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun FreeSlotItem(entry: DeprecatedUiCalendarCardContext.CalendarItem.FreeSlot) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .height(50.dp)
        .background(MaterialTheme.colorScheme.surfaceDim, RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    Text(
      text = entry.text,
      fontSize = 14.sp,
      lineHeight = 16.sp,
      fontWeight = FontWeight.W500,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun NoEventsItem(content: DeprecatedUiCalendarCardContext.NoEvents) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .height(50.dp)
        .background(MaterialTheme.colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    contentAlignment = Alignment.CenterStart,
  ) {
    Text(
      text = content.text,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private val Attribution.isValid: Boolean
  get() = sourceAppIcons.isNotEmpty() && title.isNotEmpty()
