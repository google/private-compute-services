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
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconGeneralCard
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconGenericContentDescriptions
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconResponseSource
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconCommonUtils.getPendingIntent
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconGeneralCardsConstants.BEACON_AI_SUMMARY_ICON_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconGeneralCardsConstants.BEACON_GENERAL_CARD_V1_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconGeneralCardsConstants.BEACON_GENERAL_CARD_V2_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconGeneralCardsConstants.EXPAND_BUTTON_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconGeneralCardsConstants.OPEN_APP_BUTTON_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconButtonSizeMedium
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeNormal
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeExtraSmall
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.SummaryIconAndTextSpacing
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import kotlinx.coroutines.launch

/**
 * The container for a the simple cards in the Beacon widget. This is the entry point to the cards.
 * There can be multiple cards in this single surface.
 */
@Composable
internal fun TemplateRendererScope.BeaconGeneralCardsContainer(
  cards: List<BeaconGeneralCard>,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  pendingIntents: List<PendingIntent>,
) {
  val beaconUiConfigs = LocalConfigReader.current.config.beaconUiConfigs

  Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
    for (i in 0 until cards.size) {
      val card = cards[i]
      val isFirstCard = i == 0
      val isLastCard = i == cards.size - 1
      val pendingIntent = getPendingIntent(pendingIntents, card.dataSource)

      key(card.listUuid) {
        // Take the lower version of either template data's display version or what the client
        // allows
        val displayUiVersion =
          Math.min(
            card.displayUiVersion,
            LocalConfigReader.current.config.beaconUiConfigs.generalCardsMaximumDisplayUiVersion,
          )
        when {
          displayUiVersion >= 2 ->
            BeaconSingleGeneralCardContainerV2(
              card = card,
              genericContentDescriptions = genericContentDescriptions,
              isFirstCard = isFirstCard,
              isLastCard = isLastCard,
              pendingIntent = pendingIntent,
            )
          displayUiVersion == 1 ->
            BeaconSingleGeneralCardContainerV1(
              card = card,
              genericContentDescriptions = genericContentDescriptions,
              isFirstCard = isFirstCard,
              isLastCard = isLastCard,
              pendingIntent = pendingIntent,
            )
          // Default to V0 if the displayUiVersion is not set or is unknown.
          else ->
            BeaconSingleGeneralCardContainerV0(
              card = card,
              genericContentDescriptions = genericContentDescriptions,
              isFirstCard = isFirstCard,
              isLastCard = isLastCard,
              pendingIntent = pendingIntent,
            )
        }
      }
    }
  }
}

/**
 * V2 layout for the general card. Main changes:
 * 1. The card itself is a button that expands/collapses the card instead of the expand/collapse
 *    icon.
 * 2. There is an "Open <SOURCE APP>" button at the bottom of the card, when expanded.
 * 3. Small layout changes
 */
