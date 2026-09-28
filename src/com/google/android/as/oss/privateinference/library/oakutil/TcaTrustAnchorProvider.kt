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

import com.google.common.util.concurrent.MoreExecutors
import com.google.oak.session.tls.OakSessionTlsException
import com.google.oak.session.tls.TrustAnchorProvider
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.inject.Inject

/**
 * A [TrustAnchorProvider] that downloads and provides the TCA root certificate using
 * [PiCertificateRepository].
 */
class TcaTrustAnchorProvider @Inject constructor(private val repository: PiCertificateRepository) :
  TrustAnchorProvider {

  @Throws(OakSessionTlsException::class)
  override fun getTrustAnchors(): List<ByteArray> {
    try {
      val pem = repository.getTcaRootCertificate(MoreExecutors.newDirectExecutorService()).get()
      val certFactory = CertificateFactory.getInstance("X.509")
      val certs = certFactory.generateCertificates(pem.byteInputStream())
      return certs.map { (it as X509Certificate).encoded }
    } catch (e: Exception) {
      if (e is InterruptedException) {
        Thread.currentThread().interrupt()
      }
      throw OakSessionTlsException("Failed to get trust anchors", e)
    }
  }
}
