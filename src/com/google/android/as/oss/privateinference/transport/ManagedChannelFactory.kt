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

package com.google.android.`as`.oss.privateinference.transport

import com.google.android.`as`.oss.privateinference.service.api.proto.IpBlindingMode
import com.google.android.`as`.oss.privateinference.service.api.proto.SessionConfiguration
import io.grpc.ManagedChannel

/**
 * Factory which asynchronously creates instances of grpc [ManagedChannel] for use during Private
 * Inference.
 */
interface ManagedChannelFactory {
  /**
   * Gets a [ManagedChannel] instance to use when calling Private Inference services. The instance
   * is singleton per [SessionConfiguration].
   *
   * The [SessionConfiguration] is used to configure the PI session, i.e. the Noise session and
   * subsequent auth request.
   */
  suspend fun getInstance(
    sessionConfiguration: SessionConfiguration = DEFAULT_SESSION_CONFIGURATION
  ): ManagedChannel

  companion object {
    val DEFAULT_SESSION_CONFIGURATION =
      SessionConfiguration.newBuilder()
        .setIpBlindingMode(IpBlindingMode.IP_BLINDING_MODE_ENABLED)
        .build()
  }
}
