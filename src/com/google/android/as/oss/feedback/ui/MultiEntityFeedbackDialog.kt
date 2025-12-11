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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.motion.ExpressiveMotionUtils
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
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory.LegacyV1
import com.google.android.`as`.oss.feedback.domain.FeedbackDialogMode.EDITING_FEEDBACK
import com.google.android.`as`.oss.feedback.domain.FeedbackDialogMode.VIEWING_FEEDBACK_DONATION_DATA
import com.google.android.`as`.oss.feedback.domain.FeedbackEntityContent
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionData
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionEvent
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmitState
import com.google.android.`as`.oss.feedback.domain.FeedbackUiElementType
import com.google.android.`as`.oss.feedback.domain.FeedbackUiState
import com.google.android.`as`.oss.feedback.domain.FeedbackViewModel
import com.google.android.`as`.oss.feedback.domain.GroundTruthData
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState.ENABLED_STATE_DISABLED
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState.ENABLED_STATE_ENABLED
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MultiEntityFeedbackDialog(
  viewModel: FeedbackViewModel = viewModel(),
  data: MultiFeedbackDialogData,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current
  val quartzCuj: QuartzCUJ? = data.feedbackEntitiesList.firstOrNull { it.hasQuartzCuj() }?.quartzCuj

  LaunchedEffect(Unit) {
    viewModel.logUiEvent(
      uiElementType = FeedbackUiElementType.FEEDBACK_SCREEN.id,
      clientSessionId = data.clientSessionId,
      interactionType = InteractionType.INTERACTION_TYPE_VIEW,
    )
    viewModel.loadDonationData(
      clientSessionId = data.clientSessionId,
      loadSpoonData =
        data.feedbackEntitiesList.any { it.hasCuj() && it.cuj != SpoonCUJ.SPOON_CUJ_UNKNOWN },
      quartzCuj = quartzCuj,
    )
    launch {
      viewModel.events.collect { event ->
        val message =
          when (event) {
            is FeedbackSubmissionEvent.Success -> data.feedbackDialogSentSuccessfullyToast
            is FeedbackSubmissionEvent.Failed -> data.feedbackDialogSentFailedToast
          }

        message?.let { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
        onDismissRequest()
      }
    }
  }

  MainTheme {
    val uiState by viewModel.uiStateFlow.collectAsState()
    MultiEntityFeedbackBottomSheet(
      uiState = uiState,
      data = data,
      onEntitySentimentChanged = { entity, sentiment ->
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_THUMBS_UP_BUTTON.id,
          clientSessionId = data.clientSessionId,
        )
        viewModel.updateSelectedSentiment(entity, sentiment)
      },
      onTagsShown = { tags ->
        repeat(tags.size) {
          viewModel.logUiEvent(
            uiElementType = FeedbackUiElementType.FEEDBACK_REASON_CHIP.id,
            clientSessionId = data.clientSessionId,
            interactionType = InteractionType.INTERACTION_TYPE_VIEW,
          )
        }
      },
      onTagSelectionChanged = { entity, sentiment, tag, selected, singleSelection ->
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_REASON_CHIP.id,
          clientSessionId = data.clientSessionId,
        )
        viewModel.updateTagSelection(entity, sentiment, tag, selected, singleSelection)
      },
      onTagGroundTruthSelected = { entity, sentiment, tag, option ->
        // TODO: Do something with this selection.
        Toast.makeText(context, "Selected: ${option.label}", Toast.LENGTH_SHORT).show()

        viewModel.updateTagGroundTruthSelection(entity, sentiment, tag, option)
      },
      onFreeFormTextChanged = { entity, value -> viewModel.updateFreeFormText(entity, value) },
      onOptInCheckedChanged = { checked ->
        viewModel.logUiEvent(
          uiElementType = FeedbackUiElementType.FEEDBACK_CONSENT_CHECKBOX.id,
          clientSessionId = data.clientSessionId,
          enabledState = if (checked) ENABLED_STATE_ENABLED else ENABLED_STATE_DISABLED,
        )
        if (uiState.enableViewDataDialogV2MultiEntity == true) {
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
      onDismissRequest = onDismissRequest,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultiEntityFeedbackBottomSheet(
  uiState: FeedbackUiState,
  data: MultiFeedbackDialogData,
  onEntitySentimentChanged: (FeedbackEntityContent, FeedbackRatingSentiment) -> Unit,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      selected: Boolean,
      singleSelection: Boolean,
    ) -> Unit,
  onTagGroundTruthSelected:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      option: GroundTruthData,
    ) -> Unit,
  onFreeFormTextChanged: (entity: FeedbackEntityContent, value: String) -> Unit,
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
      var fullScreen by remember { mutableStateOf(false) }
      SameSizeLayout(selector = uiState.feedbackDialogMode, matchSize = !fullScreen) {
        base {
          MultiEntityFeedbackEditingScreen(
            uiState = uiState,
            data = data,
            onEntitySentimentChanged = onEntitySentimentChanged,
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
          MultiEntityFeedbackViewFeedbackScreen(
            uiState = uiState,
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
    }
  }
}

@Composable
private fun MultiEntityFeedbackEditingScreen(
  uiState: FeedbackUiState,
  data: MultiFeedbackDialogData,
  onEntitySentimentChanged: (FeedbackEntityContent, FeedbackRatingSentiment) -> Unit,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      selected: Boolean,
      singleSelection: Boolean,
    ) -> Unit,
  onTagGroundTruthSelected:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      option: GroundTruthData,
    ) -> Unit,
  onFreeFormTextChanged: (entity: FeedbackEntityContent, value: String) -> Unit,
  onOptInCheckedChanged: (Boolean) -> Unit,
  onViewDataClicked: () -> Unit,
  onSendFeedback: () -> Unit,
) {
  FeedbackContentScaffold(
    // Need header and buttons to be scrollable, to make room for text input in landscape.
    modifier = Modifier.verticalScroll(rememberScrollState()),
    headerIcon = null,
    headerIconContentDescription = null,
    headerIconOnClick = {},
    headerTitle = data.title,
    primaryButtonLabel = data.buttonLabel,
    primaryButtonLoading = uiState.feedbackSubmitStatus == FeedbackSubmitState.SUBMIT_PENDING,
    primaryButtonOnClick = {
      if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
        onSendFeedback()
      }
    },
  ) {
    val foundQuartzCuj: QuartzCUJ? =
      data.feedbackEntitiesList.firstOrNull { it.hasQuartzCuj() }?.quartzCuj

    Column(
      Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Column(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
      ) {
        for (entity in data.feedbackEntitiesList) {
          MultiFeedbackEntityEditingContent(
            commonData = data.feedbackEntityCommonData,
            entity = entity,
            selectedSentiment =
              uiState.selectedSentimentMap[entity.entityContent] ?: RATING_SENTIMENT_UNDEFINED,
            tagsSelection = uiState.tagsSelectionMap[entity.entityContent].orEmpty(),
            tagsGroundTruthSelection =
              uiState.tagsGroundTruthSelectionMap[entity.entityContent].orEmpty(),
            freeFormText = uiState.freeFormTextMap[entity.entityContent].orEmpty(),
            onSelectedSentimentChanged = { onEntitySentimentChanged(entity.entityContent, it) },
            onTagSelectionChanged = { entity, sentiment, tag, selected ->
              val singleSelection = foundQuartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE
              onTagSelectionChanged(entity, sentiment, tag, selected, singleSelection)
            },
            onTagGroundTruthSelected = onTagGroundTruthSelected,
            onTagsShown = onTagsShown,
            onFreeFormTextChanged = onFreeFormTextChanged,
          )
        }
      }

      FeedbackOptInControl(
        modifier = Modifier.fillMaxWidth(),
        optInCheckboxContentDescription = data.optInCheckboxContentDescription,
        optInChecked = uiState.optInChecked.any { it.value },
        onOptInCheckedChanged = onOptInCheckedChanged,
        viewDataTitle = data.feedbackDialogOptInV2Title,
        viewDataDescription = data.feedbackDialogOptInV2Description,
        onViewDataClicked = onViewDataClicked,
      )

      // Opt-in privacy statement
      FeedbackOptInPrivacyStatement(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        optInLabel = data.optInLabel,
        optInLabelLinkPrivacyPolicy = data.optInLabelLinkPrivacyPolicy,
        optInLabelLinkViewData = data.optInLabelLinkViewData,
        onViewDataClicked = onViewDataClicked,
      )
    }
  }
}

@Composable
private fun MultiFeedbackEntityEditingContent(
  commonData: FeedbackEntityCommonData,
  entity: FeedbackEntityData,
  selectedSentiment: FeedbackRatingSentiment,
  tagsSelection: Map<FeedbackRatingSentiment, Map<FeedbackTagData, Boolean>>,
  tagsGroundTruthSelection: Map<FeedbackRatingSentiment, Map<FeedbackTagData, GroundTruthData?>>,
  freeFormText: String,
  onSelectedSentimentChanged: (FeedbackRatingSentiment) -> Unit,
  onTagSelectionChanged:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      selected: Boolean,
    ) -> Unit,
  onTagGroundTruthSelected:
    (
      entity: FeedbackEntityContent,
      sentiment: FeedbackRatingSentiment,
      tag: FeedbackTagData,
      option: GroundTruthData,
    ) -> Unit,
  onTagsShown: (tags: List<FeedbackTagData>) -> Unit,
  onFreeFormTextChanged: (entity: FeedbackEntityContent, value: String) -> Unit,
) {
  Surface(
    modifier = Modifier.clip(RoundedCornerShape(4.dp)),
    color = MaterialTheme.colorScheme.surfaceBright,
  ) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).animateContentSize()) {
      EntityHeaderContent(
        commonData = commonData,
        entity = entity,
        selectedSentiment = selectedSentiment,
        onSelectedSentimentChanged = onSelectedSentimentChanged,
      )

      val ratingData =
        when {
          selectedSentiment == RATING_SENTIMENT_THUMBS_UP && entity.hasPositiveRatingData() -> {
            entity.positiveRatingData
          }
          selectedSentiment == RATING_SENTIMENT_THUMBS_DOWN && entity.hasNegativeRatingData() -> {
            entity.negativeRatingData
          }
          else -> null
        }
      EntityBodyContent(
        ratingData = ratingData,
        tagsSelection = tagsSelection[selectedSentiment].orEmpty(),
        tagsGroundTruthSelection = tagsGroundTruthSelection[selectedSentiment].orEmpty(),
        freeFormText = freeFormText,
        onTagsShown = onTagsShown,
        onTagSelectionChanged = { tag, selected ->
          onTagSelectionChanged(entity.entityContent, selectedSentiment, tag, selected)
        },
        onTagGroundTruthSelected = { tag, option ->
          onTagGroundTruthSelected(entity.entityContent, selectedSentiment, tag, option)
        },
        onFreeFormTextChanged = { onFreeFormTextChanged(entity.entityContent, it) },
      )
    }
  }
}

