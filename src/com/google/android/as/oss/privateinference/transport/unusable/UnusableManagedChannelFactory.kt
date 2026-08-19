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

package com.google.android.`as`.oss.privateinference.transport.unusable

import com.google.android.`as`.oss.privateinference.service.api.proto.SessionConfiguration
import com.google.android.`as`.oss.privateinference.transport.ManagedChannelFactory
import io.grpc.ManagedChannel
import javax.inject.Inject
import javax.inject.Singleton

/** A [ManagedChannelFactory] that returns an [UnusableManagedChannel]. */
@Singleton
class UnusableManagedChannelFactory @Inject constructor() : ManagedChannelFactory {

  private val channel =
    UnusableManagedChannel("Transport is unusable (explicitly configured for failure).")

  override suspend fun getInstance(sessionConfiguration: SessionConfiguration): ManagedChannel =
    channel
}
