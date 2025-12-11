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

package com.google.android.`as`.oss.conversationid.util

import android.os.Build.VERSION_CODES
import android.os.DeadObjectException
import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.annotation.RequiresApi
import com.google.android.`as`.oss.conversationid.service.aidl.IConversationIdListener
import com.google.common.flogger.GoogleLogger

/** Manages conversation id updates and keep track of the listeners. */
@RequiresApi(VERSION_CODES.BAKLAVA)
class ConversationIdManager(private val conversationIdHolder: ConversationIdHolder) {
  // RemoteCallbackList will handle the cleanup of the client's callback
  private var listeners: RemoteCallbackList<IConversationIdListener> =
    RemoteCallbackList<IConversationIdListener>()

  @Synchronized
  fun enterConversation(conversationId: String, hashSalt: String) {
    if (conversationIdHolder.setValueWithSalt(conversationId, hashSalt)) {
      notifyEnterConversation()
    }
  }

  @Synchronized
  fun exitConversation() {
    conversationIdHolder.clear()
    notifyConversationExited()
  }

  @Synchronized
  fun addListener(listener: IConversationIdListener) {
    if (listeners.register(listener)) {
      notifyEnterConversation()
      logger.atInfo().log("added listener")
    } else {
      logger.atInfo().log("add listener failed")
    }
  }

  @Synchronized
  fun removeListener(listener: IConversationIdListener) {
    listeners.unregister(listener)
  }

  @Synchronized
  fun clear() {
    conversationIdHolder.clear()
  }

  @Synchronized
  fun resetListeners() {
    listeners.kill()
    listeners = RemoteCallbackList<IConversationIdListener>()
  }

  private fun notifyEnterConversation() {
    val conversationId = conversationIdHolder.getAnonymousId() ?: return
    listeners.broadcast({ listener ->
      try {
        listener.onEnterConversation(conversationId)
      } catch (e: DeadObjectException) {
        logger.atWarning().withCause(e).log("Listener is likely dead.")
      } catch (e: RemoteException) {
        logger.atSevere().withCause(e).log("Failed to notify conversation id.")
      }
    })
    logger.atInfo().log("enter")
  }

  private fun notifyConversationExited() {
    listeners.broadcast({ listener ->
      try {
        listener.onExitConversation()
      } catch (e: DeadObjectException) {
        logger.atWarning().withCause(e).log("Listener is likely dead.")
      } catch (e: RemoteException) {
        logger.atSevere().withCause(e).log("Failed to notify conversation exited.")
      }
    })
    logger.atInfo().log("exit")
  }

  companion object {
    private val logger: GoogleLogger = GoogleLogger.forEnclosingClass()
  }
}
