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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import androidx.annotation.ChecksSdkIntAtLeast
import com.google.android.`as`.oss.supericon.aidl.IConversationContentCallback
import com.google.android.`as`.oss.supericon.aidl.IConversationContentService
import com.google.android.`as`.oss.supericon.utils.SuperIconErrorCodes
import com.google.common.flogger.android.AndroidFluentLogger
import kotlin.math.pow
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Manages the connection to the ConversationContentService.
 *
 * This class handles binding, unbinding, and rebinding to the service, including exponential
 * backoff on unexpected disconnections.
 *
 * @property context The application context.
 * @property scope The coroutine scope to launch rebinding jobs in.
 * @property callback The callback to receive conversation content updates.
 */
class ConversationContentConnection(
  private val context: Context,
  private val scope: CoroutineScope,
  private val callback: IConversationContentCallback,
  private val packageName: String = "",
  private val enableConversationContentV2: Boolean = false,
) : AutoCloseable, ServiceConnection {
  private var conversationContentService: IConversationContentService? = null
    @Synchronized set
    @Synchronized get

  private var delayedRebind: Job? = null
    @Synchronized set
    @Synchronized get

  private var rebindAttempts = 0
    @Synchronized set
    @Synchronized get

  private var isBound = false
    @Synchronized set
    @Synchronized get

  init {
    logger.atVerbose().log("ConversationContentConnection initialize")
    bind()
  }

  /** Closes the connection and unbinds from the service. */
  override fun close() {
    delayedRebind?.cancel()
    delayedRebind = null
    unbind()
  }

  /** Called when the service is connected. Requests conversation content. */
  override fun onServiceConnected(name: ComponentName, service: IBinder) {
    conversationContentService = IConversationContentService.Stub.asInterface(service)
    logger.atFine().log("ConversationContent service connected")
    delayedRebind?.cancel()
    delayedRebind = null
    rebindAttempts = 0
    requestConversationContentSafely()
  }

  private fun requestConversationContentSafely() {
    val service = conversationContentService ?: return
    if (enableConversationContentV2 && packageName.isNotEmpty()) {
      try {
        val success =
          service.requestConversationContentV2(
            callback,
            packageName,
            /* requestScreenshot = */ false,
          )
        if (success) {
          logger.atDebug().log("requestConversationContentV2 called successfully")
          return
        }
        logger.atInfo().log("requestConversationContentV2 returned false, falling back to V1")
      } catch (e: Exception) {
        logger
          .atWarning()
          .withCause(e)
          .log("requestConversationContentV2 failed with exception, falling back to V1")
      }
    }
    service.requestConversationContent(callback)
  }

  /** Called when the service is unexpectedly disconnected. Attempts to rebind. */
  override fun onServiceDisconnected(name: ComponentName) {
    logger.atInfo().log("disconnected")
    unbindAndRetry()
  }

  /** Called when the binding is null. Unbinds from the service. */
  override fun onNullBinding(name: ComponentName) {
    logger.atInfo().log("onNullBinding")
    unbind()
  }

  /** Called when the binding dies. Attempts to rebind. */
  override fun onBindingDied(name: ComponentName) {
    logger.atInfo().log("BindingDied")
    unbindAndRetry()
  }

  private fun bind() {
    if (isBound) {
      logger.atInfo().log("Already bound")
      return
    }
    logger.atInfo().log("bind")
    val intent = Intent()
    intent.component = CONVERSATION_CONTENT_SERVICE_NAME
    try {
      val success = context.bindService(intent, this, Context.BIND_AUTO_CREATE)
      if (success) {
        isBound = true
        logger.atInfo().log("bind succeeded: %s", CONVERSATION_CONTENT_SERVICE_NAME)
      } else {
        logger.atInfo().log("bind failed: %s", CONVERSATION_CONTENT_SERVICE_NAME)
        callback.onError(SuperIconErrorCodes.NOT_ABLE_TO_BIND, NOT_ABLE_TO_BIND_ERROR_MESSAGE)
        unbind()
      }
    } catch (e: Exception) {
      unbind()
      logger
        .atWarning()
        .withCause(e)
        .log("Unable to bind service: %s", CONVERSATION_CONTENT_SERVICE_NAME)
      callback.onError(SuperIconErrorCodes.NOT_ABLE_TO_BIND, NOT_ABLE_TO_BIND_ERROR_MESSAGE)
    }
  }

  private fun unbind() {
    conversationContentService = null
    if (isBound) {
      try {
        context.unbindService(this)
      } catch (e: IllegalArgumentException) {
        logger.atWarning().withCause(e).log("Service not registered when unbinding.")
      } finally {
        isBound = false
      }
    }
    logger.atVerbose().log("unbind")
  }

  private fun unbindAndRetry() {
    if (delayedRebind?.isCompleted == false) {
      // skip if an existing retry job is running
      return
    }
    unbind()
    if (rebindAttempts < MAX_RETRY_ATTEMPTS) {
      val delayDuration = INITIAL_DELAY * EXPONENTIAL_BACKOFF.pow(rebindAttempts).toInt()
      rebindAttempts++
      logger.atVerbose().log("retry #%s", rebindAttempts)
      delayedRebind = scope.launch {
        delay(delayDuration)
        bind()
      }
    }
  }

  companion object {

    /** Checks if the SDK version is sufficient to support this connection. */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun isSupported() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM

    private val CONVERSATION_CONTENT_SERVICE_NAME =
      ComponentName(
        "com.google.android.as",
        "com.google.android.apps.miphone.aiai.pecan.supericon.ConversationContentService",
      )
    private val INITIAL_DELAY = 3.seconds
    private const val MAX_RETRY_ATTEMPTS = 6
    private const val EXPONENTIAL_BACKOFF = 4.0

    private const val NOT_ABLE_TO_BIND_ERROR_MESSAGE = "Unable to bind service"
    private val logger = AndroidFluentLogger.create("PcsSuperIcon")
  }
}
