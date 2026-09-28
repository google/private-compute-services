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
import androidx.annotation.VisibleForTesting
import com.google.oak.session.tls.CustomCertVerifier
import com.google.security.oak.oakct.OakCtVerifier
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Base64
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
  private val namespace: String = DEFAULT_NAMESPACE,
  private val baseDomain: String = DEFAULT_BASE_DOMAIN,
  private val context: Context? = null,
) : CustomCertVerifier {
  @Inject
  constructor(
    @ApplicationContext context: Context
  ) : this(
    verifier = createOakCtVerifier(),
    oakCtCerts = null,
    namespace = DEFAULT_NAMESPACE,
    baseDomain = DEFAULT_BASE_DOMAIN,
    context = context,
  )

  override fun verify(certChainDer: List<ByteArray>, standardResult: CertificateException?) {
    if (standardResult != null) {
      throw OakCtCertificateException(
        "Standard certificate chain verification failed",
        standardResult,
      )
    }
    if (certChainDer.isEmpty()) {
      throw OakCtCertificateException("Certificate chain is empty")
    }
    val certs =
      try {
        oakCtCerts
          ?: context?.let { getCachedOakCtCert(it) }
          ?: throw OakCtCertificateException("No OakCT certificates or context provided")
      } catch (e: CertificateException) {
        throw e
      } catch (e: Exception) {
        throw OakCtCertificateException("Failed to load OakCT certificates", e)
      }
    // Convert DER bytes of the root certificate (last in certChainDer) to PEM format.
    // The TLS certChainDer's root cert is passed as verifier.verify's content arg,
    // and oakCtCerts is passed as verifier.verify's contentCerts arg.
    val rootCertDer = certChainDer.last()
    val certFactory = CertificateFactory.getInstance("X.509")
    val rootCert =
      certFactory.generateCertificate(ByteArrayInputStream(rootCertDer)) as X509Certificate
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
    const val DEFAULT_NAMESPACE = "tcadev"
    const val DEFAULT_BASE_DOMAIN = "oakct.transparentrelease.goog"
    private const val PEM_HEADER = "-----BEGIN CERTIFICATE-----"
    private const val PEM_FOOTER = "-----END CERTIFICATE-----"
    @VisibleForTesting internal const val CACHE_DIR_NAME = "pi_certs"
    @VisibleForTesting internal const val DEFAULT_CERT_FILE_NAME = "dev_oakct_cert.pem"

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

    private fun getCachedOakCtCert(context: Context): Array<X509Certificate> {
      val cacheDir = File(context.filesDir, CACHE_DIR_NAME)
      val cacheFile = File(cacheDir, DEFAULT_CERT_FILE_NAME)
      if (!cacheFile.exists() || cacheFile.length() == 0L) {
        throw OakCtCertificateException("OakCT certificate not available in cache")
      }
      val pemContent = cacheFile.readText(UTF_8)
      val certFactory = CertificateFactory.getInstance("X.509")
      return certFactory
        .generateCertificates(ByteArrayInputStream(pemContent.toByteArray(UTF_8)))
        .map { it as X509Certificate }
        .toTypedArray()
    }
  }
}
