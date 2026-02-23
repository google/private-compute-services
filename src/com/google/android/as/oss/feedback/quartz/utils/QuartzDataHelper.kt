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

package com.google.android.`as`.oss.feedback.quartz.utils

import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.gateway.FeedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.KeyTypeOptionTag
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.NegativeRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.PositiveRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCommonData
import com.google.android.`as`.oss.feedback.api.gateway.QuartzKeyTypeData
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.RuntimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.UserDonation
import com.google.android.`as`.oss.feedback.api.gateway.feedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.logFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.quartzCommonData
import com.google.android.`as`.oss.feedback.api.gateway.quartzDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.quartzDataDonationV2
import com.google.android.`as`.oss.feedback.api.gateway.quartzKeySummarizationData
import com.google.android.`as`.oss.feedback.api.gateway.quartzKeyTypeData
import com.google.android.`as`.oss.feedback.api.gateway.quartzModelData
import com.google.android.`as`.oss.feedback.api.gateway.quartzNotificationData
import com.google.android.`as`.oss.feedback.api.gateway.runtimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.structuredUserInput
import com.google.android.`as`.oss.feedback.api.gateway.userDonation
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionData
import com.google.android.`as`.oss.feedback.domain.FeedbackUiState
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
import com.google.common.flogger.GoogleLogger
import com.google.protobuf.Timestamp
import com.google.protobuf.util.kotlin.toJavaInstant
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/** Helper class for Quartz feedbackdata. */
@Singleton
class QuartzDataHelper @Inject constructor() {
  fun FeedbackSubmissionData.toQuartzFeedbackUploadRequest(
    data: QuartzFeedbackDonationData,
    uiState: FeedbackUiState,
  ): LogFeedbackV2Request? {
    return logFeedbackV2Request {
      val submissionData = this@toQuartzFeedbackUploadRequest
      this.appId = data.appId
      this.interactionId = data.interactionId
      this.feedbackCuj = feedbackCUJ { this.quartzCuj = submissionData.quartzCuj ?: data.quartzCuj }
      this.runtimeConfig = runtimeConfig {
        appBuildType = data.runtimeConfig.appBuildType
        appVersion = data.runtimeConfig.appVersion
        modelMetadata = data.runtimeConfig.modelMetadata
        modelId = data.runtimeConfig.modelId
      }
      this.rating =
        when (ratingSentiment) {
          RATING_SENTIMENT_THUMBS_UP -> Rating.THUMB_UP
          RATING_SENTIMENT_THUMBS_DOWN -> Rating.THUMB_DOWN
          else -> {
            logger.atInfo().log("FeedbackViewModel#Rating sentiment not defined. Skipping.")
            return null
          }
        }
      uiState.tagsSelectionMap[selectedEntityContent]?.get(RATING_SENTIMENT_THUMBS_UP)?.let { entry
        ->
        this.positiveTags += entry.keys.map { PositiveRatingTag.entries[it.ratingTagOrdinal] }
      }
      uiState.tagsSelectionMap[selectedEntityContent]?.get(RATING_SENTIMENT_THUMBS_DOWN)?.let {
        entry ->
        this.negativeTags += entry.keys.map { NegativeRatingTag.entries[it.ratingTagOrdinal] }
      }

      if (feedbackCuj.quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE) {
        val firstNegativeRatingTagOrdinal: Int? = negativeTags.firstOrNull()?.number
        structuredUserInput = structuredUserInput {
          keyTypeOption =
            if (firstNegativeRatingTagOrdinal != null) {
              KeyTypeOptionTag.forNumber(firstNegativeRatingTagOrdinal)
                ?: KeyTypeOptionTag.KEY_TYPE_OPTION_UNSPECIFIED
            } else {
              KeyTypeOptionTag.KEY_TYPE_OPTION_UNSPECIFIED
            }
        }
      }

      additionalComment = uiState.freeFormTextMap[selectedEntityContent] ?: ""
      donationOption =
        if (uiState.optInChecked.any { it.value }) {
          // The semantic meaning is changed to mean *any* category opt-in.
          UserDataDonationOption.OPT_IN
        } else {
          UserDataDonationOption.OPT_OUT
        }
      userDonation = userDonation {
        // Only include donation data if user has opted in the consent.
        if (uiState.optInChecked.any { it.value }) {
          if (data.notificationData != null) {
            quartzDataDonationV2 = quartzDataDonationV2 {
              quartzCuj = data.quartzCuj
              quartzNotificationData = quartzNotificationData {
                title = data.notificationData!!.title
                content = data.notificationData!!.content
                channelId = data.notificationData!!.channelId
                conversationMessages = data.notificationData!!.conversationMessages
                conversationHistoricMessages = data.notificationData!!.conversationHistoricMessages
              }
              quartzModelData =
                if (data.modelData != null) {
                  quartzModelData { modelInfo = data.modelData!!.modelInfo }
                } else {
                  quartzModelData {}
                }
            }
          } else {
            quartzDataDonation = quartzDataDonation {
              when (data.quartzCuj) {
                QuartzCUJ.QUARTZ_CUJ_KEY_TYPE -> {
                  quartzKeyTypeData = quartzKeyTypeData {
                    quartzCommonData = quartzCommonData {
                      sbnKey = data.typeData.sbnKey
                      uuid = data.typeData.uuid
                      asiVersion = data.typeData.asiVersion
                      detectedLanguage = data.typeData.detectedLanguage
                      packageName = data.typeData.packageName
                      title = data.typeData.title
                      content = data.typeData.content
                      notificationCategory = data.typeData.notificationCategory
                      notificationTag = data.typeData.notificationTag
                      isConversation = data.typeData.isConversation
                      channelId = data.typeData.channelId
                      channelName = data.typeData.channelName
                      channelImportance =
                        QuartzCommonData.ChannelImportance.valueOf(data.typeData.channelImportance)
                      channelDescription = data.typeData.channelDescription
                      channelConversationId = data.typeData.channelConversationId
                      playStoreCategory = data.typeData.playStoreCategory
                      extraTitle = data.typeData.extraTitle
                      extraTitleBig = data.typeData.extraTitleBig
                      extraText = data.typeData.extraText
                      extraTextLines = data.typeData.extraTextLines
                      extraSummaryText = data.typeData.extraSummaryText
                      extraPeopleList = data.typeData.extraPeopleList
                      extraMessagingPerson = data.typeData.extraMessagingPerson
                      extraMessages = data.typeData.extraMessages
                      extraHistoricMessages += data.typeData.extraHistoricMessages
                      extraConversationTitle = data.typeData.extraConversationTitle
                      extraBigText = data.typeData.extraBigText
                      extraInfoText = data.typeData.extraInfoText
                      extraSubText = data.typeData.extraSubText
                      extraIsGroupConversation = data.typeData.extraIsGroupConversation
                      extraPictureContentDescription = data.typeData.extraPictureContentDescription
                      extraTemplate = data.typeData.extraTemplate
                      extraShowBigPictureWhenCollapsed =
                        data.typeData.extraShowBigPictureWhenCollapsed
                      extraColorized = data.typeData.extraColorized
                      extraRemoteInputHistory += data.typeData.extraRemoteInputHistory
                      locusId = data.typeData.locusId
                      hasPromotableCharacteristics = data.typeData.hasPromotableCharacteristics
                      groupKey = data.typeData.groupKey
                    }
                    notificationId = data.typeData.notificationId
                    postTimestamp = data.typeData.postTimestamp
                    appCategory = data.typeData.appCategory
                    modelInfoList += data.typeData.modelInfoList
                    classificationMethod =
                      QuartzKeyTypeData.ClassificationMethod.valueOf(
                        data.typeData.classificationMethod
                      )
                    classificationBertCategoryResult =
                      data.typeData.classificationBertCategoryResult
                    classificationBertCategoryScore = data.typeData.classificationBertCategoryScore
                    classificationBertCategoryExecutedTimeMs =
                      data.typeData.classificationBertCategoryExecutedTimeMs
                    classificationCategory = data.typeData.classificationCategory
                    isThresholdChangedCategory = data.typeData.isThresholdChangedCategory
                    classificationExecutedTimeMs = data.typeData.classificationExecutedTimeMs
                    exemptionExecutedTimeMsString = data.typeData.exemptionExecutedTimeMsString
                    classificationDefaultCategoryResult =
                      data.typeData.classificationDefaultCategoryResult
                    defaultCategoryCorrectionThreshold =
                      data.typeData.defaultCategoryCorrectionThreshold
                    isSuppressDuplicate = data.typeData.isSuppressDuplicate
                  }
                }
                QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION -> {
                  quartzKeySummarizationData = quartzKeySummarizationData {
                    quartzCommonData = quartzCommonData {
                      sbnKey = data.summarizationData.sbnKey
                      uuid = data.summarizationData.uuid
                      asiVersion = data.summarizationData.asiVersion
                      detectedLanguage = data.summarizationData.detectedLanguage
                      packageName = data.summarizationData.packageName
                    }
                    featureName = data.summarizationData.featureName
                    modelName = data.summarizationData.modelName
                    modelVersion = data.summarizationData.modelVersion
                    isGroupConversation = data.summarizationData.isGroupConversation
                    conversationTitle = data.summarizationData.conversationTitle
                    messages = data.summarizationData.messages
                    notificationCount = data.summarizationData.notificationCount
                    executionTimeMs = data.summarizationData.executionTimeMs
                    summaryText = data.summarizationData.summaryText
                  }
                }
                else -> {}
              }
            }
          }
        }
      }
    }
  }

