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

package com.google.android.`as`.oss.feedback.ui

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.motion.ExpressiveMotionUtils
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.FeedbackRatingTagSource.RATING_TAG_SOURCE_NEGATIVE_RATING_TAG
import com.google.android.`as`.oss.feedback.api.FeedbackRatingTagSource.RATING_TAG_SOURCE_POSITIVE_RATING_TAG
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.LegacyV1
import com.google.android.`as`.oss.feedback.domain.FeedbackDialogMode.EDITING_FEEDBACK
import com.google.android.`as`.oss.feedback.domain.FeedbackDialogMode.VIEWING_FEEDBACK_DONATION_DATA
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionData
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionEvent
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmitState
import com.google.android.`as`.oss.feedback.domain.FeedbackUiElementType
import com.google.android.`as`.oss.feedback.domain.FeedbackUiState
import com.google.android.`as`.oss.feedback.domain.FeedbackViewModel
import com.google.android.`as`.oss.feedback.domain.GroundTruthData
import com.google.android.`as`.oss.feedback.domain.fold
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState.ENABLED_STATE_DISABLED
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState.ENABLED_STATE_ENABLED
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Dialog displayed when a user gives feedback for a specific entity. */
@Composable
fun SingleEntityFeedbackDialog(
  viewModel: FeedbackViewModel = viewModel(),
  data: EntityFeedbackDialogData,
  onFeedbackEvent: (event: FeedbackSubmissionEvent) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  LaunchedEffect(Unit) {
    viewModel.logUiEvent(
      uiElementType = FeedbackUiElementType.FEEDBACK_SCREEN.id,
      clientSessionId = data.clientSessionId,
      interactionType = InteractionType.INTERACTION_TYPE_VIEW,
    )
    viewModel.loadDonationData(clientSessionId = data.clientSessionId, loadSpoonData = true)
    launch {
      viewModel.events.collect { event: FeedbackSubmissionEvent ->
        onFeedbackEvent(event)

        event.message?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        onDismissRequest()
      }
    }
  }

  val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

  MainTheme {
    SingleEntityFeedbackBottomSheet(
      uiState = uiState,
      data = data,
      onTagsShown = { tags ->
        repeat(tags.size) {
          viewModel.logUiEvent(
            uiElementType = FeedbackUiElementType.FEEDBACK_REASON_CHIP.id,
            clientSessionId = data.clientSessionId,
            interactionType = InteractionType.INTERACTION_TYPE_VIEW,
          )
        }
      },
      onTagSelectionChanged = { tag, selected ->
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_REASON_CHIP.id,
          clientSessionId = data.clientSessionId,
        )
        viewModel.updateTagSelection(data.entityContent, data.ratingSentiment, tag, selected)
      },
      onTagGroundTruthSelected = { tag, option ->
        viewModel.updateTagGroundTruthSelection(
          entity = data.entityContent,
          sentiment = data.ratingSentiment,
          tag = tag,
          option = option,
        )
      },
      onFreeFormTextChanged = { viewModel.updateFreeFormText(data.entityContent, it) },
      onOptInCheckedChanged = { checked ->
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_CONSENT_CHECKBOX.id,
          clientSessionId = data.clientSessionId,
          enabledState = if (checked) ENABLED_STATE_ENABLED else ENABLED_STATE_DISABLED,
        )
        if (uiState.enableViewDataDialogV2SingleEntity) {
          viewModel.updateAllOptInChecked(checked)
        } else {
          viewModel.updateOptInChecked(LegacyV1, checked)
        }
      },
      onViewDataClicked = {
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_VIEW_ALL_DATA_BUTTON.id,
          clientSessionId = data.clientSessionId,
        )
        viewModel.updateFeedbackDialogMode(VIEWING_FEEDBACK_DONATION_DATA)
      },
      onViewDataScreenDisplayed = {
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_VIEW_ALL_DATA_BUTTON.id,
          clientSessionId = data.clientSessionId,
          interactionType = InteractionType.INTERACTION_TYPE_VIEW,
        )
      },
      onViewDataSectionCheckedChange = viewModel::updateOptInChecked,
      onViewDataScreenBackPressed = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
      onSendFeedback = {
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_SUBMIT_BUTTON.id,
          clientSessionId = data.clientSessionId,
        )
        viewModel.submitFeedback(
          listOf(
            FeedbackSubmissionData(
              cuj =
                if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED) {
                  SpoonCUJ.SPOON_CUJ_OVERALL_FEEDBACK
                } else {
                  null
                },
              selectedEntityContent = data.entityContent,
              ratingSentiment = data.ratingSentiment,
            )
          )
        )
      },
      onDismissRequest = onDismissRequest,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleEntityFeedbackBottomSheet(
  uiState: FeedbackUiState,
  data: EntityFeedbackDialogData,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
  onFreeFormTextChanged: (String) -> Unit,
  onOptInCheckedChanged: (Boolean) -> Unit,
  onViewDataClicked: () -> Unit,
  onViewDataScreenDisplayed: () -> Unit,
  onViewDataSectionCheckedChange: (DataCollectionCategory, Boolean) -> Unit,
  onViewDataScreenBackPressed: () -> Unit,
  onSendFeedback: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  val scrimColor = remember { Animatable(Color.Transparent) }

  val targetColor = BottomSheetDefaults.ScrimColor
  val transitionDuration = integerResource(android.R.integer.config_shortAnimTime)
  LaunchedEffect(Unit) {
    delay(transitionDuration.milliseconds * 1.5)
    scrimColor.animateTo(targetColor)
  }

  ModalBottomSheet(
    onDismissRequest = { onDismissRequest() },
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle = null,
    scrimColor = scrimColor.value,
    sheetGesturesEnabled = false,
  ) {
    Box(
      modifier = Modifier.fillMaxWidth().heightIn(min = 168.dp).padding(top = 16.dp),
      contentAlignment = Alignment.Center,
    ) {
      uiState.feedbackDonationData.fold(
        onNull = {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(16.dp))
        },
        onSuccess = { donationData ->
          var fullScreen by remember { mutableStateOf(false) }
          SameSizeLayout(selector = uiState.feedbackDialogMode, matchSize = !fullScreen) {
            base {
              SingleEntityFeedbackEditingScreen(
                uiState = uiState,
                donationData = donationData,
                data = data,
                onTagsShown = onTagsShown,
                onTagSelectionChanged = onTagSelectionChanged,
                onTagGroundTruthSelected = onTagGroundTruthSelected,
                onFreeFormTextChanged = onFreeFormTextChanged,
                onOptInCheckedChanged = onOptInCheckedChanged,
                onViewDataClicked = onViewDataClicked,
                onSendFeedback = onSendFeedback,
              )
            }

            alternative(key = VIEWING_FEEDBACK_DONATION_DATA) {
              SingleEntityFeedbackViewFeedbackScreen(
                uiState = uiState,
                donationData = donationData,
                data = data,
                onViewDataScreenDisplayed = onViewDataScreenDisplayed,
                onViewDataSectionCheckedChange = onViewDataSectionCheckedChange,
                onViewDataSectionExpanded = { fullScreen = true },
                onViewDataScreenBackPressed = {
                  fullScreen = false
                  onViewDataScreenBackPressed()
                },
                onSendFeedback = onSendFeedback,
                onDismissRequest = onDismissRequest,
              )
            }
          }
        },
        onFailure = { FeedbackDataFailureContent(onDismissRequest = onDismissRequest) },
      )
    }
  }
}

