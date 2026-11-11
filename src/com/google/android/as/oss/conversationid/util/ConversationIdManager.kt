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

import android.app.BroadcastOptions
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION_CODES
import android.os.DeadObjectException
import android.os.RemoteCallbackList
import android.os.RemoteException
import androidx.annotation.RequiresApi
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.conversationid.config.ConversationIdConfig
import com.google.android.`as`.oss.conversationid.service.aidl.IConversationIdListener
import com.google.common.flogger.GoogleLogger

/** Manages conversation id updates and keep track of the listeners. */
@RequiresApi(VERSION_CODES.BAKLAVA)
class ConversationIdManager(
  private val conversationIdHolder: ConversationIdHolder,
  private val context: Context,
  private val configReader: ConfigReader<ConversationIdConfig>,
) {
  // RemoteCallbackList will handle the cleanup of the client's callback
  private var listeners: RemoteCallbackList<IConversationIdListener> =
    RemoteCallbackList<IConversationIdListener>()

  @Synchronized
  fun enterConversation(conversationId: String, hashSalt: String) {
    if (conversationIdHolder.setValueWithSalt(conversationId, hashSalt)) {
      notifyEnterConversation()
      broadcast(ACTION_CONVERSATION_ENTERED, conversationIdHolder.getAnonymousId())
    }
  }

  @Synchronized
  fun exitConversation() {
    conversationIdHolder.clear()
    notifyConversationExited()
    broadcast(ACTION_CONVERSATION_EXITED, conversationId = null)
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

  private fun broadcast(action: String, conversationId: String?) {
    if (!configReader.config.enableBroadcast) {
      return
    }
    val options = BroadcastOptions.makeBasic().setShareIdentityEnabled(true).toBundle()
    for (pkg in GBOARD_PACKAGES) {
      val intent =
        Intent(action).apply {
          setPackage(pkg)
          if (conversationId != null) {
            putExtra(EXTRA_CONVERSATION_ID, conversationId)
          }
        }
      try {
        context.sendBroadcast(intent, PERMISSION_RECEIVE_CONVERSATION_ID, options)
      } catch (e: Exception) {
        logger.atWarning().withCause(e).log("Failed to broadcast %s to %s", action, pkg)
      }
    }
    logger.atInfo().log("Broadcasted %s to Gboard variants", action)
  }

  companion object {
    const val ACTION_CONVERSATION_ENTERED = "com.google.android.as.oss.ACTION_CONVERSATION_ENTERED"
    const val ACTION_CONVERSATION_EXITED = "com.google.android.as.oss.ACTION_CONVERSATION_EXITED"
    const val EXTRA_CONVERSATION_ID = "conversation_id"
    const val PERMISSION_RECEIVE_CONVERSATION_ID =
      "com.google.android.as.oss.permission.RECEIVE_CONVERSATION_ID"

    val GBOARD_PACKAGES: List<String> =
      listOf(
        "com.google.android.inputmethod.latin",
        "com.google.android.inputmethod.latin.canary",
        "com.google.android.inputmethod.latin.dev",
      )

    private val logger: GoogleLogger = GoogleLogger.forEnclosingClass()
  }
}
