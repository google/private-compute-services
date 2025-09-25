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

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.feedback.FeedbackDialogMode.EDITING_FEEDBACK
import com.google.android.`as`.oss.feedback.FeedbackDialogMode.VIEWING_FEEDBACK_DONATION_DATA
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingTagSource
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import kotlinx.coroutines.launch

/** Dialog displayed when a user gives feedback for a specific entity. */
@Composable
fun SingleEntityFeedbackDialog(
  viewModel: FeedbackViewModel = viewModel(),
  data: EntityFeedbackDialogData,
  onFeedbackEvent: (event: FeedbackEvent) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiStateFlow.collectAsState()

  LaunchedEffect(Unit) {
    viewModel.loadDonationData(clientSessionId = data.clientSessionId)
    launch {
      viewModel.events.collect { event: FeedbackEvent ->
        onFeedbackEvent(event)

        val data = uiState.feedbackDonationData?.getOrNull()?.feedbackUiRenderingData
        val message =
          when (event) {
            FeedbackEvent.SUBMISSION_SUCCESSFUL -> data?.feedbackDialogSentSuccessfullyToast
            FeedbackEvent.SUBMISSION_FAILED -> data?.feedbackDialogSentFailedToast
          }

        message?.let { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
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
  val uiState by viewModel.uiStateFlow.collectAsState()

  ModalBottomSheet(
    onDismissRequest = { onDismissRequest() },
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    dragHandle = null,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .heightIn(min = 168.dp)
          .padding(top = 16.dp)
          .verticalScroll(rememberScrollState()),
      contentAlignment = Alignment.Center,
    ) {
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
        onFailure = { FeedbackDataFailureContent(onDismissRequest = onDismissRequest) },
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
  Column(modifier = Modifier.padding(horizontal = 24.dp)) {
    // Title and back button
    Row(
      modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
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

    SameSizeLayout(
      modifier = Modifier.fillMaxWidth(),
      baseKey = EDITING_FEEDBACK,
      activeKey = uiState.feedbackDialogMode,
      contents =
        arrayOf(
          EDITING_FEEDBACK to
            {
              EntityFeedbackEditingContent(
                data = data,
                donationData = donationData,
                tagsSelection =
                  uiState.tagsSelectionMap[data.entityContent]
                    .orEmpty()[data.ratingSentiment]
                    .orEmpty(),
                freeFormText = uiState.freeFormTextMap[data.entityContent].orEmpty(),
                optInChecked = uiState.optInChecked,
                onFreeFormTextChanged = { viewModel.updateFreeFormText(data.entityContent, it) },
                onOptInCheckedChanged = { viewModel.updateOptInChecked(it) },
                onViewDataClicked = {
                  viewModel.updateFeedbackDialogMode(VIEWING_FEEDBACK_DONATION_DATA)
                },
                onTagSelectionChanged = { tag, selected ->
                  viewModel.updateTagSelection(
                    data.entityContent,
                    data.ratingSentiment,
                    tag,
                    selected,
                  )
                },
              )
            },
          VIEWING_FEEDBACK_DONATION_DATA to
            {
              EntityFeedbackDataCollectionContent(
                selectedEntityContents = listOf(data.entityContent),
                feedbackDonationDataResult = Result.success(donationData),
                onBackPressed = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
                onDismissRequest = onDismissRequest,
              )
            },
        ),
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Button
    Row(modifier = Modifier.padding(vertical = 4.dp).align(Alignment.End)) {
      Button(
        onClick = {
          if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
            onSendFeedback()
          }
        }
      ) {
        SameSizeLayout(
          contentAlignment = Alignment.Center,
          baseKey = SUBMIT_LABEL_KEY,
          activeKey =
            when (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_PENDING) {
              true -> SUBMIT_LABEL_KEY
              else -> CIRCULAR_INDICATOR_KEY
            },
          contents =
            arrayOf(
              SUBMIT_LABEL_KEY to
                {
                  Text(
                    text = donationData.feedbackUiRenderingData.feedbackDialogButtonLabel,
                    style = MaterialTheme.typography.labelLarge,
                  )
                },
              CIRCULAR_INDICATOR_KEY to
                {
                  CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 3.dp,
                  )
                },
            ),
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
  }
}

@Composable
private fun EntityFeedbackEditingContent(
  data: EntityFeedbackDialogData,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  freeFormText: String,
  optInChecked: Boolean,
  onFreeFormTextChanged: (String) -> Unit,
  onOptInCheckedChanged: ((Boolean) -> Unit),
  onViewDataClicked: () -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  donationData: FeedbackDonationData,
) {
  Column {
    // 8.dp outside, 8.dp baked into the chips, total of 24.dp
    Spacer(modifier = Modifier.height(8.dp))

    // Tag chips
    if (
      donationData.feedbackUiRenderingData.feedbackChipsList.isNotEmpty() &&
        data.ratingSentiment != FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED
    ) {
      EntityFeedbackTagChips(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        tags =
          donationData.feedbackUiRenderingData.feedbackChipsList.filter {
            it.ratingTagSource ==
              if (data.ratingSentiment == FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP) {
                FeedbackRatingTagSource.RATING_TAG_SOURCE_POSITIVE_RATING_TAG
              } else {
                FeedbackRatingTagSource.RATING_TAG_SOURCE_NEGATIVE_RATING_TAG
              }
          },
        tagsSelection = tagsSelection,
        onTagSelectionChanged = onTagSelectionChanged,
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
    FeedbackOptInContent(
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
      optInChecked = optInChecked,
      onOptInCheckedChanged = onOptInCheckedChanged,
      onViewDataClicked = onViewDataClicked,
    )

    Spacer(modifier = Modifier.height(24.dp))
  }
}

@Composable
private fun EntityFeedbackTagChips(
  modifier: Modifier,
  tags: List<FeedbackTagData>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement =
      Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
  ) {
    for (tag in tags) {
      val selected = tagsSelection[tag] == true
      FilterChip(
        selected = selected,
        onClick = { onTagSelectionChanged(tag, !selected) },
        leadingIcon = {
          if (selected) Icon(imageVector = Icons.Filled.Check, contentDescription = null)
        },
        label = { Text(text = tag.label) },
      )
    }
  }
}

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
      dynamicColor -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography().let { if (flexFont) it.withFlexFont() else it },
  ) {
    content()
  }
}

private const val SUBMIT_LABEL_KEY = "submit_label"
private const val CIRCULAR_INDICATOR_KEY = "circular_indicator"
