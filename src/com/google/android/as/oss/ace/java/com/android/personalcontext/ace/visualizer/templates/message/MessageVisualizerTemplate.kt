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
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.android.personalcontext.ace.visualizer.templates.message

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.personalcontext.PersonalContextManager
import android.service.personalcontext.hint.MessagesHint
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.interaction.InsightEvent
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.client.prototype.PrototypeInsightUtils.toContextInsight
import com.android.personalcontext.ace.client.prototype.clientaction.params.showcards.ShowCardsParams
import com.android.personalcontext.ace.client.prototype.message.MessageMetadataHint
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.common.FindHintUtils.findContextHint
import com.android.personalcontext.ace.common.gradientTint
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.internal.energyeffects.EnergyEffectsAnimationUtils
import com.android.personalcontext.ace.visualizer.compat.ClientActionInsightCompat
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import com.android.personalcontext.ace.visualizer.compat.FlexFontCompat
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import com.android.personalcontext.ace.visualizer.templates.message.MessageTemplateData.Companion.toMessageTemplateData
import com.android.personalcontext.ace.visualizer.templates.utils.IconOrImage
import com.android.personalcontext.ace.visualizer.templates.utils.RemoteActionUtils.execute
import com.android.personalcontext.ace.visualizer.templates.utils.asTintableIcon
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.delegatedui.config.DelegatedUiConfig
import com.google.android.`as`.oss.delegatedui.service.templates.motion.ExpressiveMotionUtils
import javax.inject.Inject
import kotlinx.coroutines.delay

