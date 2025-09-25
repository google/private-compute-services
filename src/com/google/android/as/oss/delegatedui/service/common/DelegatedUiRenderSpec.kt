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

package com.google.android.`as`.oss.delegatedui.service.common

import android.content.res.Configuration
import androidx.annotation.ColorInt
import androidx.core.view.ViewCompat.ScrollAxis
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiClientId
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiDataProviderInfo
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiIngressData

/**
 * The set of client inputs that uniquely and completely define a render request. This class can act
 * as a key - if two render specs are equal then the result will also be equal, for some reasonable
 * TTL.
 *
 * @param configuration Resource configuration from the client app's local context. Includes locale,
 *   screen size, device orientation, and other information that can be used to provide
 *   [alternative resources](https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources)
 *   for the delegated ui.
 * @param clientId The unique identifier for the client app.
 * @param sessionUuid The unique identifier for the delegated ui session.
 * @param ingressData The ingress data passed from the client app as input to the delegated ui data
 *   service.
 * @param dataProviderInfo Metadata about the data source that will be used to populate the
 *   delegated ui. This also determines which backend to use for logging.
 * @param measureSpecWidth A [android.view.View.MeasureSpec] provided by the client app that
 *   constrains the width of the delegated UI.
 * @param measureSpecHeight A [android.view.View.MeasureSpec] provided by the client app that
 *   constrains the height of the delegated UI.
 * @param backgroundColor The background color to draw behind the delegated UI.
 * @param clientNestedScrollAxes The nested scroll axes supported by the client.
 */
data class DelegatedUiRenderSpec(
  val configuration: Configuration,
  val clientId: DelegatedUiClientId,
  val sessionUuid: String,
  val ingressData: DelegatedUiIngressData,
  val dataProviderInfo: DelegatedUiDataProviderInfo,
  val measureSpecWidth: Int,
  val measureSpecHeight: Int,
  @ColorInt val backgroundColor: Int,
  @ScrollAxis val clientNestedScrollAxes: Int,
)
