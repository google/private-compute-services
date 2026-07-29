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
@file:OptIn(
  androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
  androidx.compose.material3.ExperimentalMaterial3Api::class,
  androidx.compose.foundation.ExperimentalFoundationApi::class,
)

package com.android.personalcontext.ace.internal.templates.weather

import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.RenderToken
import android.service.personalcontext.insight.PublishedContextInsight
import android.service.personalcontext.insight.interaction.InsightEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint
import com.android.personalcontext.ace.client.prototype.weather.WeatherHint.SuggestionType
import com.android.personalcontext.ace.client.prototype.weather.WeatherInsight.ChipContent
import com.android.personalcontext.ace.common.gradientTint
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.internal.R
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.weather.WeatherTemplateData.Companion.toWeatherTemplateData
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import javax.inject.Inject

/** A [VisualizerTemplate] that renders a weather template UI. */
class WeatherVisualizerTemplate @Inject internal constructor(private val themeCompat: ThemeCompat) :
  VisualizerTemplate {

  override fun handleInsight(
    publishedInsight: IPublishedContextInsight
  ): (@Composable () -> Unit)? {
    val insight = publishedInsight.insight
    val hint = insight.findPrototypeHint<WeatherHint>() ?: return null
    val weatherTemplateData = insight.toWeatherTemplateData(hint, themeCompat)

    return { WeatherTemplate(weatherTemplateData, publishedInsight) }
  }
}

@Composable
private fun WeatherTemplate(data: WeatherTemplateData, publishedInsight: IPublishedContextInsight) {
  val context = LocalContext.current
  val renderToken = LocalRenderToken.current
  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }

  WeatherTheme {
    if (data.suggestionType == SuggestionType.EVENT) {
      EventTemplate(data) { eventType ->
        personalContextManager?.reportInsightEvent(
          publishedInsight.unwrap(),
          eventType,
          renderToken.unwrap(),
        )
      }
    } else {
      LocationTemplate(data) { eventType ->
        personalContextManager?.reportInsightEvent(
          publishedInsight.unwrap(),
          eventType,
          renderToken.unwrap(),
        )
      }
    }
  }
}

@Composable
private fun EventTemplate(data: WeatherTemplateData, reportEvent: (Int) -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    for (chip in data.chipContents) {
      EventCard(
        chip = chip,
        reportEvent = reportEvent,
        showBrandedIcon = data.shouldShowBrandedIcon,
      )
    }
  }
}

