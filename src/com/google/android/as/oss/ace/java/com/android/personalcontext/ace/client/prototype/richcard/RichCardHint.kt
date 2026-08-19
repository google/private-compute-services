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

package com.android.personalcontext.ace.client.prototype.richcard

import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.Keep
import androidx.core.os.BundleCompat
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.RichCardHintId
import kotlinx.parcelize.Parcelize

/**
 * A hint for the rich card use case. It is published from the client app to indicate that a card
 * preview chip is clicked and a rich card should be rendered.
 *
 * @property clientSessionId The DAG client session ID for generating the card preview chip that is
 *   clicked.
 * @property cardIds A list of card IDs within that DAG session that will be rendered as rich cards.
 * @property cardClientSessionId The DAG client session ID for generating the card UI. If this is
 *   not null, the card UI will be rendered with this client session ID. Otherwise, the card UI will
 *   be rendered with the client session ID from the DAG chip session.
 * @property error An error representing unexpected errors in the client app that caused failure to
 *   render the card UI. If this is not null, the card UI will show an error state.
 * @property bottomInsetPx The inset to apply to the card contents from the bottom of the card, in
 *   pixels.
 */
data class RichCardHint(
  val clientSessionId: String,
  val cardIds: List<String>,
  val cardClientSessionId: String? = null,
  val error: RichCardError? = null,
  val bottomInsetPx: Int = 0,
) : PrototypeHint(RichCardHintId, this) {
  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putString(CLIENT_SESSION_ID_KEY, clientSessionId)
    bundle.putStringArrayList(CARD_IDS_KEY, ArrayList(cardIds))
    bundle.putString(CARD_CLIENT_SESSION_ID_KEY, cardClientSessionId)
    bundle.putParcelable(ERROR_KEY, error)
    bundle.putInt(BOTTOM_INSET_PX_KEY, bottomInsetPx)
  }

  companion object : Creator {
    private const val CLIENT_SESSION_ID_KEY = "clientSessionId"
    private const val CARD_IDS_KEY = "cardIds"
    private const val CARD_CLIENT_SESSION_ID_KEY = "cardClientSessionId"
    private const val ERROR_KEY = "error"
    private const val BOTTOM_INSET_PX_KEY = "bottomInsetPx"

    override fun create(bundle: Bundle): PrototypeHint {
      bundle.classLoader = RichCardError::class.java.classLoader
      return RichCardHint(
        bundle.getString(CLIENT_SESSION_ID_KEY) ?: "",
        bundle.getStringArrayList(CARD_IDS_KEY) ?: emptyList(),
        bundle.getString(CARD_CLIENT_SESSION_ID_KEY),
        BundleCompat.getParcelable(bundle, ERROR_KEY, RichCardError::class.java),
        bundle.getInt(BOTTOM_INSET_PX_KEY, 0),
      )
    }
  }
}

/** Base error class for errors that occurred within the client app for the rich card use case. */
@Keep
@Parcelize
data class RichCardError(val message: String, val exceptionName: String? = null) : Parcelable
