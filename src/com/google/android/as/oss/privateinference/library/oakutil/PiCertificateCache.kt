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

/** Handles disk persistence for PI certificates. */
interface PiCertificateCache {
  enum class CertificateType {
    OAK_CT,
    TCA_ROOT,
  }

  /**
   * Reads the certificate from cache.
   *
   * @param type The type of the cached certificate.
   * @return The certificate content, or null if not found or empty.
   */
  fun read(type: CertificateType): String?

  /**
   * Writes the certificate to cache.
   *
   * @param type The type of the cached certificate.
   * @param content The certificate content to write.
   */
  fun write(type: CertificateType, content: String)
}
