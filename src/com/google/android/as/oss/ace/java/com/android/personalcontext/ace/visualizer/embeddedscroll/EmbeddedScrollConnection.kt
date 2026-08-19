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

package com.android.personalcontext.ace.visualizer.embeddedscroll

import android.util.Log
import android.view.View
import androidx.compose.foundation.gestures.draggable2D
import androidx.compose.foundation.gestures.rememberDraggable2DState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.core.view.ViewCompat.SCROLL_AXIS_HORIZONTAL
import androidx.core.view.ViewCompat.SCROLL_AXIS_NONE
import androidx.core.view.ViewCompat.SCROLL_AXIS_VERTICAL
import androidx.core.view.ViewCompat.ScrollAxis
import com.android.personalcontext.ace.common.EmbeddedScrollEvent
import com.android.personalcontext.ace.common.EmbeddedScrollEventType.SCROLL_DELTA
import com.android.personalcontext.ace.common.EmbeddedScrollEventType.SCROLL_START
import com.android.personalcontext.ace.common.EmbeddedScrollEventType.SCROLL_STOP
import com.android.personalcontext.ace.visualizer.embeddedscroll.LockedAxis.Horizontal
import com.android.personalcontext.ace.visualizer.embeddedscroll.LockedAxis.Undecided
import com.android.personalcontext.ace.visualizer.embeddedscroll.LockedAxis.Vertical
import kotlin.math.abs

/**
 * Attaches a scroll listener that detects both nested scrolls (from children) and direct drag
 * gestures.
 *
 * It enforces axis locking: once a gesture starts in a specific direction (H or V), movement is
 * locked to that axis until the gesture ends (Stop/Fling).
 *
 * @param availableAxes Bitmask of axes to detect (e.g., SCROLL_AXIS_HORIZONTAL |
 *   SCROLL_AXIS_VERTICAL). Defaults to allowing both.
 * @param onScrollEvent Callback invoked with [EmbeddedScrollEvent] events (START, DELTA, STOP).
 */
@Composable
fun Modifier.embeddedScroll(
  @ScrollAxis availableAxes: Int = SCROLL_AXIS_VERTICAL or SCROLL_AXIS_HORIZONTAL,
  onScrollEvent: (EmbeddedScrollEvent) -> Unit,
): Modifier {
  return this.nestedScroll(rememberEmbeddedScrollConnection(availableAxes, onScrollEvent))
    .draggableScroll(availableAxes, onScrollEvent)
}

/**
 * Creates a NestedScrollConnection that monitors unconsumed scroll events (overscroll) from
 * descendants.
 */
@Composable
private fun rememberEmbeddedScrollConnection(
  @ScrollAxis availableAxes: Int,
  onScrollEvent: (EmbeddedScrollEvent) -> Unit,
): NestedScrollConnection {
  return remember {
    object : NestedScrollConnection {

      private var lockedAxis = Undecided
      private var reportedAxis: LockedAxis? = null
      private var reportedStop = false

      override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        Log.v(TAG, "onPreScroll(available = $available)")
        return super.onPreScroll(available, source)
      }

      override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource,
      ): Offset {
        Log.v(TAG, "onPostScroll(consumed = $consumed, available = $available)")

        if (lockedAxis == Undecided) {
          lockedAxis = available.getPrimaryAxis(availableAxes)

          if (reportedAxis != lockedAxis) {
            onScrollEvent(EmbeddedScrollEvent(type = SCROLL_START, axes = lockedAxis.axis))
            reportedAxis = lockedAxis
          }
        }

        if (source != NestedScrollSource.UserInput) {
          return Offset.Zero
        }

        val delta = available.clampToAxis(lockedAxis)
        if (!delta.isEmpty()) {
          onScrollEvent(EmbeddedScrollEvent(type = SCROLL_DELTA, x = delta.x, y = delta.y))
        }

        return delta
      }

      override suspend fun onPreFling(available: Velocity): Velocity {
        Log.v(TAG, "onPreFling(available = $available)")

        val velocity = available.clampToAxis(lockedAxis)
        if (!velocity.isEmpty()) {
          onScrollEvent(EmbeddedScrollEvent(type = SCROLL_STOP, x = velocity.x, y = velocity.y))

          lockedAxis = Undecided
          reportedAxis = null
          reportedStop = true
        }
        return velocity
      }

      override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        Log.v(TAG, "onPostFling(consumed = $consumed, available = $available)")

        if (reportedStop) {
          reportedStop = false
          return Velocity.Zero
        }

        val velocity = available.clampToAxis(lockedAxis)
        onScrollEvent(EmbeddedScrollEvent(type = SCROLL_STOP, x = velocity.x, y = velocity.y))

        lockedAxis = Undecided
        reportedAxis = null
        reportedStop = false

        return velocity
      }
    }
  }
}