@Composable
private fun SingleEntityFeedbackEditingScreen(
  uiState: FeedbackUiState,
  donationData: FeedbackDonationData,
  data: EntityFeedbackDialogData,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
  onFreeFormTextChanged: (String) -> Unit,
  onOptInCheckedChanged: (Boolean) -> Unit,
  onViewDataClicked: () -> Unit,
  onSendFeedback: () -> Unit,
) {
  val renderingData = donationData.feedbackUiRenderingData

  val title =
    when (data.ratingSentiment) {
      RATING_SENTIMENT_THUMBS_UP -> renderingData.feedbackDialogGoodFeedbackTitle
      RATING_SENTIMENT_THUMBS_DOWN -> renderingData.feedbackDialogBadFeedbackTitle
      else -> renderingData.feedbackDialogFallbackTitle
    }

  FeedbackContentScaffold(
    // Need header and buttons to be scrollable, to make room for text input in landscape.
    modifier = Modifier.verticalScroll(rememberScrollState()),
    headerIcon = null,
    headerIconContentDescription = null,
    headerIconOnClick = {},
    headerTitle = title,
    primaryButtonLabel = renderingData.feedbackDialogButtonLabel,
    primaryButtonLoading = uiState.feedbackSubmitStatus == FeedbackSubmitState.SUBMIT_PENDING,
    primaryButtonOnClick = {
      if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
        onSendFeedback()
      }
    },
  ) {
    Column(
      modifier =
        Modifier.padding(horizontal = 16.dp)
          .padding(top = 8.dp) // 8.dp outside, 8.dp baked into the chips, total of 24.dp
          .padding(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Tag chips
      if (
        renderingData.feedbackChipsList.isNotEmpty() &&
          data.ratingSentiment != FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED
      ) {
        val tags =
          renderingData.feedbackChipsList.filter {
            it.ratingTagSource ==
              if (data.ratingSentiment == RATING_SENTIMENT_THUMBS_UP) {
                RATING_TAG_SOURCE_POSITIVE_RATING_TAG
              } else {
                RATING_TAG_SOURCE_NEGATIVE_RATING_TAG
              }
          }

        FeedbackTagChipsCompat(
          modifier = Modifier.align(Alignment.CenterHorizontally),
          enableGroundTruthSelectorSingleEntity = uiState.enableGroundTruthSelectorSingleEntity,
          feedbackDialogGroundTruthTitle = renderingData.feedbackDialogGroundTruthTitle,
          data = data,
          tagsGroundTruthSelection =
            uiState.tagsGroundTruthSelectionMap[data.entityContent]
              .orEmpty()[data.ratingSentiment]
              .orEmpty(),
          tagsSelection =
            uiState.tagsSelectionMap[data.entityContent].orEmpty()[data.ratingSentiment].orEmpty(),
          tags = tags,
          onTagsShown = onTagsShown,
          onTagSelectionChanged = onTagSelectionChanged,
          onTagGroundTruthSelected = onTagGroundTruthSelected,
        )
      }

      MainTheme(flexFont = false) {
        // Freeform text
        OutlinedTextField(
          modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp).padding(top = 4.dp),
          value = uiState.freeFormTextMap[data.entityContent].orEmpty(),
          onValueChange = onFreeFormTextChanged,
          textStyle = MaterialTheme.typography.bodyLarge,
          keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
          label = { Text(text = renderingData.feedbackDialogFreeFormLabel) },
          placeholder = { Text(text = renderingData.feedbackDialogFreeFormHint) },
        )
      }

      // Opt-in checkbox row
      FeedbackOptInControl(
        modifier = Modifier.fillMaxWidth(),
        optInCheckboxContentDescription =
          renderingData.feedbackDialogOptInCheckboxContentDescription,
        optInChecked = uiState.optInChecked.any { it.value },
        onOptInCheckedChanged = onOptInCheckedChanged,
        viewDataTitle = renderingData.feedbackDialogOptInV2Title,
        viewDataDescription = renderingData.feedbackDialogOptInV2Description,
        onViewDataClicked = onViewDataClicked,
      )

      // Opt-in privacy statement
      FeedbackOptInPrivacyStatement(
        modifier = Modifier.fillMaxWidth(),
        optInLabel =
          if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED) {
            renderingData.feedbackDialogOptInLabelGenericInfoLabel
          } else {
            renderingData.feedbackDialogOptInLabel
          },
        optInLabelLinkPrivacyPolicy = renderingData.feedbackDialogOptInLabelLinkPrivacyPolicy,
        optInLabelLinkViewData = renderingData.feedbackDialogOptInLabelLinkViewData,
        onViewDataClicked = onViewDataClicked,
      )
    }
  }
}

