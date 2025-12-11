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

package com.google.android.`as`.oss.privateinference.logging

import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId

/** A wrapper around [PcsStatsLog] for logging Private Inference metrics. */
interface PcsStatsLogger {
  fun logEventCount(countMetricId: CountMetricId)

  fun logEventLatency(valueMetricId: ValueMetricId, latencyMs: Long)

  /**
   * Compute the result of the given block, logging the supplied result status metric after the
   * block completes.
   */
  fun <T> getResultAndLogStatus(metricIdMap: MetricIdMap, block: () -> T): T

  /**
   * Compute the result of the given suspend block, logging the supplied result status metric after
   * the block completes.
   */
  suspend fun <T> getResultAndLogStatusAsync(metricIdMap: MetricIdMap, block: suspend () -> T): T
}

data class MetricIdMap(
  val successMetricId: CountMetricId,
  val failureMetricId: CountMetricId,
  val successLatencyMetricId: ValueMetricId?,
  val failureLatencyMetricId: ValueMetricId?,
)
