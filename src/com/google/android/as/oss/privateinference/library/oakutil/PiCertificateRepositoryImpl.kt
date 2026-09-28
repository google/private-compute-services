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
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.guava.future

/**
 * Implementation of [PiCertificateRepository] that orchestrates downloading and caching of
 * certificates.
 */
class PiCertificateRepositoryImpl
@Inject
constructor(
  private val downloader: PiCertificateDownloader,
  private val cache: PiCertificateCache,
) : PiCertificateRepository {

  @CanIgnoreReturnValue
  override fun getOakCtCertificate(
    executor: ListeningExecutorService,
    forceRefresh: Boolean,
  ): ListenableFuture<String> =
    getCertificate(
      executor,
      PiCertificateCache.CertificateType.OAK_CT,
      PiCertificateDownloader.CertificateType.OAK_CT,
      forceRefresh,
    )

  @CanIgnoreReturnValue
  override fun getTcaRootCertificate(executor: ListeningExecutorService): ListenableFuture<String> =
    getCertificate(
      executor,
      PiCertificateCache.CertificateType.TCA_ROOT,
      PiCertificateDownloader.CertificateType.TCA_ROOT,
    )

  /**
   * Retrieves the certificate from cache or downloads it if it's not present.
   *
   * @param executor The executor to run the background tasks on.
   * @param cacheType The type of the certificate to store in cache.
   * @param downloaderType The type of the certificate to download.
   * @param forceRefresh If true, forces a re-download of the certificate even if cached.
   * @return A [ListenableFuture] resolving to the certificate content.
   */
  private fun getCertificate(
    executor: ListeningExecutorService,
    cacheType: PiCertificateCache.CertificateType,
    downloaderType: PiCertificateDownloader.CertificateType,
    forceRefresh: Boolean = false,
  ): ListenableFuture<String> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      if (!forceRefresh) {
        val cached = cache.read(cacheType)
        if (cached != null) {
          return@future cached
        }
      }

      val content = downloader.download(downloaderType)
      cache.write(cacheType, content)
      content
    }
}