  fun LogFeedbackV2Request.convertToQuartzRequestString(): String {
    logger
      .atInfo()
      .log("QuartzDataHelper#convertToQuartzRequestString interactionId: %s", interactionId)

    var finalString: String =
      "{" +
        "${quote("appId")}: ${quote(appId)}, " +
        "${quote("interactionId")}: ${quote(interactionId)}, " +
        "${quote("donationOption")}: ${quote(donationOption.name)}, " +
        "${quote("appCujType")}: ${getQuartzCujTypeString(feedbackCuj)}, " +
        "${quote("runtimeConfig")}: ${getRuntimeConfigString(runtimeConfig)}"

    if (rating == Rating.THUMB_UP) {
      finalString = finalString.plus(", ${quote("positiveTags")}: [")
      for (i in 0 until positiveTagsList.size) {
        finalString = finalString.plus(quote(positiveTagsList[i].name))
        // Add comma between tags.
        if (i < positiveTagsList.size - 1) finalString = finalString.plus(", ")
      }
      finalString = finalString.plus("]")
    }

    // Add QUARTZ_CUJ_KEY_TYPE its own structured user input.
    if (feedbackCuj.quartzCuj == QuartzCUJ.QUARTZ_CUJ_KEY_TYPE) {
      finalString =
        finalString.plus(
          ", ${quote("structuredUserInput")}: {" +
            "${quote(PREFIX + "QuartzUserInput")}: {" +
            "${quote("key_type_option")}: ${quote(structuredUserInput.keyTypeOption.name)}" +
            "}" +
            "}"
        )
    } else {
      if (rating == Rating.THUMB_DOWN) {
        finalString = finalString.plus(", ${quote("negativeTags")}: [")
        for (i in 0 until negativeTagsList.size) {
          finalString = finalString.plus(quote(negativeTagsList[i].name))
          // Add comma between tags.
          if (i < negativeTagsList.size - 1) finalString = finalString.plus(", ")
        }
        finalString = finalString.plus("]")
      }
    }

    // UserDonation
    if (donationOption == UserDataDonationOption.OPT_IN) {
      finalString = finalString.plus(getDonationDataString(userDonation, feedbackCuj))
    }

    // Feedback rating.
    finalString =
      finalString.plus(
        ", ${quote("feedbackRating")}: {${quote("binaryRating")}: ${quote(rating.name)}}"
      )

    // Feedback additional comment.
    finalString = finalString.plus(", ${quote("additionalComment")}: ${quote(additionalComment)}")

    // Add the ending indicator.
    finalString = finalString.plus("}")
    return finalString
  }

