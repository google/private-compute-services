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

import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
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
  private val pcsStatsLogger: PcsStatsLogger,
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
  override fun getTcaRootCertificate(
    executor: ListeningExecutorService,
    forceRefresh: Boolean,
  ): ListenableFuture<String> =
    getCertificate(
      executor,
      PiCertificateCache.CertificateType.TCA_ROOT,
      PiCertificateDownloader.CertificateType.TCA_ROOT,
      forceRefresh,
    )

  /**
   * Retrieves the certificate from cache or downloads it if it's not present.
   *
   * If the download fails, [PiCertificateDownloader.download] throws an exception (e.g.
   * [java.io.IOException]). Inside the coroutine launched by [kotlinx.coroutines.guava.future], any
   * uncaught exception immediately completes the returned [ListenableFuture] exceptionally with
   * that exception, skipping subsequent success logging and cache writes.
   *
   * @param executor The executor to run the background tasks on.
   * @param cacheType The type of the certificate to store in cache.
   * @param downloaderType The type of the certificate to download.
   * @param forceRefresh If true, forces a re-download of the certificate even if cached.
   * @return A [ListenableFuture] resolving to the certificate content, or failing with the
   *   underlying exception (e.g. [java.io.IOException]) if the download fails.
   */
  private fun getCertificate(
    executor: ListeningExecutorService,
    cacheType: PiCertificateCache.CertificateType,
    downloaderType: PiCertificateDownloader.CertificateType,
    forceRefresh: Boolean = false,
  ): ListenableFuture<String> =
    CoroutineScope(executor.asCoroutineDispatcher()).future {
      if (!forceRefresh) {
        pcsStatsLogger.logEventCount(
          when (cacheType) {
            PiCertificateCache.CertificateType.OAK_CT ->
              CountMetricId.PCS_PI_OAKCT_CERT_CACHE_LOOKUP
            PiCertificateCache.CertificateType.TCA_ROOT ->
              CountMetricId.PCS_PI_TCA_CERT_CACHE_LOOKUP
          }
        )
        val cacheHit = cache.read(cacheType)
        if (cacheHit != null) {
          return@future cacheHit
        }
        pcsStatsLogger.logEventCount(
          when (cacheType) {
            PiCertificateCache.CertificateType.OAK_CT -> CountMetricId.PCS_PI_OAKCT_CERT_CACHE_MISS
            PiCertificateCache.CertificateType.TCA_ROOT -> CountMetricId.PCS_PI_TCA_CERT_CACHE_MISS
          }
        )
      }

      pcsStatsLogger.logEventCount(
        when (downloaderType) {
          PiCertificateDownloader.CertificateType.OAK_CT ->
            CountMetricId.PCS_PI_OAKCT_CERT_DOWNLOAD_COUNT
          PiCertificateDownloader.CertificateType.TCA_ROOT ->
            CountMetricId.PCS_PI_TCA_CERT_DOWNLOAD_COUNT
        }
      )
      // Failures in download() throw an exception (e.g. IOException). Inside CoroutineScope.future,
      // uncaught exceptions complete the returned ListenableFuture exceptionally, skipping the
      // success logging and cache writing below.
      val content = downloader.download(downloaderType)
      pcsStatsLogger.logEventCount(
        when (downloaderType) {
          PiCertificateDownloader.CertificateType.OAK_CT ->
            CountMetricId.PCS_PI_OAKCT_CERT_DOWNLOAD_SUCCESS
          PiCertificateDownloader.CertificateType.TCA_ROOT ->
            CountMetricId.PCS_PI_TCA_CERT_DOWNLOAD_SUCCESS
        }
      )
      cache.write(cacheType, content)
      content
    }
}
