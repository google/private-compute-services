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

@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.google.android.`as`.oss.dataattribution

import android.app.ActivityOptions
import android.app.PendingIntent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.window.flags.ExportedFlags.balAdditionalStartModes
import com.google.android.`as`.oss.dataattribution.proto.AttributionCardData
import com.google.android.`as`.oss.dataattribution.proto.AttributionChipData
import com.google.android.`as`.oss.dataattribution.proto.AttributionDialogData
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.utils.IconOrImage
import com.google.android.`as`.oss.delegatedui.utils.SerializableBitmap.deserializeToBitmap
import com.google.android.`as`.oss.delegatedui.utils.asTintableIcon
import com.google.android.`as`.oss.feedback.FeedbackApi
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment
import com.google.android.`as`.oss.feedback.api.entityFeedbackDialogData
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import com.google.common.flogger.GoogleLogger

/** Dialog displayed when a user long clicks Delegated UI. */
@Composable
internal fun DataAttributionDialog(
  viewModel: DataAttributionDialogViewModel = viewModel(),
  attributionDialogData: AttributionDialogData,
  attributionChipData: AttributionChipData?,
  sourceDeepLinks: Array<PendingIntent?>?,
  onDismissRequest: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsState()
  val logUiEvent = { elementType: Int, interactionType: InteractionType ->
    viewModel.logUiEvent(
      elementType,
      clientSessionId = attributionDialogData.sessionId,
      interactionType = interactionType,
    )
  }

  LaunchedEffect(Unit) {
    logUiEvent(
      DataAttributionUiElementType.ATTRIBUTION_DIALOG.id,
      InteractionType.INTERACTION_TYPE_VIEW,
    )
  }

  DataAttributionScaffold(
    data = attributionDialogData,
    chipContent = { if (attributionChipData != null) AttributionChip(attributionChipData) },
    cardContent = { i, cardData ->
      AttributionCard(
        data = cardData,
        deepLink = sourceDeepLinks?.getOrNull(i),
        logUiEvent = logUiEvent,
        onDismissRequest = onDismissRequest,
      )
    },
    footerContent = {
      DataAttributionDialogFooter(
        uiState = uiState,
        data = attributionDialogData,
        feedbackSessionId = attributionDialogData.sessionId,
        feedbackEntityContent = attributionChipData?.chipLabel,
        logUiEvent = logUiEvent,
        onDismissRequest = onDismissRequest,
      )
    },
    onDismissRequest = onDismissRequest,
  )
}

@Composable
private fun DataAttributionScaffold(
  data: AttributionDialogData,
  chipContent: @Composable LazyItemScope.() -> Unit,
  cardContent: @Composable LazyItemScope.(Int, AttributionCardData) -> Unit,
  footerContent: @Composable () -> Unit,
  onDismissRequest: () -> Unit,
) {
  MainTheme {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerLow) {
      Column(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
        // Virtual dismiss button for TalkBack.
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .semantics {
                role = Role.Button
                contentDescription = data.dismissButtonText
                traversalIndex = 1f // Don't initially focus on this element.
              }
              .clickable { onDismissRequest() }
        )

        // Title and logo
        Row(
          modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = data.title,
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.onSurface,
          )
          data.logo?.deserializeToBitmap()?.let { logoBitmap ->
            Spacer(Modifier.width(16.dp))
            Box(modifier = Modifier.padding(vertical = 5.dp), contentAlignment = Alignment.Center) {
              Icon(
                modifier = Modifier.size(24.dp),
                bitmap = logoBitmap.asImageBitmap(),
                tint = MaterialTheme.colorScheme.onSurface,
                contentDescription = null,
              )
            }
          }
        }

        LazyColumn(
          modifier =
            Modifier.fillMaxWidth()
              .weight(1f, fill = false)
              .padding(start = 16.dp, top = 16.dp, end = 16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
          // Message in header.
          item {
            Text(
              text = data.message,
              modifier = Modifier.fillMaxWidth(),
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }

          // Chip in header.
          item { chipContent() }

          if (data.attributionsList.isNotEmpty()) {
            // Header of attributions list.
            item {
              Text(
                text = data.attributionHeader,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
            }

            // Cards of attributions list.
            item {
              Column {
                for (i in data.attributionsList.indices) {
                  Box { cardContent(i, data.attributionsList[i]) }
                }
              }
            }
          }
        }

        footerContent()
      }
    }
  }
}

