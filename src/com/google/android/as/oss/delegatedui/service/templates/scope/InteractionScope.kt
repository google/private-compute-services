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

import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope

/** Receiver scope for invoking side-effects, allowing for concurrent decomposition of work. */
interface InteractionCoroutineScope : InteractionScope, CoroutineScope

internal class InteractionCoroutineScopeImpl(
  val interactionScope: InteractionScopeImpl,
  val coroutineScope: CoroutineScope,
) :
  InteractionCoroutineScope, InteractionScope by interactionScope, CoroutineScope by coroutineScope

/** Similar to [coroutineScope], but allows for [block] to invoke side-effects. */
internal suspend fun <T> interactionCoroutineScope(
  interactionScope: InteractionScopeImpl,
  block: suspend InteractionCoroutineScope.() -> T,
): T {
  return coroutineScope { InteractionCoroutineScopeImpl(interactionScope, this).block() }
}

/**
 * Receiver scope on which
 * [side-effects][com.google.android.as.oss.delegatedui.service.templates.scope.sideeffects] can be
 * invoked.
 */
sealed interface InteractionScope {

  /** The interaction type that was triggered. */
  val interactionType: InteractionType

  /** The UI ID token that was triggered. */
  val interactionUiIdToken: UiIdToken
}

internal class InteractionScopeImpl(
  override val interactionType: InteractionType,
  override val interactionUiIdToken: UiIdToken,
) : InteractionScope {

  private var valid = true

  fun validate() {
    check(valid)
  }

  fun dispose() {
    valid = false
  }
}
