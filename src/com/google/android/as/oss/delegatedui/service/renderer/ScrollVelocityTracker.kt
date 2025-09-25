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

package com.google.android.`as`.oss.delegatedui.service.renderer

import android.os.SystemClock
import android.view.MotionEvent
import android.view.VelocityTracker

/**
 * Wrapper around [VelocityTracker] that accepts scroll deltas instead of motion events.
 *
 * This class will create a synthetic ACTION_MOVE [MotionEvent] for each scroll delta, as well as
 * synthetic ACTION_DOWN and ACTION_UP events.
 */
class ScrollVelocityTracker private constructor(private val velocityTracker: VelocityTracker) {

  private var _downTime: Long? = null
  private val downTime: Long
    get() = _downTime ?: (SystemClock.uptimeMillis() - 1.frame) // Purely for non-nullability.

  private var lastX = 0f
  private var lastY = 0f

  /**
   * Add a user's scroll delta to the tracker. You should call this for every scroll event that you
   * receive.
   */
  fun addScrollDelta(scrollX: Float, scrollY: Float) {
    if (_downTime == null) {
      _downTime = SystemClock.uptimeMillis() - 1.frame

      val event =
        MotionEvent.obtain(
          /* downTime = */ downTime,
          /* eventTime = */ downTime,
          /* action = */ MotionEvent.ACTION_DOWN,
          /* x = */ lastX,
          /* y = */ lastY,
          /* metaState = */ 0,
        )
      velocityTracker.addMovement(event)
      event.recycle()
    }

    lastX += scrollX
    lastY += scrollY

    val event =
      MotionEvent.obtain(
        /* downTime = */ downTime,
        /* eventTime = */ SystemClock.uptimeMillis(),
        /* action = */ MotionEvent.ACTION_MOVE,
        /* x = */ lastX,
        /* y = */ lastY,
        /* metaState = */ 0,
      )
    velocityTracker.addMovement(event)
    event.recycle()
  }

  /**
   * Compute the current velocity based on the points that have been collected. Only call this when
   * you actually want to retrieve velocity information, as it is relatively expensive. You can then
   * retrieve the velocity with {@link #getXVelocity()} and {@link #getYVelocity()}.
   *
   * @param units The units you would like the velocity in. A value of 1 provides units per
   *   millisecond, 1000 provides units per second, etc. Note that the units referred to here are
   *   the same units with which motion is reported. For axes X and Y, the units are pixels.
   * @param maxVelocity The maximum velocity that can be computed by this method. This value must be
   *   declared in the same unit as the units parameter. This value must be positive.
   */
  fun computeCurrentVelocity(units: Int, maxVelocity: Float) {
    if (_downTime != null) {
      val event =
        MotionEvent.obtain(
          /* downTime = */ downTime,
          /* eventTime = */ SystemClock.uptimeMillis(),
          /* action = */ MotionEvent.ACTION_UP,
          /* x = */ lastX,
          /* y = */ lastY,
          /* metaState = */ 0,
        )
      velocityTracker.addMovement(event)
      event.recycle()
    }

    velocityTracker.computeCurrentVelocity(units, maxVelocity)
  }

  /**
   * Retrieve the last computed X velocity. You must first call [computeCurrentVelocity] before
   * calling this function.
   *
   * @return The previously computed X velocity.
   */
  /**
   * Retrieve the last computed X velocity. You must first call [computeCurrentVelocity] before
   * calling this function.
   *
   * @return The previously computed X velocity.
   */
  val xVelocity: Float
    get() = velocityTracker.xVelocity

  /**
   * Retrieve the last computed Y velocity. You must first call [computeCurrentVelocity] before
   * calling this function.
   *
   * @return The previously computed Y velocity.
   */
  val yVelocity: Float
    get() = velocityTracker.yVelocity

  /**
   * Return a VelocityTracker object back to be re-used by others. You must not touch the object
   * after calling this function.
   */
  fun recycle() {
    velocityTracker.recycle()
    _downTime = null
    lastX = 0f
    lastY = 0f
  }

  /** Gets the number of milliseconds for this many frames. */
  private val Int.frame: Long
    get() = this * 16L

  companion object {

    /**
     * Retrieve a new VelocityTracker object to watch the velocity of a motion. Be sure to call
     * {@link #recycle} when done. You should generally only maintain an active object while
     * tracking a movement, so that the VelocityTracker can be re-used elsewhere.
     *
     * @return Returns a new VelocityTracker.
     */
    fun obtain(): ScrollVelocityTracker {
      return ScrollVelocityTracker(VelocityTracker.obtain())
    }
  }
}
