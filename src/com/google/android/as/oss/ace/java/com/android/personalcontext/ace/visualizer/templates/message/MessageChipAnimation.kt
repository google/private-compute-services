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
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
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
          tween(durationMillis = MessageConstants.ROTATION_DURATION_MILLIS, easing = LinearEasing),
      )
    }

    launch {
      fadeProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = MessageConstants.FADE_DURATION_MILLIS,
            delayMillis = MessageConstants.FADE_DELAY_MILLIS,
            easing = LinearEasing,
          ),
      )
    }
  }

  return this.clip(RoundedCornerShape(CornerSize(cornerRadius.x))).drawBehind {
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
      drawInnerGlow(glowWidthPx, blurRadiusPx, gradientBrush, cornerRadius, gradientOutlineFadeOut)
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
