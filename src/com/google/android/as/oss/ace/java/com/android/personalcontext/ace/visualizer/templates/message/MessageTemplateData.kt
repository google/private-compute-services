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

package com.android.personalcontext.ace.visualizer.templates.message

import android.app.RemoteAction
import android.graphics.drawable.Icon
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.DisplayInsight
import android.service.personalcontext.insight.InsightCollection
import android.util.Log
import com.android.personalcontext.ace.client.prototype.PrototypeHintUtils.toPrototypeHint
import com.android.personalcontext.ace.client.prototype.message.MessageMetadataHint
import com.android.personalcontext.ace.visualizer.compat.ClientActionInsightCompat
import com.android.personalcontext.ace.visualizer.compat.ThemeCompat
import com.android.personalcontext.ace.visualizer.templates.message.ClientActionChip.Companion.toClientActionChip
import com.android.personalcontext.ace.visualizer.templates.message.RemoteActionChip.Companion.toRemoteActionChip
import com.android.personalcontext.ace.visualizer.templates.message.SuggestionChip.Companion.toSuggestionChip

/**
 * A data class that contains the data for the message template.
 *
 * @property messageChipList The list of chips to display in the message template. This list must be
 *   non-empty.
 */
data class MessageTemplateData(
  val messageChipList: List<MessageChip>,
  val styleConfig: MessageMetadataHint?,
) {
  companion object {
    private const val TAG = "MessageTemplateData"

    fun ContextInsight.toMessageTemplateData(
      clientActionInsightCompat: ClientActionInsightCompat,
      themeCompat: ThemeCompat,
    ): MessageTemplateData {
      Log.i(TAG, "[MessagesEmbedded] toMessageTemplateData")

      if (this !is InsightCollection) {
        error(
          "[MessagesEmbedded] Expected a top-level InsightCollection, actual: ${this.javaClass.simpleName}"
        )
      }

      val messageChipList = insights.mapNotNull { insight ->
        when (insight) {
          is DisplayInsight -> {
            Log.i(TAG, "[MessagesEmbedded] Find display insight")
            insight.toSuggestionChip(themeCompat)
          }
          is ActionableInsight -> {
            Log.i(TAG, "[MessagesEmbedded] Find actionable insight")
            insight.toRemoteActionChip()
          }
          else -> {
            insight.toClientActionChip(clientActionInsightCompat, themeCompat)
          }
        }
      }
      if (messageChipList.isEmpty()) {
        error("[MessagesEmbedded] messageChipList is empty")
      }

      val styleConfig =
        this.originHints.firstNotNullOfOrNull {
          it.contextHint.toPrototypeHint<MessageMetadataHint>()
        }
      return MessageTemplateData(messageChipList, styleConfig)
    }
  }
}

/** A sealed interface for chips that can be displayed in the message template. */
sealed interface MessageChip {
  val title: String
  val subtitle: String?
  val contentDescription: String
  val icon: Icon?
  val insight: ContextInsight
  val isIconGradient: Boolean
}

data class SuggestionChip(
  override val title: String,
  override val subtitle: String,
  override val contentDescription: String,
  override val icon: Icon?,
  override val insight: ContextInsight,
  override val isIconGradient: Boolean,
) : MessageChip {
  companion object {
    private const val TAG = "SuggestionChip"

    fun DisplayInsight.toSuggestionChip(themeCompat: ThemeCompat): SuggestionChip {
      Log.i(TAG, "[MessagesEmbedded] displayInsight title: ${details.title}")
      return SuggestionChip(
        title = details.title.toString(),
        subtitle = details.subtitle.toString(),
        contentDescription = details.contentDescription.toString(),
        icon = details.icon,
        insight = this,
        isIconGradient = with(themeCompat) { shouldShowBrandedIcon() },
      )
    }
  }
}

data class RemoteActionChip(
  override val title: String,
  override val subtitle: String?,
  override val contentDescription: String,
  override val icon: Icon,
  override val insight: ContextInsight,
  val remoteAction: RemoteAction,
  override val isIconGradient: Boolean = false,
) : MessageChip {
  companion object {
    private const val TAG = "RemoteActionChip"

    fun ActionableInsight.toRemoteActionChip(): RemoteActionChip? {
      Log.i(
        TAG,
        "[MessagesEmbedded] actionDetails.remoteAction is null: ${actionDetails.remoteAction == null}",
      )
      return actionDetails.remoteAction?.let { remoteAction ->
        Log.i(TAG, "[MessagesEmbedded] actionableInsight title: ${remoteAction.title}")
        RemoteActionChip(
          title = displayDetails.title.toString(),
          subtitle = displayDetails.subtitle?.toString(),
          contentDescription = displayDetails.contentDescription.toString(),
          icon = remoteAction.icon,
          insight = this,
          remoteAction = remoteAction,
          isIconGradient = false,
        )
      }
    }
  }
}

data class ClientActionChip(
  override val title: String,
  override val subtitle: String?,
  override val contentDescription: String,
  override val icon: Icon?,
  override val insight: ContextInsight,
  val trailingIcon: Icon? = null,
  override val isIconGradient: Boolean,
) : MessageChip {
  companion object {
    private const val TAG = "ClientActionChip"

    fun ContextInsight.toClientActionChip(
      clientActionInsightCompat: ClientActionInsightCompat,
      themeCompat: ThemeCompat,
    ): MessageChip? =
      clientActionInsightCompat.ifClientActionInsight(this) { clientActionInsight ->
        val insightDisplayDetails = clientActionInsight.insightDisplayDetails
        val trailingIcon = clientActionInsight.insightExtendedDetails?.trailingIcon
        ClientActionChip(
          title = insightDisplayDetails.title.toString(),
          subtitle = insightDisplayDetails.subtitle?.toString(),
          contentDescription = insightDisplayDetails.contentDescription.toString(),
          icon = insightDisplayDetails.icon,
          insight = this,
          trailingIcon = trailingIcon,
          isIconGradient = with(themeCompat) { shouldShowBrandedIcon() },
        )
      }
        ?: run {
          Log.i(TAG, "[MessagesEmbedded] insight is not a ClientActionInsight")
          (this as? DisplayInsight)?.toSuggestionChip(themeCompat)
        }
  }
}
