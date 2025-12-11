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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.feedback.FeedbackApi.EXTRA_ENTITY_FEEDBACK_DIALOG_DATA_PROTO
import com.google.android.`as`.oss.feedback.FeedbackApi.EXTRA_MULTI_FEEDBACK_DIALOG_DATA_PROTO
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData
import com.google.android.`as`.oss.feedback.config.FeedbackConfig
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionEvent
import com.google.android.`as`.oss.feedback.ui.MultiEntityFeedbackDialog
import com.google.android.`as`.oss.feedback.ui.MultiEntityFeedbackDialogV1
import com.google.android.`as`.oss.feedback.ui.SingleEntityFeedbackDialog
import com.google.android.`as`.oss.feedback.ui.SingleEntityFeedbackDialogV1
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** An activity to host UI components for a user to give feedback on an entity or bundle. */
@AndroidEntryPoint(ComponentActivity::class)
class FeedbackActivity : Hilt_FeedbackActivity() {

  @Inject lateinit var configReader: ConfigReader<FeedbackConfig>

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

      logger
        .atInfo()
        .log(
          "FeedbackActivity.onCreate() with entityFeedbackDialogData: %s, multiFeedbackDialogData: %s.",
          entityFeedbackDialogData,
          multiFeedbackDialogData,
        )

      setContent {
        when {
          entityFeedbackDialogData != null -> {
            SingleEntityFeedbackDialogWrapper(
              data = entityFeedbackDialogData,
              onFeedbackEvent = { event: FeedbackSubmissionEvent ->
                when (event) {
                  is FeedbackSubmissionEvent.Success -> setResult(RESULT_OK)
                  is FeedbackSubmissionEvent.Failed -> setResult(RESULT_CANCELED)
                }
              },
              onDismissRequest = { finish() },
            )
          }
          multiFeedbackDialogData != null -> {
            MultiEntityFeedbackDialogWrapper(
              data = multiFeedbackDialogData,
              onDismissRequest = { finish() },
            )
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

  @Composable
  private fun SingleEntityFeedbackDialogWrapper(
    data: EntityFeedbackDialogData,
    onFeedbackEvent: (FeedbackSubmissionEvent) -> Unit,
    onDismissRequest: () -> Unit,
  ) {
    if (configReader.config.enableOptInUiV2) {
      SingleEntityFeedbackDialog(
        data = data,
        onFeedbackEvent = onFeedbackEvent,
        onDismissRequest = onDismissRequest,
      )
    } else {
      SingleEntityFeedbackDialogV1(
        data = data,
        onFeedbackEvent = onFeedbackEvent,
        onDismissRequest = onDismissRequest,
      )
    }
  }

  @Composable
  private fun MultiEntityFeedbackDialogWrapper(
    data: MultiFeedbackDialogData,
    onDismissRequest: () -> Unit,
  ) {
    if (configReader.config.enableOptInUiV2) {
      MultiEntityFeedbackDialog(data = data, onDismissRequest = onDismissRequest)
    } else {
      MultiEntityFeedbackDialogV1(data = data, onDismissRequest = onDismissRequest)
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
