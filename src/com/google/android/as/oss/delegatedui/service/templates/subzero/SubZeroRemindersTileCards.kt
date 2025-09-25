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
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.google.android.`as`.oss.delegatedui.api.integration.templates.subzero.Reminder
import com.google.android.`as`.oss.delegatedui.api.integration.templates.subzero.SubZeroRemindersTileTemplateData
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope

@Composable
internal fun TemplateRendererScope.SubZeroRemindersTileCards(
  templateData: SubZeroRemindersTileTemplateData,
  pendingIntentList: List<PendingIntent>,
) {
  Column(
    modifier =
      Modifier.padding(start = SubZeroSpacing.large, end = SubZeroSpacing.large).fillMaxWidth(),
    verticalArrangement = spacedBy(10.dp),
  ) {
    templateData.remindersList.forEachIndexed { index, reminder ->
      val pendingIntent = pendingIntentList.getOrNull(index)
      Row(modifier = Modifier.fillMaxWidth()) {
        SubZeroReminderCard(index, reminder, pendingIntent)
      }
    }
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TemplateRendererScope.SubZeroReminderCard(
  index: Int,
  reminder: Reminder,
  pendingIntent: PendingIntent?,
) {
  val uiIdToken = reminder.uiIdToken
  val cardShape = RoundedCornerShape(SubZeroRadius.medium)
  Card(
    modifier =
      Modifier.fillMaxWidth()
        .then(
          if (pendingIntent != null) {
            Modifier.clip(cardShape)
              .doOnClick(uiIdToken) { executeAction { pendingIntent.toAction() } }
              .semantics {
                onClick(
                  label =
                    if (reminder.hasClickLabel()) {
                      reminder.clickLabel
                    } else {
                      reminder.defaultClickLabel()
                    },
                  action = null,
                )
              }
          } else {
            Modifier
          }
        ),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
      ),
    shape = cardShape,
  ) {
    doOnImpression(uiIdToken) { logUsage() }
    Row(
      modifier = Modifier.fillMaxWidth().padding(SubZeroSpacing.large),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Left text
      Text(
        text = reminder.text,
        style = MaterialTheme.typography.titleMedium.withFlexFont(weight = 500),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )

      // Right icon and time left
      Spacer(modifier = Modifier.size(SubZeroSpacing.large))
      Text(
        text = reminder.timeLeft,
        style = MaterialTheme.typography.titleSmall.withFlexFont(weight = 500),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Spacer(modifier = Modifier.size(SubZeroSpacing.small))
      Icon(dataType = reminder.dataType)
    }
  }
}

private fun Reminder.defaultClickLabel(): String = dataType.substringBefore(Constants.UNDERSCORE)

@Composable
private fun Icon(dataType: String) {
  val icon =
    when (dataType) {
      Constants.DataType.GMAIL.name -> R.drawable.logo_gmail
      Constants.DataType.MESSAGE.name -> R.drawable.logo_messages
      Constants.DataType.CALENDAR_EVENT.name -> R.drawable.logo_calendar
      Constants.DataType.KEEP_NOTE.name -> R.drawable.logo_keep
      else -> R.drawable.logo_gemini
    }
  val context = LocalContext.current
  val drawable = context.getDrawable(icon)

  Image(
    modifier = Modifier.size(with(LocalDensity.current) { SubZeroSpacing.xLarge }),
    painter = rememberDrawablePainter(drawable),
    contentDescription = null,
    contentScale = ContentScale.Fit,
  )
}

private object Constants {
  val CARD_HEIGHT = 53.dp
  val UNDERSCORE = "_"

  enum class DataType {
    GMAIL,
    MESSAGE,
    CALENDAR_EVENT,
    KEEP_NOTE,
    UNKNOWN,
  }
}
