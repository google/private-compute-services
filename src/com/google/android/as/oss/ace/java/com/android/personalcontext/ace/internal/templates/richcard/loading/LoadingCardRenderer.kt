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

package com.android.personalcontext.ace.internal.templates.richcard.loading

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import javax.inject.Inject

/** [CardRenderer] for Loading cards. */
class LoadingCardRenderer @Inject internal constructor() : CardRenderer<LoadingCardUiData> {

  /** Renders the loading card layout. */
  @Composable
  override fun Render(cardUiData: CardUiData<LoadingCardUiData>, modifier: Modifier) {
    val elapsedMillis by
      produceState(0L) {
        val startFrameMillis = withFrameMillis { it }
        while (true) {
          withFrameMillis { frameMillis -> value = frameMillis - startFrameMillis }
        }
      }

    Surface(
      modifier = modifier.fillMaxWidth().wrapContentHeight(),
      shape = RoundedCornerShape(32.dp),
      color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            val icon = cardUiData.icon
            if (icon != null) {
              val context = LocalContext.current
              val imageBitmap =
                remember(icon) { icon.loadDrawable(context)?.toBitmap()?.asImageBitmap() }
              if (imageBitmap != null) {
                Image(
                  bitmap = imageBitmap,
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                  modifier = Modifier.size(18.dp),
                )
              }
            }
            LoadingText(
              elapsedMillis = elapsedMillis,
              lineIndex = 0,
              modifier = Modifier.width(134.dp).height(18.dp),
            )
          }

          LoadingText(
            elapsedMillis = elapsedMillis,
            lineIndex = 1,
            modifier = Modifier.padding(start = 26.dp).width(265.dp).height(18.dp),
          )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Column(
          modifier = Modifier.fillMaxWidth().padding(start = 26.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          LoadingText(
            elapsedMillis = elapsedMillis,
            lineIndex = 2,
            modifier = Modifier.width(312.dp).fillMaxWidth(0.95f).height(18.dp),
          )
          LoadingText(
            elapsedMillis = elapsedMillis,
            lineIndex = 3,
            modifier = Modifier.width(206.dp).fillMaxWidth(0.6f).height(18.dp),
          )
          LoadingText(
            elapsedMillis = elapsedMillis,
            lineIndex = 4,
            modifier = Modifier.width(260.dp).fillMaxWidth(0.8f).height(18.dp),
          )
          LoadingText(
            elapsedMillis = elapsedMillis,
            lineIndex = 5,
            modifier = Modifier.width(209.dp).fillMaxWidth(0.65f).height(18.dp),
          )
        }
      }
    }
  }
}

/** A loading placeholder that renders a horizontal gradient representing a text line. */
@Composable
private fun LoadingText(
  elapsedMillis: Long,
  lineIndex: Int,
  modifier: Modifier = Modifier,
  shape: RoundedCornerShape = RoundedCornerShape(50.dp),
) {
  val effectiveMillis = if (LocalInspectionMode.current) 732L else elapsedMillis
  val (widthFraction, alpha) =
    remember(effectiveMillis, lineIndex) { computeGhostWritingState(effectiveMillis, lineIndex) }

  val colorScheme = MaterialTheme.colorScheme
  val energyColors =
    EnergyEffectsAnimationUtils.deriveEnergyColors(colorScheme.primary, colorScheme)
  val baseColor = energyColors.firstOrNull() ?: colorScheme.primary

  val brush =
    remember(baseColor) { Brush.horizontalGradient(colors = listOf(baseColor, Color.Transparent)) }

  Box(modifier = modifier) {
    Box(
      modifier =
        Modifier.fillMaxHeight()
          .fillMaxWidth(widthFraction)
          .graphicsLayer { this.alpha = alpha }
          .background(brush = brush, shape = shape)
    )
  }
}

// Computes the width fraction and alpha value for a ghost writing line based on elapsed time and
// line index.
private fun computeGhostWritingState(elapsedMillis: Long, lineIndex: Int): GhostWritingState {
  val entryDelayMs = 150L + lineIndex * 35L
  val entryDurationMs = 500L
  val entryEndMs = entryDelayMs + entryDurationMs

  val rollingStartMs = 733L + lineIndex * 67L

  if (elapsedMillis < entryDelayMs) {
    return GhostWritingState(widthFraction = 0f, alpha = 0f)
  }
  if (elapsedMillis < entryEndMs) {
    val progress = (elapsedMillis - entryDelayMs).toFloat() / entryDurationMs.toFloat()
    val widthFraction = FastOutSlowInEasing.transform(progress)
    val alpha = progress
    return GhostWritingState(widthFraction, alpha)
  }
  if (elapsedMillis < rollingStartMs) {
    return GhostWritingState(widthFraction = 1f, alpha = 1f)
  }
  val loopTime = (elapsedMillis - rollingStartMs) % 1800L
  val alpha =
    when {
      loopTime < 432L -> {
        val fraction = loopTime.toFloat() / 432f
        1.0f - (0.6f * fraction)
      }
      loopTime < 1096L -> {
        val fraction = (loopTime - 432L).toFloat() / 664f
        0.4f + (0.6f * fraction)
      }
      else -> 1.0f
    }
  return GhostWritingState(widthFraction = 1f, alpha = alpha)
}

private data class GhostWritingState(val widthFraction: Float, val alpha: Float)
