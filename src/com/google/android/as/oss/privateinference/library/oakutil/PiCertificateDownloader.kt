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

import java.io.IOException

/** Handles fetching PI certificate over HTTP given a URL. */
interface PiCertificateDownloader {
  enum class CertificateType {
    OAK_CT,
    TCA_ROOT,
  }

  /**
   * Downloads the certificate for the given type.
   *
   * @param type The type of the certificate to download.
   * @return The certificate content as a String.
   * @throws IOException if download fails.
   */
  @Throws(IOException::class) suspend fun download(type: CertificateType): String
}
