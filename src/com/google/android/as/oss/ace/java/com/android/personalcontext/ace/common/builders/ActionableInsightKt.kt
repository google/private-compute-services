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

package com.android.personalcontext.ace.common.builders

import android.service.personalcontext.Token
import android.service.personalcontext.hint.PublishedContextHint
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.InsightActionDetails
import android.service.personalcontext.insight.InsightDisplayDetails

/** Creates an [ActionableInsight] using an idiomatic Kotlin DSL. */
inline fun actionableInsight(
  actionDetails: InsightActionDetails,
  displayDetails: InsightDisplayDetails,
  originHints: Collection<PublishedContextHint>? = null,
  block: ActionableInsightKt.Dsl.() -> Unit = {},
): ActionableInsight =
  ActionableInsightKt.Dsl(ActionableInsight.Builder(actionDetails, displayDetails))
    .apply {
      originHints?.let { this.originHints += it }
      block()
    }
    .build()

object ActionableInsightKt {

  @DslMarker annotation class ActionableInsightDslMarker

  @ActionableInsightDslMarker
  class Dsl(@PublishedApi internal val builder: ActionableInsight.Builder) {

    /** Allows adding originHints using the `+=` operator. */
    val originHints = DslCollection<PublishedContextHint> { builder.addOriginHint(it) }

    /** Allows adding tokens using the `+=` operator. */
    val tokens = DslCollection<Token> { builder.addToken(it) }

    @PublishedApi internal fun build(): ActionableInsight = builder.build()
  }
}
