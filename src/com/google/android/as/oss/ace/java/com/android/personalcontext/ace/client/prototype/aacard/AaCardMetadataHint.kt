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

/** A hint for the AA Card use case. */
data class AaCardMetadataHint(
  /* Client session UUID. */
  val clientSessionId: String
  // TODO: add more fields
) : PrototypeHint(AaCardMetadataHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(KEY_CLIENT_SESSION_ID, clientSessionId)
  }

  companion object : Creator {
    private const val KEY_CLIENT_SESSION_ID = "client_session_id"

    override fun create(bundle: Bundle): PrototypeHint =
      AaCardMetadataHint(clientSessionId = bundle.getString(KEY_CLIENT_SESSION_ID) ?: "")
  }
}
