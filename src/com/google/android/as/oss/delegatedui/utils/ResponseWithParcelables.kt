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

package com.google.android.`as`.oss.delegatedui.utils

import android.app.PendingIntent
import android.app.RemoteAction
import android.content.Intent
import android.graphics.Bitmap
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateListOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateOf
import com.google.protobuf.MessageLite

/**
 * Data class holding the response itself, plus any raw and derived data from Parcelables that may
 * have been sent over RPC.
 *
 * @param data The response data.
 * @param image The image to be displayed.
 * @param pendingIntentList The list of pending intents to be launched.
 * @param remoteActionList The list of remote actions to be performed.
 * @param recallAttributionIntentList The list of pending intents for recall attribution.
 * @param actionAttributionIntentList The list of pending intents for action attribution.
 * @param actionIntentList The list of intents to be launched. This is the same list of intents
 *   which are wrapped in the remote action list.
 */
data class ResponseWithParcelables<Data : MessageLite>(
  val data: Data,
  val image: ParcelableOverRpcDelegate<Bitmap> = delegateOf(),
  val pendingIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> = delegateListOf(),
  val remoteActionList: ParcelableOverRpcDelegate<List<RemoteAction>> = delegateListOf(),
  val recallAttributionIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> =
    delegateListOf(),
  val actionAttributionIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> =
    delegateListOf(),
  val actionIntentList: ParcelableOverRpcDelegate<List<Intent>> = delegateListOf(),
) {

  /** Maps a [ResponseWithParcelables] to another by transforming the held response. */
  fun <T : MessageLite> map(transform: (Data) -> T): ResponseWithParcelables<T> {
    return transform(data)
      .withParcelablesToReceive(
        image,
        pendingIntentList,
        remoteActionList,
        recallAttributionIntentList,
        actionAttributionIntentList,
        actionIntentList,
      )
  }
}

/**
 * Wraps a response with Parcelables. Each Parcelable is passed in as a delegate, which can
 * initially be empty but filled in by the infra for you to read later.
 */
fun <T : MessageLite> T.withParcelablesToReceive(
  image: ParcelableOverRpcDelegate<Bitmap> = delegateOf(),
  pendingIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> = delegateListOf(),
  remoteActionList: ParcelableOverRpcDelegate<List<RemoteAction>> = delegateListOf(),
  recallAttributionIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> = delegateListOf(),
  actionAttributionIntentList: ParcelableOverRpcDelegate<List<PendingIntent>> = delegateListOf(),
  actionIntentList: ParcelableOverRpcDelegate<List<Intent>> = delegateListOf(),
): ResponseWithParcelables<T> {
  return ResponseWithParcelables(
    data = this,
    image = image,
    pendingIntentList = pendingIntentList,
    remoteActionList = remoteActionList,
    recallAttributionIntentList = recallAttributionIntentList,
    actionAttributionIntentList = actionAttributionIntentList,
    actionIntentList = actionIntentList,
  )
}

/** Wraps a response with Parcelables. */
fun <T : MessageLite> T.withParcelablesToSend(
  image: Bitmap? = null,
  pendingIntentList: List<PendingIntent>? = null,
  remoteActionList: List<RemoteAction>? = null,
  recallAttributionIntentList: List<PendingIntent>? = null,
  actionAttributionIntentList: List<PendingIntent>? = null,
  actionIntentList: List<Intent>? = null,
): ResponseWithParcelables<T> {
  return ResponseWithParcelables(
    data = this,
    image = image.delegateOf(),
    pendingIntentList = pendingIntentList.delegateListOf(),
    remoteActionList = remoteActionList.delegateListOf(),
    recallAttributionIntentList = recallAttributionIntentList.delegateListOf(),
    actionAttributionIntentList = actionAttributionIntentList.delegateListOf(),
    actionIntentList = actionIntentList.delegateListOf(),
  )
}
