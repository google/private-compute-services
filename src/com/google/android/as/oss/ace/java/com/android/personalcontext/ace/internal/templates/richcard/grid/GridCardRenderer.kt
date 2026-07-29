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

package com.android.personalcontext.ace.internal.templates.richcard.grid

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.client.prototype.grid.InsightGrid
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.common.GoogleSansText
import com.android.personalcontext.ace.internal.templates.richcard.common.LoadingBox
import com.android.personalcontext.ace.internal.templates.richcard.common.cardContextActionClickable
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import com.android.personalcontext.ace.visualizer.templates.ui.common.grid.JustifiedWrapLayout
import javax.inject.Inject

/** Renderer for [GridCardUiData]. */
class GridCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<GridCardUiData> {

  @Composable
  override fun Render(cardUiData: CardUiData<GridCardUiData>, modifier: Modifier) {

    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      val uiData = cardUiData.cardContext
      if (uiData != null) {
        GridCard(modifier = Modifier.fillMaxWidth(), uiData = uiData)
      }
    }
  }

  @Composable
  private fun GridCard(modifier: Modifier, uiData: GridCardUiData) {
    Column(
      modifier =
        modifier
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surfaceContainer)
          .cardContextActionClickable(uiData.action)
          .padding(horizontal = 10.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      // Title section
      uiData.title?.let { title ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            title.mainTitle?.let {
              Text(
                text = it,
                color = MaterialTheme.colorScheme.onSurface,
                style =
                  MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 20.sp,
                    lineHeight = 26.sp,
                    letterSpacing = 0.sp,
                  ),
                modifier =
                  title.mainTitleContentDescription?.let { desc ->
                    Modifier.clearAndSetSemantics { contentDescription = desc }
                  } ?: Modifier,
              )
            }
            title.mainSubtitle?.let {
              Text(
                text = it,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style =
                  MaterialTheme.typography.bodySmall.copy(
                    fontFamily = GoogleSansText,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 0.2.sp,
                  ),
              )
            }
          }

          // Accessory
          title.accessory?.let { accessoryData ->
            val foregroundColor =
              MaterialTheme.colorScheme.onTertiaryContainer.takeIf { accessoryData.isVariant }
                ?: MaterialTheme.colorScheme.onErrorContainer
            val backgroundColor =
              MaterialTheme.colorScheme.tertiaryContainer.takeIf { accessoryData.isVariant }
                ?: MaterialTheme.colorScheme.errorContainer

            Box(
              modifier =
                Modifier.background(backgroundColor, RoundedCornerShape(12.dp))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(
                text = accessoryData.text,
                color = foregroundColor,
                style =
                  MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 12.sp,
                    lineHeight = 26.sp,
                    letterSpacing = 0.sp,
                  ),
              )
            }
          }
        }
      }

      // Grid section
      uiData.gridItems?.let { items ->
        JustifiedWrapLayout(
          items = items,
          totalSpanCapacity = InsightGrid.TOTAL_SPAN_CAPACITY_PHONE,
          spanSelector = { item -> item.span.value },
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) { item ->
          when (item) {
            is GridCardItem.Loading -> {
              LoadingBox(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(8.dp),
              )
            }
            is GridCardItem.Loaded -> {
              Column(
                modifier =
                  Modifier.fillMaxWidth()
                    .background(
                      MaterialTheme.colorScheme.surfaceContainerHigh,
                      RoundedCornerShape(8.dp),
                    )
                    .semantics(mergeDescendants = true) {}
                    .padding(vertical = 4.dp, horizontal = 8.dp)
              ) {
                item.title?.let { titleText ->
                  Text(
                    text = titleText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style =
                      MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 12.sp,
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp,
                      ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                item.subtitle?.let { subtitleText ->
                  Text(
                    text = subtitleText,
                    color = MaterialTheme.colorScheme.onSurface,
                    style =
                      MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.W500,
                        fontSize = 20.sp,
                        lineHeight = 26.sp,
                        letterSpacing = 0.sp,
                      ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
