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

package com.google.android.`as`.oss.supericon.utils

import androidx.annotation.IntDef

/** The error code of a remote render request. */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
  SuperIconErrorCodes.UNKNOWN,
  SuperIconErrorCodes.INVALID_PARAMETER,
  SuperIconErrorCodes.NOT_ABLE_TO_BIND,
  SuperIconErrorCodes.EMPTY_SCREEN_CONTENT,
  SuperIconErrorCodes.FEATURE_DISABLED,
  SuperIconErrorCodes.CALLER_NOT_AUTHORIZED,
  SuperIconErrorCodes.RENDER_FAILED,
  SuperIconErrorCodes.RENDER_CANCELLED,
)
annotation class SuperIconErrorCodes {
  companion object {
    const val UNKNOWN = 0
    const val INVALID_PARAMETER = 1
    const val NOT_ABLE_TO_BIND = 2
    const val EMPTY_SCREEN_CONTENT = 3
    const val FEATURE_DISABLED = 4
    const val CALLER_NOT_AUTHORIZED = 5
    const val RENDER_FAILED = 6
    /**
     * Indicates that an in-flight render request was cancelled because it was superseded by a newer
     * request. Clients should release their callback reference without treating this as a fatal UI
     * render failure.
     */
    const val RENDER_CANCELLED = 7
  }
}
