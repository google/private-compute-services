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

package com.android.personalcontext.ace.internal.compat

import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.InsightDisplayDetails
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsightV1
import com.android.personalcontext.ace.client.prototype.clientaction.ClientActionInsight
import com.android.personalcontext.ace.client.prototype.clientaction.params.ClientActionParams
import com.android.personalcontext.ace.common.InsightExtendedDetails
import com.android.personalcontext.ace.visualizer.compat.ClientActionInsightCompat
import com.android.personalcontext.ace.visualizer.compat.ClientActionInsightCompat.SafeClientActionInsight
import javax.inject.Inject

/**
 * [ClientActionInsightCompat] implementation that parses the [ContextInsight] using
 * [ClientActionInsight] prototype insight.
 */
class ClientActionInsightCompatImpl @Inject constructor() : ClientActionInsightCompat {
  override fun <T> ifClientActionInsight(
    insight: ContextInsight,
    block: (SafeClientActionInsight) -> T,
  ): T? =
    insight.toPrototypeInsightV1<ClientActionInsight>()?.let {
      clientActionInsight: ClientActionInsight ->
      block(
        object : SafeClientActionInsight {
          override val clientActionParams: ClientActionParams
            get() = clientActionInsight.clientActionParams

          override val insightDisplayDetails: InsightDisplayDetails
            get() = clientActionInsight.insightDisplayDetails

          override val insightExtendedDetails: InsightExtendedDetails?
            get() = clientActionInsight.insightExtendedDetails
        }
      )
    }
}
