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
import android.os.Bundle
import android.view.Display
import android.view.SurfaceControlViewHost
import android.window.InputTransferToken
import androidx.annotation.UiThread
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiRenderSpec
import com.google.android.`as`.oss.delegatedui.service.common.isExactly
import com.google.android.`as`.oss.delegatedui.service.common.size
import com.google.common.flogger.GoogleLogger

/**
 * This acts as the lifecycle component that hosts the delegated UI view hierarchy, since it is not
 * part of a standard Android framework component (ie: Activity). Internally, this class is a
 * wrapper around a [SurfaceControlViewHost].
 *
 * This component essentially has two lifecycle states: active (which corresponds to
 * [Lifecycle.State.RESUMED]) and inactive (which corresponds to [Lifecycle.State.DESTROYED]). You
 * must manage the lifecycle of this component manually by calling [destroy] when the component is
 * no longer active.
 */
@SuppressLint("NewApi") // SurfaceControlViewHost requires API 30
@UiThread
class DelegatedUiViewRoot(
  val sessionUuid: String,
  context: Context,
  display: Display,
  token: InputTransferToken,
  private val onSizeChange: (Int, Int) -> Unit,
  onNestedScroll: (DelegatedUiNestedScrollEvent) -> Unit,
  onRenderError: (Exception) -> Unit,
) : SavedStateRegistryOwner, ViewModelStoreOwner, LifecycleOwner {

  private val host = SurfaceControlViewHost(context, display, token)

  private val savedStateRegistryController = SavedStateRegistryController.create(this)
  private val lifecycleRegistry = LifecycleRegistry(this)
  private val savedInstanceState: Bundle = Bundle()

  override val savedStateRegistry: SavedStateRegistry =
    savedStateRegistryController.savedStateRegistry
  override val viewModelStore: ViewModelStore = ViewModelStore()
  override val lifecycle: Lifecycle = lifecycleRegistry

  /** The surface package of the internal [SurfaceControlViewHost]. */
  val surfacePackage: SurfaceControlViewHost.SurfacePackage
    get() = checkNotNull(host.surfacePackage)

  /** The parent view that wraps the latest [setContentView]. */
  private val parent = DelegatedUiViewParent(context, onNestedScroll, onRenderError)

  /** The callback to update the view set by [setContentView]. */
  lateinit var updateInputSpec: suspend (DelegatedUiInputSpec) -> Unit
    private set

  /** The latest spec set by [setContentView]. */
  lateinit var spec: DelegatedUiRenderSpec
    private set

  init {
    savedStateRegistryController.performRestore(savedInstanceState)
    // STARTED is the minimum required state for core Compose features like LaunchedEffect or
    // recomposition to work.
    lifecycleRegistry.currentState = Lifecycle.State.STARTED

    parent.setViewTreeLifecycleOwner(this)
    parent.setViewTreeSavedStateRegistryOwner(this)
    parent.setViewTreeViewModelStoreOwner(this)

    host.setView(parent, 0, 0)
  }

  /**
   * Set the content of the delegated UI view hierarchy, similar to
   * [android.app.Activity.setContentView].
   * - If the given [renderResult] is a [RenderedView], then it will be measured using [spec].
   * - Attributes on [spec] will be applied.
   */
  fun setContentView(renderResult: RenderResult, spec: DelegatedUiRenderSpec) {
    if (renderResult is RenderedView) {
      doNotTriggerOnRequestLayoutListener {
        parent.removeAllViews()
        parent.addView(renderResult.view)
      }

      ViewCompat.setAccessibilityPaneTitle(parent, renderResult.accessibilityPaneTitle)
      this.updateInputSpec = renderResult.updateInputSpec
    }

    this.spec = spec
    parent.setBackgroundColor(spec.backgroundColor)
    parent.clientNestedScrollAxes = spec.clientNestedScrollAxes
    parent.clientNestedScrollAxisLock = spec.clientNestedScrollAxisLock

    onContentViewChanged()
  }

  private fun doNotTriggerOnRequestLayoutListener(block: () -> Unit) {
    val listener = parent.onRequestLayoutListener

    parent.onRequestLayoutListener = null
    block()
    parent.onRequestLayoutListener = listener
  }

  private fun onContentViewChanged() {
    val shouldMeasure = !spec.measureSpecWidth.isExactly() || !spec.measureSpecHeight.isExactly()
    if (shouldMeasure) {
      parent.onRequestLayoutListener = ::onRequestLayout
      parent.requestLayout()
    } else {
      host.relayout(spec.measureSpecWidth.size, spec.measureSpecHeight.size)
    }
  }

  private fun onRequestLayout() {
    if (lifecycleRegistry.currentState == Lifecycle.State.DESTROYED) {
      return
    }

    val prevMeasuredWidth = parent.measuredWidth
    val prevMeasuredHeight = parent.measuredHeight
    parent.measure(spec.measureSpecWidth, spec.measureSpecHeight)

    val newMeasuredWidth = parent.measuredWidth
    val newMeasuredHeight = parent.measuredHeight
    logger.atFiner().log("View measured: %d, %d", newMeasuredWidth, newMeasuredHeight)

    if (newMeasuredWidth != prevMeasuredWidth || newMeasuredHeight != prevMeasuredHeight) {
      onSizeChange(newMeasuredWidth, newMeasuredHeight)
      host.relayout(newMeasuredWidth, newMeasuredHeight)
    }
  }

  /** Called when the lifecycle of this component is no longer active. */
  fun destroy() {
    parent.onDestroy()
    host.release()

    lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
    savedStateRegistryController.performSave(savedInstanceState)
    viewModelStore.clear()
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
