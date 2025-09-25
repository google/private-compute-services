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

package com.google.android.`as`.oss.feedback

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData

/** The public API for showing the FeedbackActivity. */
object FeedbackApi {

  /** Key for the [EntityFeedbackDialogData] proto extra in the Intent bundle. */
  internal const val EXTRA_ENTITY_FEEDBACK_DIALOG_DATA_PROTO: String =
    "entity_feedback_dialog_data_proto"
  /** Key for the [MultiFeedbackDialogData] proto extra in the Intent bundle. */
  internal const val EXTRA_MULTI_FEEDBACK_DIALOG_DATA_PROTO: String =
    "multi_feedback_dialog_data_proto"

  private const val PCS_PKG_NAME: String = "com.google.android.as.oss"
  private const val FEEDBACK_ACTIVITY_NAME: String =
    "com.google.android.as.oss.feedback.FeedbackActivity"

  /**
   * Creates an [Intent] that starts [FeedbackActivity] to allow the user to give feedback on a
   * single entity.
   */
  fun createEntityFeedbackIntent(context: Context, data: EntityFeedbackDialogData): Intent {
    val intent =
      Intent(Intent.ACTION_MAIN).apply {
        setComponent(ComponentName(PCS_PKG_NAME, FEEDBACK_ACTIVITY_NAME))
        // FLAG_ACTIVITY_NEW_TASK: Needed to start an Activity outside of an Activity context.
        // FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS: Ensure the Activity launched doesn't show up as a
        // separate task when a user opens recents.
        setFlags(
          FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        putExtra(EXTRA_ENTITY_FEEDBACK_DIALOG_DATA_PROTO, data.toByteArray())
      }

    return intent
  }

  /**
   * Creates an [Intent] that starts [FeedbackActivity] to allow the user to give feedback on
   * multiple entities.
   */
  fun createMultiFeedbackIntent(context: Context, data: MultiFeedbackDialogData): Intent {
    val intent =
      Intent(Intent.ACTION_MAIN).apply {
        setComponent(ComponentName(PCS_PKG_NAME, FEEDBACK_ACTIVITY_NAME))
        // FLAG_ACTIVITY_NEW_TASK: Needed to start an Activity outside of an Activity context.
        // FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS: Ensure the Activity launched doesn't show up as a
        // separate task when a user opens recents.
        setFlags(
          FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        putExtra(EXTRA_MULTI_FEEDBACK_DIALOG_DATA_PROTO, data.toByteArray())
      }

    return intent
  }
}
