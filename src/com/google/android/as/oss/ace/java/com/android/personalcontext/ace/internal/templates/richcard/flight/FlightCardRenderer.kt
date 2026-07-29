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

package com.android.personalcontext.ace.internal.templates.richcard.flight

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.personalcontext.ace.internal.templates.richcard.Attribution
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardAppContextBlock
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.common.LoadingBox
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import javax.inject.Inject

/** [CardRenderer] for Flight cards. */
class FlightCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<DeprecatedUiFlightCardContext> {

  @Composable
  override fun Render(cardUiData: CardUiData<DeprecatedUiFlightCardContext>, modifier: Modifier) {
    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      val attribution = cardUiData.attribution
      val cardContext = cardUiData.cardContext

      if (attribution?.isValid == true && cardContext != null) {
        CardAppContextBlock(attribution) {
          FlightCard(modifier = Modifier.fillMaxWidth(), cardContext = cardContext)
        }
      }
    }
  }
}

@Composable
private fun FlightCard(modifier: Modifier, cardContext: DeprecatedUiFlightCardContext) {
  Column(
    modifier =
      modifier
        .background(MaterialTheme.colorScheme.surfaceBright, RoundedCornerShape(4.dp))
        .padding(horizontal = 10.dp, vertical = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    val status = cardContext.flightStatus

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val originField = cardContext.originAirportCode
        val destField = cardContext.destinationAirportCode
        val dateRangeField = cardContext.flightDatetimeRange

        val titleText =
          if (originField != null && destField != null) {
            "${originField.text} \u2192 ${destField.text}"
          } else {
            dateRangeField?.text ?: cardContext.departureTimeMs.toString()
          }
        Text(
          text = titleText,
          color = MaterialTheme.colorScheme.onSurface,
          style = MaterialTheme.typography.titleLarge,
        )

        if (originField != null && destField != null) {
          if (dateRangeField != null) {
            Text(
              text = dateRangeField.text,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              style = MaterialTheme.typography.bodySmall,
            )
          } else {
            LoadingBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
          }
        }
      }

      if (status != null) {
        Box(
          modifier =
            Modifier.background(
                MaterialTheme.colorScheme.surfaceContainerHigh,
                RoundedCornerShape(12.dp),
              )
              .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
          Text(
            text = status.text,
            color = MaterialTheme.colorScheme.onSurface,
            style = TextStyle(fontWeight = FontWeight.W500, fontSize = 12.sp, lineHeight = 26.sp),
          )
        }
      }
    }

    val gridItems = mutableListOf<GridData>()

    // Tile 1: Airline + Flight Number
    val airlineText = cardContext.airline?.text
    gridItems.add(GridData(title = airlineText, subtitle = cardContext.flightNumber))

    // Tile 2: Arrival Time
    if (cardContext.arrivalTime != null) {
      gridItems.add(
        GridData(title = cardContext.arrivalTime.title, subtitle = cardContext.arrivalTime.text)
      )
    } else {
      gridItems.add(GridData(title = null, subtitle = null))
    }

    // Tile 3: Departure Gate
    if (cardContext.departureGate != null) {
      gridItems.add(
        GridData(title = cardContext.departureGate.title, subtitle = cardContext.departureGate.text)
      )
    }

    if (gridItems.isNotEmpty()) {
      FlightCardGrid(modifier = Modifier.padding(top = 8.dp), items = gridItems) { item ->
        if (item.isLoading) {
          LoadingBox(
            modifier = Modifier.size(width = 60.dp, height = 40.dp),
            shape = RoundedCornerShape(8.dp),
          )
        } else {
          Column(
            modifier =
              Modifier.background(
                  MaterialTheme.colorScheme.surfaceContainerHigh,
                  RoundedCornerShape(8.dp),
                )
                .padding(vertical = 4.dp, horizontal = 8.dp)
          ) {
            item.title?.let { titleText ->
              Text(
                text = titleText,
                color = MaterialTheme.colorScheme.onSurface,
                style =
                  TextStyle(fontWeight = FontWeight.W500, fontSize = 12.sp, lineHeight = 26.sp),
                maxLines = 1,
              )
            }
            Text(
              text = item.subtitle ?: "",
              color = MaterialTheme.colorScheme.onSurface,
              style = TextStyle(fontWeight = FontWeight.W500, fontSize = 20.sp, lineHeight = 26.sp),
              maxLines = 1,
            )
          }
        }
      }
    }
  }
}

