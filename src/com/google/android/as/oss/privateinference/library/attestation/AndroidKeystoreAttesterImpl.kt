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

package com.google.android.`as`.oss.privateinference.library.attestation

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.VisibleForTesting
import com.google.common.flogger.GoogleLogger
import com.google.errorprone.annotations.ThreadSafe
import com.google.protobuf.ByteString
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

/**
 * Generate device attestation against a server-provided challenge using Android KeyStore.
 *
 * @param context The application context.
 */
@ThreadSafe
@Singleton
class AndroidKeystoreAttesterImpl
@VisibleForTesting
@Inject
constructor(@ApplicationContext context: Context) {

  // Lock to prevent concurrent access to the underlying KeyStore.
  private val keystoreLock = ReentrantLock()

  private val hasDeviceIdFeature = context.packageManager.hasSystemFeature(DEVICE_ID_FEATURE_NAME)

  /**
   * Generate device attestation against a server-provided challenge.
   *
   * This method generates a new key pair in Android KeyStore, and returns the generated certificate
   * chain which includes the attestation that was generated against the provided challenge.
   *
   * @param attestationChallenge The challenge provided by the server.
   * @return The generated certificate chain, where each certificate is encoded to bytes and
   *   conatenated into a single array.
   */
  fun generateAttestation(
    attestationChallenge: ByteArray,
    includeDeviceProperties: Boolean,
  ): List<ByteString> =
    keystoreLock.withLock {
      logger
        .atFine()
        .log(
          "Generating device attestation with includeDeviceProperties: %s",
          includeDeviceProperties,
        )
      // Create an attested KeyPair in order to create a new cert chain with our attestation
      // challenge.
      // The EC algorithm was chosen because it generates a new keypair the quickest.
      //
      // We will not actually use the generated key pair, we only want to use the generated cert
      // chain to provide device attestation to the server.
      val keyPairGenerator =
        KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE)

      val includeDeviceProperties =
        if (includeDeviceProperties && !hasDeviceIdFeature) {
          logger
            .atWarning()
            .log("Device does not support device ID attestation but properties were requested.")
          false
        } else {
          includeDeviceProperties
        }

      keyPairGenerator.initialize(
        KeyGenParameterSpec.Builder(ANDROID_KEY_STORE_ALIAS, KeyProperties.PURPOSE_SIGN)
          .setKeySize(224)
          .setDevicePropertiesAttestationIncluded(includeDeviceProperties)
          .setAttestationChallenge(attestationChallenge)
          .build()
      )

      // Generate the key pair. This will result in calls to both generate_key() and
      // attest_key() at the keymaster2 HAL.
      val unused = keyPairGenerator.generateKeyPair()

      // Get the certificate chain
      val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
      keyStore.load(null)

      (keyStore.getCertificateChain(ANDROID_KEY_STORE_ALIAS) ?: emptyArray()).map {
        ByteString.copyFrom(it.encoded)
      }
    }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    private const val ANDROID_KEY_STORE = "AndroidKeyStore"
    // Alias of the entry under which the generated key will appear in Android KeyStore. This alias
    // is constant so, new keys will overwrite older keys on generation.
    private const val ANDROID_KEY_STORE_ALIAS = "PiAttestationKey"

    private const val DEVICE_ID_FEATURE_NAME = "android.software.device_id_attestation"
  }
}
