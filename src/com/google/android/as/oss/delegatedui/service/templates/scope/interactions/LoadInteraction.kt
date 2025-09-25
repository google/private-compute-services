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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_LOAD
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiAdditionalData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionHelper
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables

/**
 * An interaction that allows a template to be notified when additional data is available, so it can
 * re-render itself. This interaction is intended as a short-term solution for latency-sensitive CUJ
 * requirements. In the future, we may make the data flow in PCS more dynamic, and remove this
 * interaction.
 */
interface LoadInteraction {

  /**
   * Invokes [onLoad] on additional data being available. The callback is intentionally missing a
   * [com.google.android.as.oss.delegatedui.service.templates.scope.InteractionScope] receiver. This
   * prevents side-effects from being invoked.
   */
  fun doOnLoad(onLoad: (ResponseWithParcelables<DelegatedUiAdditionalData>?) -> Unit)
}

class LoadInteractionImpl(private val helper: InteractionHelper) : LoadInteraction {

  override fun doOnLoad(onLoad: (ResponseWithParcelables<DelegatedUiAdditionalData>?) -> Unit) {
    with(helper) {
      onInteraction(uiIdToken = UiIdToken.getDefaultInstance(), INTERACTION_TYPE_LOAD) {
        onLoad(it)
      }
    }
  }
}
