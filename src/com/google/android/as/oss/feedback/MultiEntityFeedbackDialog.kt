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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.feedback.FeedbackDialogMode.EDITING_FEEDBACK
import com.google.android.`as`.oss.feedback.FeedbackDialogMode.VIEWING_FEEDBACK_DONATION_DATA
import com.google.android.`as`.oss.feedback.api.FeedbackEntityCommonData
import com.google.android.`as`.oss.feedback.api.FeedbackEntityData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.api.MultiFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import kotlinx.coroutines.launch

@Composable
fun MultiEntityFeedbackDialog(
  viewModel: FeedbackViewModel = viewModel(),
  data: MultiFeedbackDialogData,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  val foundQuartzCuj: QuartzCUJ? =
    data.feedbackEntitiesList.firstOrNull { it.hasQuartzCuj() }?.quartzCuj

  LaunchedEffect(Unit) {
    viewModel.loadDonationData(clientSessionId = data.clientSessionId, quartzCuj = foundQuartzCuj)
    launch {
      viewModel.events.collect { event ->
        val message =
          when (event) {
            FeedbackEvent.SUBMISSION_SUCCESSFUL -> data.feedbackDialogSentSuccessfullyToast
            FeedbackEvent.SUBMISSION_FAILED -> data.feedbackDialogSentFailedToast
          }

        message?.let { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        onDismissRequest()
      }
    }
  }

  MainTheme {
    val uiState by viewModel.uiStateFlow.collectAsState()
    MultiFeedbackBottomSheet(
      viewModel = viewModel,
      data = data,
      onDismissRequest = onDismissRequest,
      onSendFeedback = {
        viewModel.submitFeedback(
          uiState.selectedSentimentMap.entries
            .filter { (_, sentiment) ->
              sentiment in listOf(RATING_SENTIMENT_THUMBS_UP, RATING_SENTIMENT_THUMBS_DOWN)
            }
            .map { entry ->
              val entityData = data.feedbackEntitiesList.first { it.entityContent == entry.key }
              val spoonCuj = if (entityData.hasCuj()) entityData.cuj else null
              val quartzCuj = if (entityData.hasQuartzCuj()) entityData.quartzCuj else null
              FeedbackSubmissionData(
                cuj = spoonCuj,
                quartzCuj = quartzCuj,
                selectedEntityContent = entry.key,
                ratingSentiment = entry.value,
              )
            }
        )
      },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiFeedbackBottomSheet(
  viewModel: FeedbackViewModel,
  data: MultiFeedbackDialogData,
  onDismissRequest: () -> Unit,
  onSendFeedback: () -> Unit,
) {
  val uiState by viewModel.uiStateFlow.collectAsState()
  val donationData = uiState.feedbackDonationData
  val foundQuartzCuj: QuartzCUJ? =
    data.feedbackEntitiesList.firstOrNull { it.hasQuartzCuj() }?.quartzCuj

  ModalBottomSheet(
    onDismissRequest = { onDismissRequest() },
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp).verticalScroll(rememberScrollState())) {

      // Title and back button
      Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 32.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        when (uiState.feedbackDialogMode) {
          VIEWING_FEEDBACK_DONATION_DATA -> {
            IconButton(
              modifier = Modifier.size(32.dp),
              onClick = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription =
                  donationData
                    ?.getOrNull()
                    ?.feedbackUiRenderingData
                    ?.feedbackDialogViewDataBackButtonContentDescription
                    ?: data.dialogCommonData.donationDataFailureBackButtonContentDescription,
              )
            }
          }
          else -> {
            Spacer(modifier = Modifier.size(32.dp))
          }
        }

        val title =
          when (uiState.feedbackDialogMode) {
            EDITING_FEEDBACK -> data.title
            VIEWING_FEEDBACK_DONATION_DATA ->
              donationData?.getOrNull()?.feedbackUiRenderingData?.feedbackDialogViewDataTitle
                ?: data.title
          }
        Text(text = title, style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.size(32.dp))
      }
      Spacer(modifier = Modifier.height(16.dp))

      SameSizeLayout(
        modifier = Modifier.fillMaxWidth(),
        baseKey = EDITING_FEEDBACK,
        activeKey = uiState.feedbackDialogMode,
        contents =
          arrayOf(
            EDITING_FEEDBACK to
              {
                MultiFeedbackEditingContent(
                  data = data,
                  selectedSentimentMap = uiState.selectedSentimentMap,
                  tagsSelectionMap = uiState.tagsSelectionMap,
                  freeFormTextMap = uiState.freeFormTextMap,
                  optInChecked = uiState.optInChecked,
                  onSelectedSentimentChanged = viewModel::updateSelectedSentiment,
                  onTagSelectionChanged = { entityContent, sentiment, tag, selected ->
                    viewModel.updateTagSelection(
                      entity = entityContent,
                      sentiment = sentiment,
                      tag = tag,
                      value = selected,
                      singleSelection = (foundQuartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE),
                    )
                  },
                  onFreeFormTextChanged = viewModel::updateFreeFormText,
                  onOptInCheckedChanged = viewModel::updateOptInChecked,
                  onViewDataClicked = {
                    viewModel.updateFeedbackDialogMode(VIEWING_FEEDBACK_DONATION_DATA)
                  },
                )
              },
            VIEWING_FEEDBACK_DONATION_DATA to
              {
                EntityFeedbackDataCollectionContent(
                  selectedEntityContents =
                    uiState.selectedSentimentMap.entries
                      .filter { it.value != RATING_SENTIMENT_UNDEFINED }
                      .map { it.key },
                  feedbackDonationDataResult = uiState.feedbackDonationData,
                  quartzFeedbackDonationDataResult = uiState.quartzFeedbackDonationData,
                  onBackPressed = { viewModel.updateFeedbackDialogMode(EDITING_FEEDBACK) },
                  onDismissRequest = onDismissRequest,
                )
              },
          ),
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Button
      Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).align(Alignment.End)) {
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
                    Text(text = data.buttonLabel, style = MaterialTheme.typography.labelLarge)
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
}

@Composable
private fun MultiFeedbackEditingContent(
  data: MultiFeedbackDialogData,
  selectedSentimentMap: Map<FeedbackEntityContent, FeedbackRatingSentiment>,
  tagsSelectionMap:
    Map<FeedbackEntityContent, Map<FeedbackRatingSentiment, Map<FeedbackTagData, Boolean>>>,
  freeFormTextMap: Map<FeedbackEntityContent, String>,
  optInChecked: Boolean,
  onSelectedSentimentChanged: (FeedbackEntityContent, FeedbackRatingSentiment) -> Unit,
  onTagSelectionChanged:
    (FeedbackEntityContent, FeedbackRatingSentiment, FeedbackTagData, Boolean) -> Unit,
  onFreeFormTextChanged: (FeedbackEntityContent, String) -> Unit,
  onOptInCheckedChanged: (Boolean) -> Unit,
  onViewDataClicked: () -> Unit,
) {
  Column {
    Column(
      modifier = Modifier.clip(RoundedCornerShape(20.dp)),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      for (entity in data.feedbackEntitiesList) {
        MultiFeedbackEntityEditingContent(
          commonData = data.feedbackEntityCommonData,
          data = entity,
          selectedSentiment =
            selectedSentimentMap[entity.entityContent] ?: RATING_SENTIMENT_UNDEFINED,
          tagsSelection = tagsSelectionMap[entity.entityContent].orEmpty(),
          freeFormText = freeFormTextMap[entity.entityContent].orEmpty(),
          onSelectedSentimentChanged = { onSelectedSentimentChanged(entity.entityContent, it) },
          onTagSelectionChanged = { sentiment, tag, selected ->
            onTagSelectionChanged(entity.entityContent, sentiment, tag, selected)
          },
          onFreeFormTextChanged = { onFreeFormTextChanged(entity.entityContent, it) },
        )
      }
    }

    Spacer(Modifier.height(16.dp))

    FeedbackOptInContent(
      modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
      optInLabel = data.optInLabel,
      optInLabelLinkPrivacyPolicy = data.optInLabelLinkPrivacyPolicy,
      optInLabelLinkViewData = data.optInLabelLinkViewData,
      optInChecked = optInChecked,
      onOptInCheckedChanged = onOptInCheckedChanged,
      onViewDataClicked = onViewDataClicked,
    )

    Spacer(Modifier.height(16.dp))
  }
}

@Composable
private fun MultiFeedbackEntityEditingContent(
  commonData: FeedbackEntityCommonData,
  data: FeedbackEntityData,
  selectedSentiment: FeedbackRatingSentiment,
  tagsSelection: Map<FeedbackRatingSentiment, Map<FeedbackTagData, Boolean>>,
  freeFormText: String,
  onSelectedSentimentChanged: (FeedbackRatingSentiment) -> Unit,
  onTagSelectionChanged: (FeedbackRatingSentiment, FeedbackTagData, Boolean) -> Unit,
  onFreeFormTextChanged: (String) -> Unit,
) {
  Surface(
    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
    color = MaterialTheme.colorScheme.surfaceBright,
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).animateContentSize()) {
      EntityHeaderContent(
        commonData = commonData,
        data = data,
        selectedSentiment = selectedSentiment,
        onSelectedSentimentChanged = onSelectedSentimentChanged,
      )

      val ratingData =
        when {
          selectedSentiment == RATING_SENTIMENT_THUMBS_UP && data.hasPositiveRatingData() -> {
            data.positiveRatingData
          }
          selectedSentiment == RATING_SENTIMENT_THUMBS_DOWN && data.hasNegativeRatingData() -> {
            data.negativeRatingData
          }
          else -> null
        }
      EntityBodyContent(
        data = ratingData,
        tagsSelection = tagsSelection[selectedSentiment].orEmpty(),
        freeFormText = freeFormText,
        onTagSelectionChanged = { tag, selected ->
          onTagSelectionChanged(selectedSentiment, tag, selected)
        },
        onFreeFormTextChanged = onFreeFormTextChanged,
      )
    }
  }
}

