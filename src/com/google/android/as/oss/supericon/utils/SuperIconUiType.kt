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

/** The type of UI that can be rendered. */
@Retention(AnnotationRetention.SOURCE)
@IntDef(SuperIconUiType.SUPER_ICON, SuperIconUiType.CONSENT_DIALOG, SuperIconUiType.CONSENT_TOGGLE)
annotation class SuperIconUiType {
  companion object {
    const val SUPER_ICON = 1
    const val CONSENT_DIALOG = 2
    const val CONSENT_TOGGLE = 3
    const val SPELL_CHECKER_CHIP = 4
    const val SUPER_ICON_IN_PANEL = 5
  }
}