class MessageVisualizerTemplate
@Inject
internal constructor(
  val flexFontCompat: FlexFontCompat,
  private val clientActionInsightCompat: ClientActionInsightCompat,
  private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat,
  private val themeCompat: ThemeCompat,
  private val configReader: ConfigReader<DelegatedUiConfig>,
) : VisualizerTemplate {

  override fun handleInsight(
    publishedInsight: IPublishedContextInsight
  ): (@Composable () -> Unit)? {
    Log.i(TAG, "[MessagesEmbedded] handleInsight")
    val insight = publishedInsight.insight
    val unused = insight.findContextHint<MessagesHint>() ?: return null
    val messageTemplateData = insight.toMessageTemplateData(clientActionInsightCompat, themeCompat)
    return { MessageTemplate(messageTemplateData) }
  }

  @Composable
  private fun MessageTemplate(messageTemplateData: MessageTemplateData) {
    val info = LocalInsightSurfaceClientInfo.current
    val timeoutMs = configReader.config.bugleMagicCardChipTimeoutMs
    val hasShowCardsChip =
      remember(messageTemplateData.messageChipList) {
        messageTemplateData.messageChipList.any { it.isShowCardsChip() }
      }
    if (hasShowCardsChip && timeoutMs > 0) {
      LaunchedEffect(messageTemplateData.messageChipList) {
        delay(timeoutMs)
        Log.i(
          TAG,
          "[MessagesEmbedded] Dismissing visualizer after ${timeoutMs}ms delay for show cards chip",
        )
        info.onReceiveInsight(ServerSideCloseInsight().toContextInsight())
      }
    }
    MainTheme(messageTemplateData.styleConfig) { MergedChipsRow(messageTemplateData) }
  }

  @Composable
  private fun MergedChipsRow(messageTemplateData: MessageTemplateData) {
    Log.i(
      TAG,
      "[MessagesEmbedded] MergedChipsRow chip count: ${messageTemplateData.messageChipList.size}",
    )
    Row(
      modifier = Modifier.wrapContentWidth().padding(4.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
      verticalAlignment = Alignment.Bottom,
    ) {
      for (messageChip in messageTemplateData.messageChipList) {
        when (messageChip) {
          is SuggestionChip -> MessageSuggestionChip(messageChip)
          is RemoteActionChip -> MessageRemoteActionChip(messageChip)
          is ClientActionChip -> MessageClientActionChip(messageChip)
        }
      }
    }
  }

  @Composable
  fun MessageRemoteActionChip(remoteActionChip: RemoteActionChip) {
    Log.i(TAG, "[MessagesEmbedded] MessageRemoteActionChip: ${remoteActionChip.title}")
    val context = LocalContext.current
    MessageOutlinedButton(
      chipOnClick = {
        Log.i(TAG, "[MessagesEmbedded] remote action clicked")
        remoteActionChip.remoteAction.execute(context)
      },
      insight = remoteActionChip.insight,
    ) {
      MessageRowContent(
        title = remoteActionChip.title,
        subtitle = remoteActionChip.subtitle,
        contentDescription = remoteActionChip.contentDescription,
        icon = remoteActionChip.icon,
        isIconTintable = false,
      )
    }
  }

  @Composable
  fun MessageClientActionChip(clientActionChip: ClientActionChip) {
    Log.i(TAG, "[MessagesEmbedded] MessageClientActionChip: ${clientActionChip.title}")
    val context = LocalContext.current
    val info = LocalInsightSurfaceClientInfo.current
    MessageOutlinedButton(
      chipOnClick = {
        Log.i(TAG, "[MessagesEmbedded] client action clicked")
        info.onReceiveInsight(clientActionChip.insight)
      },
      insight = clientActionChip.insight,
    ) {
      MessageRowContent(
        title = clientActionChip.title,
        subtitle = clientActionChip.subtitle,
        contentDescription = clientActionChip.contentDescription,
        icon = clientActionChip.icon,
        isIconTintable = clientActionChip.trailingIcon != null && !clientActionChip.isIconGradient,
        isIconGradient = clientActionChip.isIconGradient,
        trailingIcon = clientActionChip.trailingIcon,
      )
    }
  }

  @Composable
  internal fun MessageSuggestionChip(suggestionChip: SuggestionChip) {
    Log.i(TAG, "[MessagesEmbedded] MessageSuggestionChip: ${suggestionChip.title}")
    val context = LocalContext.current
    val info = LocalInsightSurfaceClientInfo.current
    MessageOutlinedButton(
      chipOnClick = {
        Log.i(TAG, "[MessagesEmbedded] display insight clicked")
        info.onReceiveInsight(suggestionChip.insight)
      },
      insight = suggestionChip.insight,
    ) {
      MessageRowContent(
        title = suggestionChip.title,
        subtitle = suggestionChip.subtitle,
        contentDescription = suggestionChip.contentDescription,
        icon = suggestionChip.icon,
        isIconTintable = !suggestionChip.isIconGradient,
        isIconGradient = suggestionChip.isIconGradient,
      )
    }
  }

  @Composable
  private fun MessageOutlinedButton(
    chipOnClick: () -> Unit,
    insight: ContextInsight,
    chipContents: @Composable () -> Unit,
  ) {
    val shape = MaterialTheme.shapes.medium
    val interactionSource = remember { MutableInteractionSource() }
    val context = LocalContext.current
    val insightEventReporter = LocalInsightEventReporter.current
    val publishedInsight = LocalPublishedContextInsight.current
    val renderToken = LocalRenderToken.current

    val personalContextManager = remember {
      context.getSystemService(PersonalContextManager::class.java)
    }
    fun reportEvent(event: Int) {
      with(insightEventReporter) {
        personalContextManager?.reportChildInsightEvent(
          publishedInsight,
          insight,
          event,
          renderToken,
        )
      }
    }
    val showAnimationV2 = with(themeCompat) { publishedInsight.insight.shouldShowAnimationV2() }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(showAnimationV2) {
      if (showAnimationV2) {
        delay(MessageConstants.ANIMATION_REVEAL_DELAY_MILLIS.toLong())
        // Fast expansion using expressive default spatial spring
        progress.animateTo(
          targetValue = 1f,
          animationSpec = ExpressiveMotionUtils.expressiveMotionScheme().defaultSpatialSpec(),
        )
      }
    }

    LaunchedEffect(Unit) { reportEvent(InsightEvent.EVENT_SHOW) }
    val density = LocalDensity.current
    val cornerRadius = remember(shape, density) { shape.toCornerRadius(density) }
    val colorScheme = MaterialTheme.colorScheme
    val strokeColor = MaterialTheme.colorScheme.outlineVariant
    val backgroundColor = MaterialTheme.colorScheme.surface
    val geminiAnimationSpec =
      EnergyEffectsAnimationUtils.rememberChipSpec(
        cornerRadius = cornerRadius,
        density = density.density,
        colorScheme = colorScheme,
        context = context,
        strokeColor = strokeColor,
        backgroundColor = backgroundColor,
        initialDelay = MessageConstants.ANIMATION_REVEAL_DELAY_MILLIS.toLong(),
      )

    with(energyEffectsAnimationCompat) {
      AnimatedMessageChipLayout(
        progress = { progress.value },
        enabled = showAnimationV2,
        modifier =
          Modifier.widthIn(min = 30.dp, max = 264.dp).heightIn(min = MessageConstants.MinHeight),
        background = {
          Box(
            modifier =
              Modifier.fillMaxSize()
                .applyEnergyEffectsAnimation(
                  geminiAnimationSpec = geminiAnimationSpec,
                  fallback = {
                    animatedActionBorder(
                      cornerRadius = cornerRadius,
                      strokeColor = strokeColor,
                      backgroundColor = backgroundColor,
                    )
                  },
                )
                .clip(RoundedCornerShape(cornerRadius.x))
                .combinedClickable(
                  onClick = {
                    chipOnClick()
                    reportEvent(InsightEvent.EVENT_USER_TAP)
                  },
                  onLongClick = {
                    Log.i(TAG, "[MessagesEmbedded] chip long clicked")
                    reportEvent(InsightEvent.EVENT_USER_LONG_PRESS)
                  },
                  interactionSource = interactionSource,
                  indication = ripple(color = MaterialTheme.colorScheme.onSurface),
                )
                .semantics { role = Role.Button }
          )
        },
        content = { Box(contentAlignment = Alignment.Center) { chipContents() } },
      )
    }
  }

  @Composable
  private fun MessageRowContent(
    title: String,
    contentDescription: String,
    icon: Icon?,
    isIconTintable: Boolean = true,
    isIconGradient: Boolean = false,
    subtitle: String? = null,
    trailingIcon: Icon? = null,
  ) {
    Row(
      modifier =
        Modifier.clearAndSetSemantics(contentDescription)
          .padding(
            start = MessageConstants.ButtonHorizontalPadding,
            end =
              if (trailingIcon != null) {
                MessageConstants.ButtonHorizontalPadding
              } else {
                MessageConstants.ButtonEndPadding
              },
            top = MessageConstants.ButtonVerticalPadding,
            bottom = MessageConstants.ButtonVerticalPadding,
          ),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      val context = LocalContext.current
      // Icon
      val tint = MaterialTheme.colorScheme.primary
      icon?.let {
        val iconModifier =
          if (isIconGradient) {
            val primaryFixedDimColor = MaterialTheme.colorScheme.primaryFixedDim
            val primaryColor = MaterialTheme.colorScheme.primary
            Modifier.gradientTint(listOf(primaryFixedDimColor, primaryColor))
          } else {
            Modifier
          }
        val mappedIcon = icon.toBitmap(context)?.asTintableIcon(tintable = isIconTintable)
        mappedIcon?.let {
          IconOrImage(
            icon = mappedIcon,
            modifier = Modifier.size(18.dp).align(Alignment.CenterVertically).then(iconModifier),
            tint = tint,
          )
        }
      }

      // Text
      if (subtitle.isNullOrEmpty()) {
        SuggestionText(
          title,
          maxLines = 2,
          modifier = Modifier.align(Alignment.CenterVertically).weight(1f, fill = false),
        )
      } else {
        Column(modifier = Modifier.weight(1f, fill = false)) {
          SuggestionText(title, maxLines = 1)
          Text(
            text = subtitle,
            style =
              flexFontCompat.flexFont(
                style = MaterialTheme.typography.bodyMedium,
                weight = 550,
                round = 0f,
              ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }

      trailingIcon?.let {
        val mappedTrailingIcon = trailingIcon.toBitmap(context)?.asTintableIcon(tintable = true)
        mappedTrailingIcon?.let {
          IconOrImage(
            icon = mappedTrailingIcon,
            modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
            tint = tint,
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
      style =
        flexFontCompat.flexFont(
          style = MaterialTheme.typography.labelLarge,
          weight = 500,
          round = 0f,
        ),
      color = MaterialTheme.colorScheme.onSurface,
      overflow = TextOverflow.Ellipsis,
      maxLines = maxLines,
    )
  }

  private fun Icon.toBitmap(context: Context): Bitmap? {
    return try {
      this.loadDrawable(context)?.toBitmap()
    } catch (e: Exception) {
      Log.w(TAG, "[MessagesEmbedded] Failed to load icon to bitmap", e)
      null
    }
  }

  private fun Modifier.clearAndSetSemantics(description: String?): Modifier {
    if (description != null) {
      return clearAndSetSemantics { contentDescription = description }
    } else {
      return this
    }
  }

  /** Extracts a [CornerRadius] from a [CornerBasedShape] with the same radius for all corners. */
  private fun CornerBasedShape.toCornerRadius(density: Density): CornerRadius {
    val radiusPx = this.topStart.toPx(Size(10000f, 10000f), density)
    return CornerRadius(radiusPx)
  }

  private data class MessageColorScheme(
    val outlineVariant: Color,
    val onSurface: Color,
    val primary: Color,
    val backgroundColor: Color,
  )

  @Composable
  private fun MainTheme(styleConfig: MessageMetadataHint?, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val baseColorScheme =
      if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
      else dynamicLightColorScheme(context)

    val colorScheme =
      baseColorScheme.copy(
        outlineVariant =
          styleConfig?.strokeColor?.let { Color(it) } ?: baseColorScheme.outlineVariant,
        onSurface = styleConfig?.textColor?.let { Color(it) } ?: baseColorScheme.onSurface,
        onSurfaceVariant =
          styleConfig?.textColor?.let { Color(it) } ?: baseColorScheme.onSurfaceVariant,
        primary = styleConfig?.iconColor?.let { Color(it) } ?: baseColorScheme.primary,
        surface =
          styleConfig?.suggestionBackgroundColor?.let { Color(it) } ?: baseColorScheme.surface,
      )

    val shapes =
      MaterialTheme.shapes.copy(
        medium =
          styleConfig?.suggestionCornerRadius?.let { RoundedCornerShape(it.toFloat()) }
            ?: RoundedCornerShape(MessageConstants.CornerRadius)
      )

    MaterialTheme(
      colorScheme = colorScheme,
      shapes = shapes,
      typography = Typography(),
      content = content,
    )
  }

  private fun MessageChip.isShowCardsChip(): Boolean {
    if (this !is ClientActionChip) return false
    return clientActionInsightCompat.ifClientActionInsight(insight) {
      it.clientActionParams is ShowCardsParams
    } == true
  }

  companion object {
    const val TAG = "MessageVisualizerTemplate"
  }
}
