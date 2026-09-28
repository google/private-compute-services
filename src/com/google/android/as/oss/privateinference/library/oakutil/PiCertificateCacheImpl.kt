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
import android.util.AtomicFile
import androidx.annotation.VisibleForTesting
import com.google.android.`as`.oss.privateinference.Annotations.OakCtCertificateFilename
import com.google.android.`as`.oss.privateinference.Annotations.TcaRootCertificateFilename
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import javax.inject.Inject

/** Implementation of [PiCertificateCache] using internal files storage. */
class PiCertificateCacheImpl
@Inject
constructor(
  @ApplicationContext private val context: Context,
  @OakCtCertificateFilename private val oakCtFilename: String,
  @TcaRootCertificateFilename private val tcaRootFilename: String,
) : PiCertificateCache {

  override fun read(type: PiCertificateCache.CertificateType): String? {
    val filename = getFilename(type)
    val cacheFile = getCacheFile(filename)
    val atomicFile = AtomicFile(cacheFile)
    return try {
      atomicFile.openRead().use { stream ->
        val bytes = stream.readBytes()
        if (bytes.isEmpty()) {
          return null
        }
        logger.atFine().log("Reading certificate from device cache: %s", cacheFile.absolutePath)
        String(bytes, UTF_8)
      }
    } catch (e: java.io.FileNotFoundException) {
      null // Expected if not cached yet
    } catch (e: IOException) {
      logger
        .atWarning()
        .withCause(e)
        .log("Failed to read certificate from cache: %s", cacheFile.absolutePath)
      null
    }
  }

  override fun write(type: PiCertificateCache.CertificateType, content: String) {
    val filename = getFilename(type)
    val cacheFile = getCacheFile(filename)
    val atomicFile = AtomicFile(cacheFile)
    var fos: FileOutputStream? = null
    try {
      fos = atomicFile.startWrite()
      fos.write(content.toByteArray(UTF_8))
      atomicFile.finishWrite(fos)
      logger.atFine().log("Cached certificate to %s", cacheFile.absolutePath)
    } catch (e: IOException) {
      logger
        .atWarning()
        .withCause(e)
        .log("Failed to cache certificate to device storage: %s", cacheFile.absolutePath)
      if (fos != null) {
        atomicFile.failWrite(fos)
      }
    }
  }

  private fun getCacheFile(filename: String): File {
    val cacheDir = File(context.filesDir, CACHE_DIR_NAME).apply { mkdirs() }
    return File(cacheDir, filename)
  }

  private fun getFilename(type: PiCertificateCache.CertificateType): String {
    return when (type) {
      PiCertificateCache.CertificateType.OAK_CT -> oakCtFilename
      PiCertificateCache.CertificateType.TCA_ROOT -> tcaRootFilename
    }
  }

  companion object {
    @VisibleForTesting internal const val CACHE_DIR_NAME = "pi_certs"
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
