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

package com.google.android.as.oss.http.client;

import com.google.android.as.oss.http.api.proto.ResponseHeaders;

/** Callback for HTTP downloading through Pcs. */
public interface HttpDownloaderCallback {

  /** Called when downloading has been started. */
  void onDownloadStarted(ResponseHeaders headers);

  // TODO: consider wrapping it into an InputStream object, supplied from onDownloadStarted.
  /** Called when a new batch has been downloaded. */
  void onDownloadedChunk(byte[] bytes);

  /** Called when downloading has been complete. */
  void onDownloadComplete();

  /** An error happened during the downloading process. */
  void onError(Throwable t);
}
