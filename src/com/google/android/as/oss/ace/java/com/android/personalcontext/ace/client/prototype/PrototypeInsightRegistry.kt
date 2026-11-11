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

package com.android.personalcontext.ace.client.prototype

import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.CardInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.ClientActionInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.ClientSignalInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.EmbeddedScrollInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.EmptyRenderInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.ExampleEmbeddedInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.InsightGridId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.LoadingInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.RenderTokenInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.ServerSideCloseInsightId
import com.android.personalcontext.ace.client.prototype.PrototypeInsightId.WeatherInsightId
import com.android.personalcontext.ace.client.prototype.card.CardInsight
import com.android.personalcontext.ace.client.prototype.clientaction.ClientActionInsight
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalInsight
import com.android.personalcontext.ace.client.prototype.embeddedscroll.EmbeddedScrollInsight
import com.android.personalcontext.ace.client.prototype.empty.EmptyRenderInsight
import com.android.personalcontext.ace.client.prototype.example.ExampleEmbeddedInsight
import com.android.personalcontext.ace.client.prototype.grid.InsightGrid
import com.android.personalcontext.ace.client.prototype.loading.LoadingInsight
import com.android.personalcontext.ace.client.prototype.rendertoken.RenderTokenInsight
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.client.prototype.weather.WeatherInsight

/**
 * Shared registry mapping [PrototypeInsightId] to concrete [PrototypeInsight.Creator] factories.
 *
 * This provides a single source of truth for insight deserialization across both V1 and V2
 * utilities.
 */
internal object PrototypeInsightRegistry {

  fun getInsightCreator(id: PrototypeInsightId): PrototypeInsight.Creator =
    when (id) {
      ExampleEmbeddedInsightId -> ExampleEmbeddedInsight
      EmbeddedScrollInsightId -> EmbeddedScrollInsight
      ClientActionInsightId -> ClientActionInsight
      WeatherInsightId -> WeatherInsight
      EmptyRenderInsightId -> EmptyRenderInsight
      CardInsightId -> CardInsight
      InsightGridId -> InsightGrid
      ServerSideCloseInsightId -> ServerSideCloseInsight
      RenderTokenInsightId -> RenderTokenInsight
      ClientSignalInsightId -> ClientSignalInsight
      LoadingInsightId -> LoadingInsight
    }
}
