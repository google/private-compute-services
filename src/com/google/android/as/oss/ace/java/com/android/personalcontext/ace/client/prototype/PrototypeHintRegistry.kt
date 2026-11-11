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

import com.android.personalcontext.ace.client.prototype.PrototypeHintId.AppLifecycleHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.AutofillRemoteAppHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.BlueflaxMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ClientSignalHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ContactHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.CrossDeviceIntentHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DialerClickEventHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DialerMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DoNotRepublishHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DryRunHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.EmbeddedSessionHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.EntityTypeHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ExampleEmbeddedHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.GboardHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.KeyEventHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.LookupHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.MessageMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.RenderTokenHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ThemeHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.VisualMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.VisualizerStateHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.WeatherHintId
import com.android.personalcontext.ace.client.prototype.applifecycle.AppLifecycleHint
import com.android.personalcontext.ace.client.prototype.autofillremoteapp.AutofillRemoteAppHint
import com.android.personalcontext.ace.client.prototype.blueflax.BlueflaxMetadataHint
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalHint
import com.android.personalcontext.ace.client.prototype.contact.ContactHint
import com.android.personalcontext.ace.client.prototype.crossdevice.CrossDeviceIntentHint
import com.android.personalcontext.ace.client.prototype.dialer.DialerMetadataHint
import com.android.personalcontext.ace.client.prototype.dryrun.DryRunHint
import com.android.personalcontext.ace.client.prototype.entitytype.EntityTypeHint
import com.android.personalcontext.ace.client.prototype.example.ExampleEmbeddedHint
import com.android.personalcontext.ace.client.prototype.gboard.GboardHint
import com.android.personalcontext.ace.client.prototype.gboard.KeyEventHint
import com.android.personalcontext.ace.client.prototype.lookup.LookupHint
import com.android.personalcontext.ace.client.prototype.message.MessageMetadataHint
import com.android.personalcontext.ace.client.prototype.metadata.VisualMetadataHint
import com.android.personalcontext.ace.client.prototype.rendertoken.RenderTokenHint
import com.android.personalcontext.ace.client.prototype.republish.DoNotRepublishHint
import com.android.personalcontext.ace.client.prototype.session.EmbeddedSessionHint
import com.android.personalcontext.ace.client.prototype.theme.ThemeHint
import com.android.personalcontext.ace.client.prototype.visualizerstate.VisualizerStateHint
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint

/**
 * Shared registry mapping [PrototypeHintId] to concrete [PrototypeHint.Creator] factories.
 *
 * This provides a single source of truth for hint deserialization across both V1 and V2 utilities.
 */
internal object PrototypeHintRegistry {

  fun getHintCreator(id: PrototypeHintId): PrototypeHint.Creator? =
    when (id) {
      ExampleEmbeddedHintId -> ExampleEmbeddedHint
      WeatherHintId -> WeatherHint
      @Suppress("DEPRECATION_ERROR") DialerClickEventHintId -> null
      CrossDeviceIntentHintId -> CrossDeviceIntentHint
      ContactHintId -> ContactHint
      EntityTypeHintId -> EntityTypeHint
      ClientSignalHintId -> ClientSignalHint
      VisualMetadataHintId -> VisualMetadataHint
      RenderTokenHintId -> RenderTokenHint
      DoNotRepublishHintId -> DoNotRepublishHint
      AutofillRemoteAppHintId -> AutofillRemoteAppHint
      LookupHintId -> LookupHint
      ThemeHintId -> ThemeHint
      DialerMetadataHintId -> DialerMetadataHint
      MessageMetadataHintId -> MessageMetadataHint
      BlueflaxMetadataHintId -> BlueflaxMetadataHint
      GboardHintId -> GboardHint
      KeyEventHintId -> KeyEventHint
      AppLifecycleHintId -> AppLifecycleHint
      EmbeddedSessionHintId -> EmbeddedSessionHint
      DryRunHintId -> DryRunHint
      VisualizerStateHintId -> VisualizerStateHint

      else -> null
    }
}
