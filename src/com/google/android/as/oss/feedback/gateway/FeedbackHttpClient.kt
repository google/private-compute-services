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

import com.google.android.`as`.oss.feedback.api.gateway.LogFeedbackV2Request

/**
 * Http client API that handles Feedback Https requests to APEX backend.
 *
 * Example:
 * ```
 * val feedbackRequest = logFeedbackV2Request {
 *   appId = "demo"
 *   interactionId = "test"
 *   feedbackCuj = feedbackCUJ { spoonFeedbackCuj = SpoonCUJ.SPOON_CUJ_SUNDOG_EVENT }
 *   rating = Rating.THUMB_UP
 *   positiveTags += PositiveRatingTag.COMPLETE
 *   positiveTags += PositiveRatingTag.CORRECT
 *   additionalComment = "Demo additional comment"
 *   runtimeConfig = runtimeConfig {
 *     appBuildType = "demo build type"
 *     appVersion = "demo app version"
 *     modelMetadata = "demo model metadata"
 *     modelId = "demo model"
 *   }
 *   donationOption = UserDataDonationOption.OPT_IN
 *   userDonation = userDonation {
 *     structuredDataDonation = spoonFeedbackDataDonation {
 *       triggeringMessages += "triggering message"
 *       triggeringMessages += "triggering message 2"
 *       intentQueries += "intent query"
 *       modelOutputs += "model output 1"
 *       modelOutputs += "model output 2"
 *       modelOutputs += "model output 3"
 *       memoryEntities += memoryEntity {
 *       entityData = "entity data 1"
 *       modelVersion = "model version 1"
 *     }
 *   }
 * }
 *
 * scope.launch {
 *   feedbackHttpClient.uploadFeedback(feedbackRequest)
 * }
 * ```
 */
interface FeedbackHttpClient {
  fun uploadFeedback(request: LogFeedbackV2Request): Boolean
}
