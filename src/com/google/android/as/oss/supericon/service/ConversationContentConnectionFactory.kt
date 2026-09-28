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
import com.google.android.`as`.oss.supericon.aidl.IConversationContentCallback
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

open class ConversationContentConnectionFactory @Inject constructor() {
  open fun create(
    context: Context,
    scope: CoroutineScope,
    callback: IConversationContentCallback,
    packageName: String = "",
    enableConversationContentV2: Boolean = false,
  ): AutoCloseable {
    return ConversationContentConnection(
      context,
      scope,
      callback,
      packageName,
      enableConversationContentV2,
    )
  }
}
