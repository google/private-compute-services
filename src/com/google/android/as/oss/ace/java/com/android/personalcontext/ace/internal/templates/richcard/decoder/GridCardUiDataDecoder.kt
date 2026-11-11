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

package com.android.personalcontext.ace.internal.templates.richcard.decoder

import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsightV1
import com.android.personalcontext.ace.client.prototype.grid.InsightGrid
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiGridCardContext
import com.android.personalcontext.ace.internal.templates.richcard.toGridEntity
import javax.inject.Inject
import javax.inject.Singleton

/** Converter between [DeprecatedUiGridCardContext] and [CardInsight]. */
@Singleton
class GridCardUiDataDecoder @Inject constructor() :
  CardUiDataDecoder<DeprecatedUiGridCardContext>() {

  /**
   * Converts [ContextInsight] back to [DeprecatedUiGridCardContext].
   *
   * @return The converted [DeprecatedUiGridCardContext].
   * @receiver The [ContextInsight] to convert, usually from [CardInsight.body].
   */
  @Suppress("NewApi")
  override fun ContextInsight.toCardContext(): DeprecatedUiGridCardContext {
    val insightGrid =
      requireNotNull(this.toPrototypeInsightV1<InsightGrid>()) { "InsightGrid cannot be null" }

    val entities = insightGrid.items.mapNotNull { it.toGridEntity() }

    require(entities.isNotEmpty()) { "Missing entities" }

    return DeprecatedUiGridCardContext(entities = entities)
  }
}
