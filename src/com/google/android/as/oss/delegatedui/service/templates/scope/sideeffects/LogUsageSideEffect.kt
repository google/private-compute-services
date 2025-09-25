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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_LOG_USAGE
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.common.flogger.GoogleLogger

/**
 * A side-effect that logs that the interaction was invoked.
 *
 * This usually isn't necessary if another side-effect is invoked, since that will automatically
 * logUsageData() as well
 */
interface LogUsageSideEffect {

  /** Logs that the interaction was invoked. */
  suspend fun InteractionScope.logUsage()
}

class LogUsageSideEffectImpl(private val helper: SideEffectHelper) : LogUsageSideEffect {

  override suspend fun InteractionScope.logUsage() =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_LOG_USAGE) { logger.atFiner().log("Logging usage data") }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
