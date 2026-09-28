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

package com.android.personalcontext.ace.internal.templates.gboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.hardware.input.InputManager
import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import android.service.personalcontext.insight.interaction.InsightEvent
import android.view.KeyEvent
import android.view.WindowManager
import android.window.TrustedPresentationThresholds
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.client.prototype.gboard.GboardHint
import com.android.personalcontext.ace.client.prototype.gboard.KeyEventHint
import com.android.personalcontext.ace.common.gradientTint
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils.GeminiAnimationSpec
import com.android.personalcontext.ace.internal.findprototypehint.FindPrototypeHint.findPrototypeHint
import com.android.personalcontext.ace.internal.templates.richcard.common.withDefaultFontFamily
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import com.android.personalcontext.ace.visualizer.templates.utils.EmbeddedTheme
import com.android.personalcontext.ace.visualizer.templates.utils.IconOrImage
import com.android.personalcontext.ace.visualizer.templates.utils.asTintableIcon
import com.google.common.flogger.GoogleLogger
import java.util.concurrent.Executor
import java.util.function.Consumer
import javax.inject.Inject

private val logger = GoogleLogger.forEnclosingClass()
val GboardChipShape = RoundedCornerShape(16.dp)

class GboardVisualizerTemplate
@Inject
constructor(
  private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat,
  private val themeCompat: ThemeCompat,
) : VisualizerTemplate {

  override fun handleInsight(
    publishedInsight: IPublishedContextInsight
  ): (@Composable () -> Unit)? {
    var insight = publishedInsight.insight
    if (insight is InsightCollection) {
      if (insight.insights.size != 1) return null
      insight = insight.insights[0] ?: return null
    }
    if (insight !is DisplayInsight) return null
    val gboardHint = insight.findPrototypeHint<GboardHint>() ?: return null
    val isNesta = gboardHint.queryCategory == GboardHint.QueryCategory.NESTA
    val positionalState = gboardHint.positionalState
    if (isNesta) {
      logger.atInfo().log("Entering GboardHint.QueryCategory.NESTA")
    } else {
      logger.atInfo().log("Entering GboardHint.QueryCategory.CHROME")
    }
    return {
      PhysicalKeyNavigationEffect(insight, publishedInsight)
      GboardVisualizer(
        insight = insight,
        publishedInsight = publishedInsight,
        energyEffectsAnimationCompat = energyEffectsAnimationCompat,
        themeCompat = themeCompat,
        isNesta = isNesta,
        positionalState = positionalState,
      )
    }
  }
}

@Composable
fun GboardVisualizer(
  insight: DisplayInsight,
  publishedInsight: IPublishedContextInsight,
  energyEffectsAnimationCompat: EnergyEffectsAnimationCompat,
  themeCompat: ThemeCompat,
  isNesta: Boolean = false,
  positionalState: GboardHint.PositionalState? = null,
) {
  val context = LocalContext.current
  val renderToken = LocalRenderToken.current
  val info = LocalInsightSurfaceClientInfo.current
  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }

  LaunchedEffect(publishedInsight) {
    personalContextManager?.reportInsightEvent(
      publishedInsight.unwrap() ?: return@LaunchedEffect,
      InsightEvent.EVENT_SHOW,
      renderToken.unwrap() ?: return@LaunchedEffect,
    )
  }

  GboardTheme {
    val spec = createGboardChipSpec(context, positionalState)
    val bgColor = getGboardChipBackgroundColor()
    val shape = getGboardChipShape(positionalState)
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .height(48.dp)
          .clip(shape)
          .then(
            with(energyEffectsAnimationCompat) {
              Modifier.applyEnergyEffectsAnimation(
                spec = spec,
                fallback = { this.background(bgColor) },
              )
            }
          )
          .clickable {
            info.onReceiveInsight(publishedInsight.insight)
            personalContextManager?.reportInsightEvent(
              publishedInsight.unwrap() ?: return@clickable,
              InsightEvent.EVENT_USER_TAP,
              renderToken.unwrap() ?: return@clickable,
            )
          }
          .padding(start = 12.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      val showBrandedIcon = with(themeCompat) { insight.shouldShowBrandedIcon() }
      val icon =
        insight.details.icon
          ?: if (!isNesta) {
            Icon.createWithResource(
              context,
              com.google.android.assets.gs.R.drawable.gs_chrome_product_vd_theme_24,
            )
          } else {
            null
          }
      icon?.let { GboardIcon(icon = it, showBrandedIcon = showBrandedIcon, size = 18.dp) }

      val title = (insight.details.title ?: "").toString()
      val subtitle = insight.details.subtitle?.toString()

      GboardChipText(title = title, subtitle = subtitle, modifier = Modifier.weight(1f))
    }
  }
}

