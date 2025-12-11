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

package com.google.android.`as`.oss.delegatedui.service.templates.scope

import android.content.Context
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiHint
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.delegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData
import com.google.common.flogger.GoogleLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.yield

/**
 * Helper class for implementing side-effects. Accept a `helper` in your constructor, and define
 * your function as `= with(helper) { ... }`.
 */
interface SideEffectHelper {

  val context: Context

  /** Invokes a side-effect with the given [semantics]. */
  suspend fun InteractionScope.invokeSideEffect(
    semantics: SemanticsType? = null,
    block: suspend SideEffectScope.() -> Unit,
  )
}

class SideEffectHelperImpl(
  private val logUsageData: suspend (DelegatedUiUsageData) -> Unit,
  private val onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
  private val onSendHints: suspend (Set<DelegatedUiHint>) -> Unit,
  private val onSessionClose: () -> Unit,
  override val context: Context,
) : SideEffectHelper {

  override suspend fun InteractionScope.invokeSideEffect(
    semanticsType: SemanticsType?,
    block: suspend SideEffectScope.() -> Unit,
  ) {
    (this as InteractionCoroutineScopeImpl).interactionScope.validate()

    yield()

    try {
      logger
        .atFiner()
        .log("Invoking side-effect %s triggered by interaction %s.", semanticsType, interactionType)
      SideEffectScopeImpl(onDataEgress, onSendHints, onSessionClose).block()
      logger
        .atFiner()
        .log("Invoked side-effect %s triggered by interaction %s.", semanticsType, interactionType)

      if (semanticsType != null) {
        logUsageData(
          delegatedUiUsageData {
            this.interaction = interactionType
            this.semantics = semanticsType
            this.uiIdToken += interactionUiIdToken
          }
        )
      }
    } catch (e: CancellationException) {
      throw e
    } catch (e: Throwable) {
      logger.atSevere().withCause(e).log("Error while invoking side-effect %s.", semanticsType)
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
