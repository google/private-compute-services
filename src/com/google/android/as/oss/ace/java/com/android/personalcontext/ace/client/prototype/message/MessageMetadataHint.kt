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

package com.android.personalcontext.ace.client.prototype.message

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.MessageMetadataHintId

/**
 * A hint for the Messages use case. Supplements MessagesHint with additional metadata and custom
 * styling. Colors are represented as Int (ARGB format).
 *
 * @property suggestionLimit The maximum number of suggestions to display.
 * @property strokeColor The color of the stroke around the suggestion.
 * @property textColor The color of the suggestion text.
 * @property iconColor The color of any icons in the suggestion.
 * @property suggestionBackgroundColor The background color of the suggestion.
 * @property suggestionCornerRadius The corner radius of the suggestion background.
 */
data class MessageMetadataHint(
  val suggestionLimit: Int = 0,
  val strokeColor: Int? = null,
  val textColor: Int? = null,
  val iconColor: Int? = null,
  val suggestionBackgroundColor: Int? = null,
  val suggestionCornerRadius: Int? = null,
) : PrototypeHint(MessageMetadataHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putInt(KEY_SUGGESTION_LIMIT, suggestionLimit)
    strokeColor?.let { bundle.putInt(KEY_STROKE_COLOR, it) }
    textColor?.let { bundle.putInt(KEY_TEXT_COLOR, it) }
    iconColor?.let { bundle.putInt(KEY_ICON_COLOR, it) }
    suggestionBackgroundColor?.let { bundle.putInt(KEY_SUGGESTION_BACKGROUND_COLOR, it) }
    suggestionCornerRadius?.let { bundle.putInt(KEY_SUGGESTION_CORNER_RADIUS, it) }
  }

  companion object : Creator {
    private const val KEY_SUGGESTION_LIMIT = "suggestion_limit"
    private const val KEY_STROKE_COLOR = "stroke_color"
    private const val KEY_TEXT_COLOR = "text_color"
    private const val KEY_ICON_COLOR = "icon_color"
    private const val KEY_SUGGESTION_BACKGROUND_COLOR = "suggestion_background_color"
    private const val KEY_SUGGESTION_CORNER_RADIUS = "suggestion_corner_radius"

    override fun create(bundle: Bundle): PrototypeHint {
      val suggestionLimit = bundle.getInt(KEY_SUGGESTION_LIMIT)
      val strokeColor =
        if (bundle.containsKey(KEY_STROKE_COLOR)) bundle.getInt(KEY_STROKE_COLOR) else null
      val textColor =
        if (bundle.containsKey(KEY_TEXT_COLOR)) bundle.getInt(KEY_TEXT_COLOR) else null
      val iconColor =
        if (bundle.containsKey(KEY_ICON_COLOR)) bundle.getInt(KEY_ICON_COLOR) else null
      val suggestionBackgroundColor =
        if (bundle.containsKey(KEY_SUGGESTION_BACKGROUND_COLOR)) {
          bundle.getInt(KEY_SUGGESTION_BACKGROUND_COLOR)
        } else {
          null
        }
      val suggestionCornerRadius =
        if (bundle.containsKey(KEY_SUGGESTION_CORNER_RADIUS)) {
          bundle.getInt(KEY_SUGGESTION_CORNER_RADIUS)
        } else {
          null
        }

      return MessageMetadataHint(
        suggestionLimit = suggestionLimit,
        strokeColor = strokeColor,
        textColor = textColor,
        iconColor = iconColor,
        suggestionBackgroundColor = suggestionBackgroundColor,
        suggestionCornerRadius = suggestionCornerRadius,
      )
    }
  }
}
