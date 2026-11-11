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
import com.android.personalcontext.ace.client.prototype.grid.InsightGrid
import com.android.personalcontext.ace.common.InsightGridItem
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat
import com.android.personalcontext.ace.visualizer.compat.InsightGridCompat.SafeInsightGrid
import javax.inject.Inject

/**
 * [InsightGridCompat] implementation that parses the [ContextInsight] using [InsightGrid] prototype
 * insight.
 */
class InsightGridCompatImpl @Inject constructor() : InsightGridCompat {

  override val totalSpanCapacityPhone: Int
    get() = InsightGrid.TOTAL_SPAN_CAPACITY_PHONE

  override val totalSpanCapacityWatch: Int
    get() = InsightGrid.TOTAL_SPAN_CAPACITY_WATCH

  override fun <T> ifInsightGrid(insight: ContextInsight, block: (SafeInsightGrid) -> T): T? =
    insight.toPrototypeInsightV1<InsightGrid>()?.let { insightGrid ->
      block(
        object : SafeInsightGrid {
          override val items: List<InsightGridItem>
            get() = insightGrid.items
        }
      )
    }
}
