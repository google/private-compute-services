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

package com.android.personalcontext.ace.client.prototype.visualizerstate

import android.os.Bundle
import android.util.Log

private const val TAG = "VisualizerStateData"

/**
 * Encodes these [VisualizerStateData]s as a list of data [Bundle]s, one entry per state (order
 * preserved). The caller chooses the key to store it under.
 */
fun Collection<VisualizerStateData>.toBundleList(): ArrayList<Bundle> =
  mapTo(ArrayList()) { state -> Bundle().also { state.exportToBundle(it) } }

/**
 * Decodes [VisualizerStateData]s from a list of data [Bundle]s produced by [toBundleList].
 *
 * @return The decoded states in order, or an empty list if absent. Entries that fail to decode are
 *   skipped.
 */
fun List<Bundle>?.toVisualizerStateDataList(): List<VisualizerStateData> =
  this.orEmpty().mapNotNull { bundle ->
    VisualizerStateData.fromBundle(bundle).also {
      if (it == null) Log.w(TAG, "Skipping undecodable VisualizerStateData entry")
    }
  }

data class VisualizerStateData(val name: String, val key: String, val value: Any) {
  fun exportToBundle(bundle: Bundle) {
    bundle.putString(KEY_STATE_NAME, name)
    bundle.putString(KEY_STATE_KEY, key)
    when (value) {
      is Boolean -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_BOOLEAN)
        bundle.putBoolean(KEY_STATE_VALUE, value)
      }
      is String -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_STRING)
        bundle.putString(KEY_STATE_VALUE, value)
      }
      is Int -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_INT)
        bundle.putInt(KEY_STATE_VALUE, value)
      }
      is Long -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_LONG)
        bundle.putLong(KEY_STATE_VALUE, value)
      }
      is Float -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_FLOAT)
        bundle.putFloat(KEY_STATE_VALUE, value)
      }
      is Double -> {
        bundle.putString(KEY_STATE_TYPE, TYPE_DOUBLE)
        bundle.putDouble(KEY_STATE_VALUE, value)
      }
      else -> throw IllegalArgumentException("Unsupported visualizer state value type: $value")
    }
  }

  companion object {
    private const val KEY_STATE_NAME = "visualizer_state_name"
    private const val KEY_STATE_KEY = "visualizer_state_key"
    private const val KEY_STATE_TYPE = "visualizer_state_type"
    private const val KEY_STATE_VALUE = "visualizer_state_value"

    private const val TYPE_BOOLEAN = "bool"
    private const val TYPE_STRING = "string"
    private const val TYPE_INT = "int"
    private const val TYPE_LONG = "long"
    private const val TYPE_FLOAT = "float"
    private const val TYPE_DOUBLE = "double"

    fun fromBundle(bundle: Bundle): VisualizerStateData? {
      val name = bundle.getString(KEY_STATE_NAME) ?: return null
      val key = bundle.getString(KEY_STATE_KEY) ?: return null
      val type = bundle.getString(KEY_STATE_TYPE) ?: return null
      if (!bundle.containsKey(KEY_STATE_VALUE)) return null
      val value: Any =
        when (type) {
          TYPE_BOOLEAN -> bundle.getBoolean(KEY_STATE_VALUE)
          TYPE_STRING -> bundle.getString(KEY_STATE_VALUE) ?: return null
          TYPE_INT -> bundle.getInt(KEY_STATE_VALUE)
          TYPE_LONG -> bundle.getLong(KEY_STATE_VALUE)
          TYPE_FLOAT -> bundle.getFloat(KEY_STATE_VALUE)
          TYPE_DOUBLE -> bundle.getDouble(KEY_STATE_VALUE)
          else -> return null
        }
      return VisualizerStateData(name = name, key = key, value = value)
    }
  }
}
