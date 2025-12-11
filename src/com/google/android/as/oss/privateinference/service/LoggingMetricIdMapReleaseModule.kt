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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module that provides the logging metric ID mapping for release flavor. */
@Module
@InstallIn(SingletonComponent::class)
internal object LoggingMetricIdMapReleaseModule {

  @Provides
  @Singleton
  @FeatureNameToSuccessCountMetricIdMap
  fun provideFeatureNameToSuccessCountMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, CountMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_MEMORY_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        CountMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_SUCCESS,
    )

  @Provides
  @Singleton
  @FeatureNameToFailureCountMetricIdMap
  fun provideFeatureNameToFailureCountMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, CountMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_MEMORY_GENERATION_FAILURE,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        CountMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_FAILURE,
    )

  @Provides
  @Singleton
  @FeatureNameToSuccessLatencyValueMetricIdMap
  fun provideFeatureNameToSuccessLatencyValueMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, ValueMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        ValueMetricId.PCS_PI_PSI_MEMORY_GENERATION_LATENCY_MS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        ValueMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_LATENCY_MS,
    )

  @Provides
  @Singleton
  @FeatureNameToFailureLatencyValueMetricIdMap
  fun provideFeatureNameToFailureLatencyValueMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, ValueMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        ValueMetricId.PCS_PI_PSI_MEMORY_GENERATION_FAILURE_LATENCY_MS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        ValueMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_FAILURE_LATENCY_MS,
    )
}
