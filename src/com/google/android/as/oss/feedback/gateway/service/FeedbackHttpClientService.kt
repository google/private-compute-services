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

package com.google.android.`as`.oss.feedback.gateway.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import com.google.android.apps.common.inject.annotation.ApplicationContext
import com.google.android.`as`.oss.common.Executors.GENERAL_SINGLE_THREAD_EXECUTOR
import com.google.android.`as`.oss.common.Executors.IO_EXECUTOR
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.api.PackageSecurityInfoList
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.feedback.api.gateway.logFeedbackV2Request
import com.google.android.`as`.oss.feedback.gateway.FeedbackHttpClient
import com.google.android.`as`.oss.feedback.gateway.service.aidl.IFeedbackHttpClientService
import com.google.android.`as`.oss.feedback.gateway.service.aidl.IFeedbackResultCallback
import com.google.common.flogger.GoogleLogger
import com.google.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A bound service that handles HTTP requests for feedback data.
 *
 * This service is designed to be invoked by other apps to upload feedback data via http client. It
 * uses a coroutine to perform the network request on a background thread, and communicates the
 * result back to the caller via a callback.
 */
@AndroidEntryPoint(Service::class)
class FeedbackHttpClientService : Hilt_FeedbackHttpClientService() {

  @Inject internal lateinit var feedbackHttpClient: FeedbackHttpClient
  @Inject internal lateinit var securityPolicyConfigReader: ConfigReader<PccSecurityConfig>
  @Inject @ApplicationContext internal lateinit var context: Context

  private val scope = CoroutineScope(GENERAL_SINGLE_THREAD_EXECUTOR.asCoroutineDispatcher())
  private val binder =
    object : IFeedbackHttpClientService.Stub() {

      override fun uploadFeedback(feedbackData: Bundle, callback: IFeedbackResultCallback) {
        val byteArray = feedbackData.getByteArray(LOG_FEEDBACK_V2_REQUEST)
        if (byteArray == null) {
          callback.onFailure(ILLEGAL_ARGUMENT_EXCEPTION_LOG_ERROR)
          throw IllegalArgumentException(ILLEGAL_ARGUMENT_EXCEPTION_LOG_ERROR)
        }

        val logFeedbackV2Request =
          try {
            logFeedbackV2Request {}.parserForType.parseFrom(byteArray)
          } catch (e: InvalidProtocolBufferException) {
            callback.onFailure(INVALID_PROTOCOL_BUFFER_EXCEPTION_LOG_ERROR)
            throw e
          }

        scope.launch {
          val success =
            withContext(IO_EXECUTOR.asCoroutineDispatcher()) {
              feedbackHttpClient.uploadFeedback(logFeedbackV2Request)
            }

          if (success) {
            logger.atInfo().log("FeedbackHttpClientService#uploadFeedback successful.")
            callback.onSuccess()
          } else {
            logger.atInfo().log("FeedbackHttpClientService#uploadFeedback failed.")
            callback.onFailure(API_REQUEST_FAILURE_LOG_ERROR)
          }
        }
      }

      override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
        if (
          !SecurityPolicyUtils.isCallerAuthorized(
            PackageSecurityInfoList.newBuilder()
              .addPackageSecurityInfos(securityPolicyConfigReader.config.asiPackageSecurityInfo())
              .build(),
            context,
            getCallingUid(),
            /* allowTestKeys= */ true,
          )
        ) {
          throw SecurityException("FeedbackHttpClientService: Caller is not allowlisted.")
        }

        return super.onTransact(code, data, reply, flags)
      }
    }

  override fun onBind(intent: Intent): IBinder? = binder.asBinder()

  private companion object {

    const val LOG_FEEDBACK_V2_REQUEST = "logFeedbackV2Request"
    const val ILLEGAL_ARGUMENT_EXCEPTION_LOG_ERROR = "logFeedbackV2Request not found in bundle"
    const val INVALID_PROTOCOL_BUFFER_EXCEPTION_LOG_ERROR =
      "Invalid logFeedbackV2Request sent in bundle"
    const val API_REQUEST_FAILURE_LOG_ERROR = "API request failure in FeedbackHttpClientService"

    val logger = GoogleLogger.forEnclosingClass()
  }
}