@Composable
private fun EntityHeaderContent(
  commonData: FeedbackEntityCommonData,
  entity: FeedbackEntityData,
  selectedSentiment: FeedbackRatingSentiment,
  onSelectedSentimentChanged: (FeedbackRatingSentiment) -> Unit,
) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {}) {
      Text(
        text = entity.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (entity.hasLabel()) {
        Text(
          text = entity.label,
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
  ratingData: FeedbackRatingData?,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  tagsGroundTruthSelection: Map<FeedbackTagData, GroundTruthData?>,
  freeFormText: String,
  onTagsShown: (tags: List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
  onFreeFormTextChanged: (String) -> Unit,
) {
  if (ratingData == null) return

  Column(
    modifier = Modifier.padding(top = 8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (ratingData.hasHeader()) {
      MainTheme(flexFont = false) {
        Text(
          text = ratingData.header,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }

    if (ratingData.tagsCount > 0) {
      val tags = ratingData.tagsList

      FeedbackTagChips(
        alignment = Alignment.Start,
        tags = tags,
        tagsSelection = tagsSelection,
        onTagsShown = onTagsShown,
        onTagSelectionChanged = onTagSelectionChanged,
        // TODO: Replace with the tag's actual title.
        groundTruthTitle = "What's the correct suggestion?",
        tagsGroupTruthOptions =
          // TODO: Replace with the tag's actual options.
          tags.associateWith {
            listOf(
              GroundTruthData("2668 Kerry Way, LA 90017"),
              GroundTruthData("1385 Winding Brook Lane, Springfield, IL 62704"),
              GroundTruthData("None of the above"),
            )
          },
        tagsGroundTruthSelection = tagsGroundTruthSelection,
        onTagGroundTruthSelected = onTagGroundTruthSelected,
      )
    }

    if (ratingData.hasFreeFormHint()) {
      OutlinedTextField(
        modifier =
          Modifier.fillMaxWidth().semantics { contentDescription = ratingData.freeFormHint },
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
            text = ratingData.freeFormHint,
            style = MaterialTheme.typography.bodyMedium,
          )
        },
      )
    }
  }
}

@Composable
private fun MultiEntityFeedbackViewFeedbackScreen(
  modifier: Modifier = Modifier,
  uiState: FeedbackUiState,
  data: MultiFeedbackDialogData,
  onViewDataScreenDisplayed: () -> Unit,
  onViewDataSectionCheckedChange: (DataCollectionCategory, Boolean) -> Unit,
  onViewDataSectionExpanded: () -> Unit,
  onViewDataScreenBackPressed: () -> Unit,
  onSendFeedback: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  val donationData = uiState.feedbackDonationData

  val iconContentDescription =
    donationData
      ?.getOrNull()
      ?.feedbackUiRenderingData
      ?.feedbackDialogViewDataBackButtonContentDescription
      ?: data.dialogCommonData.donationDataFailureBackButtonContentDescription

  FeedbackContentScaffold(
    modifier = modifier,
    headerIcon = Icons.AutoMirrored.Filled.ArrowBack,
    headerIconContentDescription = iconContentDescription,
    headerIconOnClick = onViewDataScreenBackPressed,
    headerTitle =
      donationData?.getOrNull()?.feedbackUiRenderingData?.feedbackDialogViewDataTitle ?: data.title,
    primaryButtonLabel = data.buttonLabel,
    primaryButtonLoading = uiState.feedbackSubmitStatus == FeedbackSubmitState.SUBMIT_PENDING,
    primaryButtonOnClick = {
      if (uiState.feedbackSubmitStatus != FeedbackSubmitState.SUBMIT_FINISHED) {
        onSendFeedback()
      }
    },
  ) {
    if (uiState.enableViewDataDialogV2MultiEntity) {
      ViewFeedbackDataContent(
        modifier = Modifier.padding(horizontal = 16.dp),
        uiState = uiState,
        selectedEntityContents = emptyList(),
        feedbackDonationDataResult = donationData,
        quartzFeedbackDonationDataResult = uiState.quartzFeedbackDonationData,
        onViewDataScreenDisplayed = onViewDataScreenDisplayed,
        onViewDataSectionCheckedChange = onViewDataSectionCheckedChange,
        onViewDataSectionExpanded = onViewDataSectionExpanded,
        onBackPressed = onViewDataScreenBackPressed,
        onDismissRequest = onDismissRequest,
      )
    } else {
      EntityFeedbackDataCollectionContentV1(
        modifier = Modifier.padding(horizontal = 16.dp),
        selectedEntityContents = emptyList(),
        feedbackDonationDataResult = donationData,
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
