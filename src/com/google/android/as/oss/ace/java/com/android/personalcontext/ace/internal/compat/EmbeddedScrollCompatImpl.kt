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

import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsightV1
import com.android.personalcontext.ace.client.prototype.embeddedscroll.EmbeddedScrollInsight.Companion.toEmbeddedScrollInsight
import com.android.personalcontext.ace.common.EmbeddedScrollEvent
import com.android.personalcontext.ace.common.wrappers.IInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.compat.EmbeddedScrollCompat
import javax.inject.Inject

/**
 * [EmbeddedScrollCompat] implementation that sends an [EmbeddedScrollEvent] to the client via a
 * [com.android.personalcontext.ace.client.prototype.embeddedscroll.EmbeddedScrollInsight] prototype
 * insight.
 */
class EmbeddedScrollCompatImpl @Inject constructor() : EmbeddedScrollCompat {
  override fun IInsightSurfaceClientInfo.sendEmbeddedScrollEvent(event: EmbeddedScrollEvent) {
    onReceiveInsight(event.toEmbeddedScrollInsight().toContextInsightV1())
  }
}
