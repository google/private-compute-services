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

import com.google.android.as.oss.ai.aidl.PccCancellationCallback;
import com.google.android.as.oss.ai.aidl.PccSummarizationResultCallback;
import com.google.android.apps.aicore.aidl.SummarizationRequest;
import com.google.android.apps.aicore.aidl.IPrepareInferenceEngineCallback;

interface PccSummarizationService  {
  /** Runs an inference that allows explicit cancellation before completion. */
  PccCancellationCallback runCancellableInference(
      in SummarizationRequest request, in PccSummarizationResultCallback callback);
  /**
   * Prepares engine in advance so as to move setup cost out of inference. Calling this method
   * is strictly optional.
   */
  PccCancellationCallback prepareInferenceEngine(in IPrepareInferenceEngineCallback callback);
};
