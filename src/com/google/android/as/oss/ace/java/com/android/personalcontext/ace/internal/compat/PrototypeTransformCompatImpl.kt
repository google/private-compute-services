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

package com.android.personalcontext.ace.internal.compat

import android.service.personalcontext.hint.ContextHint
import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHintV1
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toPrototypeInsightV1
import com.android.personalcontext.ace.common.LabeledContextInsight
import com.android.personalcontext.ace.visualizer.compat.PrototypeTransformCompat
import javax.inject.Inject

/**
 * [com.android.personalcontext.ace.visualizer.compat.PrototypeTransformCompat] implementation that
 * transforms [android.service.personalcontext.hint.ContextHint] and
 * [android.service.personalcontext.insight.ContextInsight] to
 * [com.android.personalcontext.ace.client.prototype.PrototypeHint] and
 * [com.android.personalcontext.ace.client.prototype.PrototypeInsight], respectively.
 */
class PrototypeTransformCompatImpl @Inject constructor() : PrototypeTransformCompat {
  override fun transform(hint: ContextHint): String? {
    return hint.toPrototypeHintV1()?.id?.typeName
  }

  override fun transform(insight: ContextInsight): String? {
    return insight.toPrototypeInsightV1()?.id?.typeName
  }

  override fun transformChildren(insight: ContextInsight): List<LabeledContextInsight>? {
    return insight.toPrototypeInsightV1()?.children
  }
}
