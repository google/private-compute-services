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
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHintV1
import com.android.personalcontext.ace.client.prototype.metadata.VisualMetadataHint
import com.android.personalcontext.ace.client.prototype.metadata.VisualStyle
import com.android.personalcontext.ace.visualizer.compat.VisualMetadataCompat
import javax.inject.Inject

/** [VisualMetadataCompat] implementation */
class VisualMetadataCompatImpl @Inject constructor() : VisualMetadataCompat {
  override fun ContextInsight.isVariant(): Boolean = originHints.any { hint ->
    hint.contextHint.toPrototypeHintV1<VisualMetadataHint>()?.visualStyle == VisualStyle.VARIANT
  }
}