@Composable
private fun PhysicalKeyNavigationEffect(
  insight: DisplayInsight,
  publishedInsight: IPublishedContextInsight,
) {
  val context = LocalContext.current
  val renderToken = LocalRenderToken.current
  val info = LocalInsightSurfaceClientInfo.current
  val personalContextManager = remember {
    context.getSystemService(PersonalContextManager::class.java)
  }
  val inputManager = remember { context.getSystemService(InputManager::class.java) }
  val windowManager = remember { context.getSystemService(WindowManager::class.java) }
  val view = LocalView.current
  val currentInsight = publishedInsight.insight as? DisplayInsight ?: insight
  val keyEventHint = currentInsight.findPrototypeHint<KeyEventHint>()

  var isTrustedPresentation by remember { mutableStateOf(false) }
  DisposableEffect(windowManager, view) {
    val listener = Consumer<Boolean> { isTrustedPresentation = it }
    // We assume that `windowManager` is never null on a real device and `view.windowToken` is
    // always non-null when this Composable is first rendered. This will need to be updated if there
    // are any users which try to render this Composable off-screen.
    if (windowManager != null && view.windowToken != null) {
      windowManager.registerTrustedPresentationListener(
        view.windowToken,
        TrustedPresentationThresholds(
          /* minAlpha = */ 0.8f,
          /* minFractionRendered = */ 0.5f,
          /* stabilityRequirementMs = */ 100,
        ),
        Executor { it.run() },
        listener,
      )
    }

    // `WindowManager.unregisterTrustedPresentationListener` is a no-op if the listener was never
    // registered.
    onDispose { windowManager?.unregisterTrustedPresentationListener(listener) }
  }

  val isKeyEventVerified =
    remember(keyEventHint?.keyEvent, inputManager) {
      val keyEvent = keyEventHint?.keyEvent ?: return@remember false
      (keyEvent.keyCode == KeyEvent.KEYCODE_ENTER ||
        keyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
        keyEvent.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) &&
        keyEvent.action == KeyEvent.ACTION_UP &&
        inputManager?.verifyInputEvent(keyEvent) != null
    }

  LaunchedEffect(isKeyEventVerified, isTrustedPresentation) {
    if (!isKeyEventVerified || !isTrustedPresentation) return@LaunchedEffect
    info.onReceiveInsight(publishedInsight.insight)
    personalContextManager?.reportInsightEvent(
      publishedInsight.unwrap() ?: return@LaunchedEffect,
      InsightEvent.EVENT_USER_TAP,
      renderToken.unwrap() ?: return@LaunchedEffect,
    )
  }
}

