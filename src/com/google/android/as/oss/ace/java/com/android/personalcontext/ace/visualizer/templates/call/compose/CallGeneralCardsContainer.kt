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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.visualizer.templates.call.compose

import android.app.RemoteAction
import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon as MaterialIcon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.visualizer.R
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.call.data.CallVisualizerGeneralCard
import com.android.personalcontext.ace.visualizer.templates.utils.IconOrImage
import com.android.personalcontext.ace.visualizer.templates.utils.TintableIcon
import com.android.personalcontext.ace.visualizer.templates.utils.asTintableIcon

private const val TAG = "CallGeneralCardsContainer"

private val EXPAND_ICON_CONTENT_SIZE = 16.dp
private val EXPAND_ICON_PADDING = 4.dp
private val EXPAND_ICON_SIZE = EXPAND_ICON_CONTENT_SIZE + (EXPAND_ICON_PADDING * 2)

/** Layout for a list of general cards. */
fun LazyListScope.CallGeneralCardsContainer(
  cards: List<List<CallVisualizerGeneralCard>>,
  numGeneralCardsPerSource: Int,
  isLoneGeneralCard: Boolean = false,
  useCardContainer: Boolean = false,
  horizontalPadding: Dp = 0.dp,
) {
  // Step 1. Get the general cards that should be displayed
  val generalCardsToDisplay = getGeneralCardsToDisplay(cards, numGeneralCardsPerSource)

  // Step 2. Flatten the 2-D list into a 1-D list
  val generalCardsFlattened = generalCardsToDisplay.flatten()

  itemsIndexed(generalCardsFlattened, key = { _, card -> card.listUuid }) { i, generalCard ->
    val isFirstCard = i == 0
    val isLastCard = i == generalCardsFlattened.size - 1

    if (!isFirstCard) {
      val cardBackground = LocalCallWidgetBackgrounds.current.cardBackground
      val containerColor =
        if (cardBackground != Color.Unspecified) cardBackground else Color.Transparent

      HorizontalDivider(
        modifier = Modifier.background(containerColor).padding(horizontal = horizontalPadding),
        color = MaterialTheme.colorScheme.outlineVariant,
        thickness = 1.dp,
      )
    }
    CallGeneralCardContainer(
      card = generalCard,
      isLoneGeneralCard = isLoneGeneralCard,
      isFirstCard = isFirstCard,
      isLastCard = isLastCard,
      useCardContainer = useCardContainer,
      horizontalPadding = horizontalPadding,
    )
  }
}

@Composable
private fun CallGeneralCardContainer(
  card: CallVisualizerGeneralCard,
  isLoneGeneralCard: Boolean,
  isFirstCard: Boolean,
  isLastCard: Boolean,
  useCardContainer: Boolean,
  horizontalPadding: Dp,
) {
  val cardBackground = LocalCallWidgetBackgrounds.current.cardBackground
  val containerColor =
    if (cardBackground != Color.Unspecified) cardBackground else Color.Transparent

  if (useCardContainer) {
    Card(
      colors = CardDefaults.cardColors(containerColor = containerColor),
      shape =
        RoundedCornerShape(
          topStart = if (isFirstCard) 24.dp else 0.dp,
          topEnd = if (isFirstCard) 24.dp else 0.dp,
          bottomStart = if (isLastCard) 24.dp else 0.dp,
          bottomEnd = if (isLastCard) 24.dp else 0.dp,
        ),
    ) {
      CallGeneralCardContent(
        card = card,
        isLoneGeneralCard = isLoneGeneralCard,
        horizontalPadding = horizontalPadding,
      )
    }
  } else {
    CallGeneralCardContent(
      card = card,
      isLoneGeneralCard = isLoneGeneralCard,
      horizontalPadding = horizontalPadding,
    )
  }
}

