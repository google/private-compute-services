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
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsightV1
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.visualizer.compat.CardInsightCompat
import com.android.personalcontext.ace.visualizer.compat.CardInsightCompat.SafeCardInsight
import javax.inject.Inject

/**
 * [CardInsightCompat] implementation that parses the [ContextInsight] using [CardInsight] prototype
 * insight.
 */
class CardInsightCompatImpl @Inject constructor() : CardInsightCompat {

  override fun <T> ifCardInsight(insight: ContextInsight, block: (SafeCardInsight) -> T): T? =
    insight.toPrototypeInsightV1<CardInsight>()?.let { cardInsight ->
      block(
        object : SafeCardInsight {
          override val title: ContextInsight?
            get() = cardInsight.title

          override val header: ContextInsight?
            get() = cardInsight.header

          override val body: ContextInsight
            get() = cardInsight.body

          override val footer: ContextInsight?
            get() = cardInsight.footer

          override val actions: ContextInsight?
            get() = cardInsight.actions
        }
      )
    }
}