private data class GridData(val title: String?, val subtitle: String?) {
  val isLoading: Boolean
    get() = title == null && subtitle == null
}

@Composable
private fun FlightCardGrid(
  modifier: Modifier = Modifier,
  items: List<GridData>,
  content: @Composable (GridData) -> Unit,
) {
  Layout(content = { for (item in items) content(item) }, modifier = modifier) {
    measurables,
    constraints ->
    val itemSpacing = 4.dp.roundToPx()
    val rowSpacing = 4.dp.roundToPx()

    val rows = mutableListOf<List<Pair<Measurable, Int>>>()
    var currentRow = mutableListOf<Pair<Measurable, Int>>()
    var currentRowWidth = 0

    for (measurable in measurables) {
      val intrinsicWidth = measurable.maxIntrinsicWidth(constraints.maxHeight)
      val intrinsicHeight = measurable.maxIntrinsicHeight(intrinsicWidth)
      val augmentedWidth = maxOf(intrinsicWidth, intrinsicHeight)

      val neededWidth = augmentedWidth + if (currentRow.isEmpty()) 0 else itemSpacing

      if (currentRowWidth + neededWidth > constraints.maxWidth) {
        if (currentRow.isNotEmpty()) {
          rows.add(currentRow)
          currentRow = mutableListOf()
          currentRowWidth = 0
        }
        currentRow.add(measurable to augmentedWidth)
        currentRowWidth = augmentedWidth
      } else {
        currentRow.add(measurable to augmentedWidth)
        currentRowWidth += neededWidth
      }
    }
    if (currentRow.isNotEmpty()) {
      rows.add(currentRow)
    }

    val rowsOfPlaceables = mutableListOf<List<Placeable>>()
    var totalHeight = 0

    for (row in rows) {
      val rowUseWidth = row.sumOf { it.second } + (row.size - 1) * itemSpacing
      val remainingWidth = constraints.maxWidth - rowUseWidth

      // Calculate row height using intrinsics to avoid measuring twice
      val rowHeight = row.maxOf { (measurable, augmentedWidth) ->
        val isLastInRow = (measurable to augmentedWidth) == row.last()
        val targetWidth =
          if (isLastInRow && remainingWidth > 0) augmentedWidth + remainingWidth else augmentedWidth
        measurable.maxIntrinsicHeight(targetWidth)
      }

      // Measure with forced row height
      val placeables = mutableListOf<Placeable>()
      for (i in row.indices) {
        val (measurable, augmentedWidth) = row[i]
        val isLastInRow = i == row.indices.last
        val targetWidth =
          if (isLastInRow && remainingWidth > 0) augmentedWidth + remainingWidth else augmentedWidth
        val placeable =
          measurable.measure(
            constraints.copy(
              minWidth = targetWidth,
              maxWidth = targetWidth,
              minHeight = rowHeight,
              maxHeight = rowHeight,
            )
          )
        placeables.add(placeable)
      }

      rowsOfPlaceables.add(placeables)
      totalHeight += rowHeight
    }

    val finalHeight = totalHeight + (rows.size - 1) * rowSpacing

    layout(constraints.maxWidth, finalHeight) {
      var y = 0
      for (placeables in rowsOfPlaceables) {
        var x = 0
        val rowHeight = placeables.maxOf { it.height }
        for (placeable in placeables) {
          placeable.placeRelative(x, y)
          x += placeable.width + itemSpacing
        }
        y += rowHeight + rowSpacing
      }
    }
  }
}

private val Attribution.isValid: Boolean
  get() = sourceAppIcons.isNotEmpty() && title.isNotEmpty()