@Composable
private fun SingleEntityFeedbackViewFeedbackScreen(
  modifier: Modifier = Modifier,
  uiState: FeedbackUiState,
  donationData: FeedbackDonationData,
  data: EntityFeedbackDialogData,
  onViewDataScreenDisplayed: () -> Unit,
  onViewDataSectionCheckedChange: (DataCollectionCategory, Boolean) -> Unit,
  onViewDataSectionExpanded: () -> Unit,
  onViewDataScreenBackPressed: () -> Unit,
  onSendFeedback: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  val renderingData = donationData.feedbackUiRenderingData

  FeedbackContentScaffold(
    modifier = modifier,
    headerIcon = Icons.AutoMirrored.Filled.ArrowBack,
    headerIconContentDescription = renderingData.feedbackDialogViewDataBackButtonContentDescription,
    headerIconOnClick = onViewDataScreenBackPressed,
    headerTitle = renderingData.feedbackDialogViewDataTitle,
    primaryButtonLabel = renderingData.feedbackDialogButtonLabel,
    primaryButtonLoading = uiState.feedbackSubmitStatus == FeedbackSubmitState.SUBMIT_PENDING,
    primaryButtonOnClick = {
      if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
        onSendFeedback()
      }
    },
  ) {
    if (uiState.enableViewDataDialogV2SingleEntity) {
      ViewFeedbackDataContent(
        modifier = Modifier.padding(horizontal = 16.dp),
        uiState = uiState,
        selectedEntityContents = listOf(data.entityContent),
        feedbackDonationDataResult = Result.success(donationData),
        onViewDataScreenDisplayed = onViewDataScreenDisplayed,
        onViewDataSectionCheckedChange = onViewDataSectionCheckedChange,
        onViewDataSectionExpanded = onViewDataSectionExpanded,
        onBackPressed = onViewDataScreenBackPressed,
        onDismissRequest = onDismissRequest,
      )
    } else {
      EntityFeedbackDataCollectionContentV1(
        modifier = Modifier.padding(horizontal = 16.dp),
        selectedEntityContents = listOf(data.entityContent),
        feedbackDonationDataResult = Result.success(donationData),
        onViewDataDisplayed = onViewDataScreenDisplayed,
        onBackPressed = onViewDataScreenBackPressed,
        onDismissRequest = onDismissRequest,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MainTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  flexFont: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && VERSION.SDK_INT >= VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography().let { if (flexFont) it.withFlexFont() else it },
    motionScheme = ExpressiveMotionUtils.expressiveMotionScheme(),
  ) {
    content()
  }
}

@Composable
private fun FeedbackTagChipsCompat(
  modifier: Modifier = Modifier,
  enableGroundTruthSelectorSingleEntity: Boolean,
  feedbackDialogGroundTruthTitle: String,
  data: EntityFeedbackDialogData,
  tagsGroundTruthSelection: Map<FeedbackTagData, GroundTruthData?>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  tags: List<FeedbackTagData>,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
) {
  if (enableGroundTruthSelectorSingleEntity) {
    FeedbackTagChips(
      modifier = modifier,
      alignment = Alignment.CenterHorizontally,
      tags = tags,
      tagsSelection = tagsSelection,
      groundTruthTitle = feedbackDialogGroundTruthTitle,
      tagsGroupTruthOptions =
        tags.associate { tag ->
          val filteredGroundTruthList =
            tag.groundTruthOptionsList
              .map { optionText -> GroundTruthData(optionText) }
              .filter { groundTruthData -> groundTruthData.label != data.entityContent }
          tag to filteredGroundTruthList
        },
      tagsGroundTruthSelection = tagsGroundTruthSelection,
      onTagsShown = onTagsShown,
      onTagSelectionChanged = onTagSelectionChanged,
      onTagGroundTruthSelected = onTagGroundTruthSelected,
    )
  } else {
    SingleEntityFeedbackTagChipsV1(
      modifier = modifier,
      tags = tags,
      onTagsShown = onTagsShown,
      tagsSelection = tagsSelection,
      onTagSelectionChanged = onTagSelectionChanged,
    )
  }
}
