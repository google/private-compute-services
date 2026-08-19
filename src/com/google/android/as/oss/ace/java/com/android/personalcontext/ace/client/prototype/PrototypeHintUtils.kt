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

import android.service.personalcontext.hint.BundleHint
import android.service.personalcontext.hint.ContextHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.BlueflaxMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ClientSignalHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ContactHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.CrossDeviceIntentHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DialerClickEventHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DialerMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DoNotRepublishHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.EntityTypeHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ExampleEmbeddedHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.GboardHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.KeyEventHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.LookupHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.MessageMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.RenderTokenHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.ThemeHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.VisualMetadataHintId
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.WeatherHintId
import com.android.personalcontext.ace.client.prototype.blueflax.BlueflaxMetadataHint
import com.android.personalcontext.ace.client.prototype.clientsignal.ClientSignalHint
import com.android.personalcontext.ace.client.prototype.contact.ContactHint
import com.android.personalcontext.ace.client.prototype.crossdevice.CrossDeviceIntentHint
import com.android.personalcontext.ace.client.prototype.dialer.DialerMetadataHint
import com.android.personalcontext.ace.client.prototype.entitytype.EntityTypeHint
import com.android.personalcontext.ace.client.prototype.example.ExampleEmbeddedHint
import com.android.personalcontext.ace.client.prototype.gboard.GboardHint
import com.android.personalcontext.ace.client.prototype.gboard.KeyEventHint
import com.android.personalcontext.ace.client.prototype.lookup.LookupHint
import com.android.personalcontext.ace.client.prototype.message.MessageMetadataHint
import com.android.personalcontext.ace.client.prototype.metadata.VisualMetadataHint
import com.android.personalcontext.ace.client.prototype.rendertoken.RenderTokenHint
import com.android.personalcontext.ace.client.prototype.republish.DoNotRepublishHint
import com.android.personalcontext.ace.client.prototype.theme.ThemeHint
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint
import com.android.personalcontext.ace.common.builders.HintFilterKt
import com.android.personalcontext.ace.common.builders.HintFilterKt.HintFilterDslMarker
import com.android.personalcontext.ace.common.builders.bundleHint

private const val PROTOTYPE_HINT_ID_KEY = "prototype_hint_id_key"

object PrototypeHintUtils {

  /** Converts the [PrototypeHint] into a [ContextHint]. */
  fun <T : PrototypeHint> T.toContextHint(): ContextHint {
    val prototype = this

    return bundleHint(prototype.id.typeName).apply {
      prototype.exportDataToBundle(dataBundle)
      dataBundle.putInt(PROTOTYPE_HINT_ID_KEY, prototype.id.uid)
    }
  }

  /** Converts a [ContextHint] back into a [PrototypeHint], if possible. */
  fun ContextHint.toPrototypeHint(): PrototypeHint? {
    if (this !is BundleHint) return null

    val uid = dataBundle.getInt(PROTOTYPE_HINT_ID_KEY)
    val id = PrototypeHintId.entries.find { it.uid == uid } ?: return null

    val creator =
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
        LookupHintId -> LookupHint
        ThemeHintId -> ThemeHint
        DialerMetadataHintId -> DialerMetadataHint
        MessageMetadataHintId -> MessageMetadataHint
        BlueflaxMetadataHintId -> BlueflaxMetadataHint
        GboardHintId -> GboardHint
        KeyEventHintId -> KeyEventHint

        else -> null
      }

    return creator?.create(dataBundle)
  }

  /** Converts a [ContextHint] back into a [PrototypeHint] as [T], if possible. */
  @JvmSynthetic
  @JvmName("toTypedPrototypeHint")
  inline fun <reified T : PrototypeHint> ContextHint.toPrototypeHint(): T? {
    return toPrototypeHint() as? T
  }

  /** Returns whether a [ContextHint] is a [PrototypeHint] of type [T]. */
  @JvmSynthetic
  inline fun <reified T : PrototypeHint> ContextHint.isPrototypeHint(): Boolean {
    return toPrototypeHint() is T
  }

  /** Adds a filter that matches a specific [PrototypeHint] using its [PrototypeHintId]. */
  @HintFilterDslMarker
  fun HintFilterKt.FilterScope.prototypeHint(id: PrototypeHintId) = bundleHint(id.typeName)
}
