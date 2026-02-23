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

package com.google.android.`as`.oss.feedback.quartz.serviceclient

import com.google.android.`as`.oss.feedback.api.dataservice.FeedbackUiRenderingData
import com.google.android.`as`.oss.feedback.api.dataservice.feedbackUiRenderingData
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryData
import com.google.android.`as`.oss.feedback.domain.ViewFeedbackData
import com.google.protobuf.Timestamp

/** Service to provide feedback donation data. */
interface QuartzFeedbackDataServiceClient {
  /** Gets the feedback donation data from the feedback data service. */
  suspend fun getFeedbackDonationData(
    clientSessionId: String,
    uiElementType: Int,
    uiElementIndex: Int? = null,
    quartzCuj: QuartzCUJ? = null,
  ): Result<QuartzFeedbackDonationData>
}

data class QuartzNotificationData(
  val title: String = "",
  val content: String = "",
  val channelId: String = "",
  val conversationMessages: String = "",
  val conversationHistoricMessages: String = "",
)

data class QuartzModelData(
  val modelInfo: String = "",
  val classificationMethod: String = "",
  val classificationBertCategoryResult: String = "",
  val classificationBertCategoryScore: String = "",
  val classificationCategory: String = "",
  val classificationDefaultCategoryResult: String = "",
  val defaultCategoryCorrectionThreshold: String = "",
  val isSuppressDuplicate: String = "",
  val featureName: String = "",
  val summaryText: String = "",
)

data class QuartzAppInfoData(
  val uuid: String = "",
  val asiVersion: String = "",
  val playStoreCategory: String = "",
  val packageName: String = "",
)

data class QuartzKeySummarizationData(
  val uuid: String = "",
  val sbnKey: String = "",
  val asiVersion: String = "",
  val detectedLanguage: String = "",
  val featureName: String = "",
  val packageName: String = "",
  val modelName: String = "",
  val modelVersion: String = "",
  val isGroupConversation: Boolean = false,
  val conversationTitle: String = "",
  val messages: String = "",
  val notificationCount: Int = 0,
  val executionTimeMs: Long = 0L,
  val summaryText: String = "",
)

data class QuartzKeyTypeData(
  // Fields from QuartzCommonData
  val sbnKey: String = "",
  val uuid: String = "",
  val asiVersion: String = "",
  val detectedLanguage: String = "",
  val packageName: String = "",
  val title: String = "",
  val content: String = "",
  val notificationCategory: String = "",
  val notificationTag: String = "",
  val isConversation: Boolean = false,
  val channelId: String = "",
  val channelName: String = "",
  val channelImportance: String = "",
  val channelDescription: String = "",
  val channelConversationId: String = "",
  val playStoreCategory: String = "",
  val extraTitle: String = "",
  val extraTitleBig: String = "",
  val extraText: String = "",
  val extraTextLines: String = "",
  val extraSummaryText: String = "",
  val extraPeopleList: String = "",
  val extraMessagingPerson: String = "",
  val extraMessages: String = "",
  val extraHistoricMessages: List<String> = emptyList(),
  val extraConversationTitle: String = "",
  val extraBigText: String = "",
  val extraInfoText: String = "",
  val extraSubText: String = "",
  val extraIsGroupConversation: Boolean = false,
  val extraPictureContentDescription: String = "",
  val extraTemplate: String = "",
  val extraShowBigPictureWhenCollapsed: Boolean = false,
  val extraColorized: Boolean = false,
  val extraRemoteInputHistory: List<String> = emptyList(),
  val locusId: String = "",
  val hasPromotableCharacteristics: Boolean = false,
  val groupKey: String = "",

  // Fields specific to QuartzKeyTypeData
  val notificationId: String = "",
  val postTimestamp: Timestamp = Timestamp.getDefaultInstance(),
  val appCategory: String = "",
  val modelInfoList: List<String> = emptyList(),
  val classificationMethod: String = "",
  val classificationBertCategoryResult: String = "",
  val classificationBertCategoryScore: Float = 0.0f,
  val classificationBertCategoryExecutedTimeMs: Long = 0L,
  val classificationCategory: String = "",
  val isThresholdChangedCategory: Boolean = false,
  val classificationExecutedTimeMs: Long = 0L,
  val exemptionExecutedTimeMsString: String = "",
  val classificationDefaultCategoryResult: String = "",
  val defaultCategoryCorrectionThreshold: String = "",
  val isSuppressDuplicate: Boolean = false,
) {}

