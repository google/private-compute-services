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

package com.android.personalcontext.ace.client.prototype.gboard

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.GboardHintId

/** A hint for the Gboard use case. Supplements GboardHint with additional metadata. */
// TODO: Consider use UserInputHint instead of this hint.
data class GboardHint(
  val userInput: String? = null,
  val userQuery: String? = null,
  val queryCategory: QueryCategory? = null,
) : PrototypeHint(GboardHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(KEY_USER_INPUT, userInput)
    bundle.putString(KEY_USER_QUERY, userQuery)
    bundle.putString(KEY_QUERY_CATEGORY, queryCategory?.value)
  }

  companion object : Creator {
    private const val KEY_USER_INPUT = "user_input"
    private const val KEY_USER_QUERY = "user_query"
    private const val KEY_QUERY_CATEGORY = "query_category"

    override fun create(bundle: Bundle): PrototypeHint =
      GboardHint(
        userInput = bundle.getString(KEY_USER_INPUT),
        userQuery = bundle.getString(KEY_USER_QUERY),
        queryCategory = QueryCategory.fromValue(bundle.getString(KEY_QUERY_CATEGORY)),
      )
  }

  enum class QueryCategory(val value: String) {
    // Browsing history
    CHROME("CHROME"),
    LOAM_FILE("LOAM_FILE"),
    // Sian local cache
    NESTA("NESTA");

    companion object {
      fun fromValue(value: String?): QueryCategory? = entries.find { it.value == value }
    }
  }
}
