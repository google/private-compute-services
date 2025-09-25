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

package com.google.android.as.oss.ai.aidl;

import androidx.annotation.Nullable;
import com.google.android.apps.aicore.aidl.AIFeature;
import com.google.android.apps.aicore.aidl.AIFeatureStatus;
import com.google.android.apps.aicore.aidl.IDownloadListener;
import com.google.android.apps.aicore.aidl.IDownloadListener2;
import com.google.android.apps.aicore.aidl.DownloadRequestStatus;
import com.google.android.as.oss.ai.aidl.PccLlmService;
import com.google.android.as.oss.ai.aidl.PccSummarizationService;
import com.google.android.as.oss.ai.aidl.PccSmartReplyService;
import com.google.android.as.oss.ai.aidl.PccTarsService;
import com.google.android.as.oss.ai.aidl.PccTextEmbeddingService;

/**
 * PCS's interface for AICore interactions.
 *
 * <p>Note that AICore keeps data for all clients isolated from each other.
 * So data from PCS's requests is never shared with any other client.
 */
// Next id: 13
interface IGenAiInferenceService {

  /** List all features which are not {@link AIFeatureStatus#UNAVAILABLE}  */
  AIFeature[] listFeatures() = 0;

  /**
   * Gets {@link AIFeature} by the unique {@link AIFeature.Id}.
   *
   * <p>It returns null the requested feature is {@link AIFeatureStatus#UNAVAILABLE}.
   */
  @Nullable AIFeature getFeature(int id) = 1;

  /**
   * Gets {@link AIFeature} by the unique {@link AIFeature.Id} and version.
   *
   * <p>If the feature with the desired version is not found, it returns
   * whichever control version of the feature is known to AICore at the moment
   * and the client is allowed to use it, {@code null} otherwise.
   */
  @Nullable AIFeature getFeatureOrControl(int id, int desiredVersion) = 12;

  /** Provide feature status infromation */
  @AIFeatureStatus int getFeatureStatus(in AIFeature feature) = 2;

  /** Request downloadable feature to be downloaded */
  @DownloadRequestStatus int requestDownloadableFeature(in AIFeature feature) = 3;

  /**
   * Request Text Embedding Service for an AIFeature of a TEXT_EMBEDDING type
   */
  PccTextEmbeddingService getTextEmbeddingService(in AIFeature feature) = 9;

  /** Request LLM Service for an AIFeature of a LLM type */
  PccLlmService getLLMService(in AIFeature feature) = 4;

  /** Request Smart Reply Service for an AIFeature of a SMART_REPLY type */
  PccSmartReplyService getSmartReplyService(in AIFeature feature) = 5;

  /** Request Summarization Service for an AIFeature of a SUMMARIZATION type */
  PccSummarizationService getSummarizationService(in AIFeature feature) = 6;

  /** Request Tars service for an AIFeature of TARS type. */
  PccTarsService getTarsService(in AIFeature feature) = 11;

  /**
  * Request downloadable feature to be downloaded and listen to download
  * progress updates. Will throw an exception if {@code null} listener is provided.
  *
  * @deprecated Deprecated in AICoreVersion.V2, use {@link #requestDownloadableFeatureWithDownloadListener2} instead
  */
  @Deprecated @DownloadRequestStatus int requestDownloadableFeatureWithDownloadListener(in AIFeature feature, IDownloadListener listener) = 7;

  /** Returns whether persistent mode is enabled for AICore service. */
  boolean isPersistentModeEnabled() = 8;

  /**
  * Request downloadable feature to be downloaded and listen to download progress updates.
  * Will throw an exception if null listener is provided. Introduced in AICoreVersion.V2.
  */
  @DownloadRequestStatus int requestDownloadableFeatureWithDownloadListener2(in AIFeature feature, IDownloadListener2 listener) = 10;
};