/** Layout for a single general card. */
@Composable
private fun CallGeneralCardContent(
  card: CallVisualizerGeneralCard,
  isLoneGeneralCard: Boolean,
  horizontalPadding: Dp,
) {
  // Metrics logging START
  val context = LocalContext.current
  val insightEventReporter = LocalInsightEventReporter.current
  val publishedInsight = LocalPublishedContextInsight.current
  val renderToken = LocalRenderToken.current

  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }

  fun reportEvent(event: Int) {
    with(insightEventReporter) {
      personalContextManager?.reportChildInsightEvent(
        publishedInsight,
        card.originalInsight,
        event,
        renderToken,
      )
    }
  }

  var hasReportedImpression by rememberSaveable { mutableStateOf(false) }
  LaunchedEffect(Unit) {
    if (!hasReportedImpression) {
      reportEvent(InsightEvent.EVENT_SHOW)
      hasReportedImpression = true
    }
  }

  // Metrics logging END

  var isExpanded by rememberSaveable { mutableStateOf(isLoneGeneralCard) }

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .semantics(mergeDescendants = true) { this.role = Role.Button }
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null,
          onClick = {
            isExpanded = !isExpanded
            Log.i(TAG, "[CallEmbedded] Expand state updated. isExpanded: $isExpanded")
          },
        )
        .padding(vertical = 16.dp, horizontal = horizontalPadding)
  ) {
    // Calculate the top padding needed for the date Text to align its top edge with the
    // title Text.
    // Only add padding if the title's top offset is larger than the date's.
    val titleTextStyle = MaterialTheme.typography.titleMedium
    val dateTextStyle = MaterialTheme.typography.labelSmall
    val dateTopOffsetSp =
      ((titleTextStyle.lineHeight.value - dateTextStyle.lineHeight.value) / 2)
        .coerceAtLeast(0f)
        .toInt()
        .sp
    val dateTopPaddingDp = with(LocalDensity.current) { dateTopOffsetSp.toPx().toDp() }

    val titleLineHeightDp = with(LocalDensity.current) { titleTextStyle.lineHeight.toPx().toDp() }
    val iconTopPaddingDp = ((titleLineHeightDp - EXPAND_ICON_SIZE) / 2).coerceAtLeast(0.dp)

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
      val ICON_SPACING = 16.dp
      if (card.dataSource != null) {
        SourceIcon(card.dataSource!!)
        Spacer(modifier = Modifier.width(ICON_SPACING))
      } else {
        // Ensure the detailedText and title are left aligned
        Spacer(modifier = Modifier.width(24.dp + ICON_SPACING))
      }
      Column(modifier = Modifier.weight(1f)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              modifier = Modifier.weight(1f, fill = false).animateContentSize(),
              text = card.title,
              style =
                titleTextStyle.copy(
                  lineBreak =
                    LineBreak(
                      strategy = LineBreak.Strategy.HighQuality,
                      strictness = LineBreak.Strictness.Strict,
                      wordBreak = LineBreak.WordBreak.Phrase,
                    ),
                  hyphens = Hyphens.Auto,
                ),
              color = MaterialTheme.colorScheme.onSurface,
              maxLines =
                if (isExpanded) {
                  2
                } else {
                  1
                },
              overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              modifier = Modifier.padding(top = dateTopPaddingDp),
              text = card.date,
              style = dateTextStyle,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(modifier = Modifier.width(6.dp))
          Box(modifier = Modifier.padding(top = iconTopPaddingDp)) {
            ExpandIcon(isExpanded = isExpanded)
          }
        }
        card.detailedText?.let { detailedText ->
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            text = detailedText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines =
              if (isExpanded) {
                4
              } else {
                1
              },
            overflow = TextOverflow.Ellipsis,
          )
        }
        AnimatedVisibility(visible = isExpanded) {
          Spacer(modifier = Modifier.height(16.dp))

          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            card.originalDataSource?.let { dataSource ->
              DeeplinkText(
                text = dataSource.title,
                sourceName = dataSource.appName,
                remoteAction = dataSource.remoteAction,
                deeplinkInsight = null,
              )
            }

            Spacer(modifier = Modifier.weight(1f))

            FeedbackButtons(card.originalInsight)
          }
        }
      }
    }
  }
}

/**
 * The icon for the source. For example, an icon for the Gmail app if the data comes from Gmail.
 * Renders nothing if no icon was found.
 */
@Composable
private fun SourceIcon(dataSource: RemoteAction) {
  val context = LocalContext.current
  val tintableIcon: TintableIcon? =
    remember(dataSource.icon) {
      dataSource.icon.loadDrawable(context)?.toBitmap()?.asTintableIcon(false)
    }

  if (tintableIcon != null) {
    IconOrImage(modifier = Modifier.size(24.dp), icon = tintableIcon)
  }
}

/**
 * Displays an icon indicating the expanded state.
 *
 * @param isExpanded Whether the card is currently expanded.
 */
@Composable
private fun ExpandIcon(isExpanded: Boolean) {
  val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f)

  Box(
    modifier =
      Modifier.background(color = MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape)
        .padding(EXPAND_ICON_PADDING),
    contentAlignment = Alignment.Center,
  ) {
    MaterialIcon(
      modifier = Modifier.size(EXPAND_ICON_CONTENT_SIZE).rotate(rotationAngle),
      painter = painterResource(R.drawable.gs_keyboard_arrow_down_vd_theme_24),
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSecondaryContainer,
    )
  }
}

/**
 * For each source, returns the first [DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY] cards if
 * [isDisplayingMoreResults] is false.
 *
 * @param generalCardsAll The list of general cards to display.
 * @param numGeneralCardsPerSource The number of general cards to display per source. Can only be
 *   Int.MAX_VALUE or [DEFAULT_MAX_GENERAL_CARDS_TO_DISPLAY] or 0. Must be >= 0.
 * @return The same 2-D list, but filtered to the first [numGeneralCardsPerSource] cards per source.
 * @throws IllegalArgumentException if [numGeneralCardsPerSource] is < 0.
 */
private fun getGeneralCardsToDisplay(
  generalCardsAll: List<List<CallVisualizerGeneralCard>>,
  numGeneralCardsPerSource: Int,
): List<List<CallVisualizerGeneralCard>> {
  require(numGeneralCardsPerSource >= 0) { "[CallEmbedded] numGeneralCardsPerSource must be >= 0" }

  val filteredGeneralCards = generalCardsAll.map { generalCardsSingleSource ->
    generalCardsSingleSource.take(numGeneralCardsPerSource)
  }

  return filteredGeneralCards
}
