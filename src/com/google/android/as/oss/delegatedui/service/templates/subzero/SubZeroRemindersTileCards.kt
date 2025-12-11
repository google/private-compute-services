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

package com.google.android.`as`.oss.delegatedui.service.templates.subzero

import android.app.PendingIntent
import android.app.RemoteAction
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.graphics.drawable.toBitmap
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.templates.subzero.Reminder
import com.google.android.`as`.oss.delegatedui.api.integration.templates.subzero.SubZeroRemindersTileTemplateData
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.Constants.ContentShape
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.Constants.DIVIDER_TEXT
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.TestTagConstants.REMINDER_SUGGESTION_CHIPS_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.TestTagConstants.REMINDER_SUMMARY_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.TestTagConstants.REMINDER_TIME_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.subzero.TestTagConstants.REMINDER_TITLE_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.widget.MagicActionSuggestionChip
import com.google.android.`as`.oss.delegatedui.service.templates.widget.SuggestionModel
import kotlinx.coroutines.launch

@Composable
internal fun TemplateRendererScope.SubZeroRemindersTileCards(
  templateData: SubZeroRemindersTileTemplateData,
  cardPendingIntentList: List<PendingIntent>,
  entireSuggestionChipRemoteActionList: List<RemoteAction>,
) {
  Column(
    modifier =
      Modifier.fillMaxWidth().padding(start = SubZeroSpacing.large, end = SubZeroSpacing.large),
    verticalArrangement = spacedBy(SubZeroSpacing.medium),
  ) {
    templateData.remindersList.forEachIndexed { index, reminder ->
      val cardPendingIntent = cardPendingIntentList.getOrNull(index)
      SubZeroReminderCard(reminder, cardPendingIntent, entireSuggestionChipRemoteActionList)
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TemplateRendererScope.SubZeroReminderCard(
  reminder: Reminder,
  cardPendingIntent: PendingIntent?,
  entireSuggestionChipRemoteActionList: List<RemoteAction>,
) {
  val uiIdToken = reminder.uiIdToken
  val scope = rememberCoroutineScope()

  LaunchedEffect(Unit) { doOnImpression(uiIdToken) { logUsage() } }
  Surface(
    onClick = {
      scope.launch {
        doOnInterop(uiIdToken, interactionType = INTERACTION_TYPE_CLICK) {
          if (cardPendingIntent != null) {
            executeAction { cardPendingIntent.toAction() }
          }
        }
      }
    },
    modifier =
      Modifier.fillMaxWidth().semantics {
        onClick(
          label =
            if (reminder.hasClickLabel()) {
              reminder.clickLabel
            } else {
              reminder.defaultClickLabel()
            },
          action = null,
        )
      },
    shape = ContentShape,
    color = MaterialTheme.colorScheme.surfaceBright,
    contentColor = MaterialTheme.colorScheme.onSurface,
  ) {
    Column(
      modifier =
        Modifier.padding(vertical = SubZeroSpacing.medium, horizontal = SubZeroSpacing.large)
    ) {
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(reminder.dataType)
        Spacer(modifier = Modifier.width(width = SubZeroSpacing.medium))
        Column(modifier = Modifier.fillMaxWidth()) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
              modifier = Modifier.weight(1f, fill = false).testTag(REMINDER_TITLE_TEST_TAG),
              text = reminder.text,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            if (reminder.timeLeft.isNotBlank()) {
              Text(
                modifier = Modifier.testTag(REMINDER_TIME_TEST_TAG),
                text = DIVIDER_TEXT + reminder.timeLeft,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
          if (reminder.summary.isNotBlank()) {
            Spacer(modifier = Modifier.height(SubZeroSpacing.xxsmall))
            Text(
              modifier = Modifier.testTag(REMINDER_SUMMARY_TEST_TAG),
              text = reminder.summary,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }
      }
      if (reminder.actionDataList.isNotEmpty()) {
        Row(
          modifier =
            Modifier.fillMaxWidth()
              .padding(start = SubZeroSpacing.medium + SubZeroSpacing.xxxLarge)
              .testTag(REMINDER_SUGGESTION_CHIPS_TEST_TAG),
          horizontalArrangement = spacedBy(SubZeroSpacing.normal),
        ) {
          for (actionData in reminder.actionDataList) {
            val remoteAction =
              entireSuggestionChipRemoteActionList.getOrNull(actionData.remoteActionIndex)
            if (remoteAction == null) {
              continue
            }
            val iconBitmap = remoteAction.iconBitmap
            val text = actionData.title
            val contentDescription = remoteAction.contentDescription.toString()

            MagicActionSuggestionChip(
              modifier = Modifier.padding(top = SubZeroSpacing.medium),
              suggestionModel =
                SuggestionModel(
                  iconBitmap = iconBitmap,
                  text = text,
                  contentDescription = contentDescription,
                ),
              chipImpression = {
                scope.launch { doOnImpression(actionData.uiIdToken) { logUsage() } }
              },
              chipOnClick = {
                scope.launch {
                  doOnInterop(actionData.uiIdToken, interactionType = INTERACTION_TYPE_CLICK) {
                    executeAction { remoteAction.toAction() }
                  }
                }
              },
            )
          }
        }
      }
    }
  }
}

private fun Reminder.defaultClickLabel(): String = dataType.substringBefore(Constants.UNDERSCORE)

private val RemoteAction.iconBitmap: Bitmap?
  @Composable
  get() {
    return if (shouldShowIcon()) {
      val context = LocalContext.current
      remember(icon) {
        try {
          icon.loadDrawable(context)?.toBitmap()
        } catch (e: Exception) {
          e.printStackTrace()
          null
        }
      }
    } else {
      null
    }
  }

@Composable
private fun Icon(dataType: String) {
  val icon =
    when (dataType) {
      Constants.DataType.GMAIL.name -> R.drawable.logo_gmail
      Constants.DataType.WONDER_CARD.name ->
        R.drawable.logo_gmail // Wonder card is PS1 data from Gmail.
      Constants.DataType.MESSAGE.name -> R.drawable.logo_messages
      Constants.DataType.CALENDAR_EVENT.name -> R.drawable.logo_calendar
      Constants.DataType.KEEP_NOTE.name -> R.drawable.logo_keep
      else -> R.drawable.logo_gemini
    }
  val context = LocalContext.current
  val drawable = context.getDrawable(icon)

  Image(
    modifier = Modifier.size(with(LocalDensity.current) { SubZeroSpacing.xxxLarge }),
    painter = rememberDrawablePainter(drawable),
    contentDescription = null,
    contentScale = ContentScale.Fit,
  )
}

private object Constants {
  const val UNDERSCORE = "_"
  const val DIVIDER_TEXT: String = " • "

  val ContentShape = RoundedCornerShape(SubZeroRadius.xsmall)

  enum class DataType {
    GMAIL,
    WONDER_CARD,
    MESSAGE,
    CALENDAR_EVENT,
    KEEP_NOTE,
    UNKNOWN,
  }
}

object TestTagConstants {
  const val REMINDER_TITLE_TEST_TAG = "ReminderTitle"
  const val REMINDER_TIME_TEST_TAG = "ReminderTime"
  const val REMINDER_SUMMARY_TEST_TAG = "ReminderSummary"
  const val REMINDER_SUGGESTION_CHIPS_TEST_TAG = "ReminderSuggestionChips"
}