@VisibleForTesting
@Composable
internal fun TemplateRendererScope.BeaconSingleGeneralCardContainerV2(
  card: BeaconGeneralCard,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  isFirstCard: Boolean,
  isLastCard: Boolean,
  pendingIntent: PendingIntent?,
) {
  val beaconUiConfigs = LocalConfigReader.current.config.beaconUiConfigs

  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) { doOnImpression(card.generalCardUiId) { logUsage() } }

  var isExpanded by remember { mutableStateOf(false) }

  Card(
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape =
      RoundedCornerShape(
        topStart = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        topEnd = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomStart = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomEnd = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
      ),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .testTag(BEACON_GENERAL_CARD_V2_TAG)
          .semantics(mergeDescendants = true) {
            this.role = Role.Button
            this.stateDescription =
              if (isExpanded) {
                genericContentDescriptions.cardExpandedAccessibilityStateDescription
              } else {
                genericContentDescriptions.cardCollapsedAccessibilityStateDescription
              }
            onClick(
              label =
                if (isExpanded) {
                  genericContentDescriptions.cardExpandedAccessibilityClickLabel
                } else {
                  genericContentDescriptions.cardCollapsedAccessibilityClickLabel
                },
              action = null,
            )
          }
          .doOnClick(if (isExpanded) card.collapseButtonUiId else card.expandButtonUiId) {
            logUsage()
            isExpanded = !isExpanded
          }
          .padding(16.dp)
    ) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        AppIcon(
          contentDescription = card.dataSource.sourceType.name,
          sourcePackageName = card.dataSource.sourcePackageName,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
          // Calculate the top padding needed for the date Text to align its top edge with the
          // title Text.
          // Only add padding if the title's top offset is larger than the date's.
          val titleTextStyle = MaterialTheme.typography.titleMedium
          val dateTextStyle = MaterialTheme.typography.bodySmall
          val dateTopOffsetSp =
            ((titleTextStyle.lineHeight.value - dateTextStyle.lineHeight.value) / 2)
              .coerceAtLeast(0f)
              .toInt()
              .sp
          val dateTopPaddingDp = with(LocalDensity.current) { dateTopOffsetSp.toPx().toDp() }

          Row(verticalAlignment = Alignment.Top) {
            Text(
              modifier = Modifier.weight(1f, fill = false),
              text = card.title,
              style = titleTextStyle,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines =
                if (isExpanded) {
                  beaconUiConfigs.generalCardTitleTextExpandedLineCount
                } else {
                  beaconUiConfigs.generalCardTitleTextCollapsedLineCount
                },
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              modifier = Modifier.padding(top = dateTopPaddingDp),
              text = card.date,
              style = dateTextStyle,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Spacer(modifier = Modifier.width(4.dp))
        ExpandIcon(isExpanded = isExpanded)
      }

      if (card.detailedTextV2.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        BeaconDetailedTextV2(
          isExpanded = isExpanded,
          detailedText = card.detailedTextV2,
          displayIcon =
            listOf(
                BeaconResponseSource.SourceType.EMAIL,
                BeaconResponseSource.SourceType.WONDER_CARD,
              )
              .contains(card.dataSource.sourceType),
        )
      }

      if (pendingIntent != null && isExpanded) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          Button(
            modifier = Modifier.testTag(OPEN_APP_BUTTON_TAG),
            onClick = {
              scope.launch {
                doOnInterop(
                  card.sourceNavigationButtonUiId,
                  interactionType = INTERACTION_TYPE_CLICK,
                ) {
                  executeAction { pendingIntent.toAction() }
                }
              }
            },
            colors =
              ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
              ),
          ) {
            Text(text = card.dataSource.ctaButtonText)
          }
        }
      }
    }
  }
}

/**
 * V1 layout for the general card. Main changes:
 * 1. The date is moved to the "title" line
 * 2. Expand button is in the same position as V0 layout's date
 * 3. Expanding will display feedback buttons
 */
