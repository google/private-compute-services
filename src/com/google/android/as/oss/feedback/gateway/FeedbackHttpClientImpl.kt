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

package com.google.android.`as`.oss.feedback.gateway

import android.content.Context
import com.google.android.`as`.oss.feedback.api.gateway.FeedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.MemoryEntity
import com.google.android.`as`.oss.feedback.api.gateway.MessageArmourCUJ
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.RuntimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.UserDonation
import com.google.android.`as`.oss.feedback.messagearmour.utils.MessageArmourDataHelper
import com.google.android.`as`.oss.feedback.quartz.utils.QuartzDataHelper
import com.google.android.`as`.oss.networkusage.db.ConnectionDetails.ConnectionType
import com.google.android.`as`.oss.networkusage.db.NetworkUsageLogRepository
import com.google.android.`as`.oss.networkusage.db.NetworkUsageLogUtils
import com.google.android.`as`.oss.networkusage.db.Status
import com.google.common.annotations.VisibleForTesting
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

/** Http client implementation that handles Feedback Https requests to APEX backend. */
class FeedbackHttpClientImpl
@Inject
internal constructor(
  private val quartzDataHelper: QuartzDataHelper,
  private val messageArmourDataHelper: MessageArmourDataHelper,
  private val networkUsageLogRepository: NetworkUsageLogRepository,
  @ApplicationContext private val context: Context,
) : FeedbackHttpClient {

  /** Uploads the survey results to server. */
  override fun uploadFeedback(request: LogFeedbackV2Request): Boolean {
    val usageLogFeatureName: String
    val bodyJsonString =
      if (request.feedbackCuj.quartzCuj != QuartzCUJ.QUARTZ_CUJ_UNSPECIFIED) {
        usageLogFeatureName = FEATURE_NAME_FEEDBACK_ASI
        with(quartzDataHelper) { request.convertToQuartzRequestString() }
      } else if (
        request.feedbackCuj.messageArmourCuj != MessageArmourCUJ.MESSAGE_ARMOUR_CUJ_UNSPECIFIED
      ) {
        usageLogFeatureName = FEATURE_NAME_FEEDBACK_ASI
        with(messageArmourDataHelper) { request.convertToMessageArmourRequestString() }
      } else {
        usageLogFeatureName = FEATURE_NAME_FEEDBACK_PSI
        request.convertToRequestString()
      }

    if (
      !networkUsageLogRepository.isKnownConnection(
        ConnectionType.FEEDBACK_REQUEST,
        FEATURE_NAME_FEEDBACK_PSI,
      ) ||
        networkUsageLogRepository.shouldRejectRequest(
          ConnectionType.FEEDBACK_REQUEST,
          FEATURE_NAME_FEEDBACK_ASI,
        )
    ) {
      logger.atInfo().log("Feedback upload request rejected or connection is not known")
      return false
    }
    val client = OkHttpClient.Builder().build()

    val okRequest =
      Request.Builder()
        .url(APEX_SERVICE_URL)
        .addHeader("Content-Type", JSON_CONTENT_TYPE)
        .addHeader("X-Android-Cert", getCertFingerprint(context) ?: "")
        .addHeader("X-Android-Package", context.packageName)
        .post(bodyJsonString.toRequestBody(JSON_MEDIA_TYPE))
        .build()

    try {
      val response = client.newCall(okRequest).execute()
      val responseBody = response.body
      insertNetworkUsageLogRow(
        networkUsageLogRepository,
        usageLogFeatureName,
        if (responseBody != null) Status.SUCCEEDED else Status.FAILED,
        responseBody?.bytes()?.size?.toLong() ?: 0L,
      )

      if (response.isSuccessful) {
        logger.atInfo().log("APEX server call successful")
        return true
      } else {
        logger.atInfo().log("APEX server call failed with response: %s", response.toString())
      }
    } catch (e: IOException) {
      logger.atSevere().log("APEX server call failed with exception: %s", e.stackTraceToString())
    }
    return false
  }

  private fun insertNetworkUsageLogRow(
    networkUsageLogRepository: NetworkUsageLogRepository,
    featureName: String,
    status: Status,
    size: Long,
  ) {
    if (
      !networkUsageLogRepository.shouldLogNetworkUsage(
        ConnectionType.FEEDBACK_REQUEST,
        featureName,
      ) || networkUsageLogRepository.contentMap.isEmpty()
    ) {
      logger.atInfo().log("Should not log network usage")
      return
    }

    val connectionDetails =
      networkUsageLogRepository.contentMap.get().getFeedbackConnectionDetails(featureName).get()
    val entity =
      NetworkUsageLogUtils.createFeedbackNetworkUsageEntity(
        connectionDetails,
        status,
        size,
        featureName,
      )

    networkUsageLogRepository.insertNetworkUsageEntity(entity)
    logger.atInfo().log("Inserted network usage log row with size: %s", size)
  }

  private companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    const val JSON_CONTENT_TYPE = "application/json"
    const val API_KEY = ""
    const val APEX_SERVICE_URL = ""
    const val FEATURE_NAME_FEEDBACK_PSI = "feedback_apex_psi"
    const val FEATURE_NAME_FEEDBACK_ASI = "feedback_apex_asi"
    val JSON_MEDIA_TYPE = "$JSON_CONTENT_TYPE; charset=utf-8".toMediaType()
  }
}

