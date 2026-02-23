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

import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.android.`as`.oss.privateinference.util.timers.Timers.Timer
import javax.inject.Inject
import kotlin.time.Duration.Companion.nanoseconds

/**
 * A [Timers] implementation that logs latency metrics using [PcsStatsLogger].
 *
 * This timer supports logging for specific operations defined in
 * [PrivateInferenceClientTimerNames]. Latency is measured from the call to [start] until the
 * returned [Timer]'s `close` method is invoked.
 */
class LatencyLoggingTimer @Inject internal constructor(private val pcsStatsLogger: PcsStatsLogger) :
  Timers {
  override fun start(name: String): Timer {
    val startingTime: Long
    return if (name !in SUPPORTED_TIMER_NAMES_MAP.keys) {
      Timers.NOP_TIMER
    } else {
      startingTime = System.nanoTime()
      timer {
        val latencyMs = (System.nanoTime() - startingTime).nanoseconds.inWholeMilliseconds
        val valueMetric: ValueMetricId = checkNotNull(SUPPORTED_TIMER_NAMES_MAP[name])
        pcsStatsLogger.logEventLatency(valueMetric, latencyMs)
      }
    }
  }

  companion object {
    private val SUPPORTED_TIMER_NAMES_MAP =
      mapOf(
        PrivateInferenceClientTimerNames.OAK_SESSION_ESTABLISH_STREAM to
          ValueMetricId.PCS_PI_OAK_HANDSHAKE_SUCCESS_LATENCY_MS,
        PrivateInferenceClientTimerNames.END_TO_END_PI_CHANNEL_SETUP to
          ValueMetricId.PCS_PI_END_TO_END_NOISE_SESSION_SETUP_SUCCESS_LATENCY_MS,
        PrivateInferenceClientTimerNames.IPP_ANONYMOUS_TOKEN_AUTH to
          ValueMetricId.PCS_PI_IPP_TERMINAL_TOKEN_AUTH_SUCCESS_LATENCY_MS,
      )
  }
}
