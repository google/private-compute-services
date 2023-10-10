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

package com.google.android.as.oss.pd.processor;

import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import com.google.android.as.oss.pd.api.proto.DownloadBlobResponse;
import com.google.android.as.oss.pd.api.proto.GetManifestConfigRequest;
import com.google.android.as.oss.pd.api.proto.GetManifestConfigResponse;
import com.google.common.util.concurrent.ListenableFuture;

/** An abstraction for the downloading operation of a blob. */
public interface ProtectedDownloadProcessor {
  /** Runs a download process. */
  ListenableFuture<DownloadBlobResponse> download(DownloadBlobRequest request);

  /** Runs a getManifestConfig process. */
  ListenableFuture<GetManifestConfigResponse> getManifestConfig(GetManifestConfigRequest request);
}
