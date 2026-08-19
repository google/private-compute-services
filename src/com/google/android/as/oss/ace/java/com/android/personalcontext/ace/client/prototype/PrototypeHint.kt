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

package com.android.personalcontext.ace.client.prototype

import android.os.Bundle
import androidx.annotation.IntRange

/**
 * [PrototypeHint] is an abstract class that allows defining custom [ContextHint] types for
 * prototyping purposes, without needing to modify the Android framework.
 *
 * These custom hint types are wrapped within a [BundleHint], which is a generic [ContextHint] type
 * that carries data in a [Bundle]. This allows new hint types to be created and used before they
 * are formally integrated into the Android framework as new [ContextHint] subclasses.
 */
abstract class PrototypeHint(val id: PrototypeHintId, val creator: Creator) {

  /** Exports the primitive data for this hint into the given [Bundle]. */
  abstract fun exportDataToBundle(bundle: Bundle)

  /** Final toString implementation to prevent subclass implementations that might leak PII. */
  final override fun toString(): String {
    return "${this.javaClass.simpleName}(id=$id, redacted...)"
  }

  /**
   * Interface for creating instances of [PrototypeHint] from their serialized state.
   *
   * Implementations of this interface act as factories that reconstruct concrete [PrototypeHint]
   * subclasses using data previously exported via [exportDataToBundle].
   *
   * This mechanism allows the framework to instantiate specific prototype hints without having
   * compile-time knowledge of their concrete classes.
   */
  interface Creator {

    /**
     * Creates a new instance of a concrete [PrototypeHint] subclass.
     *
     * @param bundle The [Bundle] containing the data for this hint, as populated by
     *   [PrototypeHint.exportDataToBundle].
     * @return A fully initialized instance of the specific [PrototypeHint] subclass.
     */
    fun create(bundle: Bundle): PrototypeHint
  }
}

/**
 * A unique identifier for each type of [PrototypeHint].
 *
 * @property uid A unique positive value for each entry that should not change after creation.
 * @property typeName The class name of the prototype, may be used by OSI for comparison.
 */
// Next ID: 22
enum class PrototypeHintId(@field:IntRange(from = 1) val uid: Int, val typeName: String) {
  ExampleEmbeddedHintId(1, "ExampleEmbeddedHint"),
  WeatherHintId(2, "WeatherHint"),
  AaCardMetadataHintId(3, "AaCardMetadataHint"),
  @Deprecated(level = DeprecationLevel.ERROR, message = "Use ClientActionInsight instead.")
  DialerClickEventHintId(4, "DialerClickEventHint"),
  CrossDeviceIntentHintId(5, "CrossDeviceIntentHint"),
  RichCardHintId(6, "RichCardHint"),
  RichCardLiveDataHintId(7, "RichCardLiveDataHint"),
  ContactHintId(8, "ContactHint"),
  EntityTypeHintId(9, "EntityTypeHint"),
  ClientSignalHintId(10, "ClientSignalHint"),
  VisualMetadataHintId(11, "VisualMetadataHint"),
  RenderTokenHintId(12, "RenderTokenHint"),
  DoNotRepublishHintId(13, "DoNotRepublishHint"),
  LookupHintId(14, "LookupHint"),
  ThemeHintId(15, "ThemeHint"),
  DialerMetadataHintId(16, "DialerMetadataHint"),
  MessageMetadataHintId(17, "MessageMetadataHint"),
  GboardHintId(18, "GboardHint"),
  RichCardErrorHintId(19, "RichCardErrorHint"),
  BlueflaxMetadataHintId(20, "BlueflaxMetadataHint"),
  KeyEventHintId(21, "KeyEventHint"),
}