@Composable
private fun EventCard(chip: ChipContent, reportEvent: (Int) -> Unit, showBrandedIcon: Boolean) {
  val info = LocalInsightSurfaceClientInfo.current
  LaunchedEffect(Unit) { reportEvent(InsightEvent.EVENT_SHOW) }
  Card(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max).clip(CARD_SHAPE)) {
    Row(
      modifier =
        Modifier.fillMaxSize()
          .clip(CARD_SHAPE)
          .combinedClickable(
            onClick = {
              info.onReceiveInsight(chip.toContextInsight())
              reportEvent(InsightEvent.EVENT_USER_TAP)
            },
            onLongClick = { reportEvent(InsightEvent.EVENT_USER_LONG_PRESS) },
          )
          .background(color = MaterialTheme.colorScheme.secondaryContainer)
          .padding(vertical = 12.dp, horizontal = 12.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val painter = chip.image?.let { BitmapPainter(it.asImageBitmap()) }
      val iconBackgroundColor =
        if (showBrandedIcon) MaterialTheme.colorScheme.surfaceBright
        else MaterialTheme.colorScheme.secondary
      PrimaryRoundedIcon(
        iconBackgroundWidth = 40.dp,
        iconWidth = 20.dp,
        iconColor = MaterialTheme.colorScheme.onSecondary,
        iconBackgroundColor = iconBackgroundColor,
        painter = painter,
        isIconGradient = showBrandedIcon,
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column(verticalArrangement = Arrangement.SpaceAround) {
        Text(
          chip.title,
          maxLines = 2,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          chip.subtitle,
          maxLines = 1,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.Normal,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun LocationTemplate(data: WeatherTemplateData, reportEvent: (Int) -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    for (chip in data.chipContents) {
      LocationCard(
        chip = chip,
        reportEvent = reportEvent,
        showBrandedIcon = data.shouldShowBrandedIcon,
      )
    }
  }
}

@Composable
private fun LocationCard(chip: ChipContent, reportEvent: (Int) -> Unit, showBrandedIcon: Boolean) {
  val info = LocalInsightSurfaceClientInfo.current
  val dismissState = rememberSwipeToDismissBoxState()

  LaunchedEffect(Unit) { reportEvent(InsightEvent.EVENT_SHOW) }

  SwipeToDismissBox(
    state = dismissState,
    backgroundContent = { DeletionRow(dismissState.dismissDirection) },
    modifier = Modifier.fillMaxWidth().height(117.dp).padding(horizontal = 16.dp),
    onDismiss = {
      // Tells client that the user dismissed the embedded session.
      info.onReceiveInsight(ServerSideCloseInsight().toContextInsight())
      // Tells pingback handler to update interaction Db
      reportEvent(InsightEvent.EVENT_USER_DISMISS)
    },
  ) {
    Row(
      modifier =
        Modifier.fillMaxSize()
          .clip(CARD_SHAPE)
          .combinedClickable(
            onClick = {
              info.onReceiveInsight(chip.toContextInsight())
              reportEvent(InsightEvent.EVENT_USER_TAP)
            },
            onLongClick = { reportEvent(InsightEvent.EVENT_USER_LONG_PRESS) },
          )
          .background(color = MaterialTheme.colorScheme.secondaryContainer)
          .padding(vertical = 20.dp, horizontal = 21.dp),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val painter = chip.image?.let { BitmapPainter(it.asImageBitmap()) }
      val iconBackgroundColor =
        if (showBrandedIcon) MaterialTheme.colorScheme.surfaceBright
        else MaterialTheme.colorScheme.secondary
      PrimaryRoundedIcon(
        iconBackgroundWidth = 59.dp,
        iconWidth = 32.dp,
        iconColor = MaterialTheme.colorScheme.onSecondary,
        iconBackgroundColor = iconBackgroundColor,
        painter = painter,
        isIconGradient = showBrandedIcon,
      )
      Spacer(modifier = Modifier.width(16.dp))
      Column(verticalArrangement = Arrangement.SpaceAround) {
        Text(
          chip.title,
          maxLines = 1,
          color = MaterialTheme.colorScheme.inverseSurface,
          style = MaterialTheme.typography.titleLargeEmphasized,
          fontWeight = FontWeight.Medium,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 2.dp),
        )
        Text(
          chip.subtitle,
          maxLines = 1,
          color = MaterialTheme.colorScheme.inverseSurface,
          style = MaterialTheme.typography.titleSmallEmphasized,
          fontWeight = FontWeight.SemiBold,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 3.dp),
        )
        chip.source?.let {
          Text(
            it,
            maxLines = 1,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun PrimaryRoundedIcon(
  iconBackgroundWidth: Dp = 56.dp,
  iconWidth: Dp = 24.dp,
  iconColor: Color,
  iconBackgroundColor: Color,
  painter: Painter?,
  isIconGradient: Boolean = false,
) {
  if (painter == null) return
  Box(
    modifier = Modifier.width(iconBackgroundWidth).fillMaxHeight(),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(
      modifier = Modifier.size(iconBackgroundWidth),
      onDraw = { drawCircle(color = iconBackgroundColor) },
    )
    val iconModifier =
      if (isIconGradient) {
        val primaryFixedDimColor = MaterialTheme.colorScheme.primaryFixedDim
        val primaryColor = MaterialTheme.colorScheme.primary
        Modifier.gradientTint(listOf(primaryFixedDimColor, primaryColor))
      } else {
        Modifier
      }
    Icon(
      painter,
      contentDescription = null,
      modifier = Modifier.size(iconWidth).then(iconModifier),
      tint = iconColor,
    )
  }
}

@Composable
private fun DeletionRow(direction: SwipeToDismissBoxValue) =
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .fillMaxHeight()
        .padding(horizontal = 1.dp)
        .clip(CARD_SHAPE)
        .background(color = MaterialTheme.colorScheme.onErrorContainer)
        .padding(horizontal = 17.dp),
    horizontalArrangement =
      if (direction == SwipeToDismissBoxValue.EndToStart) Arrangement.End else Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(id = R.drawable.gs_delete_wght300_vd_theme_24),
      contentDescription = null,
      modifier = Modifier.size(26.dp),
      tint = MaterialTheme.colorScheme.onError,
    )
  }

@Composable
private fun WeatherTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
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

  MaterialTheme(colorScheme = colorScheme, typography = Typography()) { content() }
}

private val CARD_SHAPE = RoundedCornerShape(100.dp)

private fun PersonalContextManager.reportInsightEvent(
  insight: PublishedContextInsight?,
  eventType: Int,
  renderToken: RenderToken?,
) {
  reportInsightEvent(insight ?: return, eventType, renderToken ?: return)
}