  private fun getQuartzCujTypeString(appCujType: FeedbackCUJ): String {
    return "{${quote(PREFIX + "QuartzCujType")}: " +
      "{${quote(PREFIX + "QuartzCuj")}: ${quote("PIXEL_${appCujType.quartzCuj.name}")}}}"
  }

  private fun getRuntimeConfigString(config: RuntimeConfig): String {
    return "{" +
      "${quote("appBuildType")}: ${quote(config.appBuildType)}, " +
      "${quote("appVersion")}: ${quote(config.appVersion)}, " +
      "${quote("modelMetadata")}: ${quote(config.modelMetadata)}, " +
      "${quote("modelId")}: ${quote(config.modelId)}" +
      "}"
  }

  private fun getDonationDataString(userDonation: UserDonation, feedbackCuj: FeedbackCUJ): String {
    val quartzDataDonation = userDonation.quartzDataDonation
    val summarizationData = quartzDataDonation.quartzKeySummarizationData
    val quartzKeyTypeData = quartzDataDonation.quartzKeyTypeData

    when (feedbackCuj.quartzCuj) {
      QuartzCUJ.QUARTZ_CUJ_KEY_TYPE -> {
        var donationString = ""
        donationString =
          donationString.plus(
            ", ${quote("userDonation")}: " +
              "{${quote("structuredDataDonation")}: " +
              "{${quote(PREFIX + "QuartzDonation")}: " +
              "{${quote(PREFIX + "QuartzKeyTypeData")}: " +
              "{${quote(PREFIX + "QuartzCommonData")}: " +
              "${getQuartzCommonDataString(quartzKeyTypeData.quartzCommonData)}, " +
              "${quote("notificationId")}: ${quote(quartzKeyTypeData.notificationId)}, " +
              "${quote("postTimestamp")}: ${quote(timestampToJsonString(quartzKeyTypeData.postTimestamp))}, " +
              "${quote("modelInfoList")}: ${buildRepeatedMessages(quartzKeyTypeData.modelInfoListList)}, " +
              "${quote("appCategory")}: ${quote(quartzKeyTypeData.appCategory)}, " +
              "${quote("classificationMethod")}: ${quote(quartzKeyTypeData.classificationMethod.name)}, " +
              "${quote("classificationBertCategoryResult")}: ${quote(quartzKeyTypeData.classificationBertCategoryResult)}, " +
              "${quote("classificationBertCategoryScore")}: ${quartzKeyTypeData.classificationBertCategoryScore}, " +
              "${quote("classificationBertCategoryExecutedTimeMs")}: ${quartzKeyTypeData.classificationBertCategoryExecutedTimeMs}, " +
              "${quote("classificationCategory")}: ${quote(quartzKeyTypeData.classificationCategory)}, " +
              "${quote("isThresholdChangedCategory")}: ${quartzKeyTypeData.isThresholdChangedCategory}, " +
              "${quote("classificationExecutedTimeMs")}: ${quartzKeyTypeData.classificationExecutedTimeMs}, " +
              "${quote("exemptionExecutedTimeMsString")}: ${quote(quartzKeyTypeData.exemptionExecutedTimeMsString)}, " +
              "${quote("classificationDefaultCategoryResult")}: ${quote(quartzKeyTypeData.classificationDefaultCategoryResult)}, " +
              "${quote("defaultCategoryCorrectionThreshold")}: ${quote(quartzKeyTypeData.defaultCategoryCorrectionThreshold)}, " +
              "${quote("isSuppressDuplicate")}: ${quote(quartzKeyTypeData.isSuppressDuplicate)}, " +
              "${quote("feedbackCategory")}: ${quote(quartzKeyTypeData.feedbackCategory)}, " +
              "${quote("feedbackInputCategory")}: ${quote(quartzKeyTypeData.feedbackInputCategory)}" +
              "}}}"
          )
        donationString = donationString.plus("}")
        return donationString
      }

      QuartzCUJ.QUARTZ_CUJ_KEY_SUMMARIZATION -> {
        var donationString = ""
        donationString =
          donationString.plus(
            ", ${quote("userDonation")}: " +
              "{${quote("structuredDataDonation")}: " +
              "{${quote(PREFIX + "QuartzDonation")}: " +
              "{${quote(PREFIX + "QuartzKeySummarizationData")}: " +
              "{${quote(PREFIX + "QuartzCommonData")}: " +
              "${getQuartzCommonDataString(summarizationData.quartzCommonData)}, " +
              "${quote("featureName")}: ${quote(summarizationData.featureName)}, " +
              "${quote("modelName")}: ${quote(summarizationData.modelName)}, " +
              "${quote("modelVersion")}: ${quote(summarizationData.modelVersion)}, " +
              "${quote("isGroupConversation")}: ${summarizationData.isGroupConversation}, " +
              "${quote("conversationTitle")}: ${quote(summarizationData.conversationTitle)}, " +
              "${quote("messages")}: ${quote(summarizationData.messages)}, " +
              "${quote("notificationCount")}: ${summarizationData.notificationCount}, " +
              "${quote("executionTimeMs")}: ${summarizationData.executionTimeMs}, " +
              "${quote("summaryText")}: ${quote(summarizationData.summaryText)}, " +
              "${quote("feedbackType")}: ${quote(summarizationData.feedbackType)}, " +
              "${quote("feedbackAdditionalDetail")}: ${quote(summarizationData.feedbackAdditionalDetail)}" +
              "}}}"
          )
        donationString = donationString.plus("}")
        return donationString
      }
      else -> {
        return ""
      }
    }
  }

