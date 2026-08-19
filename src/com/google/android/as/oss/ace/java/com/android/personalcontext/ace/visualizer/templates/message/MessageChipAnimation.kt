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

package com.android.personalcontext.ace.visualizer.templates.message

import android.graphics.BlurMaskFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.launch

/**
 * Messages chip border with animation.
 *
 * @param cornerRadius The corner radius of the animated border.
 * @param strokeColor The stroke color of the animated border.
 * @param backgroundColor The background color of the chip.
 * @param strokeWidth The width of the animated border.
 * @param innerGlowStrokeWidth The width of the inner glow effect. If null or 0, no glow is drawn.
 * @param innerGlowBlurRadius The blur radius for the inner glow effect.
 */
@Composable
fun Modifier.animatedActionBorder(
  cornerRadius: CornerRadius,
  strokeColor: Color,
  backgroundColor: Color = Color.Transparent,
  strokeWidth: Dp = MessageConstants.BorderStrokeWidth,
  innerGlowStrokeWidth: Dp? = MessageConstants.InnerBorderStrokeWidth,
  innerGlowBlurRadius: Dp = MessageConstants.InnerBorderBlurRadius,
): Modifier {
  val rotationAngle = remember { Animatable(MessageConstants.INITIAL_ROTATION_DEGREES) }
  val fadeProgress = remember { Animatable(0f) } // 0f = full gradient, 1f = full solid
  val entryAlpha = remember { Animatable(0f) }

  val density = LocalDensity.current
  val strokeWidthPx = with(density) { strokeWidth.toPx() }
  val glowWidthPx = with(density) { innerGlowStrokeWidth?.toPx() ?: 0f }
  val blurRadiusPx = with(density) { innerGlowBlurRadius.toPx() }

  val strokeAnimStartColor: Color = boostChroma(MaterialTheme.colorScheme.tertiaryContainer)
  val strokeAnimMiddleColor: Color = boostChroma(MaterialTheme.colorScheme.primaryFixedDim)
  val strokeAnimEndColor: Color = boostChroma(MaterialTheme.colorScheme.primary)

  LaunchedEffect(Unit) {
    launch {
      rotationAngle.animateTo(
        targetValue = MessageConstants.INITIAL_ROTATION_DEGREES + 360f,
        animationSpec =
          tween(
            durationMillis = MessageConstants.ROTATION_DURATION_MILLIS,
            delayMillis = MessageConstants.ANIMATION_REVEAL_DELAY_MILLIS,
            easing = LinearEasing,
          ),
      )
    }

    launch {
      fadeProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = MessageConstants.FADE_DURATION_MILLIS,
            delayMillis =
              MessageConstants.FADE_DELAY_MILLIS + MessageConstants.ANIMATION_REVEAL_DELAY_MILLIS,
            easing = LinearEasing,
          ),
      )
    }

    launch { entryAlpha.animateTo(targetValue = 1f, animationSpec = suggestionEnterSpec()) }
  }

  return this.graphicsLayer { this.alpha = entryAlpha.value }
    .clip(RoundedCornerShape(CornerSize(cornerRadius.x)))
    .drawBehind {
      val currentRotationRad = Math.toRadians(rotationAngle.value.toDouble()).toFloat()
      val gradientRadius = sqrt(size.width * size.width + size.height * size.height) / 2f
      val center = size.center
      val cosTheta = cos(currentRotationRad)
      val sinTheta = sin(currentRotationRad)

      val startOffset =
        Offset(x = center.x - gradientRadius * cosTheta, y = center.y - gradientRadius * sinTheta)
      val endOffset =
        Offset(x = center.x + gradientRadius * cosTheta, y = center.y + gradientRadius * sinTheta)

      val gradientBrush =
        Brush.linearGradient(
          MessageConstants.GRADIENT_START_FRACTION to strokeAnimStartColor,
          MessageConstants.GRADIENT_MIDDLE_FRACTION to strokeAnimMiddleColor,
          MessageConstants.GRADIENT_END_FRACTION to strokeAnimEndColor,
          start = startOffset,
          end = endOffset,
          tileMode = TileMode.Clamp,
        )

      val solidOutlineFadeIn = fadeProgress.value
      val gradientOutlineFadeOut = (1f - solidOutlineFadeIn)

      if (backgroundColor != Color.Transparent) {
        drawRoundRect(color = backgroundColor, cornerRadius = cornerRadius)
      }

      if (innerGlowStrokeWidth != null && innerGlowStrokeWidth > 0.dp) {
        drawInnerGlow(
          glowWidthPx,
          blurRadiusPx,
          gradientBrush,
          cornerRadius,
          gradientOutlineFadeOut,
        )
      }

      drawMainBorder(
        strokeWidthPx,
        gradientBrush,
        cornerRadius,
        gradientOutlineFadeOut,
        strokeColor,
        solidOutlineFadeIn,
      )
    }
}

private fun DrawScope.drawInnerGlow(
  glowWidthPx: Float,
  blurRadiusPx: Float,
  gradientBrush: Brush,
  cornerRadius: CornerRadius,
  gradientOutlineFadeOut: Float,
) {
  drawIntoCanvas { canvas ->
    val paint = Paint()
    paint.style = PaintingStyle.Stroke
    paint.strokeWidth = glowWidthPx

    val frameworkPaint = paint.asFrameworkPaint()
    frameworkPaint.maskFilter = BlurMaskFilter(blurRadiusPx, BlurMaskFilter.Blur.NORMAL)

    gradientBrush.applyTo(size, paint, alpha = 0.2f * gradientOutlineFadeOut)

    canvas.drawRoundRect(
      left = 0f,
      top = 0f,
      right = size.width,
      bottom = size.height,
      radiusX = cornerRadius.x,
      radiusY = cornerRadius.y,
      paint = paint,
    )
  }
}

