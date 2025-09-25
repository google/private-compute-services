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

import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.common.flogger.GoogleLogger

/** A side-effect that closes the delegated UI session. */
interface CloseSessionSideEffect {

  /** Closes the delegated UI session. */
  suspend fun InteractionScope.closeSession()
}

class CloseSessionSideEffectImpl(private val helper: SideEffectHelper) : CloseSessionSideEffect {

  override suspend fun InteractionScope.closeSession() =
    with(helper) {
      invokeSideEffect {
        logger.atFiner().log("Closing delegated UI session")
        onSessionClose()
      }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
