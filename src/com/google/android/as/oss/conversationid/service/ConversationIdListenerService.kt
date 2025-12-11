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

package com.google.android.`as`.oss.conversationid.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build.VERSION_CODES
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import androidx.annotation.RequiresApi
import com.google.android.apps.common.inject.annotation.ApplicationContext
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.conversationid.config.ConversationIdConfig
import com.google.android.`as`.oss.conversationid.service.aidl.IConversationIdListener
import com.google.android.`as`.oss.conversationid.service.aidl.IConversationIdListenerService
import com.google.android.`as`.oss.conversationid.util.ConversationIdManager
import com.google.android.`as`.oss.conversationid.util.ServiceValidator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/*
 Service to listen to anynoumous conversation id updates. This service is only used by Gboard at
 the moment. Once Gboard registers the listener, it will be notified of anynoumous conversation id
 updates.

 val conversationIdListener =
   object : IConversationIdListener.Stub() {
     override fun onEnterConversation(conversationId: String) {
       log("onEnterConversation %s", conversationId)
     }

     override fun onExitConversation() {
       log("onExitConversation")
     }
   }

 val serviceConnection =
   object : ServiceConnection {
     override fun onServiceConnected(name: ComponentName, service: IBinder) {
       conversationIdService = IConversationIdListenerService.Stub.asInterface(service)
       conversationIdService?.registerConversationIdListener(conversationIdListener)
     }

     override fun onServiceDisconnected(name: ComponentName) {
     }
   }
*/
@RequiresApi(VERSION_CODES.BAKLAVA)
@AndroidEntryPoint(Service::class)
class ConversationIdListenerService : Hilt_ConversationIdListenerService() {

  @Inject lateinit var validator: ServiceValidator
  @Inject lateinit var conversationIdManager: ConversationIdManager
  @Inject lateinit var configReader: ConfigReader<ConversationIdConfig>
  @Inject @ApplicationContext lateinit var context: Context

  override fun onBind(intent: Intent): IBinder? {
    if (!validator.isPixel()) {
      return null
    }
    return ConversationIdServiceBinderStub()
  }

  override fun onUnbind(intent: Intent): Boolean {
    conversationIdManager.resetListeners()
    return super.onUnbind(intent)
  }

  private inner class ConversationIdServiceBinderStub : IConversationIdListenerService.Stub() {
    // Currently only allow Gboard to listener to conversation id updates.
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
      if (!configReader.config.enableConversationId) {
        throw RemoteException("Conversation id feature is disabled.")
      }
      if (!validator.validateGboardCaller(context, Binder.getCallingUid())) {
        throw SecurityException("ConversationIdListenerService: Caller is not allowlisted.")
      }
      return super.onTransact(code, data, reply, flags)
    }

    override fun registerConversationIdListener(listener: IConversationIdListener) {
      conversationIdManager.addListener(listener)
    }

    override fun unregisterConversationIdListener(listener: IConversationIdListener) {
      conversationIdManager.removeListener(listener)
    }
  }
}
