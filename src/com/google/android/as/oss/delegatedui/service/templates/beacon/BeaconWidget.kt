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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.egress.beacon.beaconEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.beacon.suggestMoreClickEvent
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconAiDisclaimer
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconWidget
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeNormal
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import kotlinx.coroutines.launch

/** The container for the Beacon widget. This is the entrypoint to the widget. */
@Composable
internal fun TemplateRendererScope.BeaconWidgetContainer(
  widget: BeaconWidget,
  pendingIntentList: List<PendingIntent>,
) {
  val scrollState = rememberScrollState()
  ScrollableContainer(scrollState = scrollState) {
    Box(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
      when (widget.displayUiVersion) {
        1 -> {
          BeaconWidgetContentV1(widget, pendingIntentList, scrollState)
        }
        else -> {
          BeaconWidgetContent(widget, pendingIntentList, scrollState)
        }
      }
    }
  }
}

/** The container for the Beacon widget. This is the entrypoint to the widget. */
@Composable
private fun TemplateRendererScope.BeaconWidgetContentV1(
  widget: BeaconWidget,
  pendingIntentList: List<PendingIntent>,
  scrollState: ScrollState,
) {
  val detailedCardsList = widget.detailedCardsList
  val generalCardsEmails = widget.generalCardsEmailsList
  val generalCardsMessages = widget.generalCardsMessagesList
  val generalCardsMerged = generalCardsEmails + generalCardsMessages

  val isOnlyDisplayingGeneralCards = detailedCardsList.isEmpty() && generalCardsMerged.isNotEmpty()
  val isDisplayMoreResultsEnabled =
    if (detailedCardsList.isNotEmpty()) {
      // If there is a detailed card and there are also general cards, display the "more results"
      // button.
      generalCardsMerged.isNotEmpty()
    } else {
      // If there is no detailed card but there are more than DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
      // general cards of a single type, display the "more results" button.
      Math.max(generalCardsEmails.size, generalCardsMessages.size) >
        BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }
  var isDisplayingMoreResults by remember { mutableStateOf(false) }
  var shouldScrollToBottom by remember { mutableStateOf(false) }

  LaunchedEffect(shouldScrollToBottom) {
    if (shouldScrollToBottom) {
      runCatching { scrollState.animateScrollTo(scrollState.maxValue) }
      shouldScrollToBottom = false
    }
  }

  val generalCardsEmailsNumToDisplay =
    if (isDisplayingMoreResults) {
      generalCardsEmails.size
    } else {
      BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }
  val generalCardsMessagesNumToDisplay =
    if (isDisplayingMoreResults) {
      generalCardsMessages.size
    } else {
      BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }

  val generalCardsToDisplay =
    if (widget.displayAllCards) {
      generalCardsMerged
    } else if (isOnlyDisplayingGeneralCards) {
      generalCardsEmails.take(generalCardsEmailsNumToDisplay) +
        generalCardsMessages.take(generalCardsMessagesNumToDisplay)
    } else if (detailedCardsList.isNotEmpty() && isDisplayingMoreResults) {
      generalCardsMerged
    } else {
      emptyList()
    }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    for (detailedCard in detailedCardsList) {
      BeaconDetailedCardContainer(
        card = detailedCard,
        genericContentDescriptions = widget.genericContentDescriptions,
        pendingIntentList = pendingIntentList,
      )
    }

    BeaconGeneralCardsContainer(
      cards = generalCardsToDisplay,
      genericContentDescriptions = widget.genericContentDescriptions,
      pendingIntents = pendingIntentList,
    )

    WidgetAiDisclaimer(widget.aiDisclaimerWithLink, widget.disclaimerButtonUiId, pendingIntentList)

    if (!widget.displayAllCards && isDisplayMoreResultsEnabled) {
      val scope = rememberCoroutineScope()
      WidgetShowAllResultsButton(
        if (isDisplayingMoreResults) {
          widget.ctaDisplayFewerResults
        } else {
          widget.ctaDisplayMoreResults
        },
        onClick = {
          scope.launch {
            doOnInterop(
              widget.showMoreResultsButtonUiId,
              interactionType = INTERACTION_TYPE_CLICK,
            ) {
              launch { logUsage() }
              if (widget.disableExpandOnSuggestMore) {
                sendEgressData {
                  beaconEgressData = beaconEgressData {
                    suggestMoreClickEvent = suggestMoreClickEvent {}
                  }
                }
              }
            }
          }
          if (!widget.disableExpandOnSuggestMore) {
            isDisplayingMoreResults = !isDisplayingMoreResults
            shouldScrollToBottom = true
          }
        },
      )
    }
  }
}

