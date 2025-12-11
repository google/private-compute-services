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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionHelper
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionListener

/**
 * An interaction that allows a template to interop with an an external interaction like
 * [Button(onClick = )][androidx.compose.material3.Button], when a native interaction is not
 * possible.
 */
interface InteropInteraction {

  /**
   * Invokes [onInteraction] on some external interaction. Can also be used to execute a side-effect
   * that is PCC compliant regardless of user interaction.
   *
   * This API is inherently more unsafe than using a native interaction like
   * [doOnClick][com.google.android.as.oss.delegatedui.service.templates.scope.interactions.ClickInteraction].
   * To ensure proper usage, this must be called immediately in the body of the external interaction
   * callback.
   *
   * Should be called from a [androidx.compose.runtime.LaunchedEffect] if in a composition, or
   * [androidx.compose.runtime.rememberCoroutineScope] if in an event callback.
   */
  suspend fun doOnInterop(
    uiTokenId: UiIdToken,
    interactionType: InteractionType,
    onInteraction: InteractionListener,
  )
}

class InteropInteractionImpl(private val helper: InteractionHelper) : InteropInteraction {

  override suspend fun doOnInterop(
    uiTokenId: UiIdToken,
    interactionType: InteractionType,
    onInteraction: InteractionListener,
  ) {
    with(helper) { onInteraction(uiTokenId, interaction = interactionType) { onInteraction() } }
  }
}
