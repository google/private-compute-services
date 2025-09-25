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

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.android.`as`.oss.feedback.FeedbackApi.EXTRA_ENTITY_FEEDBACK_DIALOG_DATA_PROTO
import com.google.android.`as`.oss.feedback.FeedbackApi.EXTRA_MULTI_FEEDBACK_DIALOG_DATA_PROTO
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import dagger.hilt.android.AndroidEntryPoint

/** An activity to host UI components for a user to give feedback on an entity or bundle. */
@AndroidEntryPoint(ComponentActivity::class)
class FeedbackActivity : Hilt_FeedbackActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      val entityFeedbackDialogData =
        intent.getByteArrayExtra(EXTRA_ENTITY_FEEDBACK_DIALOG_DATA_PROTO)?.let {
          EntityFeedbackDialogData.parseFrom(it)
        }
      val multiFeedbackDialogData =
        intent.getByteArrayExtra(EXTRA_MULTI_FEEDBACK_DIALOG_DATA_PROTO)?.let {
          MultiFeedbackDialogData.parseFrom(it)
        }

      if (entityFeedbackDialogData == null && multiFeedbackDialogData == null) {
        logger.atSevere().log("No feedback data request provided. Finishing activity.")
        finish()
        return
      }

      setContent {
        when {
          entityFeedbackDialogData != null -> {
            SingleEntityFeedbackDialog(
              data = checkNotNull(entityFeedbackDialogData),
              onFeedbackEvent = { event: FeedbackEvent ->
                when (event) {
                  FeedbackEvent.SUBMISSION_SUCCESSFUL -> {
                    setResult(Activity.RESULT_OK)
                  }
                  FeedbackEvent.SUBMISSION_FAILED -> {
                    setResult(Activity.RESULT_CANCELED)
                  }
                }
              },
              onDismissRequest = { finish() },
            )
          }
          multiFeedbackDialogData != null -> {
            MultiEntityFeedbackDialog(data = multiFeedbackDialogData) { finish() }
          }
        }
      }
    } catch (e: Exception) {
      logger
        .atSevere()
        .withCause(e)
        .withStackTrace(StackSize.SMALL)
        .log("FeedbackActivity.onCreate() failed. Finishing activity.")
      finish()
      return
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
