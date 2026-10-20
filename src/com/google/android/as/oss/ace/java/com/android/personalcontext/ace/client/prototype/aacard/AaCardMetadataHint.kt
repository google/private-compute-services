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

package com.android.personalcontext.ace.client.prototype.aacard

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.AaCardMetadataHintId

/**
 * A hint for the AA Card use case.
 *
 * @property clientSessionId Client session UUID.
 * @property dataSourceBlockList List of String representing blocklisted data sources. Note: If user
 *   disallows GMAIL, WONDER_CARD should also be passed.
 */
data class AaCardMetadataHint(
  /* Client session UUID. */
  val clientSessionId: String,
  val dataSourceBlockList: List<String> = emptyList(),
) : PrototypeHint(AaCardMetadataHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(KEY_CLIENT_SESSION_ID, clientSessionId)
    bundle.putStringArrayList(KEY_DATA_SOURCE_BLOCK_LIST, ArrayList(dataSourceBlockList))
  }

  companion object : Creator {
    private const val KEY_CLIENT_SESSION_ID = "client_session_id"
    private const val KEY_DATA_SOURCE_BLOCK_LIST = "data_source_block_list"

    override fun create(bundle: Bundle): PrototypeHint =
      AaCardMetadataHint(
        clientSessionId = bundle.getString(KEY_CLIENT_SESSION_ID) ?: "",
        dataSourceBlockList = bundle.getStringArrayList(KEY_DATA_SOURCE_BLOCK_LIST) ?: emptyList(),
      )
  }
}
