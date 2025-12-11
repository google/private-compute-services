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

import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiHint
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData

/**
 * Receiver scope on which operations for
 * [side-effects][com.google.android.as.oss.delegatedui.service.templaterenderscope.sideeffects] can
 * be invoked.
 */
sealed interface SideEffectScope {

  /**
   * Sends the given
   * [com.google.android.as.oss.delegatedui.api.integration.egress.DelegatedUiEgressData] to the
   * local client.
   */
  val onDataEgress: suspend (DelegatedUiEgressData) -> Unit

  /** Sends the given set of [DelegatedUiHint] to the local client. */
  val onSendHints: suspend (Set<DelegatedUiHint>) -> Unit

  /** Closes the delegated UI session. */
  val onSessionClose: () -> Unit
}

internal class SideEffectScopeImpl(
  override val onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
  override val onSendHints: suspend (Set<DelegatedUiHint>) -> Unit,
  override val onSessionClose: () -> Unit,
) : SideEffectScope
