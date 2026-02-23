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
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.google.android.`as`.oss.dataattribution.proto.attributionChipData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_DRAG
import com.google.android.`as`.oss.delegatedui.api.integration.egress.sundog.sundogLocationClickEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.sundog.SundogLocationTemplateData
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.Common.CARD_SHAPE
import com.google.android.`as`.oss.delegatedui.service.templates.sundog.Common.FIXED_VELOCITY
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.SerializableBitmap.serializeToByteString
import com.google.common.flogger.GoogleLogger
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SundogLocationTemplateRenderer @Inject internal constructor() : TemplateRenderer {

  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val data: SundogLocationTemplateData = response.data.sundogLocationTemplateData
    if (!data.hasLocationName()) {
      return null
    }
    val uiIdToken = data.uiIdToken

    val composeView =
      ComposeView(context).apply {
        setContent {
          LaunchedEffect(Unit) { doOnImpression(uiIdToken) { logUsage() } }

          var dragDirection by remember { mutableFloatStateOf(0f) }
          SundogTheme {
            Box(
              modifier =
                Modifier.fillMaxWidth().height(IntrinsicSize.Max).padding(horizontal = 16.dp),
              contentAlignment = Alignment.Center,
            ) {
              val deleteBackgroundColor =
                if (data.hasDeleteBackgroundColor()) {
                  Color(data.deleteBackgroundColor)
                } else {
                  MaterialTheme.colorScheme.onErrorContainer
                }
              DeletionRow(dragDirection, deleteBackgroundColor)
              val scope = rememberCoroutineScope()
              Row(
                modifier =
                  Modifier.fillMaxSize()
                    .doOnDrag(
                      uiTokenId = uiIdToken,
                      fixedVelocity = FIXED_VELOCITY,
                      onDragDirectionChanged = { dragDirection = it },
                      onDragCompleted = {
                        logger.atInfo().log("Drag to dismiss")
                        dismissSuggestion()
                        closeSession()
                      },
                    )
                    .clip(CARD_SHAPE)
                    .doOnClick(
                      uiIdToken,
                      onClick = {
                        sendEgressData {
                          this.sundogLocationClickEgressData = sundogLocationClickEgressData {
                            locationName = data.locationName
                            latitude = data.latitude
                            longitude = data.longitude
                          }
                        }
                      },
                      onLongClick = {
                        showDataAttribution(
                          attributionDialogData = data.attributionDialogData,
                          attributionChipData =
                            attributionChipData {
                              response.image.valueOrNull?.serializeToByteString()?.let { image ->
                                chipIcon = image
                              }
                              chipLabel = data.locationName
                            },
                          sourceDeepLinks = response.pendingIntentList.valueOrNull?.toTypedArray(),
                        )
                      },
                    )
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .semantics {
                      customActions =
                        listOf(
                          CustomAccessibilityAction(
                            label =
                              if (data.hasDeleteLabel()) {
                                data.deleteLabel
                              } else {
                                "Delete"
                              }
                          ) {
                            logger.atInfo().log("Talkback drag to dismiss")
                            scope.launch {
                              doOnInterop(uiIdToken, interactionType = INTERACTION_TYPE_DRAG) {
                                dismissSuggestion()
                                closeSession()
                              }
                            }
                            true
                          }
                        )
                    }
                    .padding(vertical = 20.dp, horizontal = 28.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                PrimaryRoundedIcon(
                  iconColor = MaterialTheme.colorScheme.onSecondary,
                  iconBackgroundColor = MaterialTheme.colorScheme.secondary,
                  imageBitmap = response.image.valueOrNull,
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.SpaceAround) {
                  Text(
                    data.locationName,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 2.dp),
                  )
                  Text(
                    data.subText,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    fontWeight = FontWeight.SemiBold,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                      Modifier.padding(top = 3.dp)
                        .then(if (data.enableMarqueeSubtitle) Modifier.basicMarquee() else Modifier),
                  )
                  Text(
                    data.source,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall.withFlexFont(weight = 500),
                    modifier = Modifier.padding(top = 2.dp),
                  )
                }
              }
            }
          }
        }
      }
    return composeView
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}

@Composable
fun SundogTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
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

@Composable
internal fun DeletionRow(dragDirection: Float, backgroundColor: Color) =
  Row(
    modifier =
      Modifier.fillMaxWidth(fraction = 0.99f)
        .fillMaxHeight()
        .zIndex(-1f)
        .clip(CARD_SHAPE)
        .background(color = backgroundColor)
        .padding(horizontal = 17.dp),
    horizontalArrangement = if (dragDirection > 0) Arrangement.Start else Arrangement.End,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      painter = painterResource(id = R.drawable.gs_delete_wght300_vd_theme_24),
      contentDescription = null,
      modifier = Modifier.size(26.dp),
      tint = MaterialTheme.colorScheme.onError,
    )
  }
