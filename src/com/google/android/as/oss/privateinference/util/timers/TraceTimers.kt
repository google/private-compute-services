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

package com.google.android.`as`.oss.privateinference.util.timers

import android.os.Build
import android.os.Trace
import androidx.annotation.RequiresApi
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.service.PrivateInferenceGrpcTimerNames
import com.google.common.flogger.GoogleLogger
import com.google.errorprone.annotations.CompileTimeConstant
import javax.inject.Inject

/**
 * A simple implementation of {@link Timers} that writes trace events to the system trace buffer.
 */
class TraceTimers @Inject internal constructor() : Timers {
  override fun start(@CompileTimeConstant name: String): Timers.Timer {
    return if (Build.VERSION.SDK_INT < 29 || name !in SUPPORTED_TIMER_NAMES) {
      Timers.NOP_TIMER
    } else {
      logger.atFine().log("Starting trace timer: %s", name)
      beginAsyncSectionTrace(name, 0)
      return timer { endAsyncSectionTrace(name, 0) }
    }
  }

  @RequiresApi(29)
  fun beginAsyncSectionTrace(name: String, cookie: Int) {
    Trace.beginAsyncSection(name, cookie)
  }

  @RequiresApi(29)
  fun endAsyncSectionTrace(name: String, cookie: Int) {
    Trace.endAsyncSection(name, cookie)
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    private val SUPPORTED_TIMER_NAMES =
      listOf(
        PrivateInferenceGrpcTimerNames.PRIVATE_INFERENCE_TIMER_NAME,
        PrivateInferenceGrpcTimerNames.PRIVATE_INFERENCE_SESSION_TIMER_NAME,
        PrivateInferenceClientTimerNames.END_TO_END_PI_CHANNEL_SETUP,
        PrivateInferenceClientTimerNames.OAK_SESSION_ESTABLISH_STREAM,
        PrivateInferenceClientTimerNames.OAK_SESSION_EXCHANGE_ATTESTATION_EVIDENCE,
        PrivateInferenceClientTimerNames.OAK_SESSION_PERFORM_HANDSHAKE_STEP,
        PrivateInferenceClientTimerNames.IPP_ANONYMOUS_TOKEN_AUTH,
        PrivateInferenceClientTimerNames.IPP_GET_PROXY_TOKEN,
        PrivateInferenceClientTimerNames.IPP_CREATE_PROXY_TOKEN,
        PrivateInferenceClientTimerNames.IPP_CREATE_TERMINAL_TOKEN,
        PrivateInferenceClientTimerNames.IPP_GET_INITIAL_DATA,
        PrivateInferenceClientTimerNames.IPP_ATTEST_AND_SIGN,
        PrivateInferenceClientTimerNames.IPP_GET_PROXY_CONFIG,
        PrivateInferenceClientTimerNames.IPP_FETCH_PROXY_CONFIG,
        PrivateInferenceClientTimerNames.IPP_MASQUE_TUNNEL_SETUP,
      )
  }
}
