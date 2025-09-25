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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_SHOW_ENTITY_FEEDBACK
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_SHOW_MULTI_FEEDBACK
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.android.`as`.oss.feedback.FeedbackApi
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData

/** A side-effect that displays a feedback dialog in PCS. */
interface ShowFeedbackSideEffect {

  /** Displays an entity feedback dialog. */
  suspend fun InteractionScope.showEntityFeedback(data: EntityFeedbackDialogData)

  /** Displays a multi feedback dialog. */
  suspend fun InteractionScope.showMultiFeedback(data: MultiFeedbackDialogData)
}

class ShowFeedbackSideEffectImpl(private val helper: SideEffectHelper) : ShowFeedbackSideEffect {

  override suspend fun InteractionScope.showEntityFeedback(data: EntityFeedbackDialogData) =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_SHOW_ENTITY_FEEDBACK) {
        val intent = FeedbackApi.createEntityFeedbackIntent(context = context, data = data)
        context.startActivity(intent)
      }
    }

  override suspend fun InteractionScope.showMultiFeedback(data: MultiFeedbackDialogData) =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_SHOW_MULTI_FEEDBACK) {
        val intent = FeedbackApi.createMultiFeedbackIntent(context = context, data = data)
        context.startActivity(intent)
      }
    }
}
