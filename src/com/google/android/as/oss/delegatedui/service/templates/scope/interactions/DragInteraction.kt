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

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_DRAG
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionHelper
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionListener
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlinx.coroutines.launch

/** An interaction that represents a user drag. */
interface DragInteraction {

  /**
   * Invokes [onDragCancelled] or [onDragCompleted] on drag.
   *
   * @param uiTokenId the token id of the ui.
   * @param offsetRatio determines whether the drag is complemented or cancelled when a drag stops.
   *   If it's bigger than the stop offset ratio, then the drag is complemented. Otherwise it's
   *   cancelled.
   * @param fixedVelocity determines the velocity of the UI slide speed when drag either cancels or
   *   completes if it's not null. Otherwise the velocity will be inherited from the framework.
   * @param dragDirection -1.0 means dragging right, 1.0 means dragging left and 0.0 means original
   *   position.
   * @param onDragDirectionChanged the callback when the drag direction changed.
   * @param onDragCancelled the callback when a drag is cancelled.
   * @param onDragCompleted the callback when a drag is completed.
   */
  fun Modifier.doOnDrag(
    uiTokenId: UiIdToken,
    offsetRatio: Float = 0.45f,
    fixedVelocity: Float? = null,
    onDragDirectionChanged: (Float) -> Unit = {},
    onDragCancelled: () -> Unit = {},
    onDragCompleted: InteractionListener,
  ): Modifier
}

/** Implementation for [DragInteraction]. */
class DragInteractionImpl(private val helper: InteractionHelper) : DragInteraction {

  override fun Modifier.doOnDrag(
    uiTokenId: UiIdToken,
    offsetRatio: Float,
    fixedVelocity: Float?,
    onDragDirectionChanged: (Float) -> Unit,
    onDragCancelled: () -> Unit,
    onDragCompleted: InteractionListener,
  ): Modifier =
    with(helper) {
      composed {
        val coroutineScope = rememberCoroutineScope()
        val offsetX = remember { Animatable(0f) }
        var maxWidth by remember { mutableIntStateOf(0) }
        var dragDirection by remember { mutableFloatStateOf(0f) }

        offset { IntOffset(offsetX.value.roundToInt(), 0) }
          .onGloballyPositioned { coordinates -> maxWidth = coordinates.size.width }
          .draggable(
            orientation = Orientation.Horizontal,
            state =
              rememberDraggableState { delta ->
                coroutineScope.launch {
                  offsetX.snapTo(offsetX.value + delta)
                  val currentDragDirection = sign(offsetX.value)
                  if (dragDirection != currentDragDirection) {
                    onDragDirectionChanged(currentDragDirection)
                  }
                }
              },
            onDragStopped = { velocity ->
              val stoppedOffsetRatio = abs(offsetX.value / maxWidth)
              coroutineScope.launch {
                if (stoppedOffsetRatio <= offsetRatio) {
                  offsetX.animateTo(targetValue = 0f, initialVelocity = fixedVelocity ?: velocity)
                  onDragCancelled()
                } else {
                  // Ensure the whole composable slide out of the screen.
                  var targetValue = sign(offsetX.value) * maxWidth
                  offsetX.animateTo(
                    targetValue = targetValue,
                    initialVelocity = fixedVelocity ?: velocity,
                  )
                  onInteraction(
                    uiTokenId,
                    interaction = INTERACTION_TYPE_DRAG,
                    block = onDragCompleted,
                  )
                }
              }
            },
          )
      }
    }
}
