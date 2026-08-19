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
import com.google.android.`as`.oss.feedback.api.gateway.BlueflaxCUJ
import com.google.android.`as`.oss.feedback.api.gateway.FeedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.MemoryEntity
import com.google.android.`as`.oss.feedback.api.gateway.MessageArmourCUJ
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.Rating
import com.google.android.`as`.oss.feedback.api.gateway.RuntimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.SpoonFeedbackDataDonation
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.blueflax.utils.BlueflaxDataHelper
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
  private val blueflaxDataHelper: BlueflaxDataHelper,
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
      } else if (request.feedbackCuj.blueflaxCuj != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED) {
        usageLogFeatureName = FEATURE_NAME_FEEDBACK_BLUEFLAX
        with(blueflaxDataHelper) { request.convertToBlueflaxRequestString() }
      } else {
        usageLogFeatureName = FEATURE_NAME_FEEDBACK_PSI
        request.convertToRequestString()
      }

    if (
      !networkUsageLogRepository.isKnownConnection(
        ConnectionType.FEEDBACK_REQUEST,
        usageLogFeatureName,
      )
    ) {
      logger.atInfo().log("Feedback upload request rejected as connection is not known")
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
      val responseBody = response.body?.string() // Read body once
      insertNetworkUsageLogRow(
        networkUsageLogRepository,
        usageLogFeatureName,
        if (response.isSuccessful) Status.SUCCEEDED else Status.FAILED,
        responseBody?.toByteArray()?.size?.toLong() ?: 0L,
      )

      if (response.isSuccessful) {
        logger.atInfo().log("APEX server call successful")
        return true
      } else {
        logger.atWarning().log("APEX server call failed with response: %s", response.toString())
        logger.atWarning().log("Response body: %s", responseBody)
      }
    } catch (e: IOException) {
      logger.atSevere().withCause(e).log("APEX server call failed with exception")
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
    const val FEATURE_NAME_FEEDBACK_BLUEFLAX = "feedback_apex_blueflax"
    val JSON_MEDIA_TYPE = "$JSON_CONTENT_TYPE; charset=utf-8".toMediaType()
  }
}

/** Converts [LogFeedbackV2Request] to a Json-like string that can be parsed by the APEX service. */
@VisibleForTesting
fun LogFeedbackV2Request.convertToRequestString(): String {
  val mainParts =
    buildList<String> {
      add("${quote("appId")}: ${quote(appId)}")
      add("${quote("interactionId")}: ${quote(interactionId)}")
      add("${quote("donationOption")}: ${quote(donationOption.name)}")
      add("${quote("appCujType")}: ${getCujTypeString(feedbackCuj)}")
      add("${quote("runtimeConfig")}: ${getRuntimeConfigString(runtimeConfig)}")

      if (rating == Rating.THUMB_UP && positiveTagsList.isNotEmpty()) {
        add("${quote("positiveTags")}: ${buildRepeatedMessages(positiveTagsList.map { it.name })}")
      }

      if (rating == Rating.THUMB_DOWN && negativeTagsList.isNotEmpty()) {
        add("${quote("negativeTags")}: ${buildRepeatedMessages(negativeTagsList.map { it.name })}")
      }

      if (donationOption == UserDataDonationOption.OPT_IN) {
        add(getDonationDataString(userDonation.structuredDataDonation))
      }

      add(
        getUserInputString(
          structuredUserInput.spoonUserInput.groundTruthListList,
          structuredUserInput.spoonUserInput.optionalSpoonComment,
        )
      )
      add("${quote("feedbackRating")}: {${quote("binaryRating")}: ${quote(rating.name)}}")
      add("${quote("additionalComment")}: ${quote(additionalComment)}")
    }

  return "{${mainParts.joinToString(", ")}}"
}

private fun getUserInputString(groundTruthList: List<String>, additionalComment: String): String {
  val parts =
    buildList<String> {
      add("${quote("optionalSpoonComment")}: ${quote(additionalComment)}")
      if (groundTruthList.isNotEmpty()) {
        add("${quote("groundTruthList")}: ${buildGroundTruth(groundTruthList)}")
      }
    }
  return "${quote("structuredUserInput")}: {${quote("pixelSpoonUserInput")}: {${parts.joinToString(", ")}}}"
}

private fun getDonationDataString(structuredDataDonation: SpoonFeedbackDataDonation): String {
  val donatedMemoryEntities: List<MemoryEntity>
  val donatedL0Entries: List<String>
  if (structuredDataDonation.sourceDocumentsList.isNotEmpty()) {
    // Client is using the fine-grained structure, map to server proto fields
    donatedMemoryEntities =
      structuredDataDonation.sourceDocumentsList
        .filter { it.hasMemoryEntity() }
        .map { it.memoryEntity }
    donatedL0Entries =
      structuredDataDonation.sourceDocumentsList.map { it.l0Content }.filter { it.isNotEmpty() }
  } else {
    // Fallback for client not using fine-grained structure
    donatedMemoryEntities = structuredDataDonation.memoryEntitiesList
    donatedL0Entries = emptyList()
  }

  val parts =
    buildList<String> {
      if (structuredDataDonation.triggeringMessagesList.isNotEmpty()) {
        add(
          "${quote("triggeringMessages")}: " +
            buildRepeatedMessages(structuredDataDonation.triggeringMessagesList)
        )
      }
      val nonEmptyIntentQueries =
        structuredDataDonation.intentQueriesList.filter { it.isNotEmpty() }
      if (nonEmptyIntentQueries.isNotEmpty()) {
        add("${quote("intentQueries")}: " + buildRepeatedMessages(nonEmptyIntentQueries))
      }
      if (structuredDataDonation.modelOutputsList.isNotEmpty()) {
        add(
          "${quote("modelOutputs")}: " +
            buildRepeatedMessages(structuredDataDonation.modelOutputsList)
        )
      }

      if (structuredDataDonation.selectedEntityContent.isNotEmpty()) {
        add(
          "${quote("selectedEntityContent")}: " +
            quote(structuredDataDonation.selectedEntityContent)
        )
      }

      if (donatedMemoryEntities.isNotEmpty()) {
        add("${quote("memoryEntities")}: " + buildMemoryEntities(donatedMemoryEntities))
      }

      if (donatedL0Entries.isNotEmpty()) {
        add("${quote("l0Entries")}: " + buildRepeatedMessages(donatedL0Entries))
      }

      if (structuredDataDonation.failureReason.isNotEmpty()) {
        add("${quote("failureReason")}: " + quote(structuredDataDonation.failureReason))
      }
    }

  val donationContent = if (parts.isEmpty()) "{}" else "{${parts.joinToString(", ")}}"

  return "${quote("userDonation")}: " +
    "{${quote("structuredDataDonation")}: " +
    "{${quote("pixelSpoonDonation")}: $donationContent}}"
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
