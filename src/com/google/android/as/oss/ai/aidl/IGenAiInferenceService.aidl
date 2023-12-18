/*
 * Copyright 2023 Google LLC
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
import com.google.android.apps.aicore.aidl.DownloadRequestStatus;
import com.google.android.as.oss.ai.aidl.PccLlmService;
import com.google.android.as.oss.ai.aidl.PccSummarizationService;
import com.google.android.as.oss.ai.aidl.PccSmartReplyService;

/**
 * PCS's interface for AICore interactions.
 *
 * <p>Note that AICore keeps data for all clients isolated from each other.
 * So data from PCS's requests is never shared with any other client.
 */
interface IGenAiInferenceService {

  /** List all features which are not {@link AIFeatureStatus#UNAVAILABLE}  */
  AIFeature[] listFeatures();

  /**
   * Gets {@link AIFeature} by the unique {@link AIFeature.Id}.
   *
   * <p>It returns null the requested feature is {@link AIFeatureStatus#UNAVAILABLE}.
   */
  @Nullable AIFeature getFeature(int id);

  /** Provide feature status infromation */
  @AIFeatureStatus int getFeatureStatus(in AIFeature feature);

  /** Request downloadable feature to be downloaded */
  @DownloadRequestStatus int requestDownloadableFeature(in AIFeature feature);

  /** Request LLM Service for an AIFeature of a LLM type */
  PccLlmService getLLMService(in AIFeature feature);

  /** Request Smart Reply Service for an AIFeature of a SMART_REPLY type */
  PccSmartReplyService getSmartReplyService(in AIFeature feature);

  /** Request Summarization Service for an AIFeature of a SUMMARIZATION type */
  PccSummarizationService getSummarizationService(in AIFeature feature);

  /**
  * Request downloadable feature to be downloaded and listen to download
  * progress updates. Will throw an exception if {@code null} listener is provided.
  */
  @DownloadRequestStatus int requestDownloadableFeatureWithDownloadListener(in AIFeature feature, IDownloadListener listener);

  /** Returns whether persistent mode is enabled for AICore service. */
  boolean isPersistentModeEnabled();
};