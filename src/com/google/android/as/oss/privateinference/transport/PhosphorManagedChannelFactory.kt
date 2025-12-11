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

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresExtension
import com.google.android.`as`.oss.privateinference.Annotations
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.ManagedChannel
import io.grpc.cronet.CronetChannelBuilder
import javax.inject.Inject
import javax.inject.Singleton
import org.chromium.net.impl.HttpEngineNativeProvider

/** Factory class to create a [ManagedChannel] for Phosphor server communication. */
@Singleton
class PhosphorManagedChannelFactory
@Inject
constructor(
  @ApplicationContext private val context: Context,
  @Annotations.TokenIssuanceEndpointUrl private val endpointUrl: String,
) {
  @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
  fun create(): ManagedChannel {
    logger.atInfo().log("Creating gRPC channel for token issuance server at %s", endpointUrl)
    Log.i(TAG, "Creating gRPC channel for token issuance server at $endpointUrl")
    val engine = HttpEngineNativeProvider(context).createBuilder().build()
    return CronetChannelBuilder.forAddress(endpointUrl, TransportConstants.HTTPS_PORT, engine)
      .maxInboundMessageSize(TransportConstants.MAX_INBOUND_MESSAGE_SIZE_BYTES)
      .build()
  }

  companion object {
    private const val TAG = "PhosphorManagedChannelFactory"
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
