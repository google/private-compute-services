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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat.NestedScrollType
import androidx.core.view.ViewCompat.ScrollAxis
import androidx.core.view.ViewCompat.TYPE_NON_TOUCH
import androidx.core.view.ViewCompat.TYPE_TOUCH
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollDelta
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollStartEvent
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollStopEvent
import com.google.common.flogger.GoogleLogger
import com.google.errorprone.annotations.CanIgnoreReturnValue
import kotlin.math.abs

/**
 * Wraps the view created by a template.
 *
 * Supports the following behaviors:
 * * On layout changes, notifies the local client of the updated remote UI size via the
 *   `onSizeChange` callback on `DelegatedUiProvider`.
 * * On scroll gestures, notifies the local client of the deltas via the `onNestedScroll` and
 *   `onNestedFling` callbacks on `DelegatedUiProvider`.
 * * Catches exceptions thrown during measure, layout, and draw. Reports it to [onRenderError].
 */
@SuppressLint("ViewConstructor")
internal class DelegatedUiViewParent(
  context: Context,
  private val onScrollEvent: (DelegatedUiNestedScrollEvent) -> Unit,
  private val onRenderError: (Exception) -> Unit,
) : FrameLayout(context), NestedScrollingParent3 {

  /** The nested scroll axes supported by the client. */
  @ScrollAxis var clientNestedScrollAxes: Int = SCROLL_AXIS_HORIZONTAL or SCROLL_AXIS_VERTICAL

  /**
   * Whether the DUI session should report a specific axis when a nested scroll gesture is detected,
   * and whether that axis should be locked such that subsequent nested scroll events are only
   * reported for that axis.
   */
  var clientNestedScrollAxisLock: Boolean = true

  // region Size changes
  // -----------------------------------------------------------------------------------------------

  internal var onRequestLayoutListener: (() -> Unit)? = null
  private val onRequestLayoutRunnable: Runnable = Runnable { onRequestLayoutListener?.invoke() }

  override fun requestLayout() {
    removeCallbacks(onRequestLayoutRunnable)
    if (onRequestLayoutListener != null) {
      post(onRequestLayoutRunnable)
    }

    super.requestLayout()
  }

  fun onDestroy() {
    onRequestLayoutListener = null
    removeCallbacks(onRequestLayoutRunnable)
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  // region Touch handling
  // -----------------------------------------------------------------------------------------------
  /**
   * Parent touch slop must be greater than the normal amount, to give scrolling children an
   * opportunity to [requestDisallowInterceptTouchEvent]. This is especially important when the
   * scroll child and this view parent want to scroll in the same axes.
   */
  private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop * 4

  private var isScrollingIntercept = false

  private val gestureDetector =
    GestureDetector(
      context,
      object : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
          logger.atFiner().log("GestureDetector.onDown: %f, %f", e.x, e.y)

          reportGestureStart(SCROLL_AXIS_NONE) // From GestureDetector.onDown()
          return true
        }

        override fun onScroll(
          downEvent: MotionEvent?,
          moveEvent: MotionEvent,
          deltaX: Float,
          deltaY: Float,
        ): Boolean {
          logger.atFiner().log("GestureDetector.onScroll: %f, %f", deltaX, deltaY)

          if (downEvent == null) return true

          if (!isScrollingIntercept) {
            val distanceX = abs(downEvent.rawX - moveEvent.rawX)
            val distanceY = abs(downEvent.rawY - moveEvent.rawY)

            val isHorizontallyScrolling =
              distanceX > touchSlop && clientNestedScrollAxes.hasFlag(SCROLL_AXIS_HORIZONTAL)
            val isVerticallyScrolling =
              distanceY > touchSlop && clientNestedScrollAxes.hasFlag(SCROLL_AXIS_VERTICAL)

            if (isHorizontallyScrolling || isVerticallyScrolling) {
              isScrollingIntercept = true
              reportScrollDelta(deltaX, deltaY) // From GestureDetector.onScroll()
            }
          } else {
            reportScrollDelta(deltaX, deltaY) // From GestureDetector.onScroll()
          }
          return true
        }

        override fun onFling(
          downEvent: MotionEvent?,
          moveEvent: MotionEvent,
          flingX: Float,
          flingY: Float,
        ): Boolean {
          logger.atFiner().log("GestureDetector.onFling: %f, %f", -flingX, -flingY)

          return reportGestureEnd(-flingX, -flingY) // From GestureDetector.onFling()
        }
      },
    )

  override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    logger.atFiner().log("requestDisallowInterceptTouchEvent: %b", disallowIntercept)

    super.requestDisallowInterceptTouchEvent(disallowIntercept)
  }

  override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
    event.withDisplayCoordinates { gestureDetector.onTouchEvent(it) }

    when (event.actionMasked) {
      MotionEvent.ACTION_MOVE -> {
        if (isScrollingIntercept) {
          logger.atFiner().log("onInterceptTouchEvent(ACTION_MOVE): true")

          parent?.requestDisallowInterceptTouchEvent(true)
          return true
        }
      }
      MotionEvent.ACTION_UP,
      MotionEvent.ACTION_CANCEL -> {
        logger.atFiner().log("onInterceptTouchEvent(ACTION_UP | ACTION_CANCEL)")
        reportGestureEnd() // From View.onInterceptTouchEvent(ACTION_UP || ACTION_CANCEL)

        isScrollingIntercept = false
      }
    }

    return false
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val wasScrolling = isScrollingIntercept
    val consumed = event.withDisplayCoordinates { gestureDetector.onTouchEvent(it) }

    when (event.actionMasked) {
      MotionEvent.ACTION_MOVE -> {
        if (isScrollingIntercept) {
          parent?.requestDisallowInterceptTouchEvent(true)
        }
      }
      MotionEvent.ACTION_UP,
      MotionEvent.ACTION_CANCEL -> {
        logger.atFiner().log("onTouchEvent(ACTION_UP | ACTION_CANCEL)")
        reportGestureEnd() // From View.onTouchEvent(ACTION_UP || ACTION_CANCEL)

        parent?.requestDisallowInterceptTouchEvent(false)
        isScrollingIntercept = false
      }
    }

    return consumed || wasScrolling
  }

  /**
   * Returns a copy of the [MotionEvent] with coordinates in the display's coordinate space instead
   * of in the view's coordinate space.
   */
  @CanIgnoreReturnValue
  private inline fun <T> MotionEvent.withDisplayCoordinates(block: (MotionEvent) -> T): T {
    val event =
      MotionEvent.obtain(
        /* downTime = */ downTime,
        /* eventTime = */ eventTime,
        /* action = */ action,
        /* x = */ rawX,
        /* y = */ rawY,
        /* metaState = */ 0,
      )
    val result = block(event)
    event.recycle()
    return result
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  // region Nested scrolling
  // -----------------------------------------------------------------------------------------------

  private val parentHelper = NestedScrollingParentHelper(this)
  private val maximumFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

  init {
    isNestedScrollingEnabled = true
  }

  override fun onStartNestedScroll(
    child: View,
    target: View,
    @ScrollAxis axes: Int,
    @NestedScrollType type: Int,
  ): Boolean {
    logger
      .atFiner()
      .log("onStartNestedScroll(%s): %s", type.toScrollTypeString(), axes.toScrollAxesString())

    // Accept only TYPE_TOUCH nested scroll events from our children.
    return clientNestedScrollAxes.hasFlag(axes) && type == TYPE_TOUCH
  }

  override fun onNestedPreScroll(
    target: View,
    dx: Int,
    dy: Int,
    consumed: IntArray,
    @NestedScrollType type: Int,
  ) {
    return super.onNestedPreScroll(target, dx, dy, consumed)
  }

  override fun onNestedScroll(
    target: View,
    dxConsumed: Int,
    dyConsumed: Int,
    dxUnconsumed: Int,
    dyUnconsumed: Int,
    @NestedScrollType type: Int,
  ) {
    val deltaX = dxUnconsumed.toFloat()
    val deltaY = dyUnconsumed.toFloat()
    logger.atFiner().log("onNestedScroll(%s): %f, %f", type.toScrollTypeString(), deltaX, deltaY)

    val velocityTracker = obtainVelocityTracker()
    velocityTracker.addScrollDelta(deltaX, deltaY)

    reportScrollDelta(deltaX, deltaY) // From NestedScrollingParent2/3.onNestedScroll()
  }

  override fun onNestedScroll(
    target: View,
    dxConsumed: Int,
    dyConsumed: Int,
    dxUnconsumed: Int,
    dyUnconsumed: Int,
    @NestedScrollType type: Int,
    consumed: IntArray,
  ) {
    onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)

    consumed[0] = dxUnconsumed
    consumed[1] = dyUnconsumed
  }

  override fun onNestedPreFling(target: View, velocityX: Float, velocityY: Float): Boolean {
    val velocityTracker = obtainVelocityTracker()
    velocityTracker.computeCurrentVelocity(1000, maximumFlingVelocity.toFloat())
    val flingX = velocityTracker.xVelocity
    val flingY = velocityTracker.yVelocity
    recycleVelocityTracker()
    logger.atFiner().log("onNestedFling (synthetic): %f, %f", flingX, flingY)

    reportGestureEnd(flingX, flingY) // From NestedScrollingParent.onNestedPreFling()

    return super.onNestedPreFling(target, velocityX, velocityY)
  }

  override fun onNestedFling(
    target: View,
    flingX: Float,
    flingY: Float,
    consumed: Boolean,
  ): Boolean {
    logger.atFiner().log("onNestedFling (actual): %f, %f", flingX, flingY)

    return reportGestureEnd(flingX, flingY) // From NestedScrollingParent.onNestedFling()
  }

  @ScrollAxis
  private fun Int.toScrollAxesString(): String {
    val axes = this

    if (axes == SCROLL_AXIS_NONE) {
      return "[NONE]"
    }

    return buildList {
        if (axes.hasFlag(SCROLL_AXIS_HORIZONTAL)) add("HORIZONTAL")
        if (axes.hasFlag(SCROLL_AXIS_VERTICAL)) add("VERTICAL")
      }
      .joinToString(", ", prefix = "[", postfix = "]")
  }

  @NestedScrollType
  private fun Int.toScrollTypeString() =
    when (this) {
      TYPE_TOUCH -> "TOUCH"
      TYPE_NON_TOUCH -> "NON-TOUCH"
      else -> "UNKNOWN"
    }

  private var _velocityTracker: ScrollVelocityTracker? = null

  private fun obtainVelocityTracker(): ScrollVelocityTracker {
    return _velocityTracker ?: ScrollVelocityTracker.obtain().also { _velocityTracker = it }
  }

  private fun recycleVelocityTracker() {
    _velocityTracker?.recycle()
    _velocityTracker = null
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  // region NestedScrollParent3
  // -----------------------------------------------------------------------------------------------

  override fun onNestedScrollAccepted(child: View, target: View, @ScrollAxis axes: Int) {
    parentHelper.onNestedScrollAccepted(child, target, axes)

    reportGestureStart(axes) // From NestedScrollingParent.onNestedScrollAccepted()
  }

  override fun onNestedScrollAccepted(
    child: View,
    target: View,
    @ScrollAxis axes: Int,
    @NestedScrollType type: Int,
  ) {
    parentHelper.onNestedScrollAccepted(child, target, axes, type)
  }

  override fun getNestedScrollAxes(): Int {
    return parentHelper.nestedScrollAxes
  }

  override fun onStopNestedScroll(child: View) {
    parentHelper.onStopNestedScroll(child)
  }

  override fun onStopNestedScroll(target: View, @NestedScrollType type: Int) {
    parentHelper.onStopNestedScroll(target, type)
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  // region Scroll and fling reporting
  // -----------------------------------------------------------------------------------------------

  @ScrollAxis private var nestedScrollAxesReported: Int? = null

  @CanIgnoreReturnValue
  private fun reportGestureStart(@ScrollAxis axes: Int): Boolean {
    // If a valid nested scroll axes was already reported, don't need to do it again.
    if (nestedScrollAxesReported != null && nestedScrollAxesReported != SCROLL_AXIS_NONE) {
      return false
    }

    val eventAxes = clientNestedScrollAxes and axes
    // Don't report the same nested scroll axes over and over again.
    if (nestedScrollAxesReported == eventAxes) {
      return false
    }

    logger
      .atFiner()
      .log("reportGestureStart(%s): %s", axes.toScrollAxesString(), eventAxes.toScrollAxesString())
    onScrollEvent(NestedScrollStartEvent(eventAxes))

    nestedScrollAxesReported = eventAxes
    return true
  }

  private fun reportScrollDelta(scrollX: Float, scrollY: Float) {
    reportGestureStart( // From reportScrollDelta()
      when (clientNestedScrollAxisLock) {
        true -> getPrimaryScrollAxis(scrollX, scrollY)
        else -> getNonZeroScrollAxes(scrollX, scrollY)
      }
    )

    val eventScrollX = if (nestedScrollAxesReported.hasFlag(SCROLL_AXIS_HORIZONTAL)) scrollX else 0f
    val eventScrollY = if (nestedScrollAxesReported.hasFlag(SCROLL_AXIS_VERTICAL)) scrollY else 0f

    if (eventScrollX == 0f && eventScrollY == 0f) return

    logger
      .atFiner()
      .log(
        "reportScrollDelta(%s): %f, %f",
        nestedScrollAxesReported?.toScrollAxesString(),
        eventScrollX,
        eventScrollY,
      )
    onScrollEvent(NestedScrollDelta(eventScrollX, eventScrollY))
  }

  @CanIgnoreReturnValue
  private fun reportGestureEnd(flingX: Float = 0f, flingY: Float = 0f): Boolean {
    // If no nested scroll axes were reported, don't need to report gesture end.
    if (nestedScrollAxesReported == null) {
      return false
    }

    val eventFlingX = if (nestedScrollAxesReported.hasFlag(SCROLL_AXIS_HORIZONTAL)) flingX else 0f
    val eventFlingY = if (nestedScrollAxesReported.hasFlag(SCROLL_AXIS_VERTICAL)) flingY else 0f

    logger
      .atFiner()
      .log(
        "reportGestureEnd(%s): %f, %f",
        nestedScrollAxesReported?.toScrollAxesString(),
        eventFlingX,
        eventFlingY,
      )
    onScrollEvent(NestedScrollStopEvent(eventFlingX, eventFlingY))

    nestedScrollAxesReported = null
    return true
  }

  @ScrollAxis
  private fun getNonZeroScrollAxes(x: Float, y: Float): Int {
    var axes = SCROLL_AXIS_NONE
    if (x != 0f) axes = axes or SCROLL_AXIS_HORIZONTAL
    if (y != 0f) axes = axes or SCROLL_AXIS_VERTICAL
    return axes
  }

  /**
   * Determines the single dominant scroll axis based on magnitude. In case of a tie, vertical is
   * prioritized.
   */
  @ScrollAxis
  private fun getPrimaryScrollAxis(x: Float, y: Float): Int {
    val canScrollHorizontally = clientNestedScrollAxes.hasFlag(SCROLL_AXIS_HORIZONTAL)
    val canScrollVertically = clientNestedScrollAxes.hasFlag(SCROLL_AXIS_VERTICAL)

    return when {
      abs(x) > abs(y) && canScrollHorizontally -> SCROLL_AXIS_HORIZONTAL
      abs(y) > abs(x) && canScrollVertically -> SCROLL_AXIS_VERTICAL
      y != 0f && canScrollVertically -> SCROLL_AXIS_VERTICAL
      x != 0f && canScrollHorizontally -> SCROLL_AXIS_HORIZONTAL
      else -> SCROLL_AXIS_NONE
    }
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  // region Exception handling
  // -----------------------------------------------------------------------------------------------

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    try {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    } catch (e: Exception) {
      setMeasuredDimension(0, 0)
      onRenderError(e)
    }
  }

  override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
    try {
      super.onLayout(changed, left, top, right, bottom)
    } catch (e: Exception) {
      onRenderError(e)
    }
  }

  override fun dispatchDraw(canvas: Canvas) {
    try {
      super.dispatchDraw(canvas)
    } catch (e: Exception) {
      onRenderError(e)
    }
  }

  // -----------------------------------------------------------------------------------------------
  // endregion

  private fun Int?.hasFlag(flag: Int): Boolean =
    when {
      this == null -> false
      else -> (this and flag) != 0
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
