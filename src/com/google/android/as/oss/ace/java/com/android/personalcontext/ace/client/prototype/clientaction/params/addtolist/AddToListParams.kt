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

package com.android.personalcontext.ace.client.prototype.clientaction.params.addtolist

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.clientaction.params.ClientActionParamId
import com.android.personalcontext.ace.client.prototype.clientaction.params.ClientActionParams

/** Parameters for ADD_TO_LIST ClientAction. */
data class AddToListParams(val title: String = "", val items: List<String> = emptyList()) :
  ClientActionParams() {
  override val id = ClientActionParamId.ADD_TO_LIST

  override fun writeToBundle(bundle: Bundle) {
    bundle.putString(TITLE_KEY, title)
    bundle.putStringArrayList(ITEMS_KEY, ArrayList(items))
  }

  companion object : ClientActionParams.Creator {
    private const val TITLE_KEY = "ADD_TO_LIST_PARAMS__TITLE"
    private const val ITEMS_KEY = "ADD_TO_LIST_PARAMS__ITEMS"

    override fun create(bundle: Bundle): AddToListParams =
      AddToListParams(
        title = bundle.getString(TITLE_KEY) ?: "",
        items = bundle.getStringArrayList(ITEMS_KEY) ?: emptyList(),
      )
  }
}
