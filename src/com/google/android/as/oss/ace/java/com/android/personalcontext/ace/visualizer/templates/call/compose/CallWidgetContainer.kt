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
import android.graphics.drawable.Icon
import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.call.compose.CallWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerDetailedCard
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerWidget
import com.android.personalcontext.ace.visualizer.templates.call.data.CtaDisplayMoreResults
import com.android.personalcontext.ace.visualizer.templates.utils.IconOrImage
import com.android.personalcontext.ace.visualizer.templates.utils.TintableIcon
import com.android.personalcontext.ace.visualizer.templates.utils.asTintableIcon

private const val TAG = "CallWidgetContainer"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
const val CALL_WIDGET_CONTAINER_TEST_TAG = "call_widget_container"

/** The container for the Magic Cue Call widget. */
@SuppressLint("FlaggedApi", "NewApi")
@Composable
internal fun CallWidgetContainer(widget: CallVisualizerWidget) {
  val info = LocalInsightSurfaceClientInfo.current
  var isDisplayingMoreResults by remember { mutableStateOf(widget.ctaDisplayMoreResults == null) }

  val detailedCards = widget.detailedCards
  val generalCards = widget.generalCards

  val numGeneralCardsPerSource = getNumGeneralCardsPerSource(detailedCards, isDisplayingMoreResults)

  val widgetBackground = LocalCallWidgetBackgrounds.current.widgetBackground

  val lazyListState = rememberLazyListState()

  LazyColumn(
    state = lazyListState,
    modifier =
      Modifier.fillMaxWidth()
        .testTag(CALL_WIDGET_CONTAINER_TEST_TAG)
        .thenIfNotNull(widgetBackground.takeIf { it != Color.Unspecified }) {
          Modifier.background(it)
        }
        .fadeEdge(
          shouldFadeTop = lazyListState.canScrollBackward,
          shouldFadeBottom = lazyListState.canScrollForward,
          color = MaterialTheme.colorScheme.surfaceContainerHighest,
          height = 24.dp,
        ),
  ) {
    // Custom spacing must be used instead of Arrangement.spacedBy because GeneralCards need to have
    // less spacing between each card

    itemsIndexed(detailedCards, key = { _, card -> card.listUuid }) { index, detailedCard ->
      val isFirstCard = index == 0
      if (!isFirstCard) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
      }
      CallDetailedCardContainer(card = detailedCard)
    }

    if (
      isDisplayingMoreResults && detailedCards.isNotEmpty() && generalCards.any { it.isNotEmpty() }
    ) {
      // Add spacing between Detailed Cards and General Cards. This can only happen if both lists
      // have cards and user has clicked on "Show more results" button.
      item {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))
      }
    }

    CallGeneralCardsContainer(
      cards = generalCards,
      numGeneralCardsPerSource = numGeneralCardsPerSource,
      isLoneGeneralCard = detailedCards.isEmpty() && generalCards.size == 1,
    )

    if (widget.ctaDisplayMoreResults != null && !isDisplayingMoreResults) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        WidgetShowAllResultsButton(
          widget.ctaDisplayMoreResults!!,
          onClick = {
            Log.i(TAG, "[CallEmbedded] WidgetShowAllResultsButton clicked")
            isDisplayingMoreResults = true
            info.onReceiveInsight(widget.ctaDisplayMoreResults!!.originalInsight)
          },
        )
      }
    }

    widget.aiDisclaimer?.let { aiDisclaimer ->
      item {
        Spacer(modifier = Modifier.height(8.dp))
        AiDisclaimer(aiDisclaimer)
      }
    }
  }
}

