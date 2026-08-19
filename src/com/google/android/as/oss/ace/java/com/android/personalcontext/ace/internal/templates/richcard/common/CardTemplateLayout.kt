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

package com.android.personalcontext.ace.internal.templates.richcard.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat

/**
 * A common template for visualizer cards, providing a consistent layout with attribution, app
 * content, and actions.
 */
@Suppress("NewApi", "FlaggedApi")
@Composable
fun CardTemplateLayout(
  cardUiData: CardUiData<DeprecatedUiCardContext>,
  energyEffectsAnimationCompat: EnergyEffectsAnimationCompat,
  modifier: Modifier = Modifier,
  timeSupplierMs: () -> Long = { System.currentTimeMillis() },
  timeOffsetMs: Long? = null,
  content: @Composable ColumnScope.() -> Unit,
) {
  val spec =
    EnergyEffectsAnimationUtils.createSageCardSpec(
      colorScheme = MaterialTheme.colorScheme,
      backgroundColor = MaterialTheme.colorScheme.surfaceContainerHighest,
      timeSupplierMs = timeSupplierMs,
      timeOffsetMs = timeOffsetMs,
    )

  Surface(
    modifier = modifier.fillMaxWidth().wrapContentHeight(),
    shape = RoundedCornerShape(32.dp),
    color = MaterialTheme.colorScheme.surfaceContainerHighest,
  ) {
    Column(
      modifier =
        Modifier.then(
            with(energyEffectsAnimationCompat) {
              Modifier.applyEnergyEffectsAnimation(geminiAnimationSpec = spec, fallback = { this })
            }
          )
          .padding(horizontal = 12.dp, vertical = 16.dp)
          .padding(cardUiData.insets.asPaddingValues()),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
          val icon = cardUiData.icon
          if (icon != null) {
            val context = LocalContext.current
            val imageBitmap =
              remember(icon) { icon.loadDrawable(context)?.toBitmap()?.asImageBitmap() }
            if (imageBitmap != null) {
              Box(
                modifier = Modifier.size(24.dp).alignByBaseline(),
                contentAlignment = Alignment.Center,
              ) {
                Image(
                  bitmap = imageBitmap,
                  contentDescription = null,
                  colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                  modifier = Modifier.size(20.dp),
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
            }
          }
          if (cardUiData.cardTitle != null) {
            CardTitle(
              cardTitle = cardUiData.cardTitle,
              modifier = Modifier.weight(1f).padding(end = 8.dp).alignByBaseline(),
            )
          } else {
            Spacer(modifier = Modifier.weight(1f))
          }
          val serverSideCloseInsight = cardUiData.dismissInsight
          if (serverSideCloseInsight != null) {
            Box(
              modifier = Modifier.size(24.dp).alignByBaseline(),
              contentAlignment = Alignment.Center,
            ) {
              CardDismissIcon(dismissInsight = serverSideCloseInsight)
            }
          }
        }
      }

      Column(modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
        content()
      }

      val actions = cardUiData.actions
      if (!actions.isNullOrEmpty()) {
        CardActionRow(cardActions = actions)
      }
    }
  }
}
