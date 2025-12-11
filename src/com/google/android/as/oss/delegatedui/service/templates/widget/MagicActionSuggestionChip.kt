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

package com.google.android.`as`.oss.delegatedui.service.templates.widget

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.utils.IconOrImage
import com.google.android.`as`.oss.delegatedui.utils.TintableIcon
import com.google.android.`as`.oss.delegatedui.utils.asTintableIcon
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.launch

/** Data class holding the theme-able values for the Magic Action Suggestion Chip. */
data class ChipTheme(
  val buttonHorizontalPadding: Dp = 12.dp,
  val buttonVerticalPadding: Dp = 4.dp,
  val cornerRadius: Dp = 20.dp,
  val borderStrokeWidth: Dp = 1.dp,
  val innerBorderStrokeWidth: Dp = 4.dp,
  val iconSpacing: Dp = 8.dp,
  val iconTintColor: Color = Color.Unspecified,
  val rotationDurationMillis: Int = 1500,
  val initialRotationDegrees: Float = 20f,
  val gradientStartFraction: Float = 0.2f,
  val gradientMiddleFraction: Float = 0.5f,
  val gradientEndFraction: Float = 0.8f,
  val fadeDurationMillis: Int = 500,
  val fadeDelayMillis: Int = 1000,
)

val LocalChipTheme = staticCompositionLocalOf { ChipTheme() }

/**
 * A data model representing the content of a single suggestion chip. This is used by
 * [MagicActionSuggestionChip] to render the UI.
 *
 * @param iconBitmap An optional [Bitmap] for the icon to be displayed at the start of the chip.
 * @param text A primary text to be displayed on the chip.
 * @param attribution An optional attribution text displayed below the primary text (e.g., "From
 *   Gmail").
 * @param contentDescription An optional content description for accessibility. This should describe
 *   the entire chip's action or content.
 */
data class SuggestionModel(
  val iconBitmap: Bitmap?,
  val text: String,
  val attribution: String? = null,
  val contentDescription: String? = null,
)

/**
 * A composable function that renders an animated suggestion chip.
 *
 * This chip features an infinite-animated border that starts as a rotating gradient and fades into
 * a solid color. It handles click and long-click interactions and logs an impression when it
 * becomes visible.
 *
 * @param suggestionModel The [SuggestionModel] data used to populate the chip's UI, containing the
 *   icon, text, and attribution.
 * @param chipImpression A lambda function to be invoked when the chip is first composed (typically
 *   for logging visibility or impression).
 * @param chipOnClick A lambda function to be invoked when the chip is clicked.
 * @param chipOnLongClick A lambda function to be invoked when the chip is long-clicked.
 */
@Composable
fun MagicActionSuggestionChip(
  suggestionModel: SuggestionModel,
  chipOnClick: () -> Unit,
  modifier: Modifier = Modifier,
  chipImpression: (() -> Unit)? = null,
  chipOnLongClick: (() -> Unit)? = null,
) {
  ChipOutlinedButton(
    modifier = modifier,
    chipOnClick = chipOnClick,
    chipOnLongClick = chipOnLongClick,
  ) {
    LaunchedEffect(Unit) { chipImpression?.invoke() }
    RowContent(
      icon = suggestionModel.iconBitmap?.asTintableIcon(tintable = false),
      text = suggestionModel.text,
      attribution = suggestionModel.attribution,
      description = suggestionModel.contentDescription,
    )
  }
}

@Composable
private fun ChipOutlinedButton(
  chipOnClick: () -> Unit,
  modifier: Modifier = Modifier,
  chipOnLongClick: (() -> Unit)? = null,
  chipContents: @Composable () -> Unit,
) {
  val shape = RoundedCornerShape(LocalChipTheme.current.cornerRadius)
  val interactionSource = remember { MutableInteractionSource() }
  Box(
    modifier =
      modifier
        .clip(shape)
        .widthIn(min = 30.dp, max = 320.dp)
        .heightIn(min = 40.dp)
        .background(color = Color.Transparent, shape = shape)
        .combinedClickable(
          onClick = chipOnClick,
          onLongClick = chipOnLongClick,
          interactionSource = interactionSource,
          indication = ripple(color = MaterialTheme.colorScheme.onSurface),
        )
        .animatedActionBorder(LocalChipTheme.current.borderStrokeWidth, true)
        .semantics { role = Role.Button },
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier =
        Modifier.matchParentSize()
          .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
          .animatedActionBorder(strokeWidth = LocalChipTheme.current.innerBorderStrokeWidth, false)
    )
    chipContents()
  }
}

