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

package com.google.android.`as`.oss.delegatedui.api.infra.dataservice

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.res.Configuration
import android.graphics.Bitmap
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverMetadataServerInterceptor
import com.google.android.`as`.oss.delegatedui.utils.RepeatedParcelableKey
import com.google.android.`as`.oss.delegatedui.utils.SingleParcelableKey

/** Parcelable keys for the Delegated UI Service. */
object DelegatedUiDataServiceParcelableKeys {

  /** Key for the configuration. */
  val CONFIGURATION_KEY = SingleParcelableKey("configuration", Configuration.CREATOR)

  /** Key for the image, this onlt supports a single image to be sent/received. */
  val IMAGE_KEY = SingleParcelableKey("image", Bitmap.CREATOR)

  /** Key for the pending intent list, allows for multiple pending intents to be sent/received. */
  val PENDING_INTENT_LIST_KEY = RepeatedParcelableKey("pending_intent_list", PendingIntent.CREATOR)

  /** Key for the remote action list, allows for multiple remote actions to be sent/received. */
  val REMOTE_ACTION_LIST_KEY = RepeatedParcelableKey("remote_action_list", RemoteAction.CREATOR)

  /**
   * Interceptor to be set on the Delegated UI data service server for receiving and sending
   * Parcelables over headers.
   */
  val SERVER_INTERCEPTOR =
    ParcelableOverMetadataServerInterceptor(
      CONFIGURATION_KEY,
      IMAGE_KEY,
      PENDING_INTENT_LIST_KEY,
      REMOTE_ACTION_LIST_KEY,
    )
}
