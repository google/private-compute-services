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

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.dataattribution.proto.attributionChipData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.sundog.sundogEventClickEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.sundog.SundogEventTemplateData
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.Common.CARD_SHAPE
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.SerializableBitmap.serializeToByteString
import javax.inject.Inject

class SundogEventTemplateRenderer @Inject internal constructor() : TemplateRenderer {

  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val data: SundogEventTemplateData = response.value.sundogEventTemplateData
    if (!data.hasLocationName()) {
      return null
    }
    val uiIdToken = response.value.sundogLocationTemplateData.uiIdToken

    val composeView =
      ComposeView(context).apply {
        setContent {
          SundogTheme {
            Box(modifier = Modifier.fillMaxSize().height(IntrinsicSize.Max)) {
              Row(
                modifier =
                  Modifier.fillMaxSize()
                    .clip(CARD_SHAPE)
                    .doOnClick(
                      uiIdToken,
                      onClick = {
                        sendEgressData {
                          this.sundogEventClickEgressData = sundogEventClickEgressData {
                            locationName = data.locationName
                            eventTitle = data.eventTitle
                            startTime = data.startTime
                            endTime = data.endTime
                          }
                        }
                      },
                      onLongClick = {
                        showDataAttribution(
                          attributionDialogData = data.attributionDialogData,
                          attributionChipData =
                            attributionChipData {
                              response.image.value?.serializeToByteString()?.let { image ->
                                chipIcon = image
                              }
                              chipLabel = data.title
                            },
                          sourceDeepLinks = null,
                        )
                      },
                    )
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .padding(vertical = 12.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                doOnImpression(uiIdToken) { logUsage() }
                PrimaryRoundedIcon(
                  iconBackgroundWidth = 40.dp,
                  iconWidth = 20.dp,
                  iconColor = MaterialTheme.colorScheme.onSecondary,
                  iconBackgroundColor = MaterialTheme.colorScheme.secondary,
                  imageBitmap = response.image.value,
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.SpaceAround) {
                  Text(
                    data.title,
                    maxLines = 2,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    data.subText,
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
      }
    return composeView
  }
}
