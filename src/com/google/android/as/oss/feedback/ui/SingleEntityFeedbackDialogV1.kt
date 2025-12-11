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

import android.R
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.motion.ExpressiveMotionUtils
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingTagSource
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
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
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Dialog displayed when a user gives feedback for a specific entity. */
@Composable
fun SingleEntityFeedbackDialogV1(
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

  MainTheme {
    EntityFeedbackBottomSheet(
      viewModel = viewModel,
      data = data,
      onDismissRequest = onDismissRequest,
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
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntityFeedbackBottomSheet(
  viewModel: FeedbackViewModel,
  data: EntityFeedbackDialogData,
  onDismissRequest: () -> Unit,
  onSendFeedback: () -> Unit,
) {
  val scrimColor = remember { Animatable(Color.Transparent) }

  val targetColor = BottomSheetDefaults.ScrimColor
  val transitionDuration = integerResource(R.integer.config_shortAnimTime)
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
      modifier =
        Modifier.fillMaxWidth()
          .heightIn(min = 168.dp)
          .padding(top = 16.dp)
          .verticalScroll(rememberScrollState()),
      contentAlignment = Alignment.Center,
    ) {
      val uiState by viewModel.uiStateFlow.collectAsState()
      uiState.feedbackDonationData.fold(
        onNull = {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center).padding(16.dp))
        },
        onSuccess = { donationData ->
          EntityFeedbackContents(
            uiState = uiState,
            viewModel = viewModel,
            donationData = donationData,
            data = data,
            onSendFeedback = onSendFeedback,
            onDismissRequest = onDismissRequest,
          )
        },
        onFailure = { FeedbackDataFailureContentV1(onDismissRequest = onDismissRequest) },
      )
    }
  }
}

