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

package com.android.personalcontext.ace.internal.templates.richcard.common

import android.service.personalcontext.insight.interaction.InsightEvent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo

/** A composable that renders the dismiss button icon when [dismissInsight] is present. */
@Suppress("NewApi", "FlaggedApi")
@Composable
fun CardDismissIcon(dismissInsight: ServerSideCloseInsight?, modifier: Modifier = Modifier) {
  val serverSideCloseInsight = dismissInsight ?: return
  val context = LocalContext.current
  val info = LocalInsightSurfaceClientInfo.current
  val reportEvent = rememberInsightEventReporter()
  val dismissIcon = serverSideCloseInsight.insightDisplayDetails?.icon
  val dismissIconBitmap =
    remember(dismissIcon) { dismissIcon?.loadDrawable(context)?.toBitmap()?.asImageBitmap() }
  val dismissContentDescription =
    serverSideCloseInsight.insightDisplayDetails?.contentDescription?.toString()
  if (dismissIconBitmap != null) {
    Icon(
      bitmap = dismissIconBitmap,
      contentDescription = dismissContentDescription,
      modifier =
        modifier
          .size(20.dp)
          .clip(CircleShape)
          .clickable(
            onClick = {
              info.onReceiveInsight(serverSideCloseInsight.toContextInsight())
              reportEvent(
                serverSideCloseInsight.toContextInsight(),
                InsightEvent.EVENT_USER_DISMISS,
              )
            },
            role = Role.Button,
          ),
      tint = MaterialTheme.colorScheme.primary,
    )
  }
}