  private fun getQuartzCommonDataString(commonData: QuartzCommonData): String {
    return "{" +
      "${quote("sbnKey")}: ${quote(commonData.sbnKey)}, " +
      "${quote("uuid")}: ${quote(commonData.uuid)}, " +
      "${quote("asiVersion")}: ${quote(commonData.asiVersion)}, " +
      "${quote("detectedLanguage")}: ${quote(commonData.detectedLanguage)}, " +
      "${quote("packageName")}: ${quote(commonData.packageName)}, " +
      "${quote("title")}: ${quote(commonData.title)}, " +
      "${quote("content")}: ${quote(commonData.content)}, " +
      "${quote("notificationCategory")}: ${quote(commonData.notificationCategory)}, " +
      "${quote("notificationTag")}: ${quote(commonData.notificationTag)}, " +
      "${quote("isConversation")}: ${commonData.isConversation}, " +
      "${quote("channelId")}: ${quote(commonData.channelId)}, " +
      "${quote("channelName")}: ${quote(commonData.channelName)}, " +
      "${quote("channelImportance")}: ${quote(commonData.channelImportance.name)}, " +
      "${quote("channelDescription")}: ${quote(commonData.channelDescription)}, " +
      "${quote("channelConversationId")}: ${quote(commonData.channelConversationId)}, " +
      "${quote("playStoreCategory")}: ${quote(commonData.playStoreCategory)}, " +
      "${quote("extraTitle")}: ${quote(commonData.extraTitle)}, " +
      "${quote("extraTitleBig")}: ${quote(commonData.extraTitleBig)}, " +
      "${quote("extraText")}: ${quote(commonData.extraText)}, " +
      "${quote("extraTextLines")}: ${quote(commonData.extraTextLines)}, " +
      "${quote("extraSummaryText")}: ${quote(commonData.extraSummaryText)}, " +
      "${quote("extraPeopleList")}: ${quote(commonData.extraPeopleList)}, " +
      "${quote("extraMessagingPerson")}: ${quote(commonData.extraMessagingPerson)}, " +
      "${quote("extraMessages")}: ${quote(commonData.extraMessages)}, " +
      "${quote("extraHistoricMessages")}: ${buildRepeatedMessages(commonData.extraHistoricMessagesList)}, " +
      "${quote("extraConversationTitle")}: ${quote(commonData.extraConversationTitle)}, " +
      "${quote("extraBigText")}: ${quote(commonData.extraBigText)}, " +
      "${quote("extraInfoText")}: ${quote(commonData.extraInfoText)}, " +
      "${quote("extraSubText")}: ${quote(commonData.extraSubText)}, " +
      "${quote("extraIsGroupConversation")}: ${commonData.extraIsGroupConversation}, " +
      "${quote("extraPictureContentDescription")}: ${quote(commonData.extraPictureContentDescription)}, " +
      "${quote("extraTemplate")}: ${quote(commonData.extraTemplate)}, " +
      "${quote("extraShowBigPictureWhenCollapsed")}: ${commonData.extraShowBigPictureWhenCollapsed}, " +
      "${quote("extraColorized")}: ${commonData.extraColorized}, " +
      "${quote("extraRemoteInputHistory")}: ${buildRepeatedMessages(commonData.extraRemoteInputHistoryList)}, " +
      "${quote("locusId")}: ${quote(commonData.locusId)}, " +
      "${quote("hasPromotableCharacteristics")}: ${commonData.hasPromotableCharacteristics}, " +
      "${quote("groupKey")}: ${quote(commonData.groupKey)}" +
      "}"
  }

  private fun buildRepeatedMessages(messages: List<String>): String {
    return "[${messages.map { quote(it) }.joinToString(", ")}]"
  }

  private fun timestampToJsonString(timestamp: Timestamp): String {
    val instant: Instant = timestamp.toJavaInstant()
    return DateTimeFormatter.ISO_INSTANT.format(instant.atOffset(ZoneOffset.UTC))
  }

  private fun quote(content: Any): String {
    val escapedContent = content.toString().replace("\"", "\\\"")
    return "\"$escapedContent\""
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
    const val PREFIX = "pixel"
  }
}
