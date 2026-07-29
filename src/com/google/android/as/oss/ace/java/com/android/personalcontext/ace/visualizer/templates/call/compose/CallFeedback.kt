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
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightDisplayDetails
import android.service.personalcontext.insight.interaction.InsightEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken

private const val TAG = "CallFeedback"

/**
 * The feedback buttons (thumbs up and thumbs down) for a single card or entity.
 *
 * @param feedbackInsight The feedback insight to report the events for.
 * @param shouldReportEvent Whether to report the event when the buttons are shown. Defaults to
 *   false if the insight passed is not a `feedbackInsight`, but part of a larger card. Prevents
 *   double-logging. Should only be true iff feedbackInsight is a separate insight from the rest of
 *   the component it is a part of
 */
@Composable
internal fun FeedbackButtons(feedbackInsight: ContextInsight, shouldReportEvent: Boolean = false) {
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
        feedbackInsight,
        event,
        renderToken,
      )
    }
  }

  LaunchedEffect(Unit) {
    if (shouldReportEvent) {
      reportEvent(InsightEvent.EVENT_SHOW)
    }
  }

  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    FeedbackButton(
      modifier = Modifier.testTag("thumbs_up"),
      icon = Icons.Outlined.ThumbUp,
      contentDescription = "Helpful",
      onClick = { reportEvent(InsightEvent.EVENT_USER_FEEDBACK_POSITIVE) },
    )
    FeedbackButton(
      modifier = Modifier.testTag("thumbs_down"),
      icon = Icons.Outlined.ThumbDown,
      contentDescription = "Not Helpful",
      onClick = { reportEvent(InsightEvent.EVENT_USER_FEEDBACK_NEGATIVE) },
    )
  }
}

/** A single feedback button (ie. thumbs up or thumbs down). */
@Composable
private fun FeedbackButton(
  modifier: Modifier = Modifier,
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
) {
  IconButton(modifier = modifier, onClick = onClick) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      modifier = Modifier.size(24.dp),
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

private val ContextInsight.details: InsightDisplayDetails?
  get() =
    when (this) {
      is DisplayInsight -> this.details
      is ActionableInsight -> this.displayDetails
      else -> null
    }
