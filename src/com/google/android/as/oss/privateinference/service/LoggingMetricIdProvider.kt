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

package com.google.android.`as`.oss.privateinference.service

import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.service.api.proto.PcsPrivateInferenceFeatureName
import io.grpc.Status.Code
import javax.inject.Inject
import kotlin.jvm.JvmSuppressWildcards

/** Provides the logging metric IDs for Private Inference service. */
class LoggingMetricIdProvider
@Inject
constructor(
  @FeatureNameToSuccessCountMetricIdMap
  private val featureNameToSuccessCountMetricIdMap:
    @JvmSuppressWildcards
    Map<PcsPrivateInferenceFeatureName, CountMetricId>,
  @FeatureNameToFailureCountMetricIdMap
  private val featureNameToFailureCountMetricIdMap:
    @JvmSuppressWildcards
    Map<PcsPrivateInferenceFeatureName, CountMetricId>,
  @FeatureNameToSuccessLatencyValueMetricIdMap
  private val featureNameToSuccessLatencyValueMetricIdMap:
    @JvmSuppressWildcards
    Map<PcsPrivateInferenceFeatureName, ValueMetricId>,
  @FeatureNameToFailureLatencyValueMetricIdMap
  private val featureNameToFailureLatencyValueMetricIdMap:
    @JvmSuppressWildcards
    Map<PcsPrivateInferenceFeatureName, ValueMetricId>,
) {
  fun getInferenceSuccessCountMetricId(featureName: PcsPrivateInferenceFeatureName): CountMetricId =
    featureNameToSuccessCountMetricIdMap.getOrDefault(
      featureName,
      CountMetricId.PCS_PI_UNKNOWN_FEATURE_SUCCESS,
    )

  fun getInferenceFailureCountMetricId(featureName: PcsPrivateInferenceFeatureName): CountMetricId =
    featureNameToFailureCountMetricIdMap.getOrDefault(
      featureName,
      CountMetricId.PCS_PI_UNKNOWN_FEATURE_FAILURE,
    )

  fun getInferenceFailureErrorCodeCountMetricId(errorCode: Code): CountMetricId =
    ERROR_CODE_TO_COUNT_METRIC_ID_MAP.getOrDefault(
      errorCode,
      CountMetricId.PCS_PI_ERROR_UNSPECIFIED,
    )

  fun getInferenceSuccessLatencyValueMetricId(
    featureName: PcsPrivateInferenceFeatureName
  ): ValueMetricId =
    featureNameToSuccessLatencyValueMetricIdMap.getOrDefault(
      featureName,
      ValueMetricId.PCS_PI_REQUEST_LATENCY_MS,
    )

  fun getInferenceFailureLatencyValueMetricId(
    featureName: PcsPrivateInferenceFeatureName
  ): ValueMetricId =
    featureNameToFailureLatencyValueMetricIdMap.getOrDefault(
      featureName,
      ValueMetricId.PCS_PI_REQUEST_FAILURE_LATENCY_MS,
    )

  private companion object {
    val ERROR_CODE_TO_COUNT_METRIC_ID_MAP =
      mapOf(
        Code.CANCELLED to CountMetricId.PCS_PI_ERROR_CANCELLED,
        Code.INVALID_ARGUMENT to CountMetricId.PCS_PI_ERROR_INVALID_ARGUMENT,
        Code.DEADLINE_EXCEEDED to CountMetricId.PCS_PI_ERROR_DEADLINE_EXCEEDED,
        Code.NOT_FOUND to CountMetricId.PCS_PI_ERROR_NOT_FOUND,
        Code.ALREADY_EXISTS to CountMetricId.PCS_PI_ERROR_ALREADY_EXISTS,
        Code.PERMISSION_DENIED to CountMetricId.PCS_PI_ERROR_PERMISSION_DENIED,
        Code.UNAUTHENTICATED to CountMetricId.PCS_PI_ERROR_UNAUTHENTICATED,
        Code.RESOURCE_EXHAUSTED to CountMetricId.PCS_PI_ERROR_RESOURCE_EXHAUSTED,
        Code.FAILED_PRECONDITION to CountMetricId.PCS_PI_ERROR_FAILED_PRECONDITION,
        Code.ABORTED to CountMetricId.PCS_PI_ERROR_ABORTED,
        Code.OUT_OF_RANGE to CountMetricId.PCS_PI_ERROR_OUT_OF_RANGE,
        Code.UNIMPLEMENTED to CountMetricId.PCS_PI_ERROR_UNIMPLEMENTED,
        Code.INTERNAL to CountMetricId.PCS_PI_ERROR_INTERNAL,
        Code.UNAVAILABLE to CountMetricId.PCS_PI_ERROR_UNAVAILABLE,
      )
  }
}
