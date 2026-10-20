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

import androidx.annotation.VisibleForTesting
import com.google.android.`as`.oss.privateinference.Annotations.OakCtNamespace
import com.google.common.util.concurrent.MoreExecutors
import com.google.oak.session.tls.CustomCertVerifier
import com.google.security.oak.oakct.OakCtVerifier
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Exception thrown when OakCT certificate verification fails or when cached OakCT root certificates
 * are unavailable.
 *
 * Subclasses [CertificateException] to conform to [CustomCertVerifier.verify]'s contract while
 * allowing callers to catch specific OakCT verification failures.
 */
class OakCtCertificateException(message: String, cause: Throwable? = null) :
  CertificateException(message, cause)

/**
 * A [CustomCertVerifier] for Private Inference that verifies the incoming server certificate chain
 * against an OakCT certificate chain using [OakCtVerifier].
 */
@Singleton
class OakCtCustomCertVerifier
@VisibleForTesting
internal constructor(
  private val verifier: OakCtVerifier,
  private val oakCtCerts: Array<X509Certificate>?,
  private val namespace: String,
  private val baseDomain: String = DEFAULT_BASE_DOMAIN,
  private val repository: PiCertificateRepository? = null,
  private val tcaRootCert: X509Certificate? = null,
) : CustomCertVerifier {
  @Inject
  constructor(
    repository: PiCertificateRepository,
    @OakCtNamespace namespace: String,
  ) : this(
    verifier = createOakCtVerifier(),
    oakCtCerts = null,
    namespace = namespace,
    baseDomain = DEFAULT_BASE_DOMAIN,
    repository = repository,
    tcaRootCert = null,
  )

  override fun verify(certChainDer: List<ByteArray>, standardResult: CertificateException?) {
    if (standardResult != null) {
      throw OakCtCertificateException(
        "Standard certificate chain verification failed",
        standardResult,
      )
    }
    val certs =
      try {
        oakCtCerts
          ?: repository?.let { loadOakCtCertChain(it) }
          ?: throw OakCtCertificateException("No OakCT certificates or repository provided")
      } catch (e: CertificateException) {
        throw e
      } catch (e: Exception) {
        throw OakCtCertificateException("Failed to load OakCT certificates", e)
      }
    // Convert the TCA root certificate from the repository to PEM format.
    // The TCA root cert is passed as verifier.verify's content arg,
    // and oakCtCerts is passed as verifier.verify's contentCerts arg.
    val rootCert =
      try {
        tcaRootCert
          ?: repository?.let { loadTcaRootCert(it) }
          ?: throw OakCtCertificateException("No TCA root certificate or repository provided")
      } catch (e: CertificateException) {
        throw e
      } catch (e: Exception) {
        throw OakCtCertificateException("Failed to load TCA root certificate", e)
      }
    val pemContent = getPemForCert(rootCert)
    try {
      verifier.verify(
        ByteArrayInputStream(pemContent.toByteArray(UTF_8)),
        namespace,
        baseDomain,
        certs,
      )
    } catch (e: Exception) {
      throw OakCtCertificateException("OakCT verification failed for server certificate", e)
    }
  }

  companion object {
    const val DEFAULT_BASE_DOMAIN = "oakct.transparentrelease.goog"
    private const val PEM_HEADER = "-----BEGIN CERTIFICATE-----"
    private const val PEM_FOOTER = "-----END CERTIFICATE-----"

    private fun getPemForCert(cert: X509Certificate): String {
      val text =
        Base64.getMimeEncoder(64, byteArrayOf('\n'.code.toByte())).encodeToString(cert.encoded)
      val formattedText = if (text.endsWith("\n")) text else "$text\n"
      return "$PEM_HEADER\n$formattedText$PEM_FOOTER\n"
    }

    private fun createOakCtVerifier(): OakCtVerifier {
      val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
      tmf.init(null as KeyStore?) // Use default platform CAs.
      val trustManager =
        checkNotNull(tmf.trustManagers.filterIsInstance<X509TrustManager>().firstOrNull()) {
          "No X509TrustManager found"
        }
      return OakCtVerifier(trustManager)
    }

    private fun loadOakCtCertChain(repository: PiCertificateRepository): Array<X509Certificate> {
      try {
        val pem = repository.getOakCtCertificate(MoreExecutors.newDirectExecutorService()).get()
        val certFactory = CertificateFactory.getInstance("X.509")
        val certs =
          certFactory
            .generateCertificates(ByteArrayInputStream(pem.toByteArray(UTF_8)))
            .map { it as X509Certificate }
            .toTypedArray()
        if (certs.isEmpty()) {
          throw OakCtCertificateException("OakCT certificate not available")
        }
        return certs
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw OakCtCertificateException("Interrupted while loading OakCT certificates", e)
      } catch (e: ExecutionException) {
        throw OakCtCertificateException("Failed to load OakCT certificates from repository", e)
      }
    }

    private fun loadTcaRootCert(repository: PiCertificateRepository): X509Certificate {
      try {
        val pem = repository.getTcaRootCertificate(MoreExecutors.newDirectExecutorService()).get()
        val certFactory = CertificateFactory.getInstance("X.509")
        val certs =
          certFactory.generateCertificates(ByteArrayInputStream(pem.toByteArray(UTF_8))).map {
            it as X509Certificate
          }
        if (certs.isEmpty()) {
          throw OakCtCertificateException("TCA root certificate not available")
        }
        return certs.first()
      } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
        throw OakCtCertificateException("Interrupted while loading TCA root certificate", e)
      } catch (e: ExecutionException) {
        throw OakCtCertificateException("Failed to load TCA root certificate from repository", e)
      }
    }
  }
}
