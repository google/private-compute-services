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

import android.view.Window
import androidx.lifecycle.ViewModel

/**
 * A [ViewModel] for the DataAttributionActivity.
 *
 * This class provides a single point of access to the [DialogAnchor] for determining the
 * appropriate gravity for a dialog.
 */
class DataAttributionActivityViewModel : ViewModel() {
  private val dialogAnchor = DialogAnchor()

  /**
   * Determines the appropriate gravity for a dialog, attempting to position it above the Input
   * Method Editor (IME) if applicable and visible in portrait mode.
   *
   * @param window The [Window] to use for determining the dialog gravity.
   * @return [DialogGravity] indicating the suggested placement.
   */
  fun getDialogGravity(window: Window) = dialogAnchor.getDialogGravity(window)
}