/** The container for the Beacon widget. This is the entrypoint to the widget. */
@Composable
private fun TemplateRendererScope.BeaconWidgetContent(
  widget: BeaconWidget,
  pendingIntentList: List<PendingIntent>,
  scrollState: ScrollState,
) {
  val detailedCardsList = widget.detailedCardsList
  val generalCardsEmails = widget.generalCardsEmailsList
  val generalCardsMessages = widget.generalCardsMessagesList
  val generalCardsMerged = generalCardsEmails + generalCardsMessages

  val isOnlyDisplayingGeneralCards = detailedCardsList.isEmpty() && generalCardsMerged.isNotEmpty()
  val isDisplayMoreResultsEnabled =
    if (detailedCardsList.isNotEmpty()) {
      // If there is a detailed card and there are also general cards, display the "more results"
      // button.
      generalCardsMerged.isNotEmpty()
    } else {
      // If there is no detailed card but there are more than DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
      // general cards of a single type, display the "more results" button.
      Math.max(generalCardsEmails.size, generalCardsMessages.size) >
        BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }
  var isDisplayingMoreResults by remember { mutableStateOf(false) }
  var shouldScrollToBottom by remember { mutableStateOf(false) }

  LaunchedEffect(shouldScrollToBottom) {
    if (shouldScrollToBottom) {
      runCatching { scrollState.animateScrollTo(scrollState.maxValue) }
      shouldScrollToBottom = false
    }
  }

  val generalCardsEmailsNumToDisplay =
    if (isDisplayingMoreResults) {
      generalCardsEmails.size
    } else {
      BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }
  val generalCardsMessagesNumToDisplay =
    if (isDisplayingMoreResults) {
      generalCardsMessages.size
    } else {
      BeaconWidgetConstants.DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY
    }

  val generalCardsToDisplay =
    if (isOnlyDisplayingGeneralCards) {
      generalCardsEmails.take(generalCardsEmailsNumToDisplay) +
        generalCardsMessages.take(generalCardsMessagesNumToDisplay)
    } else if (detailedCardsList.isNotEmpty() && isDisplayingMoreResults) {
      generalCardsMerged
    } else {
      emptyList()
    }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    for (detailedCard in detailedCardsList) {
      BeaconDetailedCardContainer(
        card = detailedCard,
        genericContentDescriptions = widget.genericContentDescriptions,
        pendingIntentList = pendingIntentList,
      )
    }

    BeaconGeneralCardsContainer(
      cards = generalCardsToDisplay,
      genericContentDescriptions = widget.genericContentDescriptions,
      pendingIntents = pendingIntentList,
    )

    WidgetAiDisclaimer(widget.aiDisclaimerWithLink, widget.disclaimerButtonUiId, pendingIntentList)

    if (isDisplayMoreResultsEnabled) {
      val scope = rememberCoroutineScope()
      WidgetShowAllResultsButton(
        if (isDisplayingMoreResults) {
          widget.ctaDisplayFewerResults
        } else {
          widget.ctaDisplayMoreResults
        },
        onClick = {
          scope.launch {
            doOnInterop(
              widget.showMoreResultsButtonUiId,
              interactionType = INTERACTION_TYPE_CLICK,
            ) {
              launch { logUsage() }
            }
          }
          isDisplayingMoreResults = !isDisplayingMoreResults
          shouldScrollToBottom = true
        },
      )
    }
  }
}

