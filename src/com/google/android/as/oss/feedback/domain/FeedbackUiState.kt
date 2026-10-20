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

package com.google.android.`as`.oss.feedback.domain

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
  val tagsGroundTruthSelectionMap:
    Map<
      FeedbackEntityContent,
      Map<FeedbackRatingSentiment, Map<FeedbackTagData, Set<GroundTruthData>>>,
    > =
    emptyMap(),
  val freeFormTextMap: Map<FeedbackEntityContent, String> = emptyMap(),
  val additionalCommentTextMap: Map<FeedbackEntityContent, String> = emptyMap(),
  val feedbackDialogMode: FeedbackDialogMode = FeedbackDialogMode.EDITING_FEEDBACK,
  val feedbackSubmitStatus: FeedbackSubmitState = FeedbackSubmitState.DRAFT,
  val feedbackDonationData: Result<FeedbackDonationData>? = null,
  val quartzFeedbackDonationData: Result<QuartzFeedbackDonationData>? = null,

  // Config values for the feedback dialog.
  val enableViewDataDialogV2SingleEntity: Boolean = false,
  val enableViewDataDialogV2MultiEntity: Boolean = false,
  val enableGroundTruthSelectorSingleEntity: Boolean = false,
  val enableGroundTruthSelectorMultiEntity: Boolean = false,
  val enableOptInUiV2: Boolean = false,
  val enableFineGrainedViewDataDialog: Boolean = false,
  val enableDefaultDonationOptInL1: Boolean = false,
  val enableDefaultDonationOptInL0: Boolean = false,

  /**
   * Stores the opt-in selection state for each [DataCollectionCategory].
   *
   * The values [OptInSelection] can be either:
   * - [OptInSelection.SingleSelection]: For categories with a single boolean opt-in.
   * - [OptInSelection.MultiSelection]: For categories with hierarchical, checkable items, where the
   *   selection state of individual items (keyed by their unique IDs) is managed internally.
   */
  val dataCollectionStates: Map<DataCollectionCategory, OptInSelection> = emptyMap(),
)

/** Sealed interface to represent the selection state for a data collection category. */
sealed interface OptInSelection {
  /** Checks if any part of the selection is positive. */
  fun isSelected(): Boolean

  /** Returns a new instance with the overall selection set to [selected]. */
  fun withSelection(selected: Boolean): OptInSelection

  /** Represents a category with a single boolean opt-in state. */
  data class SingleSelection(val selected: Boolean) : OptInSelection {
    override fun isSelected(): Boolean = selected

    override fun withSelection(selected: Boolean): SingleSelection = this.copy(selected = selected)
  }

  /** Represents a category with multiple checkable sub-items. */
  data class MultiSelection(val itemStates: Map<String, Boolean>) : OptInSelection {
    override fun isSelected(): Boolean = itemStates.values.any { it }

    override fun withSelection(selected: Boolean): MultiSelection =
      this.copy(itemStates = itemStates.mapValues { selected })

    /** Returns a new MultiSelection with selection state of [ids] set to [selected]. */
    fun withItemSelections(ids: Collection<String>, selected: Boolean): MultiSelection =
      this.copy(itemStates = itemStates + ids.associateWith { selected })

    val allSelected: Boolean
      get() = itemStates.keys.isNotEmpty() && itemStates.values.all { it }

    val isIndeterminate: Boolean
      get() = isSelected() && !allSelected
  }
}

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
sealed class FeedbackSubmissionEvent(open val message: String?) {

  data class Success(override val message: String?, val interactionId: String? = "") :
    FeedbackSubmissionEvent(message)

  data class Failed(override val message: String?) : FeedbackSubmissionEvent(message)
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