data class QuartzFeedbackDonationData(
  val appId: String = "",
  val interactionId: String = "",
  val quartzCuj: QuartzCUJ = QuartzCUJ.QUARTZ_CUJ_UNSPECIFIED,
  val summarizationData: QuartzKeySummarizationData = QuartzKeySummarizationData(),
  val typeData: QuartzKeyTypeData = QuartzKeyTypeData(),
  val notificationData: QuartzNotificationData? = null,
  val modelData: QuartzModelData? = null,
  val appInfoData: QuartzAppInfoData? = null,
  val runtimeConfig: QuartzRuntimeConfig = QuartzRuntimeConfig(),
  val feedbackUiRenderingData: FeedbackUiRenderingData = feedbackUiRenderingData {},
) : ViewFeedbackData {
  override val viewFeedbackHeader: String = feedbackUiRenderingData.feedbackDialogViewDataHeader
  override val viewFeedbackBody: String = toString()

  override val dataCollectionCategories: Map<DataCollectionCategory, DataCollectionCategoryData>
    get() {
      return buildMap {
        if (notificationData != null) {
          put(
            DataCollectionCategory.NotificationContent,
            DataCollectionCategoryData(
              header =
                feedbackUiRenderingData.feedbackViewDataCategoryTitles.notificationContentTitle,
              body = getNotificationDataBody(),
            ),
          )
        }
        if (modelData != null) {
          put(
            DataCollectionCategory.QuartzModelOutputs,
            DataCollectionCategoryData(
              header =
                feedbackUiRenderingData.feedbackViewDataCategoryTitles.quartzModelOutputsTitle,
              body = getModelOutputsBody(),
            ),
          )
        }
        put(
          DataCollectionCategory.AppInfo,
          DataCollectionCategoryData(
            header = feedbackUiRenderingData.feedbackViewDataCategoryTitles.appInfoTitle,
            body = getAppInfoBody(),
          ),
        )
      }
    }

  private fun getNotificationDataBody(): String {
    if (notificationData == null) {
      return ""
    }
    return buildString {
      appendLine(notificationData.title)
      appendLine(notificationData.content)
      appendLine(notificationData.channelId)
      if (notificationData.conversationMessages.isNotEmpty()) {
        appendLine(notificationData.conversationMessages)
      }
      if (notificationData.conversationHistoricMessages.isNotEmpty()) {
        appendLine(notificationData.conversationHistoricMessages)
      }
    }
  }

  private fun getModelOutputsBody(): String {
    if (modelData == null) {
      return ""
    }
    return buildString {
      appendLine(modelData.modelInfo)
      if (modelData.classificationMethod.isNotEmpty()) {
        appendLine(modelData.classificationMethod)
      }
      if (modelData.classificationBertCategoryResult.isNotEmpty()) {
        appendLine(modelData.classificationBertCategoryResult)
      }
      if (modelData.classificationBertCategoryScore.isNotEmpty()) {
        appendLine(modelData.classificationBertCategoryScore)
      }
      if (modelData.classificationCategory.isNotEmpty()) {
        appendLine(modelData.classificationCategory)
      }
      if (modelData.classificationDefaultCategoryResult.isNotEmpty()) {
        appendLine(modelData.classificationDefaultCategoryResult)
      }
      if (modelData.defaultCategoryCorrectionThreshold.isNotEmpty()) {
        appendLine(modelData.defaultCategoryCorrectionThreshold)
      }
      if (modelData.isSuppressDuplicate.isNotEmpty()) {
        appendLine(modelData.isSuppressDuplicate)
      }
      if (modelData.featureName.isNotEmpty()) {
        appendLine(modelData.featureName)
      }
      if (modelData.summaryText.isNotEmpty()) {
        appendLine(modelData.summaryText)
      }
    }
  }

  private fun getAppInfoBody(): String {
    if (appInfoData == null) {
      return ""
    }
    return buildString {
      appendLine(appInfoData.uuid)
      appendLine(appInfoData.asiVersion)
      appendLine(appInfoData.playStoreCategory)
      appendLine(appInfoData.packageName)
    }
  }

  override val dataCollectionCategoryExpandContentDescription: String =
    feedbackUiRenderingData.feedbackViewDataCategoryTitles.expandCategoryButtonSemanticsDescription

  override val dataCollectionCategoryCollapseContentDescription: String =
    feedbackUiRenderingData.feedbackViewDataCategoryTitles
      .collapseCategoryButtonSemanticsDescription

  override fun toString(): String {
    return buildString {
      appendLine("appId: $appId")
      appendLine("interactionId: $interactionId")
      appendLine("quartzCuj: $quartzCuj")
      appendLine("runtimeConfig {")
      appendLine("  appBuildType: ${runtimeConfig.appBuildType}")
      appendLine("  appVersion: ${runtimeConfig.appVersion}")
      appendLine("  modelMetadata: ${runtimeConfig.modelMetadata}")
      appendLine("  modelId: ${runtimeConfig.modelId}")
      appendLine("}")
      when (quartzCuj) {
        QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION -> {
          // Summarization data is empty, don't append summarization data fields.
          if (summarizationData.uuid.isNotEmpty() && summarizationData.sbnKey.isNotEmpty()) {
            appendLine("uuid: ${summarizationData.uuid}")
            appendLine("sbnKey: ${summarizationData.sbnKey}")
            appendLine("asiVersion: ${summarizationData.asiVersion}")
            appendLine("detectedLanguage: ${summarizationData.detectedLanguage}")
            appendLine("featureName: ${summarizationData.featureName}")
            appendLine("packageName: ${summarizationData.packageName}")
            appendLine("modelName: ${summarizationData.modelName}")
            appendLine("modelVersion: ${summarizationData.modelVersion}")
            appendLine("isGroupConversation: ${summarizationData.isGroupConversation}")
            appendLine("conversationTitle: ${summarizationData.conversationTitle}")
            appendLine("messages: ${summarizationData.messages}")
            appendLine("notificationCount: ${summarizationData.notificationCount}")
            appendLine("executionTimeMs: ${summarizationData.executionTimeMs}")
            appendLine("summaryText: ${summarizationData.summaryText}")
          }
        }
        QuartzCUJ.QUARTZ_CUJ_KEY_TYPE -> {
          // Type data is empty, don't append type data fields.
          if (typeData.sbnKey.isNotEmpty() && typeData.uuid.isNotEmpty()) {
            appendLine("sbnKey: ${typeData.sbnKey}")
            appendLine("uuid: ${typeData.uuid}")
            appendLine("asiVersion: ${typeData.asiVersion}")
            appendLine("detectedLanguage: ${typeData.detectedLanguage}")
            appendLine("packageName: ${typeData.packageName}")
            appendLine("title: ${typeData.title}")
            appendLine("content: ${typeData.content}")
            appendLine("notificationCategory: ${typeData.notificationCategory}")
            appendLine("notificationTag: ${typeData.notificationTag}")
            appendLine("isConversation: ${typeData.isConversation}")
            appendLine("channelId: ${typeData.channelId}")
            appendLine("channelName: ${typeData.channelName}")
            appendLine("channelImportance: ${typeData.channelImportance}")
            appendLine("channelDescription: ${typeData.channelDescription}")
            appendLine("channelConversationId: ${typeData.channelConversationId}")
            appendLine("playStoreCategory: ${typeData.playStoreCategory}")
            appendLine("extraTitle: ${typeData.extraTitle}")
            appendLine("extraTitleBig: ${typeData.extraTitleBig}")
            appendLine("extraText: ${typeData.extraText}")
            appendLine("extraTextLines: ${typeData.extraTextLines}")
            appendLine("extraSummaryText: ${typeData.extraSummaryText}")
            appendLine("extraPeopleList: ${typeData.extraPeopleList}")
            appendLine("extraMessagingPerson: ${typeData.extraMessagingPerson}")
            appendLine("extraMessages: ${typeData.extraMessages}")
            appendLine("extraHistoricMessages: ${typeData.extraHistoricMessages}")
            appendLine("extraConversationTitle: ${typeData.extraConversationTitle}")
            appendLine("extraBigText: ${typeData.extraBigText}")
            appendLine("extraInfoText: ${typeData.extraInfoText}")
            appendLine("extraSubText: ${typeData.extraSubText}")
            appendLine("extraIsGroupConversation: ${typeData.extraIsGroupConversation}")
            appendLine("extraPictureContentDescription: ${typeData.extraPictureContentDescription}")
            appendLine("extraTemplate: ${typeData.extraTemplate}")
            appendLine(
              "extraShowBigPictureWhenCollapsed: ${typeData.extraShowBigPictureWhenCollapsed}"
            )
            appendLine("extraColorized: ${typeData.extraColorized}")
            appendLine("extraRemoteInputHistory: ${typeData.extraRemoteInputHistory}")
            appendLine("locusId: ${typeData.locusId}")
            appendLine("hasPromotableCharacteristics: ${typeData.hasPromotableCharacteristics}")
            appendLine("groupKey: ${typeData.groupKey}")
            appendLine("notificationId: ${typeData.notificationId}")
            appendLine("postTimestamp: ${typeData.postTimestamp}")
            appendLine("appCategory: ${typeData.appCategory}")
            appendLine("modelInfoList: ${typeData.modelInfoList}")
            appendLine("classificationMethod: ${typeData.classificationMethod}")
            appendLine(
              "classificationBertCategoryResult: ${typeData.classificationBertCategoryResult}"
            )
            appendLine(
              "classificationBertCategoryScore: ${typeData.classificationBertCategoryScore}"
            )
            appendLine(
              "classificationBertCategoryExecutedTimeMs: ${typeData.classificationBertCategoryExecutedTimeMs}"
            )
            appendLine("classificationCategory: ${typeData.classificationCategory}")
            appendLine("isThresholdChangedCategory: ${typeData.isThresholdChangedCategory}")
            appendLine("classificationExecutedTimeMs: ${typeData.classificationExecutedTimeMs}")
            appendLine("exemptionExecutedTimeMsString: ${typeData.exemptionExecutedTimeMsString}")
            appendLine(
              "classificationDefaultCategoryResult: ${typeData.classificationDefaultCategoryResult}"
            )
            appendLine(
              "defaultCategoryCorrectionThreshold: ${typeData.defaultCategoryCorrectionThreshold}"
            )
            appendLine("isSuppressDuplicate: ${typeData.isSuppressDuplicate}")
          }
        }
        else -> {}
      }
    }
  }
}

data class QuartzRuntimeConfig(
  val appBuildType: String = "",
  val appVersion: String = "",
  val modelMetadata: String = "",
  val modelId: String = "",
)
