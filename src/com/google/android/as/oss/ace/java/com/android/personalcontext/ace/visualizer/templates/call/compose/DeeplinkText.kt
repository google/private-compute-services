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

import android.app.RemoteAction
import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.utils.RemoteActionUtils.execute

private const val TAG = "DeeplinkText"

/**
 * Displays the deeplink text for the card.
 *
 * @param text The text to display.
 * @param sourceName The name of the source. The [text] should contain this name, e.g. "Gmail".
 * @param remoteAction The remote action to execute when the text is clicked.
 * @param deeplinkInsight The insight that contains the deeplink.
 */
@Composable
internal fun DeeplinkText(
  text: String,
  sourceName: String,
  remoteAction: RemoteAction,
  deeplinkInsight: ContextInsight,
) {
  val context = LocalContext.current

  // Metrics logging START
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
      reportEvent(deeplinkInsight, InsightEvent.EVENT_SHOW)
      hasReportedImpression = true
    }
  }
  // Metrics logging END

  val onClickLambda =
    remember(remoteAction, context, deeplinkInsight) {
      {
        Log.v(TAG, "[CallEmbedded] DeeplinkText onClick")
        reportEvent(deeplinkInsight, InsightEvent.EVENT_USER_TAP)
        remoteAction.execute(context)
      }
    }

  val annotatedString = getDeeplinkAnnotatedString(text = text, sourceName = sourceName)

  Box(
    contentAlignment = Alignment.CenterStart,
    modifier =
      Modifier.defaultMinSize(minHeight = 48.dp)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = onClickLambda,
          role = Role.Button,
        )
        .semantics { contentDescription = text },
  ) {
    Text(
      text = annotatedString,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.primary,
    )
  }
}

/**
 * Returns an [AnnotatedString] with [sourceName] underlined and clickable.
 *
 * If the source name is not found in the text, the whole text will be underlined.
 *
 * @param text The text to display.
 * @param sourceName The name of the source app. Leave it empty to underline the whole [text].
 */
private fun getDeeplinkAnnotatedString(text: String, sourceName: String): AnnotatedString =
  buildAnnotatedString {
    val _startIndex = text.indexOf(sourceName)
    val endIndex =
      if (_startIndex != -1 && sourceName.isNotBlank()) {
        _startIndex + sourceName.length
      } else {
        text.length
      }
    // If the source name is not found in the text, underline the whole text
    val startIndex = Math.max(0, _startIndex)
    append(text.substring(0, startIndex))
    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
      append(text.substring(startIndex, endIndex))
    }
    append(text.substring(endIndex))
  }
