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
import com.google.android.`as`.oss.delegatedui.service.common.ConnectLifecycle
import com.google.common.flogger.GoogleLogger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Helper class for implementing interactions. Accept a `helper` in your constructor, and define
 * your function as `= with(helper) { ... }`.
 */
interface InteractionHelper {

  /** Handles the [interaction] with the given [block]. */
  fun InteractionHelper.onInteraction(
    uiIdToken: UiIdToken,
    interaction: InteractionType,
    block: InteractionListener,
  )
}

class InteractionHelperImpl(
  private val lifecycle: ConnectLifecycle,
  private val mainCoroutineContext: CoroutineContext,
) : InteractionHelper {

  override fun InteractionHelper.onInteraction(
    uiIdToken: UiIdToken,
    interaction: InteractionType,
    block: InteractionListener,
  ) {
    logger.atFiner().log("Handling interaction %s.", interaction)

    lifecycle.streamScope.launch {
      withContext(mainCoroutineContext) {
        InteractionScopeImpl(interaction, uiIdToken).apply {
          try {
            interactionCoroutineScope(this) { block() }
          } catch (e: CancellationException) {
            logger
              .atWarning()
              .withCause(e)
              .log("Cancelling interaction %s, skipping the remaining side-effects.", interaction)
            throw e
          } catch (e: Throwable) {
            logger.atSevere().withCause(e).log("Error while handling interaction %s.", interaction)
          } finally {
            dispose()
          }
        }
      }
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
