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

import android.app.PendingIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconDetailedCard
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconGenericContentDescriptions
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconResponseSource
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconRow
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconRowItem
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconRowItemText.TextSize
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconCommonUtils.getPendingIntent
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeMedium
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData

/**
 * The container for a single card in the Beacon widget. This is the entry point to the card. Cards
 * must be placed within a single widget. There can be multiple cards in a single widget.
 */
@Composable
internal fun TemplateRendererScope.BeaconDetailedCardContainer(
  card: BeaconDetailedCard,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  pendingIntentList: List<PendingIntent>,
  uiIdToken: UiIdToken,
) {
  doOnImpression(uiIdToken) { logUsage() }
  Card(
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape = RoundedCornerShape(RoundedCornerSizeLarge),
  ) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
      CardHeader(
        cardTitle = card.title,
        dataSource = card.dataSource,
        pendingIntent = getPendingIntent(pendingIntentList, card.dataSource),
        sourceNavigationButtonUiIdToken = card.sourceNavigationButtonUiId,
      )
      Spacer(modifier = Modifier.height(16.dp))
      Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in card.rowsList) {
          CardRow(row)
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      CardFooter(
        cardSentimentLabel = genericContentDescriptions.cardSentimentLabel,
        goodFeedbackContentDescription = genericContentDescriptions.goodFeedbackContentDescription,
        badFeedbackContentDescription = genericContentDescriptions.badFeedbackContentDescription,
        feedbackSubmittedStateDescription =
          genericContentDescriptions.feedbackSubmittedStateDescription,
        feedbackNotSubmittedStateDescription =
          genericContentDescriptions.feedbackNotSubmittedStateDescription,
        goodFeedbackButtonUiId = card.goodFeedbackButtonUiId,
        badFeedbackButtonUiId = card.badFeedbackButtonUiId,
        thumbsUpEntityFeedbackDialogData = card.thumbsUpEntityFeedbackDialogData,
        thumbsDownEntityFeedbackDialogData = card.thumbsDownEntityFeedbackDialogData,
      )
    }
  }
}

/**
 * Represents a single row in the card. Not to be confused with a single item, which is represented
 * by [CardRowItem].
 */
@Composable
private fun CardRow(row: BeaconRow) {
  Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
    when (row.rowLayoutCase) {
      BeaconRow.RowLayoutCase.FULL_LENGTH -> {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) { CardRowItem(row.fullLength.item) }
      }
      BeaconRow.RowLayoutCase.HALF_HALF_SPLIT -> {
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
          CardRowItem(row.halfHalfSplit.itemOne)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
          CardRowItem(row.halfHalfSplit.itemTwo)
        }
      }
      BeaconRow.RowLayoutCase.SEVEN_THREE_SPLIT -> {
        Column(modifier = Modifier.weight(7f).fillMaxHeight()) {
          CardRowItem(row.sevenThreeSplit.itemOne)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Column(modifier = Modifier.weight(3f).fillMaxHeight()) {
          CardRowItem(row.sevenThreeSplit.itemTwo)
        }
      }
      else -> {}
    }
  }
}

/** Represents a single item in a row. This is the smallest unit of data in a row. */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CardRowItem(rowItem: BeaconRowItem) {
  @Composable
  fun getTextSize(textSize: TextSize): TextStyle =
    when (textSize) {
      TextSize.SMALL -> MaterialTheme.typography.labelMedium
      TextSize.MEDIUM -> MaterialTheme.typography.titleMedium
      TextSize.LARGE -> MaterialTheme.typography.titleLargeEmphasized
      else -> MaterialTheme.typography.labelMedium
    }

  Surface(
    modifier = Modifier.fillMaxSize(),
    shape = RoundedCornerShape(RoundedCornerSizeMedium),
    color = MaterialTheme.colorScheme.surfaceContainerHigh,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
          .clearAndSetSemantics {
            this.contentDescription =
              if (rowItem.contentDescription.isEmpty()) {
                "${rowItem.label.text}: ${rowItem.content.text}. ${rowItem.contentSummary.text}"
              } else {
                rowItem.contentDescription
              }
          }
    ) {
      Text(
        text = rowItem.label.text,
        style = getTextSize(rowItem.label.textSize),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = rowItem.content.text,
        style = getTextSize(rowItem.content.textSize),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

@Composable
private fun TemplateRendererScope.CardHeader(
  cardTitle: String,
  dataSource: BeaconResponseSource,
  pendingIntent: PendingIntent?,
  sourceNavigationButtonUiIdToken: UiIdToken,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      modifier = Modifier.weight(1f, fill = false),
      text = cardTitle,
      style = MaterialTheme.typography.titleLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(modifier = Modifier.width(0.dp))
    if (pendingIntent != null) {
      SourceNavigationButton(
        dataSource = dataSource,
        pendingIntent = pendingIntent,
        sourceNavigationButtonUiIdToken = sourceNavigationButtonUiIdToken,
      )
    }
  }
}

@Composable
private fun TemplateRendererScope.CardFooter(
  cardSentimentLabel: String,
  goodFeedbackContentDescription: String,
  badFeedbackContentDescription: String,
  feedbackSubmittedStateDescription: String,
  feedbackNotSubmittedStateDescription: String,
  goodFeedbackButtonUiId: UiIdToken,
  badFeedbackButtonUiId: UiIdToken,
  thumbsUpEntityFeedbackDialogData: EntityFeedbackDialogData,
  thumbsDownEntityFeedbackDialogData: EntityFeedbackDialogData,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = cardSentimentLabel,
      style = MaterialTheme.typography.labelMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    CardFeedback(
      goodFeedbackContentDescription = goodFeedbackContentDescription,
      badFeedbackContentDescription = badFeedbackContentDescription,
      goodFeedbackButtonUiId = goodFeedbackButtonUiId,
      badFeedbackButtonUiId = badFeedbackButtonUiId,
      feedbackSubmittedStateDescription = feedbackSubmittedStateDescription,
      feedbackNotSubmittedStateDescription = feedbackNotSubmittedStateDescription,
      thumbsUpEntityFeedbackDialogData = thumbsUpEntityFeedbackDialogData,
      thumbsDownEntityFeedbackDialogData = thumbsDownEntityFeedbackDialogData,
    )
  }
}

@Composable
private fun TemplateRendererScope.SourceNavigationButton(
  dataSource: BeaconResponseSource,
  pendingIntent: PendingIntent,
  sourceNavigationButtonUiIdToken: UiIdToken,
) {
  IconButton(
    modifier = Modifier.size(IconSizeLarge),
    onClick = {
      doOnInterop(sourceNavigationButtonUiIdToken) { executeAction { pendingIntent.toAction() } }
    },
  ) {
    doOnImpression(sourceNavigationButtonUiIdToken) { logUsage() }
    Icon(
      modifier = Modifier.size(IconSizeLarge),
      painter = painterResource(R.drawable.gs_open_in_new_vd_theme_24),
      tint = MaterialTheme.colorScheme.secondary,
      contentDescription = dataSource.ctaButtonText,
    )
  }
}
