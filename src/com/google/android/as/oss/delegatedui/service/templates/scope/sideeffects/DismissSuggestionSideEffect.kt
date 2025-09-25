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

package com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_DISMISS_SUGGESTION
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.common.flogger.GoogleLogger

/** A side-effect that logs suggestion is dismissed by the user. */
interface DismissSuggestionSideEffect {

  /** Logs that the suggestion is dismissed. */
  suspend fun InteractionScope.dismissSuggestion()
}

/** Implementation for [DismissSuggestionSideEffect]. */
class DismissSuggestionSideEffectImpl(private val helper: SideEffectHelper) :
  DismissSuggestionSideEffect {

  override suspend fun InteractionScope.dismissSuggestion() =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_DISMISS_SUGGESTION) {
        logger.atFiner().log("Suggestion is dismissed")
      }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
