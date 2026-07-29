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

package com.android.personalcontext.ace.internal.templates.richcard.common

import android.service.personalcontext.insight.interaction.InsightEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.internal.templates.richcard.ActionableCardAction
import com.android.personalcontext.ace.internal.templates.richcard.CardAction
import com.android.personalcontext.ace.internal.templates.richcard.EgressableCardAction
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.utils.RemoteActionUtils.execute

/** A composable that renders a row of action buttons for a visualizer card. */
@Composable
@Suppress("NewApi")
fun CardActionRow(cardActions: List<CardAction>, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
  ) {
    for (cardAction in cardActions) {
      CardActionButton(cardAction = cardAction)
    }
  }
}

@Composable
@Suppress("NewApi")
private fun CardActionButton(cardAction: CardAction) {
  val info = LocalInsightSurfaceClientInfo.current
  val context = LocalContext.current
  val reportEvent = rememberInsightEventReporter()

  var hasReportedImpression by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (!hasReportedImpression) {
      cardAction.insight?.let { insight -> reportEvent(insight, InsightEvent.EVENT_SHOW) }
      hasReportedImpression = true
    }
  }

  Surface(
    shape = RoundedCornerShape(100.dp),
    color = Color.Transparent,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    modifier =
      Modifier.clip(RoundedCornerShape(100.dp)).let { baseModifier ->
        when (cardAction) {
          is ActionableCardAction -> {
            val description = cardAction.remoteAction.contentDescription?.toString()
            val title = cardAction.displayDetails.title?.toString()

            baseModifier
              .clearAndSetSemantics { contentDescription = description ?: title ?: "" }
              .clickable {
                cardAction.insight?.let { insight ->
                  reportEvent(insight, InsightEvent.EVENT_USER_TAP)
                }
                cardAction.remoteAction.execute(context)
                info.onReceiveInsight(ServerSideCloseInsight().toContextInsight())
              }
          }
          is EgressableCardAction ->
            baseModifier.clickable {
              cardAction.insight?.let { insight ->
                reportEvent(insight, InsightEvent.EVENT_USER_TAP)
                info.onReceiveInsight(insight)
                info.onReceiveInsight(ServerSideCloseInsight().toContextInsight())
              }
            }
        }
      },
  ) {
    val displayDetails = cardAction.displayDetails
    val imageBitmap =
      remember(displayDetails.icon) {
        displayDetails.icon?.loadDrawable(context)?.toBitmap()?.asImageBitmap()
      }
    val hasIcon = imageBitmap != null
    val startPadding = if (hasIcon) 12.dp else 16.dp

    Row(
      modifier = Modifier.padding(start = startPadding, end = 16.dp, top = 10.dp, bottom = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (hasIcon) {
        Image(bitmap = imageBitmap, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = displayDetails.title?.toString() ?: "",
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
