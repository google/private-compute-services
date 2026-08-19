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
import com.google.android.apps.pixel.psi.service.AmbientDataParcelables
import com.google.android.apps.pixel.psi.service.TakeScreenshotRequest
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.supericon.aidl.ConversationData
import com.google.android.`as`.oss.supericon.aidl.IConversationContentCallback
import com.google.android.`as`.oss.supericon.aidl.ISuperIconRenderCallback
import com.google.android.`as`.oss.supericon.config.SuperIconConfig
import com.google.android.`as`.oss.supericon.utils.SuperIconErrorCodes
import com.google.android.libraries.pixel.psi.ambientdata.AmbientDataClient
import com.google.common.flogger.android.AndroidFluentLogger
import io.grpc.Metadata
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/** Helper class to manage the IConversationContentCallback binder connection. */
internal class ConversationContentCallbackHelper
@Inject
constructor(
  private val connectionFactory: ConversationContentConnectionFactory,
  private val consentManager: SuperIconConsentManager,
  private val configReader: ConfigReader<SuperIconConfig>,
  private val ambientDataClient: AmbientDataClient,
) {

  suspend fun awaitCallback(
    context: Context,
    backgroundScope: CoroutineScope,
    clientCallback: ISuperIconRenderCallback,
    consentVersion: Long = 0L,
  ): ConversationData {
    return if (configReader.config.enableScreenshot) {
      logger.atInfo().log("Using awaitCallbackV2 (Asynchronous dynamic screenshots)")
      awaitCallbackV2(context, backgroundScope, clientCallback, consentVersion)
    } else {
      logger.atInfo().log("Using awaitCallbackV1 (Synchronous baseline content)")
      awaitCallbackV1(context, backgroundScope)
    }
  }

  // =========================================================================================
  // V2 LOGIC (Asynchronous Screenshot + Content)
  // =========================================================================================

  private suspend fun awaitCallbackV2(
    context: Context,
    backgroundScope: CoroutineScope,
    clientCallback: ISuperIconRenderCallback,
    consentVersion: Long,
  ): ConversationData {
    val isConsentGranted = consentManager.hasGrantedConsent(consentVersion)
    val stateManager = V2OperationStateManager(isConsentGranted)

    if (isConsentGranted) {
      val job = backgroundScope.launch {
        fetchAndSendScreenshotSafely(stateManager, clientCallback)
      }
      stateManager.setScreenshotJob(job)
    }

    return try {
      withTimeoutOrNull(CALLBACK_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
          val callback = createV2Callback(continuation, stateManager)
          val connection = connectionFactory.create(context, backgroundScope, callback)

          stateManager.setConnection(connection)
        }
      }
        ?: run {
          logger.atSevere().log("Callback timed out after %d ms", CALLBACK_TIMEOUT_MS)
          // Mark data ready allows the screenshot job to keep running in the background safely
          stateManager.markDataReady()
          EMPTY_CONVERSATION_DATA
        }
    } catch (e: CancellationException) {
      logger.atInfo().log("awaitCallback V2 cancelled, closing connection")
      // Explicit cancellation kills everything immediately
      stateManager.closeImmediately()
      throw e
    } catch (e: Exception) {
      // Catches crashes specifically from connectionFactory.create()
      logger.atSevere().withCause(e).log("Failed to create conversation callback connection")
      stateManager.closeImmediately()
      EMPTY_CONVERSATION_DATA
    }
  }

  private suspend fun fetchAndSendScreenshotSafely(
    stateManager: V2OperationStateManager,
    clientCallback: ISuperIconRenderCallback,
  ) {
    try {
      withTimeoutOrNull(configReader.config.screenshotTimeoutMs) {
        val headerCapture = AtomicReference<Metadata>()
        logger.atInfo().log("Calling AmbientDataClient.takeScreenshot")

        val response =
          ambientDataClient.takeScreenshot(
            TakeScreenshotRequest.getDefaultInstance(),
            headerCapture,
          )

        if (response.success) {
          val screenshot =
            headerCapture.get()?.get(AmbientDataParcelables.SCREENSHOT_KEY.metadataKey)
          if (screenshot != null) {
            logger.atInfo().log("Screenshot fetched successfully via AmbientDataClient")
            // Only send the screenshot to the client if the overall operation hasn't been cancelled
            if (!stateManager.isClosed()) {
              clientCallback.onScreenshotReceived(screenshot)
            }
          } else {
            logger.atWarning().log("Screenshot is null in headers")
          }
        } else {
          logger.atWarning().log("Failed to take screenshot: %s", response.errorMessage)
        }
      } ?: logger.atWarning().log("Screenshot fetch timed out")
    } catch (e: CancellationException) {
      throw e // Let the coroutine cancel properly
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Error taking screenshot")
    } finally {
      stateManager.markScreenshotDone()
    }
  }

  private fun createV2Callback(
    continuation: CancellableContinuation<ConversationData>,
    stateManager: V2OperationStateManager,
  ) =
    object : IConversationContentCallback.Stub() {

      override fun onResponse(conversationData: ConversationData) {
        logger.atInfo().log("IConversationContentCallback.onResponse")
        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(conversationData)
        stateManager.markDataReady()
      }

      override fun onError(@SuperIconErrorCodes errorCode: Int, errorMessage: String) {
        if (errorCode == SuperIconErrorCodes.EMPTY_SCREEN_CONTENT) {
          logger
            .atWarning()
            .log("IConversationContentCallback.onError %d %s", errorCode, errorMessage)
        } else {
          logger
            .atSevere()
            .log("IConversationContentCallback.onError %d %s", errorCode, errorMessage)
        }

        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(EMPTY_CONVERSATION_DATA)

        if (errorCode == SuperIconErrorCodes.EMPTY_SCREEN_CONTENT) {
          stateManager.markDataReady() // Naturally waits for screenshot to finish
        } else {
          stateManager.closeImmediately()
        }
      }

      override fun onScreenshotResponse(screenshot: Bitmap) {
        logger.atInfo().log("IConversationContentCallback.onScreenshotResponse (ignored)")
      }
    }

  /**
   * Encapsulates the concurrent lifecycle of the V2 Binder connection and the Screenshot Job.
   * Guarantees thread-safe transitions and centralized cleanup.
   */
  private class V2OperationStateManager(isConsentGranted: Boolean) {
    private val lock = Any()
    private var connection: AutoCloseable? = null
    private var screenshotJob: Job? = null

    private var isDataReady = false
    private var isScreenshotDone = !isConsentGranted
    private var isClosedInternal = false

    fun isClosed(): Boolean = synchronized(lock) { isClosedInternal }

    fun setConnection(c: AutoCloseable) {
      val toClose =
        synchronized(lock) {
          if (isClosedInternal) {
            c
          } else {
            connection = c
            null
          }
        }
      toClose?.close()
    }

    fun setScreenshotJob(job: Job) {
      val toCancel =
        synchronized(lock) {
          if (isClosedInternal) {
            job
          } else {
            screenshotJob = job
            null
          }
        }
      toCancel?.cancel()
    }

    fun markDataReady() {
      val toClose =
        synchronized(lock) {
          isDataReady = true
          checkCleanupUnsafe()
        }
      toClose?.let {
        logger.atInfo().log("Closing connection (V2)")
        it.close()
      }
    }

    fun markScreenshotDone() {
      val toClose =
        synchronized(lock) {
          isScreenshotDone = true
          checkCleanupUnsafe()
        }
      toClose?.let {
        logger.atInfo().log("Closing connection (V2)")
        it.close()
      }
    }

    fun closeImmediately() {
      var connectionToClose: AutoCloseable? = null
      var jobToCancel: Job? = null

      synchronized(lock) {
        if (isClosedInternal) return
        isClosedInternal = true

        connectionToClose = connection
        jobToCancel = screenshotJob
        connection = null
        screenshotJob = null
      }

      connectionToClose?.let {
        logger.atInfo().log("Closing connection (V2)")
        it.close()
      }
      jobToCancel?.let {
        logger.atInfo().log("Cancelling screenshot job (V2)")
        it.cancel()
      }
    }

    // Must only be called inside synchronized(lock)
    private fun checkCleanupUnsafe(): AutoCloseable? {
      if (isDataReady && isScreenshotDone && !isClosedInternal) {
        isClosedInternal = true
        val conn = connection
        connection = null
        // We intentionally don't cancel the job here because
        // isScreenshotDone implies it naturally finished.
        return conn
      }
      return null
    }
  }

  // =========================================================================================
  // V1 LOGIC (Synchronous Baseline Content)
  // =========================================================================================

  private suspend fun awaitCallbackV1(
    context: Context,
    backgroundScope: CoroutineScope,
  ): ConversationData {
    var connection: AutoCloseable? = null

    return try {
      withTimeoutOrNull(CALLBACK_TIMEOUT_MS) {
        suspendCancellableCoroutine { continuation ->
          connection =
            connectionFactory.create(context, backgroundScope, createV1Callback(continuation))
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

  private fun createV1Callback(continuation: CancellableContinuation<ConversationData>) =
    object : IConversationContentCallback.Stub() {

      override fun onResponse(conversationData: ConversationData) {
        logger.atInfo().log("IConversationContentCallback.onResponse (V1)")
        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(conversationData)
      }

      override fun onError(@SuperIconErrorCodes errorCode: Int, errorMessage: String) {
        if (errorCode == SuperIconErrorCodes.EMPTY_SCREEN_CONTENT) {
          logger
            .atWarning()
            .log("IConversationContentCallback.onError (V1) %d %s", errorCode, errorMessage)
        } else {
          logger
            .atSevere()
            .log("IConversationContentCallback.onError (V1) %d %s", errorCode, errorMessage)
        }
        // Prevent "Already resumed" crash if timeout already occurred
        if (continuation.isActive) continuation.resume(EMPTY_CONVERSATION_DATA)
      }

      override fun onScreenshotResponse(screenshot: Bitmap) {
        logger.atInfo().log("IConversationContentCallback.onScreenshotResponse (V1, ignored)")
      }
    }

  private companion object {
    val logger: AndroidFluentLogger = AndroidFluentLogger.create("PcsSuperIcon")
    const val CALLBACK_TIMEOUT_MS = 1000L
    val EMPTY_CONVERSATION_DATA = ConversationData(listOf(), packageName = "")
  }
}