/** Converts [LogFeedbackV2Request] to a Json-like string that can be parsed by the APEX service. */
@VisibleForTesting
fun LogFeedbackV2Request.convertToRequestString(): String {
  var finalString: String =
    "{" +
      "${quote("appId")}: ${quote(appId)}, " +
      "${quote("interactionId")}: ${quote(interactionId)}, " +
      "${quote("donationOption")}: ${quote(donationOption.name)}, " +
      "${quote("appCujType")}: ${getCujTypeString(feedbackCuj)}, " +
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

  if (rating == Rating.THUMB_DOWN) {
    finalString = finalString.plus(", ${quote("negativeTags")}: [")
    for (i in 0 until negativeTagsList.size) {
      finalString = finalString.plus(quote(negativeTagsList[i].name))
      // Add comma between tags.
      if (i < negativeTagsList.size - 1) finalString = finalString.plus(", ")
    }
    finalString = finalString.plus("]")
  }

  if (donationOption == UserDataDonationOption.OPT_IN) {
    finalString = finalString.plus(getDonationDataString(userDonation))
  }

  finalString =
    finalString.plus(getUserInputString(structuredUserInput.spoonUserInput.groundTruthListList))

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

private fun getUserInputString(groundTruthList: List<String>): String {
  var inputString = ""
  inputString =
    inputString.plus(
      ", ${quote("structuredUserInput")}: {" +
        "${quote("pixelSpoonUserInput")}: {" +
        if (groundTruthList.isNotEmpty()) {
          "${quote("groundTruthList")}: " + buildGroundTruth(groundTruthList)
        } else {
          ""
        } +
        "}}"
    )

  return inputString
}

private fun getDonationDataString(userDonation: UserDonation): String {
  var donationString = ""
  donationString =
    donationString.plus(
      ", ${quote("userDonation")}: " +
        "{${quote("structuredDataDonation")}: " +
        "{${quote("pixelSpoonDonation")}: " +
        "{${quote("triggeringMessages")}: " +
        buildRepeatedMessages(userDonation.structuredDataDonation.triggeringMessagesList) +
        ", " +
        "${quote("intentQueries")}: " +
        buildRepeatedMessages(userDonation.structuredDataDonation.intentQueriesList) +
        ", " +
        "${quote("modelOutputs")}: " +
        buildRepeatedMessages(userDonation.structuredDataDonation.modelOutputsList) +
        if (userDonation.structuredDataDonation.memoryEntitiesList.isNotEmpty()) {
          ", " +
            "${quote("memoryEntities")}: " +
            buildMemoryEntities(userDonation.structuredDataDonation.memoryEntitiesList)
        } else {
          ""
        } +
        if (userDonation.structuredDataDonation.selectedEntityContent.isNotEmpty()) {
          ", " +
            "${quote("selectedEntityContent")}: " +
            quote(userDonation.structuredDataDonation.selectedEntityContent)
        } else {
          ""
        } +
        "}}"
    )

  donationString = donationString.plus("}")
  return donationString
}

private fun getRuntimeConfigString(config: RuntimeConfig): String {
  return "{" +
    "${quote("appBuildType")}: ${quote(config.appBuildType)}, " +
    "${quote("appVersion")}: ${quote(config.appVersion)}, " +
    "${quote("modelMetadata")}: ${quote(config.modelMetadata)}, " +
    "${quote("modelId")}: ${quote(config.modelId)}" +
    "}"
}

private fun buildRepeatedMessages(messages: List<String>): String {
  return "[${messages.map { quote(it) }.joinToString(", ")}]"
}

private fun buildMemoryEntities(entities: List<MemoryEntity>): String {
  return "[${ entities.map {"{${quote("entityData")}: ${quote(it.entityData)}, "+
  "${quote("modelVersion")}: ${quote(it.modelVersion)}}" }.joinToString(", ")}]"
}

private fun buildGroundTruth(groundTruthList: List<String>): String {
  return "[${groundTruthList.map { quote(it) }.joinToString(", ")}]"
}

private fun getCujTypeString(appCujType: FeedbackCUJ): String {
  return "{${quote("pixelSpoonCujType")}: " +
    "{${quote("pixelSpoonCuj")}: ${quote(appCujType.spoonFeedbackCuj.name)}}}"
}

private fun quote(content: Any): String = "\"$content\""