@Composable
private fun WidgetShowAllResultsButton(
  ctaDisplayMoreResults: CtaDisplayMoreResults,
  onClick: () -> Unit,
) {
  val context = LocalContext.current
  val insightEventReporter = LocalInsightEventReporter.current
  val publishedInsight = LocalPublishedContextInsight.current
  val renderToken = LocalRenderToken.current

  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }

  fun reportEvent(event: Int) {
    with(insightEventReporter) {
      personalContextManager?.reportChildInsightEvent(
        publishedInsight,
        ctaDisplayMoreResults.originalInsight,
        event,
        renderToken,
      )
    }
  }

  LaunchedEffect(Unit) { reportEvent(InsightEvent.EVENT_SHOW) }

  OutlinedButton(
    modifier =
      Modifier.fillMaxWidth().thenIfNotNull(ctaDisplayMoreResults.contentDescription) {
        Modifier.clearAndSetSemantics { contentDescription = it }
      },
    onClick = {
      Log.v(TAG, "[CallEmbedded] WidgetShowAllResultsButton clicked")
      reportEvent(InsightEvent.EVENT_USER_TAP)
      onClick()
    },
    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outline),
    colors =
      ButtonDefaults.outlinedButtonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
      ),
    contentPadding = PaddingValues(vertical = 10.dp),
  ) {
    Text(text = ctaDisplayMoreResults.title, style = MaterialTheme.typography.labelLarge)
  }
}

/** Icon for the "Show all results" button. */
@Composable
private fun ShowAllResultsIcon(icon: Icon) {
  val context = LocalContext.current
  val tintableIcon: TintableIcon? =
    remember(icon) { icon.loadDrawable(context)?.toBitmap()?.asTintableIcon(true) }

  if (tintableIcon != null) {
    IconOrImage(modifier = Modifier.size(16.dp), icon = tintableIcon)
  }
}

/** Returns the number of general cards to display per source. */
private fun getNumGeneralCardsPerSource(
  detailedCards: List<CallVisualizerDetailedCard>,
  isDisplayingMoreResults: Boolean,
): Int {
  return if (isDisplayingMoreResults) {
    // Always display all general cards if user has clicked the "Show more results" button
    Int.MAX_VALUE
  } else if (detailedCards.isEmpty()) {
    // If there are no detailed cards, initially display the default number of general cards per
    // source
    DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
  } else {
    // If there are detailed cards (and implied the user has not clicked the "Show more results"
    // button), do not display any general cards by default
    0
  }
}

/**
 * Applies [modify] to this modifier if [value] is not null. The non-null [value] is passed as the
 * argument to the [modify] lambda.
 */
private fun <T> Modifier.thenIfNotNull(value: T?, modify: (T) -> Modifier): Modifier =
  if (value != null) {
    this.then(modify(value))
  } else {
    this
  }

/**
 * Applies a fading edge effect to this modifier.
 *
 * This draws a vertical gradient that fades from transparent to the specified color at the top
 * and/or bottom of the content, based on scroll flags.
 *
 * @param shouldFadeTop Whether to fade the top edge.
 * @param shouldFadeBottom Whether to fade the bottom edge.
 * @param color The color to fade to at the edges.
 * @param height The height of the fading edge.
 */
@Composable
private fun Modifier.fadeEdge(
  shouldFadeTop: Boolean,
  shouldFadeBottom: Boolean,
  color: Color,
  height: Dp,
): Modifier {
  val topFadeFraction by
    animateFloatAsState(
      targetValue = if (shouldFadeTop) 1f else 0f,
      animationSpec = tween(durationMillis = 300),
      label = "topFade",
    )
  val bottomFadeFraction by
    animateFloatAsState(
      targetValue = if (shouldFadeBottom) 1f else 0f,
      animationSpec = tween(durationMillis = 300),
      label = "bottomFade",
    )

  return drawWithContent {
    val heightValue = height.toPx()

    drawContent()

    if (topFadeFraction > 0f) {
      val currentHeight = heightValue * topFadeFraction
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(color, color.copy(alpha = 0f)),
            startY = 0f,
            endY = currentHeight,
          ),
        size = Size(this.size.width, currentHeight),
      )
    }

    if (bottomFadeFraction > 0f) {
      val currentHeight = heightValue * bottomFadeFraction
      drawRect(
        brush =
          Brush.verticalGradient(
            colors = listOf(color.copy(alpha = 0f), color),
            startY = size.height - currentHeight,
            endY = size.height,
          ),
        topLeft = Offset(x = 0f, y = size.height - currentHeight),
        size = Size(this.size.width, currentHeight),
      )
    }
  }
}
