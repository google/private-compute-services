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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_SEND_EGRESS_DATA
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressDataKt
import com.google.android.`as`.oss.delegatedui.api.integration.egress.delegatedUiEgressData
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.common.flogger.GoogleLogger

/** A side-effect that sends data egress out of PCC. */
interface SendEgressDataSideEffect {

  /**
   * Sends egress data given by [egressDataBuilder] out of PCC and back to the local client.
   *
   * This function follows the proto builder pattern:
   * ```
   * sendEgressData { additionalData ->
   *   this.myEgressType = /* ... */
   * }
   * ```
   */
  suspend fun InteractionScope.sendEgressData(
    egressDataBuilder: DelegatedUiEgressDataKt.Dsl.() -> Unit
  )
}

class SendEgressDataSideEffectImpl(private val helper: SideEffectHelper) :
  SendEgressDataSideEffect {

  override suspend fun InteractionScope.sendEgressData(
    egressDataBuilder: DelegatedUiEgressDataKt.Dsl.() -> Unit
  ) =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_SEND_EGRESS_DATA) {
        val egressData = delegatedUiEgressData { egressDataBuilder() }
        logger.atFiner().log("Sending egress data: %s", egressData)
        onDataEgress(egressData)
      }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
