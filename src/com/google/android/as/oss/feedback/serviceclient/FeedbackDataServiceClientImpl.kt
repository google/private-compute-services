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

package com.google.android.`as`.oss.feedback.serviceclient

import com.google.android.`as`.oss.feedback.api.dataservice.FeedbackDataServiceGrpcKt
import com.google.android.`as`.oss.feedback.api.dataservice.GetFeedbackDonationDataResponse
import com.google.android.`as`.oss.feedback.api.dataservice.getFeedbackDonationDataRequest
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import javax.inject.Inject

class FeedbackDataServiceClientImpl
@Inject
internal constructor(
  private val service: FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub
) : FeedbackDataServiceClient {
  override suspend fun getFeedbackDonationData(
    clientSessionId: String,
    uiElementType: Int,
    uiElementIndex: Int?,
    quartzCuj: QuartzCUJ?,
  ): Result<FeedbackDonationData> {
    return runCatching {
        val request = getFeedbackDonationDataRequest {
          this.clientSessionId = clientSessionId
          this.uiElementType = uiElementType
          uiElementIndex?.let { this.uiElementIndex = it }
          quartzCuj?.let { this.quartzCuj = it }
        }
        service.getFeedbackDonationData(request).toFeedbackDonationData()
      }
      .onFailure { e ->
        logger
          .atSevere()
          .withCause(e)
          .withStackTrace(StackSize.SMALL)
          .log("Error getting feedback donation data")
      }
  }

  private fun GetFeedbackDonationDataResponse.toFeedbackDonationData(): FeedbackDonationData {
    return FeedbackDonationData(
      triggeringMessages = donationData.structuredDataDonation.triggeringMessagesList,
      intentQueries = donationData.structuredDataDonation.intentQueriesList,
      modelOutputs = donationData.structuredDataDonation.modelOutputsList,
      runtimeConfig =
        RuntimeConfig(
          appBuildType = runtimeConfig.appBuildType,
          appVersion = runtimeConfig.appVersion,
          modelMetadata = runtimeConfig.modelMetadata,
          modelId = runtimeConfig.modelId,
        ),
      appId = appId,
      interactionId = interactionId,
      memoryEntities =
        donationData.structuredDataDonation.memoryEntitiesList.map {
          MemoryEntity(it.entityData, it.modelVersion)
        },
      feedbackUiRenderingData = feedbackUiRenderingData,
      cuj = cuj,
    )
  }

  private companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
