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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.internal.findprototypehint

import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHintV1

object FindPrototypeHint {

  /**
   * Finds the first hint that can be converted to a
   * [com.android.personalcontext.ace.client.prototype.PrototypeHint] of type [T] within this
   * [ContextInsight].
   *
   * @param T The specific type of [com.android.personalcontext.ace.client.prototype.PrototypeHint]
   *   to find.
   * @return The converted prototype hint of type [T], or `null` if no hint could be successfully
   *   converted.
   */
  @JvmSynthetic
  inline fun <reified T : PrototypeHint> ContextInsight.findPrototypeHint(): T? =
    originHints.firstNotNullOfOrNull {
      it.contextHint.toPrototypeHintV1<T>()
    }
}
