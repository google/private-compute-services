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

import com.google.android.`as`.oss.feedback.api.dataservice.FeedbackDataServiceGrpcKt
import com.google.android.`as`.oss.feedback.api.dataservice.GetFeedbackDonationDataResponse
import com.google.android.`as`.oss.feedback.api.dataservice.getFeedbackDonationDataRequest
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.serviceclient.serviceconnection.QuartzAnnotations.QuartzFeedbackDataService
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import javax.inject.Inject

class QuartzFeedbackDataServiceClientImpl
@Inject
internal constructor(
  @QuartzFeedbackDataService
  private val service: FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub
) : QuartzFeedbackDataServiceClient {
  override suspend fun getFeedbackDonationData(
    clientSessionId: String,
    uiElementType: Int,
    uiElementIndex: Int?,
    quartzCuj: QuartzCUJ?,
  ): Result<QuartzFeedbackDonationData> {
    return runCatching {
        val request = getFeedbackDonationDataRequest {
          this.clientSessionId = clientSessionId
          this.uiElementType = uiElementType
          uiElementIndex?.let { this.uiElementIndex = it }
          quartzCuj?.let { this.quartzCuj = it }
        }
        service.getFeedbackDonationData(request).toFeedbackDonationData(quartzCuj)
      }
      .onFailure { e ->
        logger
          .atSevere()
          .withCause(e)
          .withStackTrace(StackSize.SMALL)
          .log("Error getting feedback donation data from Quartz service")
      }
  }

  private fun GetFeedbackDonationDataResponse.toFeedbackDonationData(
    quartzCuj: QuartzCUJ?
  ): QuartzFeedbackDonationData {
    return QuartzFeedbackDonationData(
      quartzCuj = quartzCuj ?: QuartzCUJ.QUARTZ_CUJ_UNSPECIFIED,
      appId = appId,
      interactionId = interactionId,
      runtimeConfig =
        QuartzRuntimeConfig(
          appBuildType = runtimeConfig.appBuildType,
          appVersion = runtimeConfig.appVersion,
          modelMetadata = runtimeConfig.modelMetadata,
          modelId = runtimeConfig.modelId,
        ),
      feedbackUiRenderingData = feedbackUiRenderingData,
      summarizationData =
        if (
          donationData.hasQuartzDataDonation() &&
            quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION
        ) {
          QuartzKeySummarizationData(
            uuid = donationData.quartzDataDonation.quartzKeySummarizationData.quartzCommonData.uuid,
            sbnKey =
              donationData.quartzDataDonation.quartzKeySummarizationData.quartzCommonData.sbnKey,
            asiVersion =
              donationData.quartzDataDonation.quartzKeySummarizationData.quartzCommonData
                .asiVersion,
            detectedLanguage =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.detectedLanguage,
            featureName = donationData.quartzDataDonation.quartzKeySummarizationData.featureName,
            packageName =
              donationData.quartzDataDonation.quartzKeySummarizationData.quartzCommonData
                .packageName,
            modelName = donationData.quartzDataDonation.quartzKeySummarizationData.modelName,
            modelVersion = donationData.quartzDataDonation.quartzKeySummarizationData.modelVersion,
            isGroupConversation =
              donationData.quartzDataDonation.quartzKeySummarizationData.isGroupConversation,
            conversationTitle =
              donationData.quartzDataDonation.quartzKeySummarizationData.conversationTitle,
            messages = donationData.quartzDataDonation.quartzKeySummarizationData.messages,
            notificationCount =
              donationData.quartzDataDonation.quartzKeySummarizationData.notificationCount,
            executionTimeMs =
              donationData.quartzDataDonation.quartzKeySummarizationData.executionTimeMs,
            summaryText = donationData.quartzDataDonation.quartzKeySummarizationData.summaryText,
          )
        } else {
          QuartzKeySummarizationData()
        },
      typeData =
        if (donationData.hasQuartzDataDonation() && quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE) {
          QuartzKeyTypeData(
            sbnKey = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.sbnKey,
            uuid = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.uuid,
            asiVersion =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.asiVersion,
            detectedLanguage =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.detectedLanguage,
            packageName =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.packageName,
            title = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.title,
            content = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.content,
            notificationCategory =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .notificationCategory,
            notificationTag =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.notificationTag,
            isConversation =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.isConversation,
            channelId =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.channelId,
            channelName =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.channelName,
            channelImportance =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.channelImportance
                .name,
            channelDescription =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.channelDescription,
            channelConversationId =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .channelConversationId,
            playStoreCategory =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.playStoreCategory,
            extraTitle =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraTitle,
            extraTitleBig =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraTitleBig,
            extraText =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraText,
            extraTextLines =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraTextLines,
            extraSummaryText =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraSummaryText,
            extraPeopleList =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraPeopleList,
            extraMessagingPerson =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraMessagingPerson,
            extraMessages =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraMessages,
            extraHistoricMessages =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraHistoricMessagesList,
            extraConversationTitle =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraConversationTitle,
            extraBigText =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraBigText,
            extraInfoText =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraInfoText,
            extraSubText =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraSubText,
            extraIsGroupConversation =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraIsGroupConversation,
            extraPictureContentDescription =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraPictureContentDescription,
            extraTemplate =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraTemplate,
            extraShowBigPictureWhenCollapsed =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraShowBigPictureWhenCollapsed,
            extraColorized =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.extraColorized,
            extraRemoteInputHistory =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .extraRemoteInputHistoryList,
            locusId = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.locusId,
            hasPromotableCharacteristics =
              donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData
                .hasPromotableCharacteristics,
            groupKey = donationData.quartzDataDonation.quartzKeyTypeData.quartzCommonData.groupKey,
            notificationId = donationData.quartzDataDonation.quartzKeyTypeData.notificationId,
            postTimestamp = donationData.quartzDataDonation.quartzKeyTypeData.postTimestamp,
            appCategory = donationData.quartzDataDonation.quartzKeyTypeData.appCategory,
            modelInfoList = donationData.quartzDataDonation.quartzKeyTypeData.modelInfoListList,
            classificationMethod =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationMethod.name,
            classificationBertCategoryResult =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationBertCategoryResult,
            classificationBertCategoryScore =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationBertCategoryScore,
            classificationBertCategoryExecutedTimeMs =
              donationData.quartzDataDonation.quartzKeyTypeData
                .classificationBertCategoryExecutedTimeMs,
            classificationCategory =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationCategory,
            isThresholdChangedCategory =
              donationData.quartzDataDonation.quartzKeyTypeData.isThresholdChangedCategory,
            classificationExecutedTimeMs =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationExecutedTimeMs,
            exemptionExecutedTimeMsString =
              donationData.quartzDataDonation.quartzKeyTypeData.exemptionExecutedTimeMsString,
            classificationDefaultCategoryResult =
              donationData.quartzDataDonation.quartzKeyTypeData.classificationDefaultCategoryResult,
            defaultCategoryCorrectionThreshold =
              donationData.quartzDataDonation.quartzKeyTypeData.defaultCategoryCorrectionThreshold,
            isSuppressDuplicate =
              donationData.quartzDataDonation.quartzKeyTypeData.isSuppressDuplicate,
          )
        } else {
          QuartzKeyTypeData()
        },
      notificationData =
        if (
          donationData.hasQuartzDataDonationV2() &&
            donationData.quartzDataDonationV2.hasQuartzNotificationData()
        ) {
          QuartzNotificationData(
            title = donationData.quartzDataDonationV2.quartzNotificationData.title,
            content = donationData.quartzDataDonationV2.quartzNotificationData.content,
            channelId = donationData.quartzDataDonationV2.quartzNotificationData.channelId,
            conversationMessages =
              donationData.quartzDataDonationV2.quartzNotificationData.conversationMessages,
            conversationHistoricMessages =
              donationData.quartzDataDonationV2.quartzNotificationData.conversationHistoricMessages,
          )
        } else {
          null
        },
      modelData =
        if (
          donationData.hasQuartzDataDonationV2() &&
            donationData.quartzDataDonationV2.hasQuartzModelData() &&
            quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION
        ) {
          QuartzModelData(
            modelInfo = donationData.quartzDataDonationV2.quartzModelData.modelInfo,
            featureName = donationData.quartzDataDonationV2.quartzModelData.featureName,
            summaryText = donationData.quartzDataDonationV2.quartzModelData.summaryText,
          )
        } else if (
          donationData.hasQuartzDataDonationV2() &&
            donationData.quartzDataDonationV2.hasQuartzModelData() &&
            quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE
        ) {
          QuartzModelData(
            modelInfo = donationData.quartzDataDonationV2.quartzModelData.modelInfo,
            classificationMethod =
              donationData.quartzDataDonationV2.quartzModelData.classificationMethod,
            classificationBertCategoryResult =
              donationData.quartzDataDonationV2.quartzModelData.classificationBertCategoryResult,
            classificationBertCategoryScore =
              donationData.quartzDataDonationV2.quartzModelData.classificationBertCategoryScore,
            classificationCategory =
              donationData.quartzDataDonationV2.quartzModelData.classificationCategory,
            classificationDefaultCategoryResult =
              donationData.quartzDataDonationV2.quartzModelData.classificationDefaultCategoryResult,
            defaultCategoryCorrectionThreshold =
              donationData.quartzDataDonationV2.quartzModelData.defaultCategoryCorrectionThreshold,
            isSuppressDuplicate =
              donationData.quartzDataDonationV2.quartzModelData.isSuppressDuplicate,
          )
        } else {
          null
        },
      appInfoData =
        if (donationData.hasQuartzDataDonationV2()) {
          QuartzAppInfoData(
            uuid = donationData.quartzDataDonationV2.quartzAppInfoData.uuid,
            asiVersion = donationData.quartzDataDonationV2.quartzAppInfoData.asiVersion,
            playStoreCategory =
              donationData.quartzDataDonationV2.quartzAppInfoData.playStoreCategory,
            packageName = donationData.quartzDataDonationV2.quartzAppInfoData.packageName,
          )
        } else {
          null
        },
    )
  }

  private companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
