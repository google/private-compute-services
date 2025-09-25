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

import android.app.PendingIntent
import android.content.Context
import android.view.View
import androidx.compose.ui.Modifier
import com.google.android.`as`.oss.dataattribution.proto.AttributionChipData
import com.google.android.`as`.oss.dataattribution.proto.AttributionDialogData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressDataKt
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiAdditionalData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.common.ConnectLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.ClickInteraction
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.ClickInteractionImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.DragInteraction
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.DragInteractionImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.ImpressionInteraction
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.ImpressionInteractionImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.InteropInteraction
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.InteropInteractionImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.LoadInteraction
import com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions.LoadInteractionImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.CloseSessionSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.CloseSessionSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.DismissSuggestionSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.DismissSuggestionSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ExecuteActionSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ExecuteActionSideEffect.Action
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ExecuteActionSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.LogUsageSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.LogUsageSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.SendEgressDataSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.SendEgressDataSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ShowDataAttributionSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ShowDataAttributionSideEffectImpl
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ShowFeedbackSideEffect
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ShowFeedbackSideEffectImpl
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Deferred

/**
 * Scoped environment provided for [TemplateRenderer] that grants access to
 * [interactions][com.google.android.as.oss.delegatedui.service.templates.scope.interactions] and
 * [side-effects][com.google.android.as.oss.delegatedui.service.templates.scope.sideeffects] on
 * delegated UI.
 *
 * | Interactions | Side-Effects        |
 * |--------------|---------------------|
 * | Click        | SendEgressData      |
 * | Load         | ExecuteAction       |
 * | Interop      | ShowDataAttribution |
 * | Impression   | CloseSession        |
 * | Drag         | LogUsage            |
 * |              | ShowEntityFeedback  |
 * |              | DismissSuggestion   |
 */
interface TemplateRendererScope :
  ClickInteraction,
  DragInteraction,
  LoadInteraction,
  InteropInteraction,
  ImpressionInteraction,
  SendEgressDataSideEffect,
  ExecuteActionSideEffect,
  ShowDataAttributionSideEffect,
  CloseSessionSideEffect,
  LogUsageSideEffect,
  ShowFeedbackSideEffect,
  DismissSuggestionSideEffect

/**
 * Creates a [TemplateRendererScope] on a create request, or returns a no-op implementation on a
 * prepare request.
 */
fun TemplateRendererScope(
  context: Context,
  lifecycle: DelegatedUiLifecycle,
  mainCoroutineContext: CoroutineContext,
  additionalData: Deferred<ResponseWithParcelables<DelegatedUiAdditionalData>>?,
  logUsageData: suspend (DelegatedUiUsageData) -> Unit,
  onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
  onSessionClose: () -> Unit,
): TemplateRendererScope =
  if (lifecycle is ConnectLifecycle) {
    TemplateRendererScopeImpl(
      interactionHelper = InteractionHelperImpl(lifecycle, mainCoroutineContext, additionalData),
      sideEffectHelper = SideEffectHelperImpl(logUsageData, onDataEgress, onSessionClose, context),
    )
  } else {
    NO_OP_SCOPE
  }

private class TemplateRendererScopeImpl(
  interactionHelper: InteractionHelper,
  sideEffectHelper: SideEffectHelper,
) :
  TemplateRendererScope,
  ClickInteraction by ClickInteractionImpl(interactionHelper),
  DragInteraction by DragInteractionImpl(interactionHelper),
  LoadInteraction by LoadInteractionImpl(interactionHelper),
  InteropInteraction by InteropInteractionImpl(interactionHelper),
  ImpressionInteraction by ImpressionInteractionImpl(interactionHelper),
  SendEgressDataSideEffect by SendEgressDataSideEffectImpl(sideEffectHelper),
  ExecuteActionSideEffect by ExecuteActionSideEffectImpl(sideEffectHelper),
  ShowDataAttributionSideEffect by ShowDataAttributionSideEffectImpl(sideEffectHelper),
  CloseSessionSideEffect by CloseSessionSideEffectImpl(sideEffectHelper),
  LogUsageSideEffect by LogUsageSideEffectImpl(sideEffectHelper),
  ShowFeedbackSideEffect by ShowFeedbackSideEffectImpl(sideEffectHelper),
  DismissSuggestionSideEffect by DismissSuggestionSideEffectImpl(sideEffectHelper)

private val NO_OP_SCOPE = NoOpTemplateRendererScope()

/**
 * No-op implementation of [TemplateRendererScope]. Useful for cases when you want to render a
 * template, but you don't need any dynamic behaviors on that template. For example, if you are just
 * rendering it to measure it once.
 */
class NoOpTemplateRendererScope : TemplateRendererScope {

  override fun View.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener?,
    onClick: InteractionListener,
  ) {}

  override fun Modifier.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener?,
    onClick: InteractionListener,
  ): Modifier = this

  override fun Modifier.doOnDrag(
    uiTokenId: UiIdToken,
    offsetRatio: Float,
    fixedVelocity: Float?,
    onDragDirectionChanged: (Float) -> Unit,
    onDragCancelled: () -> Unit,
    onDragCompleted: InteractionListener,
  ): Modifier = this

  override fun doOnLoad(onLoad: (ResponseWithParcelables<DelegatedUiAdditionalData>?) -> Unit) {}

  override fun doOnInterop(uiTokenId: UiIdToken, onInteraction: InteractionListener) {}

  override fun doOnImpression(uiIdToken: UiIdToken, onImpression: InteractionListener) {}

  override suspend fun InteractionScope.sendEgressData(
    egressDataBuilder: DelegatedUiEgressDataKt.Dsl.() -> Unit
  ) {}

  override suspend fun InteractionScope.executeAction(action: () -> Action) {}

  override suspend fun InteractionScope.showDataAttribution(
    attributionDialogData: AttributionDialogData,
    attributionChipData: AttributionChipData?,
    sourceDeepLinks: Array<PendingIntent?>?,
  ) {}

  override suspend fun InteractionScope.closeSession() {}

  override suspend fun InteractionScope.logUsage() {}

  override suspend fun InteractionScope.showEntityFeedback(data: EntityFeedbackDialogData) {}

  override suspend fun InteractionScope.showMultiFeedback(data: MultiFeedbackDialogData) {}

  override suspend fun InteractionScope.dismissSuggestion() {}
}