@Composable
private fun RowContent(
  icon: TintableIcon?,
  text: String,
  attribution: String? = null,
  description: String? = null,
) {
  val modifier =
    description?.let { Modifier.clearAndSetSemantics { this.contentDescription = it } } ?: Modifier
  Row(
    modifier =
      modifier.padding(
        PaddingValues(
          vertical = LocalChipTheme.current.buttonVerticalPadding,
          horizontal = LocalChipTheme.current.buttonHorizontalPadding,
        )
      ),
    horizontalArrangement = Arrangement.spacedBy(LocalChipTheme.current.iconSpacing),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Icon
    icon?.let {
      IconOrImage(
        icon = icon,
        modifier = Modifier.size(24.dp).align(Alignment.CenterVertically),
        tint = LocalChipTheme.current.iconTintColor,
      )
    }

    // Text
    if (attribution == null) {
      SuggestionText(text, maxLines = 2, modifier = Modifier.align(Alignment.CenterVertically))
    } else {
      Column {
        SuggestionText(text, maxLines = 1)
        Text(
          text = attribution,
          style = MaterialTheme.typography.bodyMedium.withFlexFont(weight = 550, round = 0f),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SuggestionText(text: String, maxLines: Int, modifier: Modifier = Modifier) {
  Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.labelLarge.withFlexFont(weight = 500, round = 0f),
    color = MaterialTheme.colorScheme.onSurface,
    overflow = TextOverflow.Ellipsis,
    maxLines = maxLines,
  )
}

@Composable
private fun Modifier.animatedActionBorder(strokeWidth: Dp, withSolidColor: Boolean): Modifier {
  val theme = LocalChipTheme.current
  val rotationAngle = remember { Animatable(theme.initialRotationDegrees) }
  val fadeProgress = remember { Animatable(0f) } // 0f = full gradient, 1f = full solid

  val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
  val halfStroke = strokeWidthPx / 2f
  val topLeft = Offset(halfStroke, halfStroke)
  val solidColor = MaterialTheme.colorScheme.outlineVariant

  val cornerRadiusPx = with(LocalDensity.current) { theme.cornerRadius.toPx() }
  val strokeAnimStartColor: Color = boostChroma(MaterialTheme.colorScheme.tertiaryContainer)
  val strokeAnimMiddleColor: Color = boostChroma(MaterialTheme.colorScheme.primaryFixedDim)
  val strokeAnimEndColor: Color = boostChroma(MaterialTheme.colorScheme.primary)

  // Trigger animations when the composable enters the composition
  LaunchedEffect(Unit) {
    launch {
      rotationAngle.animateTo(
        targetValue = theme.initialRotationDegrees + 360f,
        animationSpec = tween(durationMillis = theme.rotationDurationMillis, easing = LinearEasing),
      )
    }

    launch {
      fadeProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = theme.fadeDurationMillis,
            delayMillis = theme.fadeDelayMillis,
            easing = LinearEasing,
          ),
      )
    }
  }

  return drawBehind {
    val currentRotationRad = Math.toRadians(rotationAngle.value.toDouble()).toFloat()
    val solidOutlineFadeIn = fadeProgress.value

    val gradientOutlineFadeOut = (1f - solidOutlineFadeIn)
    val gradientRadius = sqrt(size.width * size.width + size.height * size.height) / 2f

    val center = size.center
    val strokeStyle = Stroke(width = strokeWidthPx)

    // Gradient
    val cosTheta = cos(currentRotationRad)
    val sinTheta = sin(currentRotationRad)

    val startOffset =
      Offset(x = center.x - gradientRadius * cosTheta, y = center.y - gradientRadius * sinTheta)
    val endOffset =
      Offset(x = center.x + gradientRadius * cosTheta, y = center.y + gradientRadius * sinTheta)

    val gradientBrush =
      Brush.linearGradient(
        theme.gradientStartFraction to strokeAnimStartColor,
        theme.gradientMiddleFraction to strokeAnimMiddleColor,
        theme.gradientEndFraction to strokeAnimEndColor,
        start = startOffset,
        end = endOffset,
        tileMode = TileMode.Clamp,
      )

    val innerGradientBrush =
      Brush.linearGradient(
        theme.gradientStartFraction to strokeAnimStartColor.copy(alpha = 0.2f),
        theme.gradientMiddleFraction to strokeAnimMiddleColor.copy(alpha = 0.2f),
        theme.gradientEndFraction to strokeAnimEndColor.copy(alpha = 0.2f),
        start = startOffset,
        end = endOffset,
        tileMode = TileMode.Clamp,
      )

    drawRoundRect(
      brush =
        if (withSolidColor) {
          gradientBrush
        } else {
          innerGradientBrush
        },
      topLeft = topLeft,
      size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
      cornerRadius = CornerRadius(cornerRadiusPx),
      alpha = gradientOutlineFadeOut,
      style = strokeStyle,
    )

    if (withSolidColor) {
      drawRoundRect(
        color = solidColor,
        topLeft = topLeft,
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        cornerRadius = CornerRadius(cornerRadiusPx),
        alpha = solidOutlineFadeIn,
        style = strokeStyle,
      )
    }
  }
}

private fun boostChroma(color: Color): Color {
  val hctColor = FloatArray(3)
  ColorUtils.colorToM3HCT(color.toArgb(), hctColor)
  val chroma = hctColor[1]
  return if (chroma < 5) {
    color
  } else {
    Color(ColorUtils.M3HCTToColor(hctColor[0], 70f, hctColor[2]))
  }
}
