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

package com.google.android.`as`.oss.dataattribution

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.Window
import android.view.WindowInsets
import android.view.WindowMetrics
import com.google.android.`as`.oss.dataattribution.DialogGravity.Center

/** The gravity of the dialog relative to the screen. */
sealed interface DialogGravity {

  /** The dialog is centered on the screen. */
  data object Center : DialogGravity

  /** The dialog is positioned above the IME. */
  data class AboveIme(val backgroundInsetBottomPx: Int) : DialogGravity
}

/**
 * A helper class for positioning a dialog relative to the ime.
 *
 * This class provides methods to determine the appropriate gravity for a dialog, taking into
 * account the current state of the IME (Input Method Editor) and screen orientation.
 */
class DialogAnchor {

  /**
   * Cached value of the dialog gravity for portrait mode.
   *
   * This is used to avoid recalculating the dialog gravity multiple times when the activity is
   * recreated.
   *
   * Note: After configuration change, the ime window metrics not updated immediately, and not found
   * a way to get the updated value, so we need to cache the first time result.
   */
  private var cachedDialogGravityForPortrait: DialogGravity? = null

  /**
   * Determines the appropriate gravity for a dialog, attempting to position it above the Input
   * Method Editor (IME) if applicable and visible in portrait mode.
   *
   * @param window The window to position the dialog relative to.
   * @return [DialogGravity] indicating the suggested placement.
   */
  fun getDialogGravity(window: Window): DialogGravity {
    // IME-aware positioning is typically desired only in portrait mode.
    if (!isScreenPortraitMode(window.context)) return Center

    return cachedDialogGravityForPortrait
      ?: calculateDialogGravity(window).also { cachedDialogGravityForPortrait = it }
  }

  private fun calculateDialogGravity(window: Window): DialogGravity {
    val windowMetrics = window.windowManager.currentWindowMetrics
    val currentInsets = windowMetrics.windowInsets

    // Check if the IME's reported state and position are suitable for anchoring.
    if (!isImeStateSuitableForAnchoring(windowMetrics, currentInsets)) return Center

    val imeBottomInset = currentInsets.getInsets(WindowInsets.Type.ime()).bottom
    // Only anchor above IME if it's actually visible and has a positive inset.
    // A zero inset means the IME isn't currently affecting the layout at the bottom.
    if (imeBottomInset > 0) return DialogGravity.AboveIme(backgroundInsetBottomPx = imeBottomInset)
    return Center // Fallback to center if IME inset is not positive.
  }

  /**
   * Checks if the IME's current state and reported position are suitable for anchoring a dialog
   * above it.
   *
   * On API 35 (VanillaIceCream) and above, this verifies that the IME has a valid bounding
   * rectangle that has a positive height and is contained within the window bounds. This provides a
   * more accurate assessment of the IME's spatial impact.
   *
   * On older APIs (below API 35), it falls back to checking if the IME is reported as visible via
   * [WindowInsets.isVisible].
   *
   * @param windowMetrics The current [WindowMetrics] for the window.
   * @param currentInsets The current [WindowInsets] for the window.
   * @return `true` if the IME state is suitable for anchoring, `false` otherwise.
   */
  private fun isImeStateSuitableForAnchoring(
    windowMetrics: WindowMetrics,
    currentInsets: WindowInsets,
  ): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      // For V+ (API 35+), get the IME bounding rectangles.
      // We expect a single, valid IME rectangle for standard keyboard behavior.
      val imeBoundingRect =
        currentInsets.getBoundingRects(WindowInsets.Type.ime()).singleOrNull()
          ?: return false // No IME bounding rect found or multiple (unexpected).

      // The IME is suitable if its rect has a positive height (is actually laid out)
      // and is fully contained within the current window bounds.
      imeBoundingRect.height() > 0 && windowMetrics.bounds.contains(imeBoundingRect)
    } else {
      // For older APIs, a simpler check: is the IME currently visible?
      currentInsets.isVisible(WindowInsets.Type.ime())
    }
  }

  /** Checks if the device screen is currently in portrait orientation. */
  private fun isScreenPortraitMode(context: Context): Boolean =
    context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}
