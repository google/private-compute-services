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

import android.app.ActivityOptions
import android.app.PendingIntent
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalContext
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.utils.RemoteActionUtils.execute
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Generic extension function for Modifier.clickable that accepts a PendingIntent. */
fun Modifier.clickable(pendingIntent: PendingIntent?, onClick: () -> Unit): Modifier {
  return if (pendingIntent == null) {
    this
  } else {
    this.clickable {
      try {
        pendingIntent.send(
          ActivityOptions.makeBasic()
            .apply {
              if (com.android.window.flags.ExportedFlags.balAdditionalStartModes()) {
                setPendingIntentBackgroundActivityStartMode(
                  ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                )
              }
            }
            .toBundle()
        )
        onClick()
      } catch (e: PendingIntent.CanceledException) {
        Log.e("ModifierUtils", "Failed to send pending intent", e)
      }
    }
  }
}

/** Overload for Modifier.clickable that accepts only a PendingIntent. */
fun Modifier.clickable(pendingIntent: PendingIntent?): Modifier {
  return this.clickable(pendingIntent = pendingIntent, onClick = {})
}

/**
 * Reusable modifier to make a card clickable using [CardContextAction]. It handles:
 * - Reporting [InsightEvent.EVENT_USER_TAP] using the insight.
 * - Executing the remote action.
 * - Sending [ServerSideCloseInsight] to close the insight on client side.
 */
fun Modifier.cardContextActionClickable(action: CardContextAction?): Modifier = composed {
  if (action == null) return@composed this
  val context = LocalContext.current
  val info = LocalInsightSurfaceClientInfo.current
  val reportEvent = rememberInsightEventReporter()
  val scope = rememberCoroutineScope()

  var hasReportedImpression by rememberSaveable(action.insight) { mutableStateOf(false) }
  LaunchedEffect(action) {
    if (!hasReportedImpression) {
      reportEvent(action.insight, InsightEvent.EVENT_SHOW)
      hasReportedImpression = true
    }
  }

  this.clickable {
    reportEvent(action.insight, InsightEvent.EVENT_USER_TAP)
    action.remoteAction.execute(context)
    scope.launch {
      // Delay dismissal slightly to allow ripple animation to show.
      delay(200)
      info.onReceiveInsight(ServerSideCloseInsight().toContextInsight())
    }
  }
}
