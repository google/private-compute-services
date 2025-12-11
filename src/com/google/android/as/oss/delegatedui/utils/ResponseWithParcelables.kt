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
import android.graphics.Bitmap
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateListOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateOf
import com.google.protobuf.MessageLite

/**
 * Data class holding the response itself, plus any raw and derived data from Parcelables that may
 * have been sent over RPC.
 */
data class ResponseWithParcelables<Data : MessageLite>(
  val data: Data,
  val image: ParcelableOverRpcDelegate<Bitmap>,
  val pendingIntentList: ParcelableOverRpcDelegate<List<PendingIntent>>,
  val remoteActionList: ParcelableOverRpcDelegate<List<RemoteAction>>,
) {

  /** Maps a [ResponseWithParcelables] to another by transforming the held response. */
  fun <T : MessageLite> map(transform: (Data) -> T): ResponseWithParcelables<T> {
    return transform(data).withParcelablesToReceive(image, pendingIntentList, remoteActionList)
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
): ResponseWithParcelables<T> {
  return ResponseWithParcelables(
    data = this,
    image = image,
    pendingIntentList = pendingIntentList,
    remoteActionList = remoteActionList,
  )
}

/** Wraps a response with Parcelables. */
fun <T : MessageLite> T.withParcelablesToSend(
  image: Bitmap? = null,
  pendingIntentList: List<PendingIntent>? = null,
  remoteActionList: List<RemoteAction>? = null,
): ResponseWithParcelables<T> {
  return ResponseWithParcelables(
    data = this,
    image = image.delegateOf(),
    pendingIntentList = pendingIntentList.delegateListOf(),
    remoteActionList = remoteActionList.delegateListOf(),
  )
}
