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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.client.prototype.clientaction.params.blueflax

import android.app.RemoteAction
import android.graphics.drawable.Icon
import android.os.Bundle
import com.android.personalcontext.ace.client.prototype.clientaction.params.ClientActionParamId
import com.android.personalcontext.ace.client.prototype.clientaction.params.ClientActionParams

/** Supported override icons for Blueflax. */
enum class BlueflaxActionType(val value: String) {
  UNKNOWN("unknown"),
  REMINDER("reminder"),
  LIST("list"),
  CALENDAR("calendar"),
  TASKS("tasks");

  companion object {
    fun fromValue(value: String?): BlueflaxActionType =
      entries.find { it.value == value } ?: UNKNOWN
  }
}

/** Parameters for the BLUEFLAX client action. */
class BlueflaxParams(
  val uuid: String,
  val remoteAction: RemoteAction?,
  val overwriteSubtitle: String,
  val actionType: BlueflaxActionType,
  val closeContentDescription: String,
  val overrideIcon: Icon? = null,
) : ClientActionParams() {
  override val id = ClientActionParamId.BLUEFLAX

  override fun writeToBundle(bundle: Bundle) {
    bundle.putString(UUID_KEY, uuid)
    bundle.putParcelable(REMOTE_ACTION_KEY, remoteAction)
    bundle.putString(OVERWRITE_SUBTITLE_KEY, overwriteSubtitle)
    bundle.putString(ACTION_TYPE_KEY, actionType.value)
    bundle.putString(CLOSE_CONTENT_DESCRIPTION_KEY, closeContentDescription)
    bundle.putParcelable(OVERRIDE_ICON_KEY, overrideIcon)
  }

  companion object : ClientActionParams.Creator {
    private const val UUID_KEY = "uuid"
    private const val REMOTE_ACTION_KEY = "remote_action"
    private const val OVERWRITE_SUBTITLE_KEY = "overwrite_subtitle"
    private const val ACTION_TYPE_KEY = "action_type"
    private const val CLOSE_CONTENT_DESCRIPTION_KEY = "close_content_description"
    private const val OVERRIDE_ICON_KEY = "override_icon"

    override fun create(bundle: Bundle): BlueflaxParams {
      return BlueflaxParams(
        uuid = bundle.getString(UUID_KEY) ?: "",
        remoteAction = bundle.getParcelable(REMOTE_ACTION_KEY, RemoteAction::class.java),
        overwriteSubtitle = bundle.getString(OVERWRITE_SUBTITLE_KEY) ?: "",
        actionType = BlueflaxActionType.fromValue(bundle.getString(ACTION_TYPE_KEY)),
        closeContentDescription = bundle.getString(CLOSE_CONTENT_DESCRIPTION_KEY) ?: "Close",
        overrideIcon = bundle.getParcelable(OVERRIDE_ICON_KEY, Icon::class.java),
      )
    }
  }
}
