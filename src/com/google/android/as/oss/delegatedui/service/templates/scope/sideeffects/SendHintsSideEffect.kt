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

import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiHint
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_SEND_HINTS
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.common.flogger.GoogleLogger

/**
 * A side-effect that sends hints out of PCC, designed to be compliant even without an explicit user
 * interaction.
 */
interface SendHintsSideEffect {

  /** Send hints out of PCC and back to the local client */
  suspend fun InteractionScope.sendHints(hints: Set<DelegatedUiHint>)
}

class SendHintsSideEffectImpl(private val helper: SideEffectHelper) : SendHintsSideEffect {

  override suspend fun InteractionScope.sendHints(hints: Set<DelegatedUiHint>) {
    with(helper) {
      invokeSideEffect(semantics = SEMANTICS_TYPE_SEND_HINTS) {
        logger.atFiner().log("Sending hints: %s", hints)
        onSendHints(hints)
      }
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
