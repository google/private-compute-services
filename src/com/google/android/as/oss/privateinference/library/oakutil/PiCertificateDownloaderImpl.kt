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

import android.content.Context
import com.google.android.`as`.oss.privateinference.Annotations.OakCtCertificateUrl
import com.google.android.`as`.oss.privateinference.Annotations.TcaRootCertificateUrl
import com.google.android.downloader.Downloader
import com.google.android.downloader.SimpleFileDownloadDestination
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.guava.await

/** Implementation of [PiCertificateDownloader] using [Downloader]. */
class PiCertificateDownloaderImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
  private val downloader: Downloader,
  @OakCtCertificateUrl private val oakCtUrl: String,
  @TcaRootCertificateUrl private val tcaRootUrl: String,
) : PiCertificateDownloader {

  override suspend fun download(type: PiCertificateDownloader.CertificateType): String {
    val url = getUrl(type)
    logger.atFine().log("Starting download of certificate from %s", url)
    val downloadId = UUID.randomUUID().toString()
    // Use cacheDir for temporary download files
    val tempFile = File(context.cacheDir, "pi_download_${downloadId}.tmp")
    val metadataFile = File(context.cacheDir, "pi_download_${downloadId}.metadata")
    val destination = SimpleFileDownloadDestination(tempFile, metadataFile)
    val request = downloader.newRequestBuilder(URI.create(url), destination).build()

    try {
      try {
        downloader.execute(request).await()
      } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        throw IOException("Failed to download certificate from $url", e)
      }

      if (tempFile.length() == 0L) {
        throw IOException("Downloaded certificate is empty")
      }
      if (tempFile.length() > MAX_CERT_SIZE) {
        throw IllegalStateException(
          "Downloaded certificate is longer than expected (max $MAX_CERT_SIZE bytes)"
        )
      }

      logger.atFine().log("Downloaded certificate (%d bytes)", tempFile.length())
      return tempFile.readText(UTF_8)
    } finally {
      tempFile.delete()
      metadataFile.delete()
    }
  }

  private fun getUrl(type: PiCertificateDownloader.CertificateType): String {
    return when (type) {
      PiCertificateDownloader.CertificateType.OAK_CT -> oakCtUrl
      PiCertificateDownloader.CertificateType.TCA_ROOT -> tcaRootUrl
    }
  }

  companion object {
    private const val MAX_CERT_SIZE = 65536 // 64 KB limit for safety
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
