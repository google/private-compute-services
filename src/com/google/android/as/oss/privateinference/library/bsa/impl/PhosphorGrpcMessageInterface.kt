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

package com.google.android.`as`.oss.privateinference.library.bsa.impl

import com.google.android.`as`.oss.privateinference.config.impl.DeviceInfo
import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth.MessageInterface
import com.google.android.`as`.oss.privateinference.library.bsa.proto.ArateaIPBlindingServiceGrpcKt.ArateaIPBlindingServiceCoroutineStub
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper.IPProtectionRequestType
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import com.google.common.flogger.GoogleLogger
import com.google.errorprone.annotations.ThreadSafe
import com.google.privacy.ppn.proto.AttestAndSignRequest
import com.google.privacy.ppn.proto.GetInitialDataRequest
import io.grpc.ManagedChannel
import java.util.Optional
import javax.inject.Provider
import kotlin.jvm.optionals.getOrNull

/** [MessageInterface] that uses gRPC to communicate with a Phosphor server for token issuance. */
@ThreadSafe
class PhosphorGrpcMessageInterface(
  @field:ThreadSafe.Suppress(reason = "ManagedChannel is thread-safe after creation")
  private val channel: Provider<ManagedChannel>,
  private val networkUsageLogHelper: PrivateInferenceNetworkUsageLogHelper,
  @field:ThreadSafe.Suppress(reason = "Timers are thread-safe") private val timerSet: TimerSet,
  @field:ThreadSafe.Suppress(reason = "DeviceInfo is thread-safe after creation")
  private val deviceInfo: Optional<DeviceInfo> = Optional.empty(),
) : MessageInterface {
  // AtomicReference to hold the stub, ensuring thread-safe lazy initialization.
  @delegate:ThreadSafe.Suppress(reason = "gRPC stubs are thread-safe")
  private val stub: ArateaIPBlindingServiceCoroutineStub by lazy {
    deviceInfo.getOrNull()?.let {
      ArateaIPBlindingServiceCoroutineStub(channel.get())
        .withInterceptors(
          DeviceInfo.PcsVersionInterceptor(deviceInfo.getOrNull()),
          DeviceInfo.HardwareRevisionInterceptor(deviceInfo.getOrNull()),
        )
    } ?: ArateaIPBlindingServiceCoroutineStub(channel.get())
  }

  override suspend fun initialData(request: ByteArray): ByteArray =
    timerSet.start(PrivateInferenceClientTimerNames.GET_INITIAL_DATA).use {
      val requestSize = request.size.toLong()
      try {
        logger.atInfo().log("Sent GetInitialData request to server with size: %d", requestSize)
        val response = stub.getInitialData(GetInitialDataRequest.parseFrom(request)).toByteArray()
        logger
          .atInfo()
          .log("Received GetInitialData response from server with size: %d", response.size.toLong())
        logNetworkUsage(isSuccess = true, requestSize, response.size.toLong())
        return response
      } catch (e: Exception) {
        logger.atWarning().log("GetInitialData request failed, error: %s", e.message)
        logNetworkUsage(isSuccess = false, requestSize, 0L)
        throw e
      }
    }

  override suspend fun attestAndSign(request: ByteArray): ByteArray =
    timerSet.start(PrivateInferenceClientTimerNames.ATTEST_AND_SIGN).use {
      val requestSize = request.size.toLong()
      try {
        logger.atInfo().log("Sent AttestAndSign request to server with size: %d", requestSize)
        val response = stub.attestAndSign(AttestAndSignRequest.parseFrom(request)).toByteArray()
        logger
          .atInfo()
          .log("Received AttestAndSign response from server with size: %d", response.size.toLong())
        logNetworkUsage(isSuccess = true, requestSize, response.size.toLong())
        return response
      } catch (e: Exception) {
        logger.atWarning().log("AttestAndSign request failed, error: %s", e.message)
        logNetworkUsage(isSuccess = false, requestSize, 0L)
        throw e
      }
    }

  internal fun logNetworkUsage(isSuccess: Boolean, requestSize: Long, responseSize: Long) {
    networkUsageLogHelper.logIPProtectionRequest(
      IPProtectionRequestType.IPP_GET_ANONYMOUS_TOKEN,
      isSuccess,
      requestSize,
      responseSize,
    )
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