private fun DrawScope.drawMainBorder(
  strokeWidthPx: Float,
  gradientBrush: Brush,
  cornerRadius: CornerRadius,
  gradientOutlineFadeOut: Float,
  strokeColor: Color,
  solidOutlineFadeIn: Float,
) {
  val halfStroke = strokeWidthPx / 2f
  val topLeft = Offset(halfStroke, halfStroke)
  val borderSize = Size(size.width - strokeWidthPx, size.height - strokeWidthPx)
  val strokeStyle = Stroke(width = strokeWidthPx)

  drawRoundRect(
    brush = gradientBrush,
    topLeft = topLeft,
    size = borderSize,
    cornerRadius = cornerRadius,
    alpha = gradientOutlineFadeOut,
    style = strokeStyle,
  )

  drawRoundRect(
    color = strokeColor,
    topLeft = topLeft,
    size = borderSize,
    cornerRadius = cornerRadius,
    alpha = solidOutlineFadeIn,
    style = strokeStyle,
  )
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

private val SuggestionEnterEasing = CubicBezierEasing(0f, 0f, 0f, 1f)

private fun <T> suggestionEnterSpec(): TweenSpec<T> =
  tween(
    durationMillis = MessageConstants.ANIMATION_REVEAL_DURATION_MILLIS,
    delayMillis = MessageConstants.ANIMATION_REVEAL_DELAY_MILLIS,
    easing = SuggestionEnterEasing,
  )

/**
 * A custom layout composable that animates the chip container height and alpha reveal during entry
 * while measuring content first to determine the natural target height cleanly in a single pass.
 *
 * @param progress The animation progress provider from 0f to 1f.
 * @param enabled Whether this animation should be applied.
 * @param modifier The modifier to apply to the layout.
 * @param background The background and border container composable.
 * @param content The chip content composable.
 */
@Composable
fun AnimatedMessageChipLayout(
  progress: () -> Float,
  enabled: Boolean,
  modifier: Modifier = Modifier,
  background: @Composable () -> Unit,
  content: @Composable () -> Unit,
) {
  val density = LocalDensity.current.density
  val fallbackEntryAlpha = remember { Animatable(0f) }
  val fallbackEntryScale = remember { Animatable(0f) }

  LaunchedEffect(enabled) {
    if (!enabled) {
      launch {
        fallbackEntryAlpha.animateTo(targetValue = 1f, animationSpec = suggestionEnterSpec())
      }
      launch {
        fallbackEntryScale.animateTo(targetValue = 1f, animationSpec = suggestionEnterSpec())
      }
    }
  }

  Layout(
    content = {
      Box(propagateMinConstraints = true) { background() }
      Box(propagateMinConstraints = true) { content() }
    },
    modifier = modifier,
  ) { measurables, constraints ->
    val backgroundMeasurable = measurables[0]
    val contentMeasurable = measurables[1]

    // 1. Measure content unconstrained vertically to get natural target height
    val childConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
    val contentPlaceable = contentMeasurable.measure(childConstraints)
    val contentHeight = contentPlaceable.height
    val targetHeight = maxOf(MessageConstants.MinHeight.roundToPx(), contentHeight)

    // 2. Calculate animated height & alpha based on progress
    val currentProgress = if (enabled) progress() else 1f
    val animatedHeight = (targetHeight * currentProgress).toInt()
    val heightDp = animatedHeight / density

    val containerAlpha =
      if (enabled) {
        ((heightDp - MessageConstants.CHIP_BOX_MIN_FADE_HEIGHT_DP) /
            (MessageConstants.CHIP_BOX_MAX_FADE_HEIGHT_DP -
              MessageConstants.CHIP_BOX_MIN_FADE_HEIGHT_DP))
          .coerceIn(0f, 1f)
      } else {
        1f
      }

    val contentAlpha =
      if (enabled) {
        ((currentProgress - MessageConstants.CHIP_CONTENT_MIN_FADE_PROGRESS) /
            (MessageConstants.CHIP_CONTENT_MAX_FADE_PROGRESS -
              MessageConstants.CHIP_CONTENT_MIN_FADE_PROGRESS))
          .coerceIn(0f, 1f)
      } else {
        1f
      }

    // 3. Measure background with exact animated dimensions
    val bgConstraints =
      constraints.copy(
        minHeight = animatedHeight,
        maxHeight = animatedHeight,
        minWidth = contentPlaceable.width,
        maxWidth = contentPlaceable.width,
      )
    val bgPlaceable = backgroundMeasurable.measure(bgConstraints)

    // 4. Place children while reserving full targetHeight for parent to prevent layout jumping
    layout(contentPlaceable.width, targetHeight) {
      // Place container background anchored to the bottom with containerAlpha
      bgPlaceable.placeRelativeWithLayer(0, targetHeight - animatedHeight) {
        if (enabled) {
          alpha = containerAlpha
        } else {
          alpha = fallbackEntryAlpha.value
          scaleX = fallbackEntryScale.value
          scaleY = fallbackEntryScale.value
        }
      }

      // Place content vertically centered within the container's currently displayed area with
      // contentAlpha
      val yOffset = (targetHeight - animatedHeight) + (animatedHeight - contentHeight) / 2
      contentPlaceable.placeRelativeWithLayer(0, yOffset) {
        if (enabled) {
          alpha = contentAlpha
        } else {
          alpha = fallbackEntryAlpha.value
          scaleX = fallbackEntryScale.value
          scaleY = fallbackEntryScale.value
        }
      }
    }
  }
}
