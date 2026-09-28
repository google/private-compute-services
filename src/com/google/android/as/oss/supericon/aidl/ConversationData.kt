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

package com.google.android.`as`.oss.supericon.aidl

import android.os.Parcel
import android.os.Parcelable
import com.google.fcp.client.common.internal.safeparcel.AbstractSafeParcelable
import com.google.fcp.client.common.internal.safeparcel.SafeParcelable

/**
 * Data class representing a single message in a conversation.
 *
 * @property isSent True if the message was sent by the user, false otherwise.
 * @property title The title of the conversation or message.
 * @property timestamp The timestamp of when the message was sent or received.
 * @property contactName The name of the contact who sent or received the message.
 * @property text The content of the message.
 * @property date The date of the message.
 */
@SafeParcelable.Class(creator = "MessageCreator")
data class Message
@SafeParcelable.Constructor
constructor(
  @field:SafeParcelable.Field(id = 1, getter = "isSent")
  @param:SafeParcelable.Param(id = 1)
  val isSent: Boolean,
  @field:SafeParcelable.Field(id = 2, getter = "getTitle")
  @param:SafeParcelable.Param(id = 2)
  val title: String,
  @field:SafeParcelable.Field(id = 3, getter = "getTimestamp")
  @param:SafeParcelable.Param(id = 3)
  val timestamp: String,
  @field:SafeParcelable.Field(id = 4, getter = "getContactName")
  @param:SafeParcelable.Param(id = 4)
  val contactName: String,
  @field:SafeParcelable.Field(id = 5, getter = "getText")
  @param:SafeParcelable.Param(id = 5)
  val text: String,
  @field:SafeParcelable.Field(id = 6, getter = "getDate")
  @param:SafeParcelable.Param(id = 6)
  val date: String? = null,
) : AbstractSafeParcelable() {
  override fun writeToParcel(dest: Parcel, flags: Int) {
    MessageCreator.writeToParcel(this, dest, flags)
  }

  companion object {
    @JvmField val CREATOR: Parcelable.Creator<Message> = MessageCreator()
  }
}

/**
 * Data class representing a collection of messages in a conversation.
 *
 * @property messages A list of [Message] objects.
 * @property packageName The package name of the app that the conversation is from.
 */
@SafeParcelable.Class(creator = "ConversationDataCreator")
data class ConversationData
@SafeParcelable.Constructor
constructor(
  @field:SafeParcelable.Field(id = 1, getter = "getMessages")
  @param:SafeParcelable.Param(id = 1)
  val messages: List<Message>,
  @field:SafeParcelable.Field(id = 2, getter = "getPackageName")
  @param:SafeParcelable.Param(id = 2)
  val packageName: String? = null,
) : AbstractSafeParcelable() {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as ConversationData

    if (messages != other.messages) return false
    if (packageName != other.packageName) return false

    return true
  }

  override fun hashCode(): Int {
    var result = messages.hashCode()
    result = 31 * result + (packageName?.hashCode() ?: 0)
    return result
  }

  override fun writeToParcel(dest: Parcel, flags: Int) {
    ConversationDataCreator.writeToParcel(this, dest, flags)
  }

  companion object {
    @JvmField val CREATOR: Parcelable.Creator<ConversationData> = ConversationDataCreator()
  }
}
