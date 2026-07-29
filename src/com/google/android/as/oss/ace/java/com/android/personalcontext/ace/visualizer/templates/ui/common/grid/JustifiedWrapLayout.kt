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

package com.android.personalcontext.ace.visualizer.templates.ui.common.grid

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** The strategy to use for justifying the items in each row when remaining space exists. */
enum class JustifyStrategy {
  /** The last item in each row will expand to fill the remaining space. */
  LAST_ITEM_ONLY,
  /** All items in each row will expand proportionally to fill the remaining space. */
  PROPORTIONAL,
  /**
   * Items will maintain their exact span width relative to total span capacity, leaving remaining
   * space empty.
   */
  EXACT_SPAN,
}

/**
 * A layout that arranges [items] in rows, wrapping to a new line when the cumulative span of items
 * exceeds [totalSpanCapacity]. Items within each row are justified, expanding to fill the row's
 * width. This is similar to how text is wrapped and justified in a word processor.
 *
 * Each row accommodates items as long as their combined spans do not exceed [totalSpanCapacity]. An
 * item that does not fit in the current row is moved to the next.
 *
 * @param items The list of items to render.
 * @param totalSpanCapacity The maximum span capacity of each row.
 * @param spanSelector A lambda to extract the span value from [T]. This allows the caller to
 *   provide any object type for [items]
 * @param justifyStrategy The strategy to use for justifying the items in each row.
 * @param horizontalArrangement The horizontal arrangement of items in a row.
 * @param verticalArrangement The vertical arrangement of rows.
 * @param renderItem The Composable function to render each item.
 */
@Composable
fun <T> JustifiedWrapLayout(
  items: List<T>,
  totalSpanCapacity: Int,
  spanSelector: (T) -> Int,
  justifyStrategy: JustifyStrategy = JustifyStrategy.LAST_ITEM_ONLY,
  horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(4.dp),
  verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(4.dp),
  renderItem: @Composable (item: T) -> Unit,
) {
  val lines = items.chunkBySpan(totalSpanCapacity, spanSelector)

  Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = verticalArrangement) {
    for (items in lines) {
      LayoutRow(
        items = items,
        totalSpanCapacity = totalSpanCapacity,
        spanSelector = spanSelector,
        justifyStrategy = justifyStrategy,
        horizontalArrangement = horizontalArrangement,
        renderItem = renderItem,
      )
    }
  }
}

/**
 * A row of [items] that are justified according to [justifyStrategy].
 *
 * @param items The list of items to render.
 * @param totalSpanCapacity The maximum span capacity of each row.
 * @param spanSelector A lambda to extract the span value from [T].
 * @param justifyStrategy The strategy to use for justifying the items in the row.
 * @param horizontalArrangement The horizontal arrangement of items in the row.
 * @param renderItem The Composable function to render each item.
 */
@Composable
private fun <T> LayoutRow(
  items: List<T>,
  totalSpanCapacity: Int,
  spanSelector: (T) -> Int,
  justifyStrategy: JustifyStrategy,
  horizontalArrangement: Arrangement.Horizontal,
  renderItem: @Composable (item: T) -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
    horizontalArrangement = horizontalArrangement,
  ) {
    val totalRowSpan = items.sumOf { spanSelector(it) }.coerceAtLeast(0)
    val remainingSpan = totalSpanCapacity - totalRowSpan

    items.forEachIndexed { index, item ->
      val itemSpan = spanSelector(item)
      val itemWeight =
        if (justifyStrategy == JustifyStrategy.LAST_ITEM_ONLY && index == items.lastIndex) {
          // Last item in the row expands to fill the remaining space
          itemSpan + remainingSpan.coerceAtLeast(0)
        } else {
          itemSpan
        }

      Column(modifier = Modifier.weight(itemWeight.toFloat()).fillMaxHeight()) { renderItem(item) }
    }

    if (justifyStrategy == JustifyStrategy.EXACT_SPAN && remainingSpan > 0) {
      Spacer(modifier = Modifier.weight(remainingSpan.toFloat()))
    }
  }
}

/**
 * Chunks a list of [T] into nested lists (rows) based on [maximumSpanCapacity], replicating the
 * behavior of greedy line wrapping.
 *
 * Each resulting sub-list will contain items whose combined spans do not exceed
 * [maximumSpanCapacity].
 *
 * For example, given [maximumSpanCapacity] = 6, if the items have spans of [2, 3, 5, 1], they will
 * be chunked into two rows: [[2, 3], [5, 1]].
 *
 * Note: This manual chunking approach was chosen over Compose's native [FlowRow] because [FlowRow]
 * combined with `fillMaxRowHeight()` forces all rows to match the tallest overall item's height in
 * certain text-wrapping scenarios, rather than sizing each row independently.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
fun <T> List<T>.chunkBySpan(maximumSpanCapacity: Int, spanSelector: (T) -> Int): List<List<T>> {
  val gridItems = this@chunkBySpan

  val lines = mutableListOf<List<T>>()
  var currentRow = mutableListOf<T>()
  var currentSpan = 0

  for (item in gridItems) {
    val itemSpan = spanSelector(item)
    if (currentSpan + itemSpan > maximumSpanCapacity) {
      // Finish the current row
      if (currentRow.isNotEmpty()) {
        lines.add(currentRow.toList())
      }
      // Start a new row
      currentRow = mutableListOf(item)
      currentSpan = itemSpan
    } else {
      // Add to the current row
      currentRow.add(item)
      currentSpan += itemSpan
    }
  }
  // Add the last row
  if (currentRow.isNotEmpty()) {
    lines.add(currentRow.toList())
  }
  return lines.toList()
}
