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

package com.google.android.`as`.oss.feedback.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import kotlin.math.max
import kotlin.math.min

/**
 * Scrolls the [ScrollState] to ensure that the child view, defined by [childTop] and [childBottom],
 * is fully visible within the parent view, defined by [parentTop] and [parentBottom].
 *
 * High level logic: Scroll into view the maximum amount of the child that is currently out of view
 * (above or below the parent bounds), such that we do not cause the other side of the child to also
 * go out of view.
 */
suspend fun ScrollState.scrollChildIntoView(
  childTop: Float?,
  childBottom: Float?,
  parentTop: Float?,
  parentBottom: Float?,
) {
  if (childTop != null && childBottom != null && parentTop != null && parentBottom != null) {
    when {
      // Scroll up if the view extends below, and there's room to scroll up.
      childTop > parentTop && childBottom > parentBottom -> {
        scrollBy(min(childTop - parentTop, childBottom - parentBottom))
      }

      // Scroll down if the view extends above, and there's room to scroll down.
      childTop < parentTop && childBottom < parentBottom -> {
        scrollBy(max(childTop - parentTop, childBottom - parentBottom))
      }
    }
  }
}