private val GboardGoogleSansText =
  FontFamily(
    Font(DeviceFontFamilyName("google-sans-text"), weight = FontWeight.Normal),
    Font(DeviceFontFamilyName("roboto"), weight = FontWeight.Normal),
    Font(DeviceFontFamilyName("google-sans-text-medium"), weight = FontWeight.Medium),
    Font(DeviceFontFamilyName("google-sans-text"), weight = FontWeight.Medium),
    Font(DeviceFontFamilyName("roboto"), weight = FontWeight.Medium),
    Font(DeviceFontFamilyName("google-sans-text"), weight = FontWeight.SemiBold),
    Font(DeviceFontFamilyName("roboto"), weight = FontWeight.SemiBold),
    Font(DeviceFontFamilyName("google-sans-text"), weight = FontWeight.Bold),
    Font(DeviceFontFamilyName("roboto"), weight = FontWeight.Bold),
  )

@Composable
fun GboardTheme(
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

  val gboardTypography = MaterialTheme.typography.withDefaultFontFamily(GboardGoogleSansText)
  MaterialTheme(colorScheme = colorScheme, typography = gboardTypography) { content() }
}

@Composable
fun createGboardChipSpec(
  context: Context,
  positionalState: GboardHint.PositionalState? = null,
): GeminiAnimationSpec {
  val shape = getGboardChipShape(positionalState)
  val density = LocalDensity.current
  val cornerRadius =
    remember(shape, density) {
      val radiusPx = shape.topStart.toPx(Size(10000f, 10000f), density)
      CornerRadius(radiusPx)
    }
  val colorScheme = MaterialTheme.colorScheme
  val strokeColor = colorScheme.outlineVariant
  val backgroundColor = getGboardChipBackgroundColor()
  return EnergyEffectsAnimationUtils.rememberChipSpec(
    cornerRadius = cornerRadius,
    density = density.density,
    colorScheme = colorScheme,
    context = context,
    strokeColor = strokeColor,
    backgroundColor = backgroundColor,
  )
}

@Composable
fun getGboardChipShape(positionalState: GboardHint.PositionalState? = null): CornerBasedShape {
  val themeShape = EmbeddedTheme.InlineSuggestion.shapes.suggestion
  if (themeShape != null) {
    return themeShape
  }
  return when (positionalState) {
    GboardHint.PositionalState.FIRST ->
      RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
    GboardHint.PositionalState.LAST ->
      RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    GboardHint.PositionalState.MIDDLE -> RoundedCornerShape(4.dp)
    GboardHint.PositionalState.SINGLE,
    null -> GboardChipShape
  }
}

@Composable
fun getGboardChipBackgroundColor(): Color {
  val embeddedColor = EmbeddedTheme.InlineSuggestion.colorScheme.suggestionBackground
  if (embeddedColor != null) {
    return embeddedColor
  }
  // This is incorrect, as `LocalInsightSurfaceClientInfo`'s `backgroundColor` should be the color
  // of what is rendered behind the chip, not the color of the chip itself.
  // Old versions of Gboard (which don't send an EmbeddedTheme) incorrectly set this to the color of
  // the chip itself, so we should fall back to it if an EmbeddedTheme wasn't set.
  val info = LocalInsightSurfaceClientInfo.current
  val argb = info.backgroundColor.toArgb()
  return if (argb != 0) {
    Color(argb)
  } else {
    MaterialTheme.colorScheme.surfaceContainer
  }
}

const val GBOARD_ICON_TEST_TAG = "gboard_icon"

@Composable
fun GboardIcon(icon: Icon, showBrandedIcon: Boolean, size: Dp, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val mappedIcon =
    remember(icon, showBrandedIcon, context) {
      icon.toBitmap(context)?.asTintableIcon(tintable = !showBrandedIcon)
    }
  mappedIcon?.let {
    val embeddedIconColor = EmbeddedTheme.InlineSuggestion.colorScheme.icon
    val tint =
      if (showBrandedIcon) {
        MaterialTheme.colorScheme.primary
      } else {
        embeddedIconColor ?: MaterialTheme.colorScheme.onSurface
      }
    val iconModifier =
      if (showBrandedIcon) {
        val primaryFixedDimColor = MaterialTheme.colorScheme.primaryFixedDim
        Modifier.gradientTint(listOf(primaryFixedDimColor, tint))
      } else {
        Modifier
      }
    IconOrImage(
      icon = mappedIcon,
      modifier = modifier.size(size).testTag(GBOARD_ICON_TEST_TAG).then(iconModifier),
      tint = tint,
    )
  }
}