@Composable
private fun TemplateRendererScope.WidgetAiDisclaimer(
  aiDisclaimer: BeaconAiDisclaimer,
  disclaimerButtonUiId: UiIdToken,
  pendingIntentList: List<PendingIntent>,
) {
  val annotatedString = buildAiDisclaimerText(aiDisclaimer, disclaimerButtonUiId, pendingIntentList)

  Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(
      modifier = Modifier.size(IconSizeNormal),
      painter = painterResource(R.drawable.gs_info_vd_theme_24),
      tint = MaterialTheme.colorScheme.onSurface,
      contentDescription = null,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = annotatedString,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

@Composable
private fun WidgetShowAllResultsButton(ctaDisplayMoreResults: String, onClick: () -> Unit) {
  Button(
    modifier = Modifier.fillMaxWidth(),
    onClick = onClick,
    colors =
      ButtonDefaults.buttonColors(
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
      ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    contentPadding =
      PaddingValues(
        horizontal =
          ButtonDefaults.ContentPadding.calculateLeftPadding(
            LayoutDirection.Ltr
          ), // Keep the horizontal defaults of ButtonDefaults
        vertical = 10.dp,
      ),
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Image(
        painterResource(R.drawable.expand_collapse_results),
        contentDescription = null,
        modifier = Modifier.size(IconSizeNormal),
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
      )
      Text(text = ctaDisplayMoreResults, style = MaterialTheme.typography.labelLarge)
    }
  }
}

@Composable
private fun TemplateRendererScope.buildAiDisclaimerText(
  aiDisclaimer: BeaconAiDisclaimer,
  disclaimerButtonUiId: UiIdToken,
  pendingIntentList: List<PendingIntent>,
): AnnotatedString = buildAnnotatedString {
  append(aiDisclaimer.fullText)

  if (
    aiDisclaimer.url.isNotEmpty() &&
      aiDisclaimer.urlStartIndex >= 0 &&
      aiDisclaimer.urlEndIndex >= 0
  ) {
    val scope = rememberCoroutineScope()
    val linkAnnotation =
      LinkAnnotation.Url(
        url = aiDisclaimer.url,
        styles =
          TextLinkStyles(
            style =
              SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                textDecoration = TextDecoration.Underline,
              )
          ),
      ) { linkAnnotationUrl ->
        val pendingIntent = pendingIntentList.getOrNull(aiDisclaimer.pendingIntentIndex - 1)
        if (pendingIntent != null) {
          scope.launch {
            doOnInterop(disclaimerButtonUiId, interactionType = INTERACTION_TYPE_CLICK) {
              executeAction { pendingIntent.toAction() }
            }
          }
        }
      }
    addLink(
      url = linkAnnotation,
      start = aiDisclaimer.urlStartIndex,
      end = aiDisclaimer.urlEndIndex,
    )
  }
}

/** A container that always displays a scrollbar. */
@Composable
private fun TemplateRendererScope.ScrollableContainer(
  modifier: Modifier = Modifier,
  scrollState: ScrollState = rememberScrollState(),
  content: @Composable() (ColumnScope.() -> Unit),
) {
  val barAlpha by
    animateFloatAsState(
      targetValue = 0.5f,
      animationSpec =
        tween(durationMillis = 500, delayMillis = if (scrollState.isScrollInProgress) 0 else 1000),
      label = "scrollbar",
    )
  val barColor = MaterialTheme.colorScheme.secondary

  Column(
    modifier =
      modifier
        .drawWithContent {
          drawContent()

          val barWidth = 4.dp.toPx()

          // Make the scrollbar height proportional to how much scroll space there is
          val maxScrollHeight = scrollState.viewportSize + scrollState.maxValue
          val barLength = (this.size.height / maxScrollHeight) * this.size.height

          // Calculate the top position of the scrollbar based on the scroll progress
          val barRange = this.size.height - barLength
          val barTop = barRange * scrollState.value / scrollState.maxValue

          drawRoundRect(
            color = barColor,
            Offset(x = (this.size.width - barWidth), y = barTop),
            Size(width = barWidth, height = barLength),
            alpha = barAlpha,
            cornerRadius = CornerRadius(x = barWidth / 2, y = barWidth / 2),
          )
        }
        .verticalScroll(scrollState),
    content = content,
  )
}

private object BeaconWidgetConstants {
  val DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY = 3
}