@Composable
private fun TemplateRendererScope.BeaconSingleGeneralCardContainerV1(
  card: BeaconGeneralCard,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  isFirstCard: Boolean,
  isLastCard: Boolean,
  pendingIntent: PendingIntent?,
) {
  val beaconUiConfigs = LocalConfigReader.current.config.beaconUiConfigs

  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) { doOnImpression(card.generalCardUiId) { logUsage() } }

  var isExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier.testTag(BEACON_GENERAL_CARD_V1_TAG),
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape =
      RoundedCornerShape(
        topStart = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        topEnd = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomStart = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomEnd = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
      ),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .then(
            if (pendingIntent != null) {
              Modifier.semantics(mergeDescendants = true) {
                  this.role = Role.Button
                  onClick(label = card.dataSource.ctaButtonText, action = null)
                }
                .doOnClick(card.sourceNavigationButtonUiId) {
                  executeAction { pendingIntent.toAction() }
                }
            } else {
              Modifier.semantics(mergeDescendants = true) {}
            }
          )
          .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      AppIcon(
        contentDescription = card.dataSource.sourceType.name,
        sourcePackageName = card.dataSource.sourcePackageName,
      )
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.Bottom) {
          Text(
            modifier = Modifier.weight(1f, fill = false),
            text = card.title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(modifier = Modifier.padding(horizontal = 4.dp).clearAndSetSemantics {}, text = "•")
          Text(
            text = card.date,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        if (card.detailedTextV2.isNotBlank()) {
          if (
            card.dataSource.sourceType == BeaconResponseSource.SourceType.EMAIL ||
              card.dataSource.sourceType == BeaconResponseSource.SourceType.WONDER_CARD
          ) {
            BeaconAiSummaryCardContainer(card = card, isExpanded = isExpanded)
          } else if (card.dataSource.sourceType == BeaconResponseSource.SourceType.MESSAGE) {
            Text(
              text = card.detailedTextV2,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines =
                if (isExpanded) {
                  beaconUiConfigs.generalCardDetailedTextExpandedLineCount
                } else {
                  beaconUiConfigs.generalCardDetailedTextCollapsedLineCount
                },
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
        if (isExpanded) {
          CardFeedback(
            goodFeedbackContentDescription =
              genericContentDescriptions.goodFeedbackContentDescription,
            badFeedbackContentDescription =
              genericContentDescriptions.badFeedbackContentDescription,
            goodFeedbackButtonUiId = card.goodFeedbackButtonUiId,
            badFeedbackButtonUiId = card.badFeedbackButtonUiId,
            feedbackSubmittedStateDescription =
              genericContentDescriptions.feedbackSubmittedStateDescription,
            feedbackNotSubmittedStateDescription =
              genericContentDescriptions.feedbackNotSubmittedStateDescription,
            thumbsUpEntityFeedbackDialogData = card.thumbsUpEntityFeedbackDialogData,
            thumbsDownEntityFeedbackDialogData = card.thumbsDownEntityFeedbackDialogData,
          )
        }
      }
      ExpandButton(
        onClick = {
          val uiTokenId = if (isExpanded) card.collapseButtonUiId else card.expandButtonUiId
          scope.launch {
            doOnInterop(uiTokenId, interactionType = INTERACTION_TYPE_CLICK) { logUsage() }
          }
          isExpanded = !isExpanded
        },
        isExpanded = isExpanded,
        contentDescription = card.cardExpandButtonAccessibilityContentDescription,
        isExpandedStateDescription =
          genericContentDescriptions.cardExpandedAccessibilityStateDescription,
        isCollapsedStateDescription =
          genericContentDescriptions.cardCollapsedAccessibilityStateDescription,
        isExpandedClickLabel = genericContentDescriptions.cardExpandedAccessibilityClickLabel,
        isCollapsedClickLabel = genericContentDescriptions.cardCollapsedAccessibilityClickLabel,
      )
    }
  }
}

/** The original version of the general card. */
@Composable
private fun TemplateRendererScope.BeaconSingleGeneralCardContainerV0(
  card: BeaconGeneralCard,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  isFirstCard: Boolean,
  isLastCard: Boolean,
  pendingIntent: PendingIntent?,
) {
  LaunchedEffect(Unit) { doOnImpression(card.generalCardUiId) { logUsage() } }

  Card(
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape =
      RoundedCornerShape(
        topStart = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        topEnd = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomStart = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomEnd = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
      ),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .then(
            if (pendingIntent != null) {
              Modifier.semantics(mergeDescendants = true) {
                  this.role = Role.Button
                  onClick(label = card.dataSource.ctaButtonText, action = null)
                }
                .doOnClick(card.sourceNavigationButtonUiId) {
                  executeAction { pendingIntent.toAction() }
                }
            } else {
              Modifier.semantics(mergeDescendants = true) {}
            }
          )
          .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AppIcon(
        contentDescription = card.dataSource.sourceType.name,
        sourcePackageName = card.dataSource.sourcePackageName,
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = card.title,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = if (card.detailedTextV2.isBlank()) 2 else 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (card.detailedTextV2.isNotBlank()) {
          Text(
            text = card.detailedTextV2,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
      Text(
        text = card.date,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/**
 * Displays the detailed text for the general card. To be used with
 * BeaconSingleGeneralCardContainerV2
 */
@Composable
private fun TemplateRendererScope.BeaconDetailedTextV2(
  isExpanded: Boolean,
  detailedText: String,
  displayIcon: Boolean,
) {
  val beaconUiConfigs = LocalConfigReader.current.config.beaconUiConfigs

  val detailedTextStyle = MaterialTheme.typography.bodyMedium
  val fontSizeSp = detailedTextStyle.fontSize
  val fontSizeDp = with(LocalDensity.current) { fontSizeSp.toPx().toDp() }

  // The size of the Icon. It's dynamically based on iconSizeDp, but is clamped between 16.dp and
  // 32.dp.
  val dynamicIconSize = max(min(fontSizeDp, 32.dp), 14.dp)
  val dynamicIconWrapperSizeDp = dynamicIconSize

  // Calculate how much padding to apply to the icon to vertically center it with the text.
  val fontHeightSp = detailedTextStyle.lineHeight
  val dynamicIconWrapperSizeSp =
    with(LocalDensity.current) { dynamicIconWrapperSizeDp.toPx().toSp() }
  val iconPaddingTopSp =
    ((fontHeightSp.value - dynamicIconWrapperSizeSp.value) / 2).coerceAtLeast(0f).toInt().sp
  val iconPaddingTopDp = with(LocalDensity.current) { iconPaddingTopSp.toPx().toDp() }

  // Calculate the left padding for the icon. This padding ensures the icon is center-aligned
  // with a reference icon of IconSizeMedium (24.dp). If dynamicIconSize is 24.dp or larger,
  // no left padding is needed. Otherwise, the padding is half the difference.
  val iconPaddingLeftDp = ((IconSizeLarge - dynamicIconSize) / 2).coerceAtLeast(0.dp)

  // Calculate the width of the Spacer, ensuring the combined width of the icon wrapper, left
  // padding, and spacer is 40.dp, with a minimum spacer width of 8.dp.
  val spacerWidth = (40.dp - dynamicIconWrapperSizeDp - iconPaddingLeftDp).coerceAtLeast(8.dp)

  Row(modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier =
        Modifier.padding(top = iconPaddingTopDp, start = iconPaddingLeftDp)
          .size(dynamicIconWrapperSizeDp),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        modifier = Modifier.size(dynamicIconSize).testTag(BEACON_AI_SUMMARY_ICON_TAG),
        painter = painterResource(R.drawable.gs_beacon_text_analysis_2_vd_theme_24),
        tint = MaterialTheme.colorScheme.onSurface,
        contentDescription = null,
      )
    }
    Spacer(modifier = Modifier.width(spacerWidth))
    Text(
      text = detailedText,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
      maxLines =
        if (isExpanded) {
          beaconUiConfigs.generalCardDetailedTextExpandedLineCount
        } else {
          beaconUiConfigs.generalCardDetailedTextCollapsedLineCount
        },
      overflow = TextOverflow.Ellipsis,
    )
  }
}

/**
 * Displays the AI summary card. Separated into its own Composable because there is an icon that
 * indicates that the summary is AI generated.
 */
@Composable
private fun TemplateRendererScope.BeaconAiSummaryCardContainer(
  card: BeaconGeneralCard,
  isExpanded: Boolean,
) {
  val beaconUiConfigs = LocalConfigReader.current.config.beaconUiConfigs

  val summaryTextStyle = MaterialTheme.typography.bodyMedium
  val iconSizeSp = summaryTextStyle.fontSize
  val iconSizeDp = with(LocalDensity.current) { iconSizeSp.toPx().toDp() }

  val fontHeightSp = summaryTextStyle.lineHeight
  val iconPaddingTopSp = Math.max(0f, (fontHeightSp.value - iconSizeSp.value) / 2).toInt().sp
  val iconPaddingTopDp = with(LocalDensity.current) { iconPaddingTopSp.toPx().toDp() }

  val textFirstLineIndentationSp =
    with(LocalDensity.current) { (iconSizeDp + SummaryIconAndTextSpacing).toPx().toSp() }

  if (!isExpanded) {
    // The summary and icon are on a single line, use a Row to align the icon with the text.
    Row(horizontalArrangement = Arrangement.spacedBy(SummaryIconAndTextSpacing)) {
      Box(modifier = Modifier.padding(top = iconPaddingTopDp)) {
        Icon(
          modifier = Modifier.size(iconSizeDp).testTag(BEACON_AI_SUMMARY_ICON_TAG),
          painter = painterResource(R.drawable.gs_beacon_text_analysis_2_vd_theme_24),
          tint = MaterialTheme.colorScheme.onSurface,
          contentDescription = null,
        )
      }
      Text(
        text = card.detailedTextV2,
        style = summaryTextStyle,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = beaconUiConfigs.generalCardDetailedTextCollapsedLineCount,
        overflow = TextOverflow.Ellipsis,
      )
    }
  } else {
    // The summary and icon are on separate lines, use a Box to align the icon with the text.
    // The text is indented, and the icon takes the place of the indented portion.
    Box {
      Box(modifier = Modifier.padding(top = iconPaddingTopDp)) {
        Icon(
          modifier = Modifier.size(iconSizeDp).testTag(BEACON_AI_SUMMARY_ICON_TAG),
          painter = painterResource(R.drawable.gs_beacon_text_analysis_2_vd_theme_24),
          tint = MaterialTheme.colorScheme.onSurface,
          contentDescription = null,
        )
      }
      Text(
        text = card.detailedTextV2,
        style =
          summaryTextStyle.copy(
            textIndent = TextIndent(firstLine = textFirstLineIndentationSp, restLine = 0.sp)
          ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = beaconUiConfigs.generalCardDetailedTextExpandedLineCount,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

/** Displays the source app's icon. If the icon is not available, displays a generic icon. */
@Composable
private fun TemplateRendererScope.AppIcon(
  modifier: Modifier = Modifier,
  contentDescription: String,
  sourcePackageName: String?,
) {
  val packageManager = LocalContext.current.packageManager
  val icon: ImageBitmap? =
    remember(sourcePackageName) { getAppIcon(packageManager, sourcePackageName) }

  if (icon != null) {
    Image(
      modifier = Modifier.size(IconSizeLarge),
      bitmap = icon,
      contentDescription = contentDescription,
    )
  } else {
    Icon(
      modifier = Modifier.size(IconSizeLarge),
      painter = painterResource(R.drawable.gs_widgets_vd_theme_24),
      tint = MaterialTheme.colorScheme.secondary,
      contentDescription = null,
    )
  }
}

/**
 * Displays an icon indicating the expanded state.
 *
 * @param isExpanded Whether the card is currently expanded.
 * @param contentDescription The content description for the icon.
 */
@Composable
private fun ExpandIcon(
  modifier: Modifier = Modifier,
  isExpanded: Boolean,
  contentDescription: String? = null,
) {
  Icon(
    modifier = modifier.size(IconSizeNormal).testTag(EXPAND_BUTTON_TAG),
    painter =
      painterResource(
        if (isExpanded) {
          R.drawable.gs_keyboard_arrow_up_vd_theme_24
        } else {
          R.drawable.gs_keyboard_arrow_down_vd_theme_24
        }
      ),
    contentDescription = contentDescription,
    tint = MaterialTheme.colorScheme.onSurfaceVariant,
  )
}

/** Expands the card to show more information. Deprecated (unknown if it will be brought back). */
@Composable
private fun ExpandButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  isExpanded: Boolean,
  contentDescription: String,
  isExpandedStateDescription: String,
  isCollapsedStateDescription: String,
  isExpandedClickLabel: String,
  isCollapsedClickLabel: String,
) {
  IconButton(
    onClick = onClick,
    modifier =
      modifier
        .size(IconButtonSizeMedium)
        .semantics {
          this.stateDescription =
            if (isExpanded) isExpandedStateDescription else isCollapsedStateDescription
          onClick(
            label = if (isExpanded) isExpandedClickLabel else isCollapsedClickLabel,
            action = null,
          )
        }
        .testTag(EXPAND_BUTTON_TAG),
    colors =
      IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
  ) {
    Icon(
      modifier = Modifier.size(IconSizeNormal),
      painter =
        painterResource(
          if (isExpanded) {
            R.drawable.gs_keyboard_arrow_up_vd_theme_24
          } else {
            R.drawable.gs_keyboard_arrow_down_vd_theme_24
          }
        ),
      contentDescription = contentDescription,
    )
  }
}

/** Gets the app icon [ImageBitmap] for the given package name. */
private fun getAppIcon(packageManager: PackageManager, packageName: String?): ImageBitmap? =
  packageName
    .takeIf { !it.isNullOrBlank() }
    ?.let { getApplicationInfo(it, packageManager) }
    ?.loadUnbadgedIcon(packageManager)
    ?.toBitmap()
    ?.asImageBitmap()

private fun getApplicationInfo(
  packageName: String,
  packageManager: PackageManager,
): ApplicationInfo? =
  try {
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
  } catch (e: Throwable) {
    null
  }

internal object BeaconGeneralCardsConstants {
  const val BEACON_AI_SUMMARY_ICON_TAG = "BeaconAiSummaryIcon"
  const val EXPAND_BUTTON_TAG = "ExpandButton"
  const val BEACON_GENERAL_CARD_V2_TAG = "BeaconGeneralCardV2"
  const val BEACON_GENERAL_CARD_V1_TAG = "BeaconGeneralCardV1"
  const val OPEN_APP_BUTTON_TAG = "OpenAppButton"
}
