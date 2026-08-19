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
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.`as`.oss.common.Executors.IO_EXECUTOR
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.delegatedui.api.integration.templates.uiIdToken
import com.google.android.`as`.oss.feedback.FeedbackApi
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.dataservice.GetFeedbackDonationDataResponse
import com.google.android.`as`.oss.feedback.api.gateway.BlueflaxCUJ
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.NegativeRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.PositiveRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.SpoonFeedbackDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.feedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.logFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.memoryEntity
import com.google.android.`as`.oss.feedback.api.gateway.runtimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.sourceDocument
import com.google.android.`as`.oss.feedback.api.gateway.spoonFeedbackDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.spoonUserInput
import com.google.android.`as`.oss.feedback.api.gateway.structuredUserInput
import com.google.android.`as`.oss.feedback.api.gateway.userDonation
import com.google.android.`as`.oss.feedback.blueflax.utils.BlueflaxDataHelper
import com.google.android.`as`.oss.feedback.blueflax.utils.isBlueflax
import com.google.android.`as`.oss.feedback.blueflax.utils.toFeedbackDonationData
import com.google.android.`as`.oss.feedback.config.FeedbackConfig
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.FailureReason
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.IntentQueries
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.LegacyV1
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.MemoryEntities
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.ModelOutputs
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.SelectedEntityContent
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.TriggeringMessages
import com.google.android.`as`.oss.feedback.domain.OptInSelection.MultiSelection
import com.google.android.`as`.oss.feedback.domain.OptInSelection.SingleSelection
import com.google.android.`as`.oss.feedback.gateway.FeedbackHttpClient
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDataServiceClient
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
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
  private val blueflaxDataHelper: BlueflaxDataHelper,
  private val usageDataService: UsageDataServiceGrpcKt.UsageDataServiceCoroutineStub,
  private val configReader: ConfigReader<FeedbackConfig>,
  @ApplicationContext private val context: Context,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
  private val _uiStateFlow = MutableStateFlow(FeedbackUiState(configReader))
  val uiStateFlow = _uiStateFlow.asStateFlow()
  private val _events = MutableSharedFlow<FeedbackSubmissionEvent>()
  val events = _events.asSharedFlow()

  val hasDonationDataExtra: Boolean
    get() = savedStateHandle.contains(FeedbackApi.EXTRA_FEEDBACK_DONATION_DATA_RESPONSE_PROTO)

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

  fun updateAdditionalCommentText(entity: FeedbackEntityContent, value: String) {
    _uiStateFlow.update {
      it.copy(additionalCommentTextMap = it.additionalCommentTextMap + (entity to value))
    }
  }

  fun updateAllDataCollectionOptInStates(value: Boolean) {
    _uiStateFlow.update { currentState ->
      currentState.copy(
        dataCollectionStates =
          currentState.dataCollectionStates.mapValues { (category, selection) ->
            if (category != LegacyV1) {
              selection.withSelection(value)
            } else {
              selection
            }
          }
      )
    }
  }

  /**
   * Updates the checked state of data collection items.
   *
   * - If [item] is provided (not null): Sets the checked state of the specific
   *   [FeedbackBodyItem.CheckableListItem] and all its recursive children to [isChecked]. This is
   *   only applicable for categories with [MultiSelection].
   * - If [item] is null (default): Sets the checked state of the *entire* [DataCollectionCategory]
   *   to [isChecked]. This applies to all sub-items if the category uses [MultiSelection], and to
   *   the category itself if it uses [SingleSelection].
   *
   * @param category The category to update.
   * @param item The specific CheckableListItem to update. Defaults to null to update the whole
   *   category.
   * @param isChecked The absolute checked state to apply.
   */
  fun updateDataCollectionOptInState(
    category: DataCollectionCategory,
    isChecked: Boolean,
    item: FeedbackBodyItem.CheckableListItem? = null,
  ) {
    _uiStateFlow.update { currentState ->
      val selection = currentState.dataCollectionStates[category]

      if (selection == null) {
        logger.atWarning().log("updateDataCollectionOptInState: Category not found: %s", category)
        return@update currentState
      }

      val newSelection =
        when {
          item == null -> {
            // Item is null, so update the entire category state.
            // This works for both SingleSelection and MultiSelection via the interface method.
            selection.withSelection(isChecked)
          }
          selection is OptInSelection.MultiSelection -> {
            // Item is non-null and we have a MultiSelection, update specific item/children.
            val ids = item.getAllDescendantIds()
            selection.withItemSelections(ids, isChecked)
          }
          selection is OptInSelection.SingleSelection -> {
            // Item is non-null, but the category is SingleSelection. This indicates a mismatch
            // between the UI sending an item and the category type. Log a warning.
            logger
              .atWarning()
              .log(
                "updateDataCollectionOptInState: Called with non-null item for category %s which is SingleSelection",
                category,
              )
            selection
          }
          else -> {
            logger
              .atWarning()
              .log("updateDataCollectionOptInState: Category not found: %s", category)
            selection
          }
        }

      currentState.copy(
        dataCollectionStates = currentState.dataCollectionStates + (category to newSelection)
      )
    }
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

    if (!value) {
      clearTagGroundTruthSelection(entity = entity, sentiment = sentiment, tag = tag)
    }
  }

  fun toggleTagGroundTruthSelection(
    entity: FeedbackEntityContent,
    sentiment: FeedbackRatingSentiment,
    tag: FeedbackTagData,
    option: GroundTruthData,
    singleSelection: Boolean = false,
  ) {
    _uiStateFlow.update { currentState ->
      val oldMap = currentState.tagsGroundTruthSelectionMap
      currentState.copy(
        tagsGroundTruthSelectionMap =
          oldMap.transformNestedMap(entity, sentiment) { oldOptionsSets ->
            val currentSet = oldOptionsSets[tag] ?: emptySet()
            val newSet =
              if (currentSet.contains(option)) {
                currentSet - option
              } else if (singleSelection) {
                setOf(option)
              } else {
                currentSet + option
              }
            if (newSet.isEmpty()) {
              oldOptionsSets - tag
            } else if (singleSelection) {
              mapOf(tag to newSet)
            } else {
              oldOptionsSets + (tag to newSet)
            }
          }
      )
    }
  }

  private fun clearTagGroundTruthSelection(
    entity: FeedbackEntityContent,
    sentiment: FeedbackRatingSentiment,
    tag: FeedbackTagData,
  ) {
    _uiStateFlow.update { currentState ->
      val oldMap = currentState.tagsGroundTruthSelectionMap
      currentState.copy(
        tagsGroundTruthSelectionMap =
          oldMap.transformNestedMap(entity, sentiment) { oldOptionsSets -> oldOptionsSets - tag }
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
    loadDonationDataJob = viewModelScope.launch {
      resetDonationState()

      var blockViewDataV2ForNotification = false

      val spoonResponse =
        if (loadSpoonData) {
          feedbackDataServiceClient.getFeedbackDonationData(
            clientSessionId = clientSessionId,
            uiElementType = 0,
            uiElementIndex = 0,
          )
        } else {
          null
        }
      val spoonData = spoonResponse?.getOrNull()
      if (spoonResponse != null) {
        blockViewDataV2ForNotification =
          blockViewDataV2ForNotification or
            !(spoonData
              ?.feedbackUiRenderingData
              ?.feedbackViewDataCategoryTitles
              ?.hasTriggeringMessagesTitle() ?: false)
      }

      val quartzResponse =
        if (quartzCuj != null) {
          quartzFeedbackDataServiceClient.getFeedbackDonationData(
            clientSessionId = clientSessionId,
            uiElementType = 0,
            uiElementIndex = 0,
            quartzCuj = quartzCuj,
          )
        } else {
          null
        }
      val quartzData = quartzResponse?.getOrNull()
      if (quartzResponse != null) {
        blockViewDataV2ForNotification =
          blockViewDataV2ForNotification or
            !(quartzData
              ?.feedbackUiRenderingData
              ?.feedbackViewDataCategoryTitles
              ?.hasNotificationContentTitle() ?: false)
      }

      val dataCollectionStates =
        createDataCollectionStates(
          spoonData = spoonData,
          quartzCuj = quartzCuj,
          quartzData = quartzData,
        )

      updateDonationState(
        feedbackDonationDataResult = spoonResponse,
        quartzDonationDataResult = quartzResponse,
        dataCollectionStates = dataCollectionStates,
        enableViewDataDialogV2MultiEntity = !blockViewDataV2ForNotification,
      )
    }
  }

  fun loadDonationDataFromExtra(blueflaxCuj: BlueflaxCUJ? = null) {
    loadDonationDataJob?.cancel()
    loadDonationDataJob = viewModelScope.launch {
      resetDonationState()

      val donationDataResponseBytes =
        savedStateHandle.get<ByteArray>(FeedbackApi.EXTRA_FEEDBACK_DONATION_DATA_RESPONSE_PROTO)
      if (donationDataResponseBytes == null) {
        logger.atWarning().log("Missing donation data response proto in SavedStateHandle.")
        updateDonationState(
          feedbackDonationDataResult =
            Result.failure<FeedbackDonationData>(
              IllegalStateException("No valid donation data extra found")
            ),
          dataCollectionStates = emptyMap(),
          enableViewDataDialogV2MultiEntity = false,
        )
        return@launch
      }

      logger.atInfo().log("Loading donation data directly from SavedStateHandle extra.")
      try {
        val response = GetFeedbackDonationDataResponse.parseFrom(donationDataResponseBytes)
        val blueflaxData =
          if (blueflaxCuj != null) {
            with(blueflaxDataHelper) { response.toFeedbackDonationData() }
          } else {
            null
          }
        val donationData =
          blueflaxData
            ?: throw UnsupportedOperationException("Unsupported client in donation data extra")

        val dataCollectionStates =
          createDataCollectionStates(blueflaxCuj = blueflaxCuj, blueflaxData = blueflaxData)
        val enableViewDataDialogV2MultiEntity =
          donationData.feedbackUiRenderingData.feedbackViewDataCategoryTitles
            .hasTriggeringMessagesTitle()
        updateDonationState(
          feedbackDonationDataResult = Result.success(donationData),
          dataCollectionStates = dataCollectionStates,
          enableViewDataDialogV2MultiEntity = enableViewDataDialogV2MultiEntity,
        )
      } catch (e: Exception) {
        logger
          .atWarning()
          .withCause(e)
          .log("Failed to load donation data from SavedStateHandle extra.")
        updateDonationState(
          feedbackDonationDataResult = Result.failure<FeedbackDonationData>(e),
          dataCollectionStates = emptyMap(),
          enableViewDataDialogV2MultiEntity = false,
        )
      }
    }
  }

  fun submitFeedback(submissionDataList: List<FeedbackSubmissionData>) {
    submitFeedbackJob?.cancel()
    submitFeedbackJob = viewModelScope.launch {
      _uiStateFlow.update { it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_PENDING) }

      runCatching { executeSubmitFeedback(submissionDataList) }
        .onSuccess { result -> _events.emit(result) }
        .onFailure { e ->
          logger
            .atWarning()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log("FeedbackViewModel#submitFeedback failed with exception")
          _events.emit(failureEvent())
        }

      _uiStateFlow.update { it.copy(feedbackSubmitStatus = FeedbackSubmitState.SUBMIT_FINISHED) }
    }
  }

  private fun resetDonationState() {
    _uiStateFlow.update {
      it.copy(
        feedbackDonationData = null,
        quartzFeedbackDonationData = null,
        dataCollectionStates = emptyMap(),
      )
    }
  }

  private fun updateDonationState(
    feedbackDonationDataResult: Result<FeedbackDonationData>? = null,
    quartzDonationDataResult: Result<QuartzFeedbackDonationData>? = null,
    dataCollectionStates: Map<DataCollectionCategory, OptInSelection> = emptyMap(),
    enableViewDataDialogV2MultiEntity: Boolean,
  ) {
    _uiStateFlow.update {
      it.copy(
        feedbackDonationData = feedbackDonationDataResult ?: it.feedbackDonationData,
        quartzFeedbackDonationData = quartzDonationDataResult ?: it.quartzFeedbackDonationData,
        dataCollectionStates = dataCollectionStates,
        enableViewDataDialogV2MultiEntity = enableViewDataDialogV2MultiEntity,
      )
    }
  }

  private fun createDataCollectionStates(
    spoonData: FeedbackDonationData? = null,
    quartzCuj: QuartzCUJ? = null,
    quartzData: QuartzFeedbackDonationData? = null,
    blueflaxCuj: BlueflaxCUJ? = null,
    blueflaxData: FeedbackDonationData? = null,
  ): Map<DataCollectionCategory, OptInSelection> {
    val cujName = spoonData?.cuj?.name ?: quartzCuj?.name ?: blueflaxCuj?.name ?: ""
    val data = spoonData ?: quartzData ?: blueflaxData

    if (data == null) return emptyMap()

    val defaultOptInCategories =
      configReader.config.dataCollectionCategoryDefaultOptIn[cujName]?.toSet() ?: emptySet()

    val allCategories: Set<DataCollectionCategory> =
      if (data is FeedbackDonationData) {
        data.dataCollectionCategories.keys + SelectedEntityContent
      } else {
        data.dataCollectionCategories.keys
      }

    return allCategories.associateWith { category ->
      val categoryDataItems = data.dataCollectionCategories[category]?.items ?: emptyList()
      val checkableItems = extractCheckableItems(categoryDataItems)

      if (checkableItems.isNotEmpty()) {
        MultiSelection(checkableItems)
      } else {
        SingleSelection(category in defaultOptInCategories)
      }
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
          } else if (submissionData.isBlueflax && data != null) {
            with(blueflaxDataHelper) {
              submissionData.toBlueflaxFeedbackUploadRequest(data, uiStateFlow.value)
            }
          } else if (data != null) {
            submissionData.toFeedbackUploadRequest(data)
          } else {
            logger
              .atWarning()
              .log(
                "No valid donation data for submission: %s",
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

  private fun getGroundTruthList(
    submissionData: FeedbackSubmissionData,
    uiState: FeedbackUiState,
  ): List<String> {
    return uiState.tagsGroundTruthSelectionMap[submissionData.selectedEntityContent]
      ?.get(RATING_SENTIMENT_THUMBS_DOWN)
      ?.flatMap { (tag, gtSet) -> // Iterate through each tag and its set of ground truths
        gtSet.map { gt -> // Iterate through each GroundTruthData in the set
          val formattedGt = formatSingleGroundTruth(gt)
          "Entity content: ${submissionData.selectedEntityContent}, Rating sentiment: ${tag.ratingTagOrdinal}, Ground truth: $formattedGt"
        }
      } ?: emptyList()
  }

  private fun formatSingleGroundTruth(gt: GroundTruthData): String {
    return if (gt.sourceApp.isNotEmpty()) "[${gt.sourceApp}] ${gt.label}" else gt.label
  }

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

  private fun isCategorySelected(category: DataCollectionCategory): Boolean {
    val dataCollectionStates = uiStateFlow.value.dataCollectionStates
    val categorySelected = dataCollectionStates[category]?.isSelected() ?: false
    val legacySelected = dataCollectionStates[LegacyV1]?.isSelected() ?: false
    return categorySelected || legacySelected
  }

  private fun FeedbackSubmissionData.toFeedbackUploadRequest(
    data: FeedbackDonationData
  ): LogFeedbackV2Request? {
    val submissionData = this
    val currentUiState = uiStateFlow.value
    val dataCollectionStates = currentUiState.dataCollectionStates

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
      currentUiState.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_UP)
        ?.let { entry ->
          val tags = entry.filterValues { it }.keys
          this.positiveTags += tags.map { PositiveRatingTag.entries[it.ratingTagOrdinal] }
        }
      currentUiState.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_DOWN)
        ?.let { entry ->
          val tags = entry.filterValues { it }.keys
          this.negativeTags += tags.map { NegativeRatingTag.entries[it.ratingTagOrdinal] }
        }
      additionalComment = currentUiState.freeFormTextMap[submissionData.selectedEntityContent] ?: ""
      runtimeConfig = runtimeConfig {
        appBuildType = data.runtimeConfig.appBuildType
        appVersion = data.runtimeConfig.appVersion
        modelMetadata = data.runtimeConfig.modelMetadata
        modelId = data.runtimeConfig.modelId
      }

      val anyOptedIn = dataCollectionStates.values.any { it.isSelected() }

      donationOption =
        if (anyOptedIn) {
          UserDataDonationOption.OPT_IN
        } else {
          UserDataDonationOption.OPT_OUT
        }

      structuredUserInput = structuredUserInput {
        spoonUserInput = spoonUserInput {
          groundTruthList += getGroundTruthList(submissionData, currentUiState)
          optionalSpoonComment =
            currentUiState.additionalCommentTextMap[submissionData.selectedEntityContent] ?: ""
        }
      }

      userDonation = userDonation {
        if (anyOptedIn) {
          structuredDataDonation =
            constructSpoonDataDonation(data, dataCollectionStates, submissionData)
        }
      }
    }
  }

  private fun constructSpoonDataDonation(
    data: FeedbackDonationData,
    dataCollectionStates: Map<DataCollectionCategory, OptInSelection>,
    submissionData: FeedbackSubmissionData,
  ): SpoonFeedbackDataDonation = spoonFeedbackDataDonation {
    if (isCategorySelected(TriggeringMessages)) {
      triggeringMessages += data.triggeringMessages
    }
    if (isCategorySelected(IntentQueries)) {
      intentQueries += data.intentQueries
    }
    if (isCategorySelected(ModelOutputs)) {
      modelOutputs += data.modelOutputs
    }
    if (isCategorySelected(SelectedEntityContent)) {
      this.selectedEntityContent = submissionData.selectedEntityContent
    }
    if (isCategorySelected(FailureReason)) {
      this.failureReason = data.failureReason
    }

    val memorySelection = dataCollectionStates[MemoryEntities]
    if (memorySelection is MultiSelection && memorySelection.isSelected()) {
      if (data.sourceDocuments.isNotEmpty()) {
        val donatedSourceDocuments =
          data.sourceDocuments.mapIndexedNotNull { index, doc ->
            val docId = "${FeedbackDonationData.DOC_ID_PREFIX}$index"
            val entityId = "${docId}${FeedbackDonationData.ENTITY_ID_SUFFIX}"
            val l0Id = "${docId}${FeedbackDonationData.L0_ID_SUFFIX}"

            val entityChecked = memorySelection.itemStates[entityId] ?: false
            val l0Checked = memorySelection.itemStates[l0Id] ?: false

            if (entityChecked || l0Checked) {
              sourceDocument {
                this.l0Title = doc.l0Title
                if (entityChecked) {
                  this.memoryEntity = memoryEntity {
                    entityData = doc.memoryEntity.entityData
                    modelVersion = doc.memoryEntity.modelVersion
                  }
                }
                if (l0Checked) {
                  this.l0Content = doc.l0Content
                }
              }
            } else {
              null
            }
          }
        this.sourceDocuments += donatedSourceDocuments
      }
    } else if (isCategorySelected(MemoryEntities)) {
      // Fallback for SingleSelection or older data structures
      memoryEntities +=
        data.memoryEntities.map {
          memoryEntity {
            entityData = it.entityData
            modelVersion = it.modelVersion
          }
        }
    }
  }

  private fun <T> Map<T, Boolean>.isAnyTrue(vararg keys: T): Boolean {
    return keys.any { this[it] == true }
  }

  private fun FeedbackBodyItem.CheckableListItem.getAllDescendantIds(): List<String> =
    listOf(this.id) +
      this.children.filterIsInstance<FeedbackBodyItem.CheckableListItem>().flatMap {
        it.getAllDescendantIds()
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
    private fun extractCheckableItems(items: List<FeedbackBodyItem>): Map<String, Boolean> =
      buildMap {
        for (item in items) {
          if (item is FeedbackBodyItem.CheckableListItem) {
            put(item.id, item.defaultChecked)
            putAll(extractCheckableItems(item.children))
          }
        }
      }

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
        enableFineGrainedViewDataDialog = configReader.config.enableFineGrainedViewDataDialog,
        enableDefaultDonationOptInL1 = configReader.config.enableDefaultDonationOptInL1,
        enableDefaultDonationOptInL0 = configReader.config.enableDefaultDonationOptInL0,
      )
  }
}
