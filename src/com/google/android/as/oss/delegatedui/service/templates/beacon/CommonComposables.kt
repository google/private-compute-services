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

package com.google.android.`as`.oss.delegatedui.service.templates.beacon

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconCommonComposablesConstants.FEEDBACK_BUTTONS_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconButtonSizeXLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeNormal
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.entityFeedbackDialogData
import kotlinx.coroutines.launch

@Composable
internal fun TemplateRendererScope.CardFeedback(
  goodFeedbackContentDescription: String,
  badFeedbackContentDescription: String,
  feedbackSubmittedStateDescription: String,
  feedbackNotSubmittedStateDescription: String,
  goodFeedbackButtonUiId: UiIdToken,
  badFeedbackButtonUiId: UiIdToken,
  thumbsUpEntityFeedbackDialogData: EntityFeedbackDialogData = entityFeedbackDialogData {},
  thumbsDownEntityFeedbackDialogData: EntityFeedbackDialogData = entityFeedbackDialogData {},
) {
  var feedbackSentiment: FeedbackRatingSentiment by remember {
    mutableStateOf(FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED)
  }

  Row(modifier = Modifier.testTag(FEEDBACK_BUTTONS_TAG)) {
    SentimentFeedbackButton(
      targetFeedbackSentimentState = FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP,
      feedbackSentimentState = feedbackSentiment,
      iconDefaultResource = R.drawable.gs_thumb_up_vd_theme_24,
      iconFilledResource = R.drawable.gs_beacon_thumb_up_filled_vd_theme_24,
      feedbackSubmittedStateDescription = feedbackSubmittedStateDescription,
      feedbackNotSubmittedStateDescription = feedbackNotSubmittedStateDescription,
      feedbackButtonUiId = goodFeedbackButtonUiId,
      feedbackContentDescription = goodFeedbackContentDescription,
      onFeedbackSentimentChanged = { feedbackSentiment = it },
      entityFeedbackDialogData = thumbsUpEntityFeedbackDialogData,
    )
    Spacer(modifier = Modifier.width(4.dp))
    SentimentFeedbackButton(
      targetFeedbackSentimentState = FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN,
      feedbackSentimentState = feedbackSentiment,
      iconDefaultResource = R.drawable.gs_thumb_down_vd_theme_24,
      iconFilledResource = R.drawable.gs_beacon_thumb_down_filled_vd_theme_24,
      feedbackSubmittedStateDescription = feedbackSubmittedStateDescription,
      feedbackNotSubmittedStateDescription = feedbackNotSubmittedStateDescription,
      feedbackButtonUiId = badFeedbackButtonUiId,
      feedbackContentDescription = badFeedbackContentDescription,
      onFeedbackSentimentChanged = { feedbackSentiment = it },
      entityFeedbackDialogData = thumbsDownEntityFeedbackDialogData,
    )
  }
}

/**
 * Renders a sentiment feedback button, either thumbs up or thumbs down.
 *
 * @param targetFeedbackSentimentState The target feedback sentiment state. ie. Is this the thumbs
 *   up button, or the thumbs down button?
 * @param feedbackSentimentState The current feedback sentiment state.
 * @param iconDefaultResource The icon for the button when it is not selected.
 * @param iconFilledResource The icon for the button when it is selected
 * @param feedbackSubmittedStateDescription The state description to show when the feedback is
 *   submitted.
 * @param feedbackNotSubmittedStateDescription The state description to show when the feedback is
 *   not submitted.
 * @param feedbackButtonUiId The UI ID of the feedback button.
 * @param feedbackContentDescription The content description of the feedback button.
 * @param onFeedbackSentimentChanged Sets the feedback sentiment state to ON or OFF (highlighted or
 *   outline)
 * @param entityFeedbackDialogData The data to show in the feedback dialog.
 */
@Composable
private fun TemplateRendererScope.SentimentFeedbackButton(
  targetFeedbackSentimentState: FeedbackRatingSentiment,
  feedbackSentimentState: FeedbackRatingSentiment,
  iconDefaultResource: Int,
  iconFilledResource: Int,
  feedbackSubmittedStateDescription: String,
  feedbackNotSubmittedStateDescription: String,
  feedbackButtonUiId: UiIdToken,
  feedbackContentDescription: String,
  onFeedbackSentimentChanged: (FeedbackRatingSentiment) -> Unit,
  entityFeedbackDialogData: EntityFeedbackDialogData,
) {
  LaunchedEffect(Unit) { doOnImpression(feedbackButtonUiId) { logUsage() } }

  val scope = rememberCoroutineScope()
  IconButton(
    modifier =
      Modifier.size(IconButtonSizeXLarge).semantics {
        if (
          feedbackSubmittedStateDescription.isNotBlank() &&
            feedbackNotSubmittedStateDescription.isNotBlank()
        ) {
          this.stateDescription =
            if (feedbackSentimentState == targetFeedbackSentimentState) {
              feedbackSubmittedStateDescription
            } else {
              feedbackNotSubmittedStateDescription
            }
        }
      },
    onClick = {
      scope.launch {
        doOnInterop(feedbackButtonUiId, interactionType = INTERACTION_TYPE_CLICK) {
          if (feedbackSentimentState == targetFeedbackSentimentState) {
            onFeedbackSentimentChanged(FeedbackRatingSentiment.RATING_SENTIMENT_UNDEFINED)
          } else {
            onFeedbackSentimentChanged(targetFeedbackSentimentState)
            showEntityFeedback(entityFeedbackDialogData)
          }
        }
      }
    },
  ) {
    Icon(
      modifier = Modifier.size(IconSizeNormal),
      painter =
        painterResource(
          if (feedbackSentimentState == targetFeedbackSentimentState) {
            iconFilledResource
          } else {
            iconDefaultResource
          }
        ),
      contentDescription = feedbackContentDescription,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

internal object BeaconCommonComposablesConstants {
  const val FEEDBACK_BUTTONS_TAG = "FeedbackButtons"
}
