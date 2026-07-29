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

package com.google.android.`as`.oss.supericon.service

import android.content.Context
import android.graphics.Bitmap
import com.google.android.`as`.oss.supericon.aidl.ConversationData
import com.google.android.`as`.oss.supericon.aidl.IConversationContentCallback
import com.google.android.`as`.oss.supericon.utils.SuperIconErrorCodes
import com.google.common.flogger.android.AndroidFluentLogger
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Helper to await conversation content callback from the service. */
class ConversationContentCallbackHelper
@Inject
constructor(private val connectionFactory: ConversationContentConnectionFactory) {

  suspend fun awaitCallback(context: Context, backgroundScope: CoroutineScope): ConversationData {
    var connection: AutoCloseable? = null

    return try {
      withTimeoutOrNull(CALLBACK_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
          connection =
            connectionFactory.create(context, backgroundScope, createCallback(continuation))
        }
      }
        ?: run {
          logger.atSevere().log("Callback timed out after %d ms", CALLBACK_TIMEOUT_MS)
          EMPTY_CONVERSATION_DATA
        }
    } catch (e: CancellationException) {
      // It's normal for coroutines to be canceled (e.g., app goes to background).
      // We MUST rethrow this to respect structured concurrency, but the `finally` block will still
      // safely run and close the connection.
      throw e
    } catch (e: Exception) {
      // This catches crashes specifically from connectionFactory.create()
      logger.atSevere().withCause(e).log("Failed to create conversation callback connection")
      EMPTY_CONVERSATION_DATA
    } finally {
      // Guaranteed to clean up safely in all scenarios (success, timeout, error, or cancellation)
      connection?.close()
    }
  }

  private fun createCallback(continuation: CancellableContinuation<ConversationData>) =
    object : IConversationContentCallback.Stub() {

      override fun onResponse(conversationData: ConversationData) {
        logger.atInfo().log("IConversationContentCallback.onResponse")
        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(conversationData)
      }

      override fun onError(@SuperIconErrorCodes errorCode: Int, errorMessage: String) {
        logger.atSevere().log("IConversationContentCallback.onError %d %s", errorCode, errorMessage)
        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(EMPTY_CONVERSATION_DATA)
      }

      override fun onScreenshotResponse(screenshot: Bitmap) {
        logger.atInfo().log("IConversationContentCallback.onScreenshotResponse")
      }
    }

  private companion object {
    val logger: AndroidFluentLogger = AndroidFluentLogger.create("PcsSuperIcon")
    const val CALLBACK_TIMEOUT_MS = 1000L
    val EMPTY_CONVERSATION_DATA = ConversationData(listOf(), packageName = "")
  }
}
