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

package com.android.personalcontext.ace.visualizer.templates.call.compose

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerWidget
import kotlin.collections.sumOf

private const val TAG = "FullScreenCallWidget"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
const val FULL_SCREEN_CALL_WIDGET_TEST_TAG = "full_screen_call_widget"

/** The container for the Magic Cue Call widget. */
@SuppressLint("FlaggedApi", "NewApi")
@Composable
internal fun FullScreenCallWidget(widget: CallVisualizerWidget) {
  val info = LocalInsightSurfaceClientInfo.current
  var isDisplayingMoreResults by remember { mutableStateOf(widget.ctaDisplayMoreResults == null) }

  val detailedCards = widget.detailedCards
  val generalCards = widget.generalCards
  val widgetBackground = LocalCallWidgetBackgrounds.current.widgetBackground

  val lazyListState = rememberLazyListState()

  LazyColumn(
    state = lazyListState,
    modifier =
      Modifier.testTag(FULL_SCREEN_CALL_WIDGET_TEST_TAG)
        .thenIfNotNull(widgetBackground.takeIf { it != Color.Unspecified }) {
          Modifier.background(it)
        }
        .scrollbar(
          state = lazyListState,
          barTint = MaterialTheme.colorScheme.outlineVariant,
          backgroundColor = Color.Transparent,
        )
        .padding(start = 24.dp, end = 24.dp),
  ) {
    itemsIndexed(detailedCards, key = { _, card -> card.listUuid }) { index, detailedCard ->
      val isFirstCard = index == 0
      if (!isFirstCard) {
        Spacer(modifier = Modifier.height(8.dp))
      }
      CallDetailedCardContainer(card = detailedCard, useCardContainer = true)
    }

    if (
      isDisplayingMoreResults && detailedCards.isNotEmpty() && generalCards.any { it.isNotEmpty() }
    ) {
      // Add spacing between Detailed Cards and General Cards. This can only happen if both lists
      // have cards and user has clicked on "Show more results" button.
      item { Spacer(modifier = Modifier.height(8.dp)) }
    }

    CallGeneralCardsContainer(
      cards = generalCards,
      numGeneralCardsPerSource = Int.MAX_VALUE,
      isLoneGeneralCard = detailedCards.isEmpty() && generalCards.size == 1,
      useCardContainer = true,
      horizontalPadding = 16.dp,
    )

    widget.aiDisclaimer?.let { aiDisclaimer ->
      item {
        Spacer(modifier = Modifier.height(8.dp))
        AiDisclaimer(aiDisclaimer)
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

/**
 * Renders a smooth, animated scrollbar on a [LazyColumn].
 *
 * Uses a weighted average of visible item heights to estimate the total content size. This prevents
 * the "jumping" behavior seen in LazyColumns with dynamic item heights (e.g., expandable cards).
 *
 * @param state The [LazyListState] used to calculate scroll progress and item positions.
 * @param barTint The color of the scrollbar thumb.
 * @param backgroundColor The color of the scrollbar track.
 */
@Composable
private fun Modifier.scrollbar(
  state: LazyListState,
  barTint: Color,
  backgroundColor: Color,
): Modifier {
  val density = LocalDensity.current
  val layoutInfo = state.layoutInfo
  val visibleItems = layoutInfo.visibleItemsInfo

  // Determine if the content actually exceeds the viewport bounds.
  val canScroll = state.canScrollForward || state.canScrollBackward

  val targetThumbHeight: Float
  val targetThumbPosition: Float

  if (!canScroll || visibleItems.isEmpty()) {
    targetThumbHeight = 0f
    targetThumbPosition = 0f
  } else {
    val totalItemsCount = layoutInfo.totalItemsCount

    // 1. Estimation Logic:
    // We sum the actual pixel heights of visible items to find an average.
    // Using an average allows us to estimate the "virtual" total height of the list
    // even though LazyColumn hasn't measured items off-screen.
    val visibleItemsHeightSum = visibleItems.sumOf { it.size }
    val avgItemSize = visibleItemsHeightSum.toFloat() / visibleItems.size
    val estimatedTotalHeight = avgItemSize * totalItemsCount
    val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset

    // 2. Thumb Height Calculation:
    // The thumb represents the ratio of the viewport to the total content size.
    val minThumbHeightPx = with(density) { 32.dp.toPx() }
    targetThumbHeight =
      ((viewportHeight.toFloat() / estimatedTotalHeight) * viewportHeight).coerceAtLeast(
        minThumbHeightPx
      )

    // 3. Scroll Progress:
    // We calculate a virtual pixel offset using (index * average) + internal item offset.
    // This creates a stable coordinate system that adapts as items change size.
    val firstItem = visibleItems.first()
    val scrollOffset = (firstItem.index * avgItemSize) + state.firstVisibleItemScrollOffset
    val maxScrollOffset = estimatedTotalHeight - viewportHeight
    val scrollProgress =
      if (maxScrollOffset > 0) (scrollOffset / maxScrollOffset).coerceIn(0f, 1f) else 0f

    // Final target position within the track.
    targetThumbPosition = scrollProgress * (viewportHeight - targetThumbHeight)
  }

  // 4. Smooth Animations:
  // We animate position and height changes to hide "estimation noise" when items
  // snap to different sizes. Alpha is animated to fade the bar in/out cleanly.
  val scrollbarAlpha by
    animateFloatAsState(
      targetValue = if (canScroll) 1f else 0f,
      animationSpec = tween(durationMillis = 200),
      label = "scrollbarAlpha",
    )

  val animatedThumbHeight by
    animateFloatAsState(
      targetValue = targetThumbHeight,
      animationSpec = tween(durationMillis = 100),
      label = "scrollbarHeight",
    )

  val animatedThumbPosition by
    animateFloatAsState(
      targetValue = targetThumbPosition,
      animationSpec = tween(durationMillis = 100),
      label = "scrollbarPosition",
    )

  return this.drawWithContent {
    drawContent()

    // Optimization: Skip drawing calls if the scrollbar is fully transparent.
    if (scrollbarAlpha <= 0f) return@drawWithContent

    val barWidth = 4.dp.toPx()
    val barPadding = 8.dp.toPx()

    // Draw Track
    drawRoundRect(
      color = backgroundColor,
      topLeft = Offset(x = size.width - barWidth - barPadding, y = 0f),
      size = Size(barWidth, size.height),
      cornerRadius = CornerRadius(barWidth / 2),
      alpha = 0.3f * scrollbarAlpha,
    )

    // Draw Thumb
    drawRoundRect(
      color = barTint,
      topLeft = Offset(x = size.width - barWidth - barPadding, y = animatedThumbPosition),
      size = Size(barWidth, animatedThumbHeight),
      cornerRadius = CornerRadius(barWidth / 2),
      alpha = 0.7f * scrollbarAlpha,
    )
  }
}

private fun <T> Modifier.thenIfNotNull(value: T?, modify: (T) -> Modifier): Modifier =
  if (value != null) {
    this.then(modify(value))
  } else {
    this
  }