@Composable
private fun EntityHeaderContent(
  commonData: FeedbackEntityCommonData,
  data: FeedbackEntityData,
  selectedSentiment: FeedbackRatingSentiment,
  onSelectedSentimentChanged: (FeedbackRatingSentiment) -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
      Text(
        text = data.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (data.hasLabel()) {
        Text(
          text = data.label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    IconButton(
      modifier =
        Modifier.size(48.dp)
          .clip(CircleShape)
          .then(
            when (selectedSentiment) {
              RATING_SENTIMENT_THUMBS_UP ->
                Modifier.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
              else -> Modifier
            }
          ),
      onClick = {
        onSelectedSentimentChanged(
          when (selectedSentiment) {
            RATING_SENTIMENT_THUMBS_UP -> RATING_SENTIMENT_UNDEFINED
            else -> RATING_SENTIMENT_THUMBS_UP
          }
        )
      },
    ) {
      Icon(
        painter = painterResource(R.drawable.gs_thumb_up_filled_vd_theme_24),
        tint =
          when (selectedSentiment) {
            RATING_SENTIMENT_THUMBS_UP -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
        contentDescription = commonData.thumbsUpButtonContentDescription,
      )
    }

    IconButton(
      modifier =
        Modifier.size(48.dp)
          .clip(CircleShape)
          .then(
            when (selectedSentiment) {
              RATING_SENTIMENT_THUMBS_DOWN ->
                Modifier.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
              else -> Modifier
            }
          ),
      onClick = {
        onSelectedSentimentChanged(
          when (selectedSentiment) {
            RATING_SENTIMENT_THUMBS_DOWN -> RATING_SENTIMENT_UNDEFINED
            else -> RATING_SENTIMENT_THUMBS_DOWN
          }
        )
      },
    ) {
      Icon(
        painter = painterResource(R.drawable.gs_thumb_down_filled_vd_theme_24),
        tint =
          when (selectedSentiment) {
            RATING_SENTIMENT_THUMBS_DOWN -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant
          },
        contentDescription = commonData.thumbsDownButtonContentDescription,
      )
    }
  }
}

@Composable
private fun EntityBodyContent(
  data: FeedbackRatingData?,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  freeFormText: String,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onFreeFormTextChanged: (String) -> Unit,
) {
  if (data == null) return

  Column(
    modifier = Modifier.padding(top = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (data.hasHeader()) {
      MainTheme(flexFont = false) {
        Text(
          text = data.header,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (data.tagsCount > 0) {
      EntityFeedbackTagChips(
        tags = data.tagsList,
        tagsSelection = tagsSelection,
        onTagSelectionChanged = onTagSelectionChanged,
      )
    }

    if (data.hasFreeFormHint()) {
      OutlinedTextField(
        modifier = Modifier.fillMaxWidth().semantics { contentDescription = data.freeFormHint },
        value = freeFormText,
        onValueChange = onFreeFormTextChanged,
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 2,
        colors =
          OutlinedTextFieldDefaults.colors(
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
        placeholder = {
          Text(
            modifier = Modifier.clearAndSetSemantics {},
            text = data.freeFormHint,
            style = MaterialTheme.typography.bodyMedium,
          )
        },
      )
    }
  }
}

@Composable
private fun EntityFeedbackTagChips(
  modifier: Modifier = Modifier,
  tags: List<FeedbackTagData>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.Start),
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
        colors =
          FilterChipDefaults.filterChipColors(
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
          ),
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
