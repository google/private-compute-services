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

import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import java.util.Collections.emptyMap

/** Ui state for [SingleEntityFeedbackDialog] and [MultiEntityFeedbackDialog]. */
data class FeedbackUiState(
  val selectedSentimentMap: Map<FeedbackEntityContent, FeedbackRatingSentiment> = emptyMap(),
  val tagsSelectionMap:
    Map<FeedbackEntityContent, Map<FeedbackRatingSentiment, Map<FeedbackTagData, Boolean>>> =
    emptyMap(),
  val freeFormTextMap: Map<FeedbackEntityContent, String> = emptyMap(),
  val optInChecked: Boolean = false,
  val feedbackDialogMode: FeedbackDialogMode = FeedbackDialogMode.EDITING_FEEDBACK,
  val feedbackSubmitStatus: FeedbackSubmitState = FeedbackSubmitState.DRAFT,
  val feedbackDonationData: Result<FeedbackDonationData>? = null,
  val quartzFeedbackDonationData: Result<QuartzFeedbackDonationData>? = null,
)

/** The modes that a feedback dialog can be in. */
enum class FeedbackDialogMode {
  EDITING_FEEDBACK,
  VIEWING_FEEDBACK_DONATION_DATA,
}

/** The state of feedback submission. */
enum class FeedbackSubmitState {
  DRAFT,
  SUBMIT_PENDING,
  /** May have resulted in a success or failure. */
  SUBMIT_FINISHED,
}

/** One time events. */
enum class FeedbackEvent {
  SUBMISSION_SUCCESSFUL,
  SUBMISSION_FAILED,
}

/** Token representing the feedback entity. */
typealias FeedbackEntityContent = String

/** Return values depending on the state of the Result. */
inline fun <R, T> Result<T>?.fold(
  onNull: () -> R,
  onSuccess: (value: T) -> R,
  onFailure: (exception: Throwable) -> R,
): R {
  return if (this == null) {
    onNull()
  } else {
    fold(onSuccess, onFailure)
  }
}
