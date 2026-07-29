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

import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.ContextInsight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken

/**
 * Returns a memoized lambda to report child insight events, resolving the ACE platform dependencies
 * (locals and PersonalContextManager) automatically.
 */
@Composable
fun rememberInsightEventReporter(): (ContextInsight, Int) -> Unit {
  val context = LocalContext.current
  val insightEventReporter = LocalInsightEventReporter.current
  val publishedInsight = LocalPublishedContextInsight.current
  val renderToken = LocalRenderToken.current
  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }
  return remember(context, insightEventReporter, publishedInsight, renderToken) {
    { childInsight, eventType ->
      with(insightEventReporter) {
        personalContextManager?.reportChildInsightEvent(
          publishedInsight,
          childInsight,
          eventType,
          renderToken,
        )
      }
    }
  }
}
