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

import com.google.android.`as`.oss.privateinference.library.attestation.AndroidKeystoreAttesterImpl
import com.google.protobuf.ByteString
import javax.inject.Inject

/** A [DeviceAttestationGenerator] that uses Android Keystore for attestation. */
class AndroidKeystoreAttestationGenerator
@Inject
internal constructor(private val attester: AndroidKeystoreAttesterImpl) :
  PrivateInferenceOakAsyncClient.DeviceAttestationGenerator {

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
  override fun generateAttestation(
    attestationChallenge: ByteArray,
    includeDeviceProperties: Boolean,
  ): List<ByteString> = attester.generateAttestation(attestationChallenge, includeDeviceProperties)
}
