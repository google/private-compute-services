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

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.`as`.oss.common.Executors.IO_EXECUTOR
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.delegatedui.api.integration.templates.uiIdToken
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
import com.google.android.`as`.oss.feedback.api.gateway.spoonUserInput
import com.google.android.`as`.oss.feedback.api.gateway.structuredUserInput
import com.google.android.`as`.oss.feedback.api.gateway.userDonation
import com.google.android.`as`.oss.feedback.config.FeedbackConfig
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.IntentQueries
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.LegacyV1
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.MemoryEntities
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.ModelOutputs
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.SelectedEntityContent
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.TriggeringMessages
import com.google.android.`as`.oss.feedback.gateway.FeedbackHttpClient
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDataServiceClient
import com.google.android.`as`.oss.feedback.quartz.utils.QuartzDataHelper
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDataServiceClient
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.SemanticsType
import com.google.android.`as`.oss.logging.uiusage.api.UsageDataServiceGrpcKt
import com.google.android.`as`.oss.logging.uiusage.api.logUsageDataRequest
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
  private val feedbackDataServiceClient: FeedbackDataServiceClient,
  private val quartzFeedbackDataServiceClient: QuartzFeedbackDataServiceClient,
  private val feedbackHttpClient: FeedbackHttpClient,
  private val quartzDataHelper: QuartzDataHelper,
  private val usageDataService: UsageDataServiceGrpcKt.UsageDataServiceCoroutineStub,
  private val configReader: ConfigReader<FeedbackConfig>,
  @ApplicationContext private val context: Context,
) : ViewModel() {
  private val _uiStateFlow = MutableStateFlow(FeedbackUiState(configReader))
  val uiStateFlow = _uiStateFlow.asStateFlow()
  private val _events = MutableSharedFlow<FeedbackSubmissionEvent>()
  val events = _events.asSharedFlow()

  private var loadDonationDataJob: Job? = null
  private var submitFeedbackJob: Job? = null

  fun logUiEvent(
    uiElementType: Int,
    uiElementIndex: Int = 0,
    clientSessionId: String,
    enabledState: EnabledState = EnabledState.ENABLED_STATE_UNSPECIFIED,
    interactionType: InteractionType = InteractionType.INTERACTION_TYPE_CLICK,
    semanticsType: SemanticsType = SemanticsType.SEMANTICS_TYPE_LOG_USAGE,
  ) {
    // Check for server app version for backwards compatibility.
    try {
      val serverAppVersion =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
          context.packageManager.getPackageInfo(PACKAGE_NAME, 0).longVersionCode
        } else {
          context.packageManager
            .getPackageInfo(PACKAGE_NAME, PackageManager.PackageInfoFlags.of(0))
            .longVersionCode
        }

      if (serverAppVersion > VERSION_THRESHOLD) {
        viewModelScope.launch {
          val unused =
            usageDataService.logUsageData(
              logUsageDataRequest {
                this.clientSessionUuid = clientSessionId
                this.uiIdToken = uiIdToken {
                  this.elementType = uiElementType
                  this.index = uiElementIndex
                }
                this.enabledState = enabledState
                this.semantics = semanticsType
                this.interaction = interactionType
              }
            )
        }
      } else {
        logger.atWarning().log("Server app version is too low to support the new logging API.")
      }
    } catch (e: Exception) {
      logger.atWarning().withCause(e).log("Failed to log usage data")
    }
  }

  fun updateFreeFormText(entity: FeedbackEntityContent, value: String) {
    _uiStateFlow.update { it.copy(freeFormTextMap = it.freeFormTextMap + (entity to value)) }
  }

  fun updateAllOptInChecked(value: Boolean) {
    _uiStateFlow.update {
      it.copy(
        optInChecked = it.optInChecked.filterKeys { key -> key != LegacyV1 }.mapValues { value }
      )
    }
  }

  fun updateOptInChecked(category: DataCollectionCategory, value: Boolean) {
    _uiStateFlow.update { it.copy(optInChecked = it.optInChecked + (category to value)) }
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
    _uiStateFlow.update { currentState ->
      val oldMap = currentState.tagsSelectionMap
      currentState.copy(
        tagsSelectionMap =
          oldMap.transformNestedMap(entity, sentiment) { oldTags ->
            val tagsMap = if (singleSelection) emptyMap() else oldTags
            tagsMap + (tag to value)
          }
      )
    }
  }

  fun updateTagGroundTruthSelection(
    entity: FeedbackEntityContent,
    sentiment: FeedbackRatingSentiment,
    tag: FeedbackTagData,
    option: GroundTruthData?,
  ) {
    _uiStateFlow.update { currentState ->
      // For this simpler case, the lambda just adds the new key-value pair.
      val oldMap = currentState.tagsGroundTruthSelectionMap
      currentState.copy(
        tagsGroundTruthSelectionMap =
          oldMap.transformNestedMap(entity, sentiment) { oldOptions ->
            oldOptions + (tag to option)
          }
      )
    }
  }

  fun updateFeedbackDialogMode(value: FeedbackDialogMode) {
    _uiStateFlow.update { it.copy(feedbackDialogMode = value) }
  }

  /**
   * Loads the donation data from the feedback data service.
   *
   * @param clientSessionId The client session ID to use for the donation data.
   * @param loadSpoonData Whether to load Spoon data.
   * @param quartzCuj The Quartz CUJ to load. If null, no Quartz data will be loaded.
   */
  fun loadDonationData(
    clientSessionId: String,
    loadSpoonData: Boolean,
    quartzCuj: QuartzCUJ? = null,
  ) {
    loadDonationDataJob?.cancel()
    loadDonationDataJob =
      viewModelScope.launch {
        _uiStateFlow.update { it.copy(feedbackDonationData = null) }

        var blockViewDataV2ForNotification = false
        if (loadSpoonData) {
          val response =
            feedbackDataServiceClient.getFeedbackDonationData(
              clientSessionId = clientSessionId,
              uiElementType = 0,
              uiElementIndex = 0,
            )
          _uiStateFlow.update { it.copy(feedbackDonationData = response) }
          blockViewDataV2ForNotification =
            blockViewDataV2ForNotification or
              !(response
                .getOrNull()
                ?.feedbackUiRenderingData
                ?.feedbackViewDataCategoryTitles
                ?.hasTriggeringMessagesTitle() ?: false)
        }

        if (quartzCuj != null) {
          val quartzResponse =
            quartzFeedbackDataServiceClient.getFeedbackDonationData(
              clientSessionId = clientSessionId,
              uiElementType = 0,
              uiElementIndex = 0,
              quartzCuj = quartzCuj,
            )
          _uiStateFlow.update { it.copy(quartzFeedbackDonationData = quartzResponse) }
          blockViewDataV2ForNotification =
            blockViewDataV2ForNotification or
              !(quartzResponse
                .getOrNull()
                ?.feedbackUiRenderingData
                ?.feedbackViewDataCategoryTitles
                ?.hasNotificationContentTitle() ?: false)
        }
        _uiStateFlow.update {
          it.copy(enableViewDataDialogV2MultiEntity = !blockViewDataV2ForNotification)
        }
      }
  }

  fun submitFeedback(submissionDataList: List<FeedbackSubmissionData>) {
    submitFeedbackJob?.cancel()
    submitFeedbackJob =
      viewModelScope.launch {
        _uiStateFlow.update { it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_PENDING) }

        runCatching { executeSubmitFeedback(submissionDataList) }
          .onSuccess { result -> _events.emit(result) }
          .onFailure { e ->
            logger
              .atWarning()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log("FeedbackViewModel#submitFeedback failed with exception", e)
            _events.emit(failureEvent())
          }

        _uiStateFlow.update { it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_FINISHED) }
      }
  }

  private suspend fun executeSubmitFeedback(
    submissionDataList: List<FeedbackSubmissionData>
  ): FeedbackSubmissionEvent {
    loadDonationDataJob?.join() // Wait for the data to load, if needed.
    val data = uiStateFlow.value.feedbackDonationData?.getOrNull()
    val quartzData = uiStateFlow.value.quartzFeedbackDonationData?.getOrNull()
    if (data == null && quartzData == null) {
      logger
        .atWarning()
        .log("FeedbackViewModel#Donation data unexpectedly not available. Skipping.")
      return failureEvent()
    }

    if (submissionDataList.isEmpty()) {
      logger.atWarning().log("FeedbackViewModel#submissionDataList is empty. Skipping.")
      return failureEvent()
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
    logger.atInfo().log("FeedbackViewModel#submitFeedback completed")
    return if (successful) successEvent() else failureEvent()
  }

  private fun successEvent() =
    FeedbackSubmissionEvent.Success(
      uiStateFlow.value.feedbackDonationData
        ?.getOrNull()
        ?.feedbackUiRenderingData
        ?.feedbackDialogSentSuccessfullyToast
    )

  private fun failureEvent() =
    FeedbackSubmissionEvent.Failed(
      uiStateFlow.value.feedbackDonationData
        ?.getOrNull()
        ?.feedbackUiRenderingData
        ?.feedbackDialogSentFailedToast
    )

  private suspend fun LogFeedbackV2Request.uploadFeedback(): Boolean {
    val request = this

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
      val optInChecked = uiStateFlow.value.optInChecked
      donationOption =
        if (optInChecked.any { it.value }) {
          // The semantic meaning is changed to mean *any* category opt-in.
          UserDataDonationOption.OPT_IN
        } else {
          UserDataDonationOption.OPT_OUT
        }

      // Add structured user input section.
      structuredUserInput = structuredUserInput {
        spoonUserInput = spoonUserInput {
          groundTruthList +=
            uiStateFlow.value.tagsGroundTruthSelectionMap[submissionData.selectedEntityContent]
              ?.get(RATING_SENTIMENT_THUMBS_DOWN)
              ?.mapNotNull {
                submissionData.selectedEntityContent +
                  "||" +
                  it.key.ratingTagOrdinal +
                  "||" +
                  it.value?.label
              }
              ?.toList() ?: emptyList()
        }
      }

      userDonation = userDonation {
        // Only include donation data if user has opted in the consent.
        if (optInChecked.any { it.value }) {
          structuredDataDonation = spoonFeedbackDataDonation {
            if (optInChecked.isAnyTrue(TriggeringMessages, LegacyV1)) {
              triggeringMessages += data.triggeringMessages
            }
            if (optInChecked.isAnyTrue(IntentQueries, LegacyV1)) {
              intentQueries += data.intentQueries
            }
            if (optInChecked.isAnyTrue(ModelOutputs, LegacyV1)) {
              modelOutputs += data.modelOutputs
            }
            if (optInChecked.isAnyTrue(MemoryEntities, LegacyV1)) {
              memoryEntities +=
                data.memoryEntities.map {
                  memoryEntity {
                    entityData = it.entityData
                    modelVersion = it.modelVersion
                  }
                }
            }
            if (optInChecked.isAnyTrue(SelectedEntityContent, LegacyV1)) {
              this.selectedEntityContent = submissionData.selectedEntityContent
            }
          }
        }
      }
    }
  }

  private fun <T> Map<T, Boolean>.isAnyTrue(vararg keys: T): Boolean {
    return keys.any { this[it] == true }
  }

  /**
   * Immutably updates a triply-nested map by applying a transformation at the third level. The
   * transformation allows for operations like clearing or merging before setting a value.
   */
  private fun <K1, K2, K3, V> Map<K1, Map<K2, Map<K3, V>>>.transformNestedMap(
    key1: K1,
    key2: K2,
    transform: (Map<K3, V>) -> Map<K3, V>,
  ): Map<K1, Map<K2, Map<K3, V>>> {
    val level2Map = this[key1].orEmpty()
    val level3Map = level2Map[key2].orEmpty()

    val newLevel3Map = transform(level3Map)
    val newLevel2Map = level2Map + (key2 to newLevel3Map)

    return this + (key1 to newLevel2Map)
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
    private const val PACKAGE_NAME = "com.google.android.apps.pixel.psi"
    private const val VERSION_THRESHOLD =
      1923 // The minimum server app version that supports the new logging API.

    private fun FeedbackUiState(configReader: ConfigReader<FeedbackConfig>) =
      FeedbackUiState(
        enableViewDataDialogV2SingleEntity = configReader.config.enableViewDataDialogV2SingleEntity,
        enableOptInUiV2 = configReader.config.enableOptInUiV2,
        enableGroundTruthSelectorSingleEntity =
          configReader.config.enableGroundTruthSelectorSingleEntity,
        enableGroundTruthSelectorMultiEntity =
          configReader.config.enableGroundTruthSelectorMultiEntity,
      )
  }
}
