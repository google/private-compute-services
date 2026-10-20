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

package com.google.android.`as`.oss.privateinference.library.oakutil

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.errorprone.annotations.CanIgnoreReturnValue

/**
 * Utility for downloading and caching Private Inference (PI) root verification certificates.
 *
 * This class uses [DownloaderTransport] to download certificates over HTTP, decoupling from direct
 * socket APIs for testability and proper proxy/network stack integration. Once downloaded,
 * certificates are cached in the device's Pcs-specific internal files directory to enable fast
 * offline access and avoid repeated network calls.
 */
interface PiCertificateRepository {
  /**
   * Asynchronously fetches the OakCT certificate from the on-device cache. If not cached, it
   * downloads and caches the certificate.
   *
   * This runs the HTTP download on the provided executor thread to unblock the calling thread. If
   * the download fails, the returned future completes exceptionally with the underlying failure
   * (e.g. [java.io.IOException]).
   *
   * @param executor The executor to run the download task on.
   * @param forceRefresh If true, forces a re-download of the certificate even if cached.
   * @return A [ListenableFuture] resolving to the certificate content, or failing with an exception
   *   if the download fails.
   */
  @CanIgnoreReturnValue
  fun getOakCtCertificate(
    executor: ListeningExecutorService,
    forceRefresh: Boolean = false,
  ): ListenableFuture<String>

  /**
   * Asynchronously fetches the TCA root certificate from the on-device cache. If not cached, it
   * downloads and caches the certificate.
   *
   * This runs the HTTP download on the provided executor thread to unblock the calling thread. If
   * the download fails, the returned future completes exceptionally with the underlying failure
   * (e.g. [java.io.IOException]).
   *
   * @param executor The executor to run the download task on.
   * @param forceRefresh If true, forces a re-download of the certificate even if cached.
   * @return A [ListenableFuture] resolving to the certificate content, or failing with an exception
   *   if the download fails.
   */
  @CanIgnoreReturnValue
  fun getTcaRootCertificate(
    executor: ListeningExecutorService,
    forceRefresh: Boolean = false,
  ): ListenableFuture<String>
}