@Composable
private fun EntityFeedbackContents(
  uiState: FeedbackUiState,
  viewModel: FeedbackViewModel,
  donationData: FeedbackDonationData,
  data: EntityFeedbackDialogData,
  onSendFeedback: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  Column(modifier = Modifier.padding(horizontal = 8.dp)) {
    // Title and back button
    Row(
      modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp).padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      when (uiState.feedbackDialogMode) {
        VIEWING_FEEDBACK_DONATION_DATA -> {
          // Back button
          IconButton(
            modifier = Modifier.size(32.dp),
            onClick = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription =
                donationData.feedbackUiRenderingData
                  .feedbackDialogViewDataBackButtonContentDescription,
            )
          }
        }

        else -> {
          Spacer(modifier = Modifier.size(32.dp))
        }
      }

      // Title
      val title =
        when (uiState.feedbackDialogMode) {
          VIEWING_FEEDBACK_DONATION_DATA ->
            donationData.feedbackUiRenderingData.feedbackDialogViewDataTitle

          else ->
            if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP) {
              donationData.feedbackUiRenderingData.feedbackDialogGoodFeedbackTitle
            } else if (
              data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
            ) {
              donationData.feedbackUiRenderingData.feedbackDialogBadFeedbackTitle
            } else {
              donationData.feedbackUiRenderingData.feedbackDialogFallbackTitle
            }
        }
      Text(text = title, style = MaterialTheme.typography.headlineSmall)

      Spacer(modifier = Modifier.size(32.dp))
    }
    Spacer(modifier = Modifier.height(8.dp))

    SameSizeLayout(selector = uiState.feedbackDialogMode, modifier = Modifier.fillMaxWidth()) {
      base {
        EntityFeedbackEditingContent(
          modifier = Modifier.padding(horizontal = 16.dp),
          data = data,
          enableGroundTruthSelectorSingleEntity = uiState.enableGroundTruthSelectorSingleEntity,
          donationData = donationData,
          tagsSelection =
            uiState.tagsSelectionMap[data.entityContent].orEmpty()[data.ratingSentiment].orEmpty(),
          freeFormText = uiState.freeFormTextMap[data.entityContent].orEmpty(),
          optInChecked = uiState.optInChecked.any { it.value },
          onFreeFormTextChanged = { viewModel.updateFreeFormText(data.entityContent, it) },
          onOptInCheckedChanged = {
            viewModel.logUiEvent(
              uiElementType = FeedbackUiElementType.FEEDBACK_CONSENT_CHECKBOX.id,
              clientSessionId = data.clientSessionId,
              enabledState =
                if (it) {
                  EnabledState.ENABLED_STATE_ENABLED
                } else {
                  EnabledState.ENABLED_STATE_DISABLED
                },
            )
            if (uiState.enableViewDataDialogV2SingleEntity) {
              viewModel.updateAllOptInChecked(it)
            } else {
              viewModel.updateOptInChecked(LegacyV1, it)
            }
          },
          onViewDataClicked = {
            viewModel.logUiEvent(
              uiElementType = FeedbackUiElementType.FEEDBACK_VIEW_ALL_DATA_BUTTON.id,
              clientSessionId = data.clientSessionId,
            )
            viewModel.updateFeedbackDialogMode(VIEWING_FEEDBACK_DONATION_DATA)
          },
          tagsGroundTruthSelection =
            uiState.tagsGroundTruthSelectionMap[data.entityContent]
              .orEmpty()[data.ratingSentiment]
              .orEmpty(),
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
        )
      }

      alternative(key = VIEWING_FEEDBACK_DONATION_DATA) {
        if (uiState.enableViewDataDialogV2SingleEntity) {
          ViewFeedbackDataContent(
            modifier = Modifier.padding(horizontal = 16.dp),
            uiState = uiState,
            selectedEntityContents = listOf(data.entityContent),
            feedbackDonationDataResult = Result.success(donationData),
            onViewDataScreenDisplayed = {
              viewModel.logUiEvent(
                uiElementType = FeedbackUiElementType.FEEDBACK_VIEW_ALL_DATA_BUTTON.id,
                clientSessionId = data.clientSessionId,
                interactionType = InteractionType.INTERACTION_TYPE_VIEW,
              )
            },
            onViewDataSectionCheckedChange = { category, checked ->
              viewModel.updateOptInChecked(category, checked)
            },
            onViewDataSectionExpanded = {},
            onBackPressed = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
            onDismissRequest = onDismissRequest,
          )
        } else {
          EntityFeedbackDataCollectionContentV1(
            modifier = Modifier.padding(horizontal = 16.dp),
            selectedEntityContents = listOf(data.entityContent),
            feedbackDonationDataResult = Result.success(donationData),
            onViewDataDisplayed = {
              viewModel.logUiEvent(
                uiElementType = FeedbackUiElementType.FEEDBACK_VIEW_ALL_DATA_BUTTON.id,
                clientSessionId = data.clientSessionId,
                interactionType = InteractionType.INTERACTION_TYPE_VIEW,
              )
            },
            onBackPressed = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
            onDismissRequest = onDismissRequest,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Button
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp).align(Alignment.End)) {
      Button(
        onClick = {
          if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
            onSendFeedback()
          }
        }
      ) {
        SameSizeLayout(selector = uiState.feedbackSubmitStatus) {
          base {
            Text(
              text = donationData.feedbackUiRenderingData.feedbackDialogButtonLabel,
              style = MaterialTheme.typography.labelLarge,
            )
          }

          alternative(key = FeedbackSubmitState.SUBMIT_PENDING) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 3.dp,
              )
            }
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun EntityFeedbackEditingContent(
  modifier: Modifier = Modifier,
  data: EntityFeedbackDialogData,
  enableGroundTruthSelectorSingleEntity: Boolean,
  tagsGroundTruthSelection: Map<FeedbackTagData, GroundTruthData?>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  freeFormText: String,
  optInChecked: Boolean,
  donationData: FeedbackDonationData,
  onFreeFormTextChanged: (String) -> Unit,
  onOptInCheckedChanged: ((Boolean) -> Unit),
  onViewDataClicked: () -> Unit,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
) {
  Column(modifier) {
    // 8.dp outside, 8.dp baked into the chips, total of 24.dp
    Spacer(modifier = Modifier.height(8.dp))

    // Tag chips
    if (
      donationData.feedbackUiRenderingData.feedbackChipsList.isNotEmpty() &&
        data.ratingSentiment != FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED
    ) {
      val tags =
        donationData.feedbackUiRenderingData.feedbackChipsList.filter {
          it.ratingTagSource ==
            if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP) {
              FeedbackRatingTagSource.RATING_TAG_SOURCE_POSITIVE_RATING_TAG
            } else {
              FeedbackRatingTagSource.RATING_TAG_SOURCE_NEGATIVE_RATING_TAG
            }
        }

      FeedbackTagChipsCompat(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        enableGroundTruthSelectorSingleEntity = enableGroundTruthSelectorSingleEntity,
        feedbackDialogGroundTruthTitle =
          donationData.feedbackUiRenderingData.feedbackDialogGroundTruthTitle,
        data = data,
        tagsGroundTruthSelection = tagsGroundTruthSelection,
        tagsSelection = tagsSelection,
        tags = tags,
        onTagsShown = onTagsShown,
        onTagSelectionChanged = onTagSelectionChanged,
        onTagGroundTruthSelected = onTagGroundTruthSelected,
      )
      // 8.dp baked into the chips, total of 32.dp
      Spacer(modifier = Modifier.height(24.dp))
    }

    MainTheme(flexFont = false) {
      // Freeform text
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp),
        value = freeFormText,
        onValueChange = onFreeFormTextChanged,
        textStyle = MaterialTheme.typography.bodyLarge,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        label = { Text(text = donationData.feedbackUiRenderingData.feedbackDialogFreeFormLabel) },
        placeholder = {
          Text(text = donationData.feedbackUiRenderingData.feedbackDialogFreeFormHint)
        },
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Opt-in checkbox and label
    FeedbackOptInContentV1(
      modifier = Modifier.fillMaxWidth(),
      optInLabel =
        if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED) {
          donationData.feedbackUiRenderingData.feedbackDialogOptInLabelGenericInfoLabel
        } else {
          donationData.feedbackUiRenderingData.feedbackDialogOptInLabel
        },
      optInLabelLinkPrivacyPolicy =
        donationData.feedbackUiRenderingData.feedbackDialogOptInLabelLinkPrivacyPolicy,
      optInLabelLinkViewData =
        donationData.feedbackUiRenderingData.feedbackDialogOptInLabelLinkViewData,
      optInCheckboxContentDescription =
        donationData.feedbackUiRenderingData.feedbackDialogOptInCheckboxContentDescription,
      optInChecked = optInChecked,
      onOptInCheckedChanged = onOptInCheckedChanged,
      onViewDataClicked = onViewDataClicked,
    )

    Spacer(modifier = Modifier.height(24.dp))
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