fun Icon.toBitmap(context: Context): Bitmap? {
  return try {
    this.loadDrawable(context)?.toBitmap()
  } catch (e: Exception) {
    logger.atWarning().withCause(e).log("Failed to load icon to bitmap")
    null
  }
}

@Composable
fun GboardChipText(title: String, subtitle: String?, modifier: Modifier = Modifier) {
  val embeddedTextColor = EmbeddedTheme.InlineSuggestion.colorScheme.text
  // Android OS's inlineSuggestion style attributes do not include a secondary/subtitle text color.
  // In Gboard's styles.xml, android:strokeColor is set to ?colorOutline, so we repurpose
  // embeddedStrokeColor here to style the subtitle text, falling back to MaterialTheme outline.
  val embeddedStrokeColor = EmbeddedTheme.InlineSuggestion.colorScheme.stroke
  val textColor = embeddedTextColor ?: MaterialTheme.colorScheme.onSurface
  val subtitleColor = embeddedStrokeColor ?: MaterialTheme.colorScheme.outline
  Layout(
    content = {
      Text(
        text = title,
        color = textColor,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      if (!subtitle.isNullOrBlank()) {
        Text(
          text = "  - ",
          color = subtitleColor,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
        )
        Text(
          text = subtitle,
          color = subtitleColor,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    if (measurables.size == 1) {
      val placeable = measurables[0].measure(constraints)
      val layoutWidth = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
      val layoutHeight = placeable.height.coerceIn(constraints.minHeight, constraints.maxHeight)
      return@Layout layout(layoutWidth, layoutHeight) {
        val y = (layoutHeight - placeable.height) / 2
        placeable.placeRelative(0, y)
      }
    }

    val titleMeasurable = measurables[0]
    val separatorMeasurable = measurables[1]
    val subtitleMeasurable = measurables[2]

    val separatorPlaceable = separatorMeasurable.measure(constraints.copy(minWidth = 0))
    val availableWidth = (constraints.maxWidth - separatorPlaceable.width).coerceAtLeast(0)

    val subtitleIntrinsicWidth = subtitleMeasurable.maxIntrinsicWidth(constraints.maxHeight)
    val titleMaxWidth =
      maxOf((availableWidth * 0.4f).toInt(), availableWidth - subtitleIntrinsicWidth)
    val titlePlaceable =
      titleMeasurable.measure(constraints.copy(minWidth = 0, maxWidth = titleMaxWidth))
    val subtitlePlaceable =
      subtitleMeasurable.measure(
        constraints.copy(
          minWidth = 0,
          maxWidth = (availableWidth - titlePlaceable.width).coerceAtLeast(0),
        )
      )
    val layoutWidth =
      (titlePlaceable.width + separatorPlaceable.width + subtitlePlaceable.width).coerceIn(
        constraints.minWidth,
        constraints.maxWidth,
      )
    val layoutHeight =
      maxOf(titlePlaceable.height, separatorPlaceable.height, subtitlePlaceable.height)
        .coerceIn(constraints.minHeight, constraints.maxHeight)

    layout(layoutWidth, layoutHeight) {
      val titleY = (layoutHeight - titlePlaceable.height) / 2
      val separatorY = (layoutHeight - separatorPlaceable.height) / 2
      val subtitleY = (layoutHeight - subtitlePlaceable.height) / 2

      titlePlaceable.placeRelative(0, titleY)
      separatorPlaceable.placeRelative(titlePlaceable.width, separatorY)
      subtitlePlaceable.placeRelative(titlePlaceable.width + separatorPlaceable.width, subtitleY)
    }
  }
}
