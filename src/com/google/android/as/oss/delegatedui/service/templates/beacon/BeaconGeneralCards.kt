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

package com.google.android.`as`.oss.delegatedui.service.templates.beacon

import android.app.PendingIntent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconGeneralCard
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconGenericContentDescriptions
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconCommonUtils.getPendingIntent
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconButtonSizeMedium
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.IconSizeNormal
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeExtraSmall
import com.google.android.`as`.oss.delegatedui.service.templates.beacon.BeaconTemplateRendererConstants.RoundedCornerSizeLarge
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope

/**
 * The container for a the simple cards in the Beacon widget. This is the entry point to the cards.
 * There can be multiple cards in this single surface.
 */
@Composable
internal fun TemplateRendererScope.BeaconGeneralCardsContainer(
  cards: List<BeaconGeneralCard>,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  pendingIntents: List<PendingIntent>,
  uiIdToken: UiIdToken,
) {
  doOnImpression(uiIdToken) { logUsage() }
  Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
    for (i in 0 until cards.size) {
      val card = cards[i]
      val isFirstCard = i == 0
      val isLastCard = i == cards.size - 1
      val pendingIntent = getPendingIntent(pendingIntents, card.dataSource)

      key(
        if (card.listUuid.isNotEmpty()) {
          card.listUuid
        } else {
          i.toString()
        }
      ) {
        BeaconSingleGeneralCardContainer(
          card = card,
          genericContentDescriptions = genericContentDescriptions,
          isFirstCard = isFirstCard,
          isLastCard = isLastCard,
          pendingIntent = pendingIntent,
        )
      }
    }
  }
}

@Composable
private fun TemplateRendererScope.BeaconSingleGeneralCardContainer(
  card: BeaconGeneralCard,
  genericContentDescriptions: BeaconGenericContentDescriptions,
  isFirstCard: Boolean,
  isLastCard: Boolean,
  pendingIntent: PendingIntent?,
) {
  Card(
    colors =
      CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    shape =
      RoundedCornerShape(
        topStart = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        topEnd = if (isFirstCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomStart = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
        bottomEnd = if (isLastCard) RoundedCornerSizeLarge else RoundedCornerSizeExtraSmall,
      ),
  ) {
    Row(
      modifier =
        Modifier.fillMaxWidth()
          .then(
            if (pendingIntent != null) {
              Modifier.semantics(mergeDescendants = true) {
                  this.role = Role.Button
                  onClick(label = card.dataSource.ctaButtonText, action = null)
                }
                .doOnClick(card.sourceNavigationButtonUiId) {
                  executeAction { pendingIntent.toAction() }
                }
            } else {
              Modifier.semantics(mergeDescendants = true) {}
            }
          )
          .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      AppIcon(
        contentDescription = card.dataSource.sourceType.name,
        sourcePackageName = card.dataSource.sourcePackageName,
      )
      Text(
        modifier = Modifier.weight(1f),
        text = card.title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = card.date,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

/** Displays the source app's icon. If the icon is not available, displays a generic icon. */
@Composable
private fun TemplateRendererScope.AppIcon(
  modifier: Modifier = Modifier,
  contentDescription: String,
  sourcePackageName: String?,
) {
  val packageManager = LocalContext.current.packageManager
  val icon: ImageBitmap? =
    remember(sourcePackageName) { getAppIcon(packageManager, sourcePackageName) }

  if (icon != null) {
    Image(
      modifier = Modifier.size(IconSizeLarge),
      bitmap = icon,
      contentDescription = contentDescription,
    )
  } else {
    Icon(
      modifier = Modifier.size(IconSizeLarge),
      painter = painterResource(R.drawable.gs_widgets_vd_theme_24),
      tint = MaterialTheme.colorScheme.secondary,
      contentDescription = null,
    )
  }
}

/** Expands the card to show more information. Deprecated (unknown if it will be brought back). */
@Composable
private fun ExpandButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
  isExpanded: Boolean,
  contentDescription: String,
  isExpandedStateDescription: String,
  isCollapsedStateDescription: String,
  isExpandedClickLabel: String,
  isCollapsedClickLabel: String,
) {
  IconButton(
    onClick = onClick,
    modifier =
      modifier.size(IconButtonSizeMedium).semantics {
        this.stateDescription =
          if (isExpanded) isExpandedStateDescription else isCollapsedStateDescription
        onClick(
          label = if (isExpanded) isExpandedClickLabel else isCollapsedClickLabel,
          action = null,
        )
      },
    colors =
      IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
      ),
  ) {
    Icon(
      modifier = Modifier.size(IconSizeNormal),
      painter =
        painterResource(
          if (isExpanded) {
            R.drawable.gs_keyboard_arrow_up_vd_theme_24
          } else {
            R.drawable.gs_keyboard_arrow_down_vd_theme_24
          }
        ),
      contentDescription = contentDescription,
    )
  }
}

/** Gets the app icon [ImageBitmap] for the given package name. */
private fun getAppIcon(packageManager: PackageManager, packageName: String?): ImageBitmap? =
  packageName
    .takeIf { !it.isNullOrBlank() }
    ?.let { getApplicationInfo(it, packageManager) }
    ?.loadUnbadgedIcon(packageManager)
    ?.toBitmap()
    ?.asImageBitmap()

private fun getApplicationInfo(
  packageName: String,
  packageManager: PackageManager,
): ApplicationInfo? =
  try {
    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
  } catch (e: Throwable) {
    null
  }
