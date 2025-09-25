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

package com.google.android.`as`.oss.delegatedui.service.common

import kotlinx.coroutines.CoroutineScope

/** The lifecycle of a delegated UI operation. */
sealed interface DelegatedUiLifecycle {

  /**
   * When the lifecycle is tied to a streaming rpc, [streamScope] is available to do background work
   * within the lifecycle of the stream.
   */
  val streamScope: CoroutineScope?
}

/** The lifecycle of the unary [DelegatedUiServiceImpl.prepareDelegatedUiSession] rpc. */
class PrepareLifecycle : DelegatedUiLifecycle {
  override val streamScope: CoroutineScope? = null
}

/** The lifecycle of the streaming [DelegatedUiServiceImpl.connectDelegatedUiSession] rpc. */
class ConnectLifecycle(override val streamScope: CoroutineScope) : DelegatedUiLifecycle