@Composable
private fun AttributionChip(data: AttributionChipData) {
  Row(
    modifier =
      Modifier.border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.outline,
          shape = RoundedCornerShape(size = 20.dp),
        )
        .fillMaxWidth()
        .heightIn(min = 72.dp)
        .padding(16.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    val chipIcon =
      data.chipIcon.deserializeToBitmap()?.asTintableIcon(tintable = true)
        ?: data.chipImage.deserializeToBitmap()?.asTintableIcon(tintable = false)

    chipIcon?.let {
      Box(
        modifier =
          Modifier.width(40.dp)
            .height(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        IconOrImage(
          icon = chipIcon,
          modifier = Modifier.sizeIn(minWidth = 24.dp, minHeight = 24.dp),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
    }
    Spacer(modifier = Modifier.width(16.dp))
    Text(
      text = data.chipLabel,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}

/** Nested cards which display attribution data in the attribution dialog. */
@Composable
fun AttributionCard(
  data: AttributionCardData,
  deepLink: PendingIntent?,
  logUiEvent: (Int, InteractionType) -> Unit,
  onDismissRequest: () -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(pendingIntent = deepLink) {
          logUiEvent(
            DataAttributionUiElementType.ATTRIBUTION_CLICKABLE_ITEM.id,
            InteractionType.INTERACTION_TYPE_CLICK,
          )
          logger.atInfo().log("Sending pending intent: %s", deepLink)
          onDismissRequest()
        }
        .padding(horizontal = 8.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    data.bitmap.deserializeToBitmap()?.let { cardBitmap ->
      Image(
        modifier = Modifier.size(40.dp),
        bitmap = cardBitmap.asImageBitmap(),
        contentDescription = null,
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = data.title,
        style = MaterialTheme.typography.titleMediumEmphasized,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurface,
      )
      data.subtitle
        ?.takeIf { it.isNotBlank() }
        ?.let { subtitleText ->
          Text(
            text = subtitleText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(),
          )
        }
    }

    if (deepLink != null) {
      Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
        Icon(
          modifier = Modifier.size(24.dp),
          painter = painterResource(R.drawable.gs_keyboard_arrow_right_24dp),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
internal fun DataAttributionDialogFooter(
  uiState: DataAttributionUiState,
  data: AttributionDialogData,
  feedbackSessionId: String,
  feedbackEntityContent: String?,
  logUiEvent: (Int, InteractionType) -> Unit,
  onDismissRequest: () -> Unit,
) {
  val context = LocalContext.current

  Row(
    modifier = Modifier.padding(8.dp).fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    // Settings
    if (uiState.settingsIntent != null) {
      Row(
        modifier =
          Modifier.clip(shape = RoundedCornerShape(percent = 100))
            .clickable(
              onClick = {
                logUiEvent(
                  DataAttributionUiElementType.ATTRIBUTION_SETTINGS_BUTTON.id,
                  InteractionType.INTERACTION_TYPE_CLICK,
                )
                context.startActivity(uiState.settingsIntent)
                onDismissRequest()
              }
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Icon(
          modifier = Modifier.size(20.dp),
          painter = painterResource(R.drawable.gs_settings_24dp),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          text = data.settingsButtonText,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Feedback. Only shown when feedbackEntityContent is provided.
    if (feedbackEntityContent != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        IconButton(
          onClick = {
            logUiEvent(
              DataAttributionUiElementType.ATTRIBUTION_GOOD_FEEDBACK_BUTTON.id,
              InteractionType.INTERACTION_TYPE_CLICK,
            )
            context.startActivity(
              FeedbackApi.createEntityFeedbackIntent(
                context = context,
                data =
                  entityFeedbackDialogData {
                    this.entityContent = feedbackEntityContent
                    this.clientSessionId = feedbackSessionId
                    this.ratingSentiment = FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
                    this.dialogCommonData = data.feedbackDialogCommonData
                  },
              )
            )
            onDismissRequest()
          },
          modifier = Modifier.size(48.dp),
        ) {
          LaunchedEffect(Unit) {
            logUiEvent(
              DataAttributionUiElementType.ATTRIBUTION_GOOD_FEEDBACK_BUTTON.id,
              InteractionType.INTERACTION_TYPE_VIEW,
            )
          }

          Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.gs_thumb_up_24dp),
            contentDescription = data.thumbsUpButtonContentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        IconButton(
          onClick = {
            logUiEvent(
              DataAttributionUiElementType.ATTRIBUTION_BAD_FEEDBACK_BUTTON.id,
              InteractionType.INTERACTION_TYPE_CLICK,
            )
            context.startActivity(
              FeedbackApi.createEntityFeedbackIntent(
                context = context,
                data =
                  entityFeedbackDialogData {
                    this.entityContent = feedbackEntityContent
                    this.clientSessionId = feedbackSessionId
                    this.ratingSentiment = FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
                    this.dialogCommonData = data.feedbackDialogCommonData
                  },
              )
            )
            onDismissRequest()
          },
          modifier = Modifier.size(48.dp),
        ) {
          LaunchedEffect(Unit) {
            logUiEvent(
              DataAttributionUiElementType.ATTRIBUTION_BAD_FEEDBACK_BUTTON.id,
              InteractionType.INTERACTION_TYPE_VIEW,
            )
          }

          Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(R.drawable.gs_thumb_down_24dp),
            contentDescription = data.thumbsDownButtonContentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@Composable
fun MainTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography().withFlexFont()) { content() }
}

@Composable
private fun Modifier.clickable(pendingIntent: PendingIntent?, onClick: () -> Unit): Modifier {
  return if (pendingIntent == null) {
    this
  } else {
    this.clickable {
      pendingIntent.send(
        ActivityOptions.makeBasic()
          .apply {
            if (balAdditionalStartModes()) {
              setPendingIntentBackgroundActivityStartMode(
                // TODO: Update to MODE_BACKGROUND_ACTIVITY_START_ALLOW_IF_VISIBLE
                ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
              )
            }
          }
          .toBundle()
      )
      onClick()
    }
  }
}

private val logger = GoogleLogger.forEnclosingClass()

enum class DataAttributionUiElementType(val id: Int) {
  ATTRIBUTION_GOOD_FEEDBACK_BUTTON(34),
  ATTRIBUTION_BAD_FEEDBACK_BUTTON(35),
  ATTRIBUTION_SETTINGS_BUTTON(41),
  ATTRIBUTION_DIALOG(42),
  ATTRIBUTION_CLICKABLE_ITEM(43),
}
