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

package com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_VIEW
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionHelper
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionListener

/** An interaction that represents a view being shown to the user. */
interface ImpressionInteraction {

  /**
   * Use [LogUsageSideEffect] to log that the UI element represented by [uiIdToken] has been seen by
   * the user.
   *
   * Should be called from a [androidx.compose.runtime.LaunchedEffect] if in a composition, or
   * [androidx.compose.runtime.rememberCoroutineScope] if in an event callback.
   */
  suspend fun doOnImpression(uiIdToken: UiIdToken, onImpression: InteractionListener)
}

class ImpressionInteractionImpl(private val helper: InteractionHelper) : ImpressionInteraction {

  override suspend fun doOnImpression(uiIdToken: UiIdToken, onImpression: InteractionListener) {
    with(helper) {
      onInteraction(interaction = INTERACTION_TYPE_VIEW, uiIdToken = uiIdToken) { onImpression() }
    }
  }
}
