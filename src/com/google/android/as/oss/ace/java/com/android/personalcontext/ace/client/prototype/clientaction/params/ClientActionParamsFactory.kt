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

package com.android.personalcontext.ace.client.prototype.clientaction.params

import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.clientaction.params.blueflax.BlueflaxParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.fullscreenrequest.FullScreenRequestParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.hidekeyboard.HideKeyboardParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.sharelivelocation.ShareLiveLocationParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.sharephoto.SharePhotoParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.showcards.ShowCardsParams
import com.android.personalcontext.ace.client.prototype.clientaction.params.textpaste.TextPasteParams

/** Central factory to reconstruct ClientActionParams using ID mapping. */
object ClientActionParamsFactory {
  fun create(bundle: Bundle): ClientActionParams {
    val uid = bundle.getInt(ClientActionParams.PARAM_ID_KEY)
    val id = ClientActionParamId.entries.find { it.uid == uid } ?: ClientActionParamId.UNKNOWN

    // Get the creator polymorphically, matching the PrototypeInsightUtils style
    val creator: ClientActionParams.Creator =
      when (id) {
        ClientActionParamId.SHARE_PHOTO -> SharePhotoParams
        ClientActionParamId.SHOW_CARDS -> ShowCardsParams
        ClientActionParamId.FULL_SCREEN_REQUEST -> FullScreenRequestParams
        ClientActionParamId.TEXT_PASTE -> TextPasteParams
        ClientActionParamId.SHARE_LIVE_LOCATION -> ShareLiveLocationParams
        ClientActionParamId.HIDE_KEYBOARD -> HideKeyboardParams
        ClientActionParamId.BLUEFLAX -> BlueflaxParams
        ClientActionParamId.UNKNOWN -> UnknownParams
      }

    return creator.create(bundle)
  }
}
