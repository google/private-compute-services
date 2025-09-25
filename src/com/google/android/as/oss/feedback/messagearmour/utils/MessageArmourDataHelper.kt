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

package com.google.android.`as`.oss.feedback.messagearmour.utils

import com.google.android.`as`.oss.feedback.api.gateway.FeedbackCUJ
import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request
import com.google.android.`as`.oss.feedback.api.gateway.MessageArmourCUJ
import com.google.android.`as`.oss.feedback.api.gateway.RuntimeConfig
import com.google.android.`as`.oss.feedback.api.gateway.UserDataDonationOption
import com.google.android.`as`.oss.feedback.api.gateway.UserDonation
import com.google.common.flogger.GoogleLogger
import javax.inject.Inject
import javax.inject.Singleton

/** Helper class for Message Armour feedback data. */
@Singleton
class MessageArmourDataHelper @Inject constructor() {

  /**
   * Converts [LogFeedbackV2Request] to a Json-like string that can be parsed by the APEX service.
   */
  fun LogFeedbackV2Request.convertToMessageArmourRequestString(): String {
    logger
      .atInfo()
      .log(
        "MessageArmourDataHelper#convertToMessageArmourRequestString interactionId: %s",
        interactionId,
      )

    var finalString: String =
      "{" +
        "${quote("appId")}: ${quote(appId)}, " +
        "${quote("interactionId")}: ${quote(interactionId)}, " +
        "${quote("donationOption")}: ${quote(donationOption.name)}, " +
        "${quote("appCujType")}: ${getMessageArmourCujTypeString(feedbackCuj)}, " +
        "${quote("runtimeConfig")}: ${getRuntimeConfigString(runtimeConfig)}"

    if (donationOption == UserDataDonationOption.OPT_IN) {
      finalString = finalString.plus(getDonationDataString(userDonation, feedbackCuj))
    }

    finalString =
      finalString.plus(
        ", ${quote("feedbackRating")}: {${quote("binaryRating")}: ${quote(rating.name)}}"
      )

    finalString = finalString.plus("}")
    return finalString
  }

  private fun getMessageArmourCujTypeString(appCujType: FeedbackCUJ): String =
    "{${quote("messageArmourCujType")}: " +
      "{${quote("messageArmourCuj")}: ${quote(appCujType.messageArmourCuj.name)}}}"

  private fun getRuntimeConfigString(config: RuntimeConfig): String =
    "{" +
      "${quote("appVersion")}: ${quote(config.appVersion)}, " +
      "${quote("modelId")}: ${quote(config.modelId)}" +
      "}"

  private fun getDonationDataString(userDonation: UserDonation, feedbackCuj: FeedbackCUJ): String {
    var donationString = ""
    if (feedbackCuj.messageArmourCuj.equals(MessageArmourCUJ.MESSAGE_ARMOUR_CUJ_SCAM_DETECTION)) {
      donationString =
        donationString.plus(
          ", ${quote("userDonation")}: " +
            "{${quote("structuredDataDonation")}: " +
            "{${quote("messageArmourDataDonation")}: " +
            "{${quote("messageArmourUserDataDonation")}: " +
            "{${quote("userDonatedMessage")}: ${quote(userDonation.messageArmourDataDonation.messageArmourUserDataDonation.userDonatedMessage)}" +
            "}}}"
        )
      donationString = donationString.plus("}")
    }

    return donationString
  }

  private fun quote(content: Any): String {
    val escapedContent = content.toString().replace("\"", "\\\"")
    return "\"$escapedContent\""
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
  }
}
