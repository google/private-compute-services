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

package com.google.android.`as`.oss.feedback.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A custom layout that arranges three children (header, content, footer) in a vertical column. The
 * [header] and [footer] are pinned to the top and bottom of the layout, while the [content] fills
 * the remaining space.
 *
 * When given unbounded (infinite) height constraints (e.g., inside a `verticalScroll`): It "wraps
 * content" and stacks all three children, each taking up only its intrinsic height. The entire
 * layout shrinks to fit the combined height.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param spacing The vertical spacing to be applied between the header, content, and footer.
 * @param header The composable to be placed at the top of the layout.
 * @param content The main composable, which will either fill the remaining space.
 * @param footer The composable to be placed at the bottom of the layout.
 */
@Composable
fun FillContentColumnLayout(
  modifier: Modifier = Modifier,
  spacing: Dp = 0.dp,
  header: @Composable () -> Unit,
  content: @Composable () -> Unit,
  footer: @Composable () -> Unit,
) {
  Layout(
    modifier = modifier,
    content = {
      header()
      content()
      footer()
    },
  ) { measurables, constraints ->
    check(measurables.size == 3) { "FillContentColumnLayout requires exactly 3 children" }

    val spacingPx = spacing.roundToPx()

    // Measure header and footer to get their intrinsic height.
    val intrinsicHeightConstraints = constraints.copy(minHeight = 0)
    val headerPlaceable = measurables[0].measure(intrinsicHeightConstraints)
    val footerPlaceable = measurables[2].measure(intrinsicHeightConstraints)

    val heightTaken = headerPlaceable.height + footerPlaceable.height + spacingPx * 2

    // Measure content with the remaining space.
    val remainingSpaceConstraints =
      constraints.copy(
        minHeight = (constraints.minHeight - heightTaken).coerceAtLeast(0),
        maxHeight =
          if (constraints.hasBoundedHeight) {
            (constraints.maxHeight - heightTaken).coerceAtLeast(0)
          } else {
            Constraints.Infinity
          },
      )
    val contentPlaceable = measurables[1].measure(remainingSpaceConstraints)

    // The width is the widest of the children, or the incoming max width
    val layoutWidth =
      maxOf(
          headerPlaceable.width,
          contentPlaceable.width,
          footerPlaceable.width,
          constraints.minWidth,
        )
        .coerceAtMost(constraints.maxWidth)

    val totalChildrenHeight =
      headerPlaceable.height + contentPlaceable.height + footerPlaceable.height + (spacingPx * 2)

    val layoutHeight = totalChildrenHeight.coerceIn(constraints.minHeight, constraints.maxHeight)

    layout(layoutWidth, layoutHeight) {
      var yPosition = 0

      headerPlaceable.placeRelative(x = 0, y = yPosition)
      yPosition += headerPlaceable.height + spacingPx

      contentPlaceable.placeRelative(x = 0, y = yPosition)

      // Pin footer to the bottom if the layout is forced to be taller than its contents.
      yPosition = layoutHeight - footerPlaceable.height
      footerPlaceable.placeRelative(x = 0, y = yPosition)
    }
  }
}
