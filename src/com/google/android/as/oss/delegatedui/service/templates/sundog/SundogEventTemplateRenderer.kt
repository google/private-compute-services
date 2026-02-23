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

package com.google.android.`as`.oss.delegatedui.service.templates.sundog

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.dataattribution.proto.attributionChipData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.sundog.sundogEventClickEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.sundog.Event
import com.google.android.`as`.oss.delegatedui.api.integration.templates.sundog.SundogEventTemplateData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.sundog.event
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.Common.CARD_SHAPE
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.TestTagConstants.SUNDOG_EVENT_CARD_TAG
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.SerializableBitmap.serializeToByteString
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class SundogEventTemplateRenderer @Inject internal constructor() : TemplateRenderer {

  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val data: SundogEventTemplateData = response.data.sundogEventTemplateData
    if (!data.hasLocationName() && data.eventsList.isEmpty()) {
      return null
    }

    val composeView =
      ComposeView(context).apply {
        setContent {
          SundogTheme {
            Box(
              modifier =
                Modifier.background(color = Color.Transparent)
                  .fillMaxSize()
                  .height(IntrinsicSize.Max)
            ) {
              if (data.eventsList.isEmpty()) {
                // When Sundog multi-event feature is not enabled, use the old event field.
                val event = event {
                  eventTitle = data.eventTitle
                  title = data.title
                  subText = data.subText
                  locationName = data.locationName
                  startTime = data.startTime
                  endTime = data.endTime
                  attributionDialogData = data.attributionDialogData
                  uiIdToken = data.uiIdToken
                }
                SundogEventCard(
                  event = event,
                  image = response.image.valueOrNull,
                  pendingIntent = response.pendingIntentList.valueOrNull?.firstOrNull(),
                )
              } else {
                // When Sundog multi-event feature is enabled, use the new events list.
                Column(
                  modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  for (event in data.eventsList) {
                    SundogEventCard(
                      event = event,
                      image = response.image.valueOrNull,
                      pendingIntent =
                        response.pendingIntentList.valueOrNull?.getOrNull(event.pendingIntentIndex),
                    )
                  }
                }
              }
            }
          }
        }
      }
    return composeView
  }

  /*
   * Renders a single event card.
   *
   * @param event The event to render.
   * @param image The image to render.
   * @param pendingIntent The pending intent to use for the long click.
   */
  @Composable
  private fun TemplateRendererScope.SundogEventCard(
    event: Event,
    image: Bitmap?,
    pendingIntent: PendingIntent?,
  ) {
    val uiIdToken = event.uiIdToken
    Card(
      modifier =
        Modifier.fillMaxWidth()
          .height(IntrinsicSize.Max)
          .clip(CARD_SHAPE)
          .testTag(SUNDOG_EVENT_CARD_TAG)
    ) {
      Row(
        modifier =
          Modifier.fillMaxSize()
            .clip(CARD_SHAPE)
            .doOnClick(
              uiIdToken,
              onClick = {
                sendEgressData {
                  this.sundogEventClickEgressData = sundogEventClickEgressData {
                    locationName = event.locationName
                    eventTitle = event.eventTitle
                    startTime = event.startTime
                    endTime = event.endTime
                  }
                }
              },
              onLongClick = {
                showDataAttribution(
                  attributionDialogData = event.attributionDialogData,
                  attributionChipData =
                    attributionChipData {
                      image?.serializeToByteString()?.let { icon -> chipIcon = icon }
                      chipLabel = event.title
                    },
                  sourceDeepLinks = listOfNotNull(pendingIntent).toTypedArray(),
                )
              },
            )
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        LaunchedEffect(Unit) { doOnImpression(uiIdToken) { logUsage() } }
        PrimaryRoundedIcon(
          iconBackgroundWidth = 40.dp,
          iconWidth = 20.dp,
          iconColor = MaterialTheme.colorScheme.onSecondary,
          iconBackgroundColor = MaterialTheme.colorScheme.secondary,
          imageBitmap = image,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(verticalArrangement = Arrangement.SpaceAround) {
          Text(
            event.title,
            maxLines = 2,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            event.subText,
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
}

@VisibleForTesting
internal object TestTagConstants {
  const val SUNDOG_EVENT_CARD_TAG = "SUNDOG_EVENT_CARD"
}
