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

import android.service.personalcontext.insight.ContextInsight
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHintV1
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsightV1
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalHint
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalInsight
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalType
import com.android.personalcontext.ace.common.wrappers.IInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.compat.ClientSignalCompat
import javax.inject.Inject

/**
 * [ClientSignalCompat] implementation that checks for [ClientSignalHint] prototype hint and emits
 * [ClientSignalInsight] prototype insight.
 */
class ClientSignalCompatImpl @Inject constructor() : ClientSignalCompat {

  override fun ContextInsight.containsPiiHint(): Boolean {
    return this.originHints.any {
      val clientSignalHint =
        it.contextHint.toPrototypeHintV1<ClientSignalHint>() ?: return@any false

      clientSignalHint.type == ClientSignalType.CONTAINS_PII && clientSignalHint.data
    }
  }

  override fun IInsightSurfaceClientInfo.sendPiiClientSignal() {
    onReceiveInsight(
      ClientSignalInsight(
          type = ClientSignalType.CONTAINS_PII,
          data = true,
          originHints = emptySet(),
        )
        .toContextInsightV1()
    )
  }
}
