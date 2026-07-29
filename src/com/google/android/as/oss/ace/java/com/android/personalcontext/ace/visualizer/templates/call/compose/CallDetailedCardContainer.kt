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

import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightDisplayDetails
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerDetailedCard
import com.android.personalcontext.ace.visualizer.templates.ui.common.grid.JustifiedWrapLayout

private const val TAG = "CallDetailedCardContainer"

/** The container for a single detailed card in the Magic Cue Call widget. */
@Composable
fun CallDetailedCardContainer(card: CallVisualizerDetailedCard, useCardContainer: Boolean = false) {
  val cardBackground = LocalCallWidgetBackgrounds.current.cardBackground
  val containerColor =
    if (cardBackground != Color.Unspecified) cardBackground else Color.Transparent

  if (useCardContainer) {
    Card(
      modifier = Modifier.testTag("detailed_card_container"),
      colors = CardDefaults.cardColors(containerColor = containerColor),
      shape = RoundedCornerShape(24.dp),
    ) {
      CallDetailedCardContent(
        card = card,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
      )
    }
  } else {
    CallDetailedCardContent(card = card)
  }
}

@Composable
private fun CallDetailedCardContent(
  card: CallVisualizerDetailedCard,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
    CardHeader(cardTitle = card.title)
    Spacer(modifier = Modifier.height(16.dp))

    val totalSpanCapacity = LocalInsightGridCompat.current.totalSpanCapacityPhone
    JustifiedWrapLayout(
      items = card.items,
      totalSpanCapacity = totalSpanCapacity,
      spanSelector = { insightGridItem -> insightGridItem.span.value },
    ) { item ->
      if (item.insight is DisplayInsight) {
        CardItem((item.insight as DisplayInsight).details)
      }
    }
    Spacer(modifier = Modifier.height(8.dp))
    CardFooter(card)
  }
}

/** Represents a single item in a row. This is the smallest unit of data in a row. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CardItem(details: InsightDisplayDetails) {
  Surface(
    modifier = Modifier.fillMaxSize(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surfaceContainer,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
          .thenIfNotNull(details.contentDescription?.toString()) {
            Modifier.clearAndSetSemantics { contentDescription = it }
          }
    ) {
      Text(
        text = details.title?.toString() ?: "",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = details.subtitle?.toString() ?: "",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CardHeader(cardTitle: DisplayInsight) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    CardTitle(title = cardTitle.details.title?.toString() ?: "", titleInsight = cardTitle)
  }
}

@Composable
private fun CardFooter(card: CallVisualizerDetailedCard) {
  if (card.feedback == null && card.originalDataSource == null) {
    Log.v(TAG, "[CallEmbedded] No elements to display in CardFooter")
    return
  }

  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    val dataSource = card.originalDataSource
    card.originalDataSource?.let { dataSource ->
      DeeplinkText(
        text = dataSource.title,
        sourceName = dataSource.appName,
        remoteAction = dataSource.remoteAction,
        deeplinkInsight = dataSource.originalInsight,
      )
    }

    // Ensure deeplink and feedback are pushed to either side
    Spacer(modifier = Modifier.weight(1f))

    card.feedback?.let { FeedbackButtons(feedbackInsight = it, shouldReportEvent = true) }
  }
}

@Composable
private fun RowScope.CardTitle(title: String, titleInsight: DisplayInsight) {
  // Metrics logging START
  val context = LocalContext.current
  val insightEventReporter = LocalInsightEventReporter.current
  val publishedInsight = LocalPublishedContextInsight.current
  val renderToken = LocalRenderToken.current

  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }

  fun reportEvent(insight: ContextInsight, event: Int) {
    with(insightEventReporter) {
      personalContextManager?.reportChildInsightEvent(publishedInsight, insight, event, renderToken)
    }
  }

  var hasReportedImpression by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (!hasReportedImpression) {
      reportEvent(titleInsight, InsightEvent.EVENT_SHOW)
      hasReportedImpression = true
    }
  }
  // Metrics logging END

  Text(
    modifier = Modifier.weight(1f),
    text = title,
    style = MaterialTheme.typography.titleMediumEmphasized,
    color = MaterialTheme.colorScheme.onSurface,
  )
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
