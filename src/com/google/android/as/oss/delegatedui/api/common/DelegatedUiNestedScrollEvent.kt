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

package com.google.android.`as`.oss.delegatedui.api.common

import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.ScrollAxis

/** Nested scrolling event. */
sealed interface DelegatedUiNestedScrollEvent {

  /** Nested scrolling control signal: start. */
  data class NestedScrollStartEvent(@ScrollAxis val axes: Int) : DelegatedUiNestedScrollEvent {
    override fun toString() = "NestedScrollStartEvent(axes=${axes.toScrollAxesString()})"
  }

  /** Nested scrolling delta. */
  data class NestedScrollDelta(val scrollX: Float, val scrollY: Float) :
    DelegatedUiNestedScrollEvent

  /** Nested scrolling control signal: stop. May include nested fling velocity. */
  data class NestedScrollStopEvent(val flingX: Float, val flingY: Float) :
    DelegatedUiNestedScrollEvent
}

@ScrollAxis
private fun Int.toScrollAxesString(): String {
  val axes = this

  if (axes == ViewCompat.SCROLL_AXIS_NONE) {
    return "[NONE]"
  }

  return buildList {
      if ((axes and ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0) add("HORIZONTAL")
      if ((axes and ViewCompat.SCROLL_AXIS_VERTICAL) != 0) add("VERTICAL")
    }
    .joinToString(", ", prefix = "[", postfix = "]")
}