/** A modifier that detects 2D drag gestures directly on the layout node. */
@Composable
private fun Modifier.draggableScroll(
  @ScrollAxis availableAxes: Int,
  onScrollEvent: (EmbeddedScrollEvent) -> Unit,
): Modifier {
  var lockedAxis by remember { mutableStateOf(Undecided) }
  var reportedAxis by remember { mutableStateOf<LockedAxis?>(null) }

  return draggable2D(
    state =
      rememberDraggable2DState { delta ->
        Log.v(TAG, "onDelta(delta = $delta)")

        if (lockedAxis == Undecided) {
          lockedAxis = delta.getPrimaryAxis(availableAxes)

          if (reportedAxis != lockedAxis) {
            onScrollEvent(EmbeddedScrollEvent(type = SCROLL_START, axes = lockedAxis.axis))
            reportedAxis = lockedAxis
          }
        }

        val delta = delta.clampToAxis(lockedAxis)
        if (!delta.isEmpty()) {
          onScrollEvent(EmbeddedScrollEvent(type = SCROLL_DELTA, x = delta.x, y = delta.y))
        }
      },
    onDragStopped = { velocity ->
      Log.v(TAG, "onDragStopped(velocity = $velocity)")
      val velocity = velocity.clampToAxis(lockedAxis)
      onScrollEvent(EmbeddedScrollEvent(type = SCROLL_STOP, x = velocity.x, y = velocity.y))

      lockedAxis = Undecided
      reportedAxis = null
    },
  )
}

private enum class LockedAxis(@property:ScrollAxis val axis: Int) {
  Undecided(SCROLL_AXIS_NONE),
  Vertical(SCROLL_AXIS_VERTICAL),
  Horizontal(SCROLL_AXIS_HORIZONTAL),
}

private fun Int.hasFlag(flag: Int): Boolean = (this and flag) != 0

private fun Offset.getPrimaryAxis(@ScrollAxis availableAxes: Int): LockedAxis {
  val canScrollHorizontally = availableAxes.hasFlag(View.SCROLL_AXIS_HORIZONTAL)
  val canScrollVertically = availableAxes.hasFlag(View.SCROLL_AXIS_VERTICAL)

  return when {
    abs(x) > abs(y) && canScrollHorizontally -> Horizontal
    abs(y) > abs(x) && canScrollVertically -> Vertical
    y != 0f && canScrollVertically -> Vertical
    x != 0f && canScrollHorizontally -> Horizontal
    else -> Undecided
  }
}

private fun Offset.clampToAxis(lockState: LockedAxis): Offset =
  when (lockState) {
    Horizontal -> copy(y = 0f)
    Vertical -> copy(x = 0f)
    Undecided -> Offset.Zero
  }

private fun Velocity.clampToAxis(lockState: LockedAxis): Velocity =
  when (lockState) {
    Horizontal -> copy(y = 0f)
    Vertical -> copy(x = 0f)
    Undecided -> Velocity.Zero
  }

private fun Offset.isEmpty(): Boolean = x == 0f && y == 0f

private fun Velocity.isEmpty(): Boolean = x == 0f && y == 0f

private const val TAG = "EmbeddedScrollConnection"
