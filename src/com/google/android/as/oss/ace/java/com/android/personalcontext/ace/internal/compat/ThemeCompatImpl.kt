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

package com.android.personalcontext.ace.internal.compat

import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHint
import com.android.personalcontext.ace.client.prototype.theme.ThemeHint
import com.android.personalcontext.ace.client.prototype.theme.ThemeType
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import javax.inject.Inject

/**
 * [ThemeCompat] implementation that checks for [ThemeHint] prototype hint and returns the
 * corresponding boolean value.
 */
class ThemeCompatImpl @Inject constructor() : ThemeCompat {

  override fun ContextInsight.shouldShowAnimationV2(): Boolean {
    return this.originHints.any {
      val themeHint = it.contextHint.toPrototypeHint<ThemeHint>() ?: return@any false

      themeHint.type == ThemeType.SHOW_ANIMATION_V2 && themeHint.data
    }
  }

  override fun ContextInsight.shouldShowBrandedIcon(): Boolean {
    return this.originHints.any {
      val themeHint = it.contextHint.toPrototypeHint<ThemeHint>() ?: return@any false

      themeHint.type == ThemeType.SHOW_BRANDED_ICON && themeHint.data
    }
  }
}
