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
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_MEMORY_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_MEMORY_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_RESPONSE_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_RESPONSE_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MINI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_MINI_RESPONSE_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_SCREENSHOT_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_SCREENSHOT_MEMORY_GENERATION_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_GBOARD_CONVERSATIONAL_WRITING_TOOLS to
        CountMetricId.PCS_PI_GBOARD_CONVERSATIONAL_WRITING_TOOLS_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_GBOARD_CONVERSATIONAL_WRITING_TOOLS_V2 to
        CountMetricId.PCS_PI_GBOARD_CONVERSATIONAL_WRITING_TOOLS_SUCCESS,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SOLTAIRE_SD to
        CountMetricId.PCS_PI_SOLTAIRE_SD_SUCCESS,
    )

  @Provides
  @Singleton
  @FeatureNameToSessionErrorCountMetricIdMap
  fun provideFeatureNameToSessionErrorCountMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, CountMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_MEMORY_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_CLASSIFICATION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_CLASSIFICATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_TRANSCRIPTION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_TRANSCRIPTION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        CountMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_MEMORY_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_MEMORY_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_RESPONSE_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_RESPONSE_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MINI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_MINI_RESPONSE_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_SCREENSHOT_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_SCREENSHOT_MEMORY_GENERATION_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_GBOARD_CONVERSATIONAL_WRITING_TOOLS to
        CountMetricId.PCS_PI_GBOARD_CONVERSATIONAL_WRITING_TOOLS_SESSION_ERROR,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SOLTAIRE_SD to
        CountMetricId.PCS_PI_SOLTAIRE_SD_SESSION_ERROR,
    )

  @Provides
  @Singleton
  @FeatureNameToSessionCountMetricIdMap
  fun provideFeatureNameToSessionCountMetricIdMap():
    Map<PcsPrivateInferenceFeatureName, CountMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_MEMORY_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_CLASSIFICATION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_CLASSIFICATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_TRANSCRIPTION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_TRANSCRIPTION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        CountMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_GBOARD_CONVERSATIONAL_WRITING_TOOLS to
        CountMetricId.PCS_PI_GBOARD_CONVERSATIONAL_WRITING_TOOLS_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_MEMORY_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_MEMORY_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_RESPONSE_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MINI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_MINI_RESPONSE_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_RESPONSE_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_SCREENSHOT_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_SCREENSHOT_MEMORY_GENERATION_SESSION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SOLTAIRE_SD to
        CountMetricId.PCS_PI_SOLTAIRE_SD_SESSION_COUNT,
    )

  @Provides
  @Singleton
  @FeatureNameToCountMetricIdMap
  fun provideFeatureNameToCountMetricIdMap(): Map<PcsPrivateInferenceFeatureName, CountMetricId> =
    mapOf(
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_MEMORY_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_CLASSIFICATION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_CLASSIFICATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_COLLABORATIVE_LISTENING_TRANSCRIPTION to
        CountMetricId.PCS_PI_COLLABORATIVE_LISTENING_TRANSCRIPTION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION to
        CountMetricId.PCS_PI_RECORDER_TRANSCRIPT_SUMMARIZATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_GBOARD_CONVERSATIONAL_WRITING_TOOLS to
        CountMetricId.PCS_PI_GBOARD_CONVERSATIONAL_WRITING_TOOLS_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_MEMORY_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_MEMORY_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SCREENSHOTS_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_SCREENSHOTS_RESPONSE_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MINI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_MINI_RESPONSE_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_RESPONSE_GENERATION to
        CountMetricId.PCS_PI_PSI_RESPONSE_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_SCREENSHOT_MEMORY_GENERATION to
        CountMetricId.PCS_PI_PSI_SCREENSHOT_MEMORY_GENERATION_COUNT,
      PcsPrivateInferenceFeatureName.FEATURE_NAME_SOLTAIRE_SD to
        CountMetricId.PCS_PI_SOLTAIRE_SD_COUNT,
    )

  @Provides
  @Singleton
  @ProxyTokenUtilizationMetricId
  fun provideProxyTokenUtilizationMetricId(): ValueMetricId =
    ValueMetricId.PCS_PI_PROXY_TOKEN_UTILIZATION_RATIO

  @Provides
  @Singleton
  @ArateaTokenUtilizationMetricId
  fun provideArateaTokenUtilizationMetricId(): ValueMetricId =
    ValueMetricId.PCS_PI_ARATEA_TOKEN_UTILIZATION_RATIO
}
