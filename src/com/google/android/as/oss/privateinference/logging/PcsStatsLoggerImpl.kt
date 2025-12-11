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

import com.google.android.`as`.oss.logging.PcsAtomsProto.IntelligenceCountReported
import com.google.android.`as`.oss.logging.PcsAtomsProto.IntelligenceValueReported
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.logging.PcsStatsLog
import javax.inject.Inject

/** A wrapper around [PcsStatsLog] for logging Private Inference metrics. */
class PcsStatsLoggerImpl @Inject constructor(private val pcsStatsLog: PcsStatsLog) :
  PcsStatsLogger {
  override fun logEventCount(countMetricId: CountMetricId) {
    pcsStatsLog.logIntelligenceCountReported(
      IntelligenceCountReported.newBuilder().setCountMetricId(countMetricId).build()
    )
  }

  override fun logEventLatency(valueMetricId: ValueMetricId, latencyMs: Long) {
    pcsStatsLog.logIntelligenceValueReported(
      IntelligenceValueReported.newBuilder()
        .setValueMetricId(valueMetricId)
        .setValue(DurationBucketLogic.snapDurationToBucket(latencyMs))
        .build()
    )
  }

  /**
   * Compute the result of the given block, logging the supplied result status metric after the
   * block completes.
   */
  override fun <T> getResultAndLogStatus(metricIdMap: MetricIdMap, block: () -> T): T {
    val startTime = System.currentTimeMillis()
    return try {
      val result = block()
      if (metricIdMap.successLatencyMetricId != null) {
        logEventLatency(metricIdMap.successLatencyMetricId, System.currentTimeMillis() - startTime)
      }
      logEventCount(metricIdMap.successMetricId)
      result
    } catch (e: Exception) {
      if (metricIdMap.failureLatencyMetricId != null) {
        logEventLatency(metricIdMap.failureLatencyMetricId, System.currentTimeMillis() - startTime)
      }
      logEventCount(metricIdMap.failureMetricId)
      throw e
    }
  }

  /**
   * Compute the result of the given suspend block, logging the supplied result status metric after
   * the block completes.
   */
  override suspend fun <T> getResultAndLogStatusAsync(
    metricIdMap: MetricIdMap,
    block: suspend () -> T,
  ): T {
    val startTime = System.currentTimeMillis()
    return try {
      val result = block()
      val latencyMs = System.currentTimeMillis() - startTime
      logEventCount(metricIdMap.successMetricId)
      if (metricIdMap.successLatencyMetricId != null) {
        logEventLatency(metricIdMap.successLatencyMetricId, latencyMs)
      }
      result
    } catch (e: Exception) {
      val latencyMs = System.currentTimeMillis() - startTime
      logEventCount(metricIdMap.failureMetricId)
      if (metricIdMap.failureLatencyMetricId != null) {
        logEventLatency(metricIdMap.failureLatencyMetricId, latencyMs)
      }
      throw e
    }
  }
}
