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

package com.google.android.`as`.oss.feedback.blueflax.utils

import com.google.android.`as`.oss.feedback.api.EntityFeedbackDialogData
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_DOWN
import com.google.android.`as`.oss.feedback.api.FeedbackRatingSentiment.RATING_SENTIMENT_THUMBS_UP
import com.google.android.`as`.oss.feedback.api.dataservice.GetFeedbackDonationDataResponse
import com.google.android.`as`.oss.feedback.api.gateway.BlueflaxCUJ
import com.google.android.`as`.oss.feedback.api.gateway.FeedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.NegativeRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.PositiveRatingTag
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.RuntimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.blueflaxFeedbackDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.feedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.logFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.runtimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.userDonation
import com.google.android.`as`.oss.feedback.domain.FeedbackSubmissionData
import com.google.android.`as`.oss.feedback.domain.FeedbackUiState
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.common.flogger.GoogleLogger
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

/** Helper class for Blueflax feedback data. */
@Singleton
class BlueflaxDataHelper @Inject constructor() {

  /** Converts [FeedbackSubmissionData] to a [LogFeedbackV2Request] for Blueflax. */
  fun FeedbackSubmissionData.toBlueflaxFeedbackUploadRequest(
    data: FeedbackDonationData,
    uiState: FeedbackUiState,
  ): LogFeedbackV2Request? {
    val submissionData = this
    val validCuj =
      submissionData.blueflaxCuj?.takeIf { it != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED }
        ?: data.blueflaxCuj?.takeIf { it != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED }
        ?: return null

    return logFeedbackV2Request {
      this.appId = data.appId
      this.interactionId = data.interactionId
      this.donationOption = UserDataDonationOption.OPT_OUT
      this.feedbackCuj = feedbackCUJ { blueflaxCuj = validCuj }
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
          else -> Rating.RATING_UNSPECIFIED
        }
      uiState.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_UP)
        ?.let { entry ->
          val tags = entry.filterValues { it }.keys
          this.positiveTags += tags.map { PositiveRatingTag.entries[it.ratingTagOrdinal] }
        }
      uiState.tagsSelectionMap[submissionData.selectedEntityContent]
        ?.get(RATING_SENTIMENT_THUMBS_DOWN)
        ?.let { entry ->
          val tags = entry.filterValues { it }.keys
          this.negativeTags += tags.map { NegativeRatingTag.entries[it.ratingTagOrdinal] }
        }
      val freeFormText = uiState.freeFormTextMap[submissionData.selectedEntityContent] ?: ""
      val additionalText =
        uiState.additionalCommentTextMap[submissionData.selectedEntityContent] ?: ""
      this.additionalComment =
        when {
          freeFormText.isNotEmpty() && additionalText.isNotEmpty() ->
            "$freeFormText\n\n$additionalText"
          freeFormText.isNotEmpty() -> freeFormText
          else -> additionalText
        }
      userDonation = userDonation {
        blueflaxFeedbackDataDonation = blueflaxFeedbackDataDonation {
          this.selectedEntityContent = submissionData.selectedEntityContent
        }
      }
    }
  }

  /**
   * Converts [LogFeedbackV2Request] to a Json-like string that can be parsed by the APEX service.
   */
  fun LogFeedbackV2Request.convertToBlueflaxRequestString(): String {
    logger
      .atInfo()
      .log("BlueflaxDataHelper#convertToBlueflaxRequestString interactionId: %s", interactionId)

    val json = JSONObject()
    json.put("appId", appId)
    json.put("interactionId", interactionId)
    json.put("donationOption", donationOption.name)
    // Blueflax CUJ only submits user ratings and text comments (no diagnostic logs or memory
    // entities), so userDonation is intentionally omitted from the serialized request.
    json.put("appCujType", getBlueflaxCujTypeJson(feedbackCuj))

    if (feedbackCuj.blueflaxCuj != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED) {
      json.put("runtimeConfig", getRuntimeConfigJson(runtimeConfig))
    }

    if (positiveTagsList.isNotEmpty()) {
      val tagsArray = JSONArray()
      for (tag in positiveTagsList) {
        tagsArray.put(tag.name)
      }
      json.put("positiveTags", tagsArray)
    }

    if (negativeTagsList.isNotEmpty()) {
      val tagsArray = JSONArray()
      for (tag in negativeTagsList) {
        tagsArray.put(tag.name)
      }
      json.put("negativeTags", tagsArray)
    }

    if (additionalComment.isNotEmpty()) {
      json.put("additionalComment", additionalComment)
    }

    json.put("feedbackRating", JSONObject().put("binaryRating", rating.name))
    return json.toString()
  }

  private fun getBlueflaxCujTypeJson(appCujType: FeedbackCUJ): JSONObject =
    JSONObject()
      .put("blueflaxCujType", JSONObject().put("blueflaxCuj", appCujType.blueflaxCuj.name))

  private fun getRuntimeConfigJson(config: RuntimeConfig): JSONObject {
    val json = JSONObject()
    if (config.appBuildType.isNotEmpty()) {
      json.put("appBuildType", config.appBuildType)
    }
    json.put("appVersion", config.appVersion)
    json.put("modelId", config.modelId)
    return json
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
  }
}

val FeedbackSubmissionData.validBlueflaxCuj: BlueflaxCUJ?
  get() = blueflaxCuj?.takeIf { it != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED }

val FeedbackSubmissionData.isBlueflax: Boolean
  get() = validBlueflaxCuj != null

val FeedbackDonationData.isBlueflax: Boolean
  get() = appId == "blueflax"

val EntityFeedbackDialogData.isBlueflax: Boolean
  get() = feedbackClient == EntityFeedbackDialogData.FeedbackClient.FEEDBACK_CLIENT_BLUEFLAX

val EntityFeedbackDialogData.validBlueflaxCuj: BlueflaxCUJ?
  get() {
    if (!isBlueflax) return null
    val cujEnum = BlueflaxCUJ.forNumber(cuj)
    return cujEnum?.takeIf { it != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED }
  }

/** Converts [GetFeedbackDonationDataResponse] to [FeedbackDonationData]. */
fun GetFeedbackDonationDataResponse.toFeedbackDonationData(
  defaultDonationOptInL1Enabled: Boolean = false,
  defaultDonationOptInL0Enabled: Boolean = false,
): com.google.android.as.oss.feedback.serviceclient.FeedbackDonationData {
  val blueflaxDonation = donationData.blueflaxFeedbackDataDonation
  return com.google.android.as.oss.feedback.serviceclient.FeedbackDonationData(
    triggeringMessages = blueflaxDonation.triggeringMessagesList,
    intentQueries = emptyList(),
    modelOutputs = blueflaxDonation.modelOutputsList,
    runtimeConfig =
      com.google.android.as.oss.feedback.serviceclient.RuntimeConfig(
        appBuildType = runtimeConfig.appBuildType,
        appVersion = runtimeConfig.appVersion,
        modelMetadata = runtimeConfig.modelMetadata,
        modelId = runtimeConfig.modelId,
      ),
    appId = appId,
    interactionId = interactionId,
    memoryEntities = emptyList(),
    failureReason = "",
    sourceDocuments = emptyList(),
    feedbackUiRenderingData = feedbackUiRenderingData,
    cuj = cuj,
    blueflaxCuj = blueflaxCuj,
    defaultDonationOptInL1Enabled = defaultDonationOptInL1Enabled,
    defaultDonationOptInL0Enabled = defaultDonationOptInL0Enabled,
  )
}
