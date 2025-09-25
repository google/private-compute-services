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

import android.app.ActivityOptions
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Context
import android.content.Intent
import com.android.window.flags.ExportedFlags.balAdditionalStartModes
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_EXECUTE_ACTION
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper
import com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects.ExecuteActionSideEffect.Action
import com.google.common.flogger.GoogleLogger

/** A side-effect that executes an action, possibly sending data out of PCC. */
interface ExecuteActionSideEffect {

  /** Executes the given [action], which may send data out of PCC. */
  suspend fun InteractionScope.executeAction(action: () -> Action)

  /** An executable action. */
  sealed interface Action

  /**
   * Converts a [PendingIntent] to an [Action].
   *
   * Parameters will be passed into [PendingIntent.send]. You only need to provide these if you were
   * planning to provide these to [PendingIntent.send] in the first place.
   */
  fun PendingIntent.toAction(
    code: Int = 0,
    fillInIntent: Intent? = null,
    options: ActivityOptions = ActivityOptions.makeBasic(),
  ): Action = PendingIntentActionInternal(this, code, fillInIntent, options)

  /**
   * Converts a [RemoteAction] to an [Action].
   *
   * Parameters will be passed into [PendingIntent.send]. You only need to provide these if you were
   * planning to provide these to [PendingIntent.send] in the first place.
   */
  fun RemoteAction.toAction(
    code: Int = 0,
    fillInIntent: Intent? = null,
    options: ActivityOptions = ActivityOptions.makeBasic(),
  ): Action = PendingIntentActionInternal(actionIntent, code, fillInIntent, options)
}

internal sealed interface ActionInternal : Action {

  /** Execute this action. */
  fun execute(context: Context)
}

private class PendingIntentActionInternal(
  private val pendingIntent: PendingIntent,
  private val code: Int,
  private val fillInIntent: Intent?,
  private val options: ActivityOptions,
) : ActionInternal {

  override fun execute(context: Context) {
    pendingIntent.sendWithBackgroundActivityStartAllowed(context)
  }

  /**
   * Uses our sender privileges to execute this [PendingIntent] action. Relies on this service being
   * bound with [io.grpc.binder.BindServiceFlags.Builder.setAllowActivityStarts].
   */
  private fun PendingIntent.sendWithBackgroundActivityStartAllowed(context: Context) =
    send(
      /* context = */ context,
      /* code = */ code,
      /* intent = */ fillInIntent,
      /* onFinished = */ null,
      /* handler = */ null,
      /* requiredPermission = */ null,
      /* options = */ options
        .apply {
          if (balAdditionalStartModes()) {
            setPendingIntentBackgroundActivityStartMode(
              // TODO: Update to MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
              ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            )
          }
        }
        .toBundle(),
    )

  override fun toString(): String = "PendingIntentAction($pendingIntent)"
}

class ExecuteActionSideEffectImpl(private val helper: SideEffectHelper) : ExecuteActionSideEffect {

  override suspend fun InteractionScope.executeAction(action: () -> Action) =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_EXECUTE_ACTION) {
        val value = action() as ActionInternal
        logger.atFiner().log("Executing action: %s", value)
        value.execute(context)
      }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
