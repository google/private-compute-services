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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.`as`.oss.common.Executors.IO_EXECUTOR
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.NegativeRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.PositiveRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.feedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.logFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.memoryEntity
import com.google.android.`as`.oss.feedback.api.gateway.runtimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.spoonFeedbackDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.userDonation
import com.google.android.`as`.oss.feedback.gateway.FeedbackHttpClient
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDataServiceClient
import com.google.android.`as`.oss.feedback.quartz.utils.QuartzDataHelper
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDataServiceClient
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** View model for [SingleEntityFeedbackDialog] and [MultiEntityFeedbackDialog]. */
@HiltViewModel
class FeedbackViewModel
@Inject
constructor(
  private var feedbackDataServiceClient: FeedbackDataServiceClient,
  private var quartzFeedbackDataServiceClient: QuartzFeedbackDataServiceClient,
  private var feedbackHttpClient: FeedbackHttpClient,
  private var quartzDataHelper: QuartzDataHelper,
) : ViewModel() {
  private val _uiStateFlow = MutableStateFlow(FeedbackUiState())
  val uiStateFlow = _uiStateFlow.asStateFlow()
  private val _events = MutableSharedFlow<FeedbackEvent>()
  val events = _events.asSharedFlow()

  private var loadDonationDataJob: Job? = null
  private var submitFeedbackJob: Job? = null

  fun updateFreeFormText(entity: FeedbackEntityContent, value: String) {
    _uiStateFlow.update { it.copy(freeFormTextMap = it.freeFormTextMap + (entity to value)) }
  }

  fun updateOptInChecked(value: Boolean) {
    _uiStateFlow.update { it.copy(optInChecked = value) }
  }

  fun updateSelectedSentiment(entity: FeedbackEntityContent, sentiment: FeedbackRatingSentiment) {
    _uiStateFlow.update {
      it.copy(selectedSentimentMap = it.selectedSentimentMap + (entity to sentiment))
    }
  }

  fun updateTagSelection(
    entity: FeedbackEntityContent,
    sentiment: FeedbackRatingSentiment,
    tag: FeedbackTagData,
    value: Boolean,
    singleSelection: Boolean = false,
  ) {
    _uiStateFlow.update {
      val oldTags = it.tagsSelectionMap[entity].orEmpty()[sentiment].orEmpty().toMutableMap()
      if (singleSelection) oldTags.clear()
      val tags: Map<FeedbackTagData, Boolean> = oldTags + (tag to value)
      val sentiments: Map<FeedbackRatingSentiment, Map<FeedbackTagData, Boolean>> =
        it.tagsSelectionMap[entity].orEmpty() + (sentiment to tags)
      it.copy(tagsSelectionMap = it.tagsSelectionMap + (entity to sentiments))
    }
  }

  fun updateFeedbackDialogMode(value: FeedbackDialogMode) {
    _uiStateFlow.update { it.copy(feedbackDialogMode = value) }
  }

  fun loadDonationData(clientSessionId: String, quartzCuj: QuartzCUJ? = null) {
    loadDonationDataJob?.cancel()
    loadDonationDataJob =
      viewModelScope.launch {
        _uiStateFlow.update { it.copy(feedbackDonationData = null) }
        val response =
          feedbackDataServiceClient.getFeedbackDonationData(
            clientSessionId = clientSessionId,
            uiElementType = 0,
            uiElementIndex = 0,
          )
        _uiStateFlow.update { it.copy(feedbackDonationData = response) }

        if (quartzCuj != null) {
          val quartzResponse =
            quartzFeedbackDataServiceClient.getFeedbackDonationData(
              clientSessionId = clientSessionId,
              uiElementType = 0,
              uiElementIndex = 0,
              quartzCuj = quartzCuj,
            )
          _uiStateFlow.update { it.copy(quartzFeedbackDonationData = quartzResponse) }
        }
      }
  }

  fun submitFeedback(submissionDataList: List<FeedbackSubmissionData>) {
    submitFeedbackJob?.cancel()
    submitFeedbackJob =
      viewModelScope.launch {
        try {
          _uiStateFlow.update { it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_PENDING) }

          loadDonationDataJob?.join() // Wait for the data to load, if needed.
          val data = uiStateFlow.value.feedbackDonationData?.getOrNull()
          val quartzData = uiStateFlow.value.quartzFeedbackDonationData?.getOrNull()
          if (data == null && quartzData == null) {
            logger
              .atWarning()
              .log("FeedbackViewModel#Donation data unexpectedly not available. Skipping.")
            _events.emit(FeedbackEvent.SUBMISSION_FAILED)
            return@launch
          }

          if (submissionDataList.isEmpty()) {
            logger.atWarning().log("FeedbackViewModel#submissionDataList is empty. Skipping.")
            _events.emit(FeedbackEvent.SUBMISSION_FAILED)
            return@launch
          }

          val successful =
            submissionDataList
              .mapNotNull { submissionData ->
                if (submissionData.quartzCuj != null && quartzData != null) {
                  with(quartzDataHelper) {
                    submissionData.toQuartzFeedbackUploadRequest(quartzData, uiStateFlow.value)
                  }
                } else if (data != null) {
                  submissionData.toFeedbackUploadRequest(data)
                } else {
                  logger
                    .atWarning()
                    .log(
                      "No valid donation data (Spoon or Quartz) for submission: %s",
                      submissionData.selectedEntityContent,
                    )
                  null
                }
              }
              .map { request -> request.uploadFeedback() }
              .all { success -> success }
          if (successful) {
            logger.atInfo().log("FeedbackViewModel#submitFeedback successful")
          }
          _events.emit(
            if (successful) FeedbackEvent.SUBMISSION_SUCCESSFUL else FeedbackEvent.SUBMISSION_FAILED
          )
        } finally {
          _uiStateFlow.update {
            it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_FINISHED)
          }
        }
      }
  }

  private suspend fun LogFeedbackV2Request.uploadFeedback(): Boolean {
    val request = this

    logger.atInfo().log("FeedbackViewModel#submitFeedback with request: %s", request)
    val success =
      withContext(IO_EXECUTOR.asCoroutineDispatcher()) {
        feedbackHttpClient.uploadFeedback(request)
      }

    if (!success) {
      logger.atInfo().log("FeedbackViewModel#submitFeedback failed with request")
    } else {
      logger.atInfo().log("FeedbackViewModel#submitFeedback successful with request")
    }

    return success
  }

  private fun FeedbackSubmissionData.toFeedbackUploadRequest(
    data: FeedbackDonationData
  ): LogFeedbackV2Request? {
    val submissionData = this
    return logFeedbackV2Request {
      this.appId = data.appId
      this.interactionId = data.interactionId
      this.feedbackCuj = feedbackCUJ { spoonFeedbackCuj = submissionData.cuj ?: data.cuj }
      this.rating =
        when (submissionData.ratingSentiment) {
          RATING_SENTIMENT_THUMBS_UP -> Rating.THUMB_UP
          RATING_SENTIMENT_THUMBS_DOWN -> Rating.THUMB_DOWN
          else -> Rating.RATING_UNSPECIFIED
        }
      uiStateFlow.value.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_UP)
        ?.let { entry ->
          this.positiveTags += entry.keys.map { PositiveRatingTag.entries[it.ratingTagOrdinal] }
        }
      uiStateFlow.value.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_DOWN)
        ?.let { entry ->
          this.negativeTags += entry.keys.map { NegativeRatingTag.entries[it.ratingTagOrdinal] }
        }
      additionalComment =
        uiStateFlow.value.freeFormTextMap[submissionData.selectedEntityContent] ?: ""
      runtimeConfig = runtimeConfig {
        appBuildType = data.runtimeConfig.appBuildType
        appVersion = data.runtimeConfig.appVersion
        modelMetadata = data.runtimeConfig.modelMetadata
        modelId = data.runtimeConfig.modelId
      }
      donationOption =
        if (uiStateFlow.value.optInChecked) {
          UserDataDonationOption.OPT_IN
        } else {
          UserDataDonationOption.OPT_OUT
        }
      userDonation = userDonation {
        // Only include donation data if user has opted in the consent.
        if (uiStateFlow.value.optInChecked) {
          structuredDataDonation = spoonFeedbackDataDonation {
            triggeringMessages += data.triggeringMessages
            intentQueries += data.intentQueries
            modelOutputs += data.modelOutputs
            memoryEntities +=
              data.memoryEntities.map {
                memoryEntity {
                  entityData = it.entityData
                  modelVersion = it.modelVersion
                }
              }
            this.selectedEntityContent = submissionData.selectedEntityContent
          }
        }
      }
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
