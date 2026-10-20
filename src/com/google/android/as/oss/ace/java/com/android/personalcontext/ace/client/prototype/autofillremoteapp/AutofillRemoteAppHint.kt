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

package com.android.personalcontext.ace.client.prototype.autofillremoteapp

import android.app.assist.ActivityId
import android.os.Bundle
import android.view.autofill.AutofillId
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.AutofillRemoteAppHintId

/**
 * A [PrototypeHint] signifying a request to perform remote app autofill.
 *
 * This hint triggers `AutofillManager.autofillRemoteApp` when evaluated as valid by the Android
 * Context Engine Understander.
 *
 * Execution is strictly gated by the `sessionToken`, which serves as the authorization token. By
 * embedding this token in the context payload, the Understander authenticates the hint,
 * guaranteeing it is a legitimate request to autofill the remote app.
 *
 * @property payload The autofill payload.
 * @property autofillId The autofill identifier parcelable of the target input field.
 * @property activityId The ActivityId parcelable of the target activity.
 * @property sessionToken The session token authorizing the autofill remote action.
 */
data class AutofillRemoteAppHint(
  val payload: AutofillRemoteAppPayload,
  val autofillId: AutofillId,
  val activityId: ActivityId,
  val sessionToken: String,
) : PrototypeHint(AutofillRemoteAppHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putBundle(EXTRA_PAYLOAD, AutofillRemoteAppPayload.toBundle(payload))
    bundle.putParcelable(EXTRA_AUTOFILL_ID, autofillId)
    bundle.putParcelable(EXTRA_ACTIVITY_ID, activityId)
    bundle.putString(EXTRA_SESSION_TOKEN, sessionToken)
  }

  companion object : Creator {
    private const val EXTRA_PAYLOAD = "message_payload"
    private const val EXTRA_AUTOFILL_ID = "autofill_id"
    private const val EXTRA_ACTIVITY_ID = "activity_id"
    private const val EXTRA_SESSION_TOKEN = "session_token"

    override fun create(bundle: Bundle): PrototypeHint {
      val payloadBundle = bundle.getBundle(EXTRA_PAYLOAD) ?: Bundle()
      val payload = AutofillRemoteAppPayload.fromBundle(payloadBundle)
      val autofillId =
        bundle.getParcelable(EXTRA_AUTOFILL_ID, AutofillId::class.java)
          ?: error("AutofillId is null")
      val activityId =
        bundle.getParcelable(EXTRA_ACTIVITY_ID, ActivityId::class.java)
          ?: error("ActivityId is null")
      val sessionToken = bundle.getString(EXTRA_SESSION_TOKEN).orEmpty()

      return AutofillRemoteAppHint(
        payload = payload,
        autofillId = autofillId,
        activityId = activityId,
        sessionToken = sessionToken,
      )
    }
  }
}

/** Data class encapsulating the autofill payload. */
data class AutofillRemoteAppPayload(val text: String) {
  companion object {
    private const val KEY_TEXT = "text"

    /** Serializes the [AutofillRemoteAppPayload] into an Android [Bundle]. */
    fun toBundle(payload: AutofillRemoteAppPayload): Bundle {
      val bundle = Bundle()
      bundle.putString(KEY_TEXT, payload.text)
      return bundle
    }

    /** Deserializes a [AutofillRemoteAppPayload] from an Android [Bundle]. */
    fun fromBundle(bundle: Bundle): AutofillRemoteAppPayload {
      val text = bundle.getString(KEY_TEXT).orEmpty()
      return AutofillRemoteAppPayload(text = text)
    }
  }
}
