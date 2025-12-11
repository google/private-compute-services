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

package com.google.android.`as`.oss.privateinference.library.bsa.impl

import android.content.Context
import com.google.android.`as`.oss.privateinference.library.attestation.AndroidKeystoreAttesterImpl
import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth
import com.google.android.`as`.oss.privateinference.library.oakutil.DeviceAttestationFlag
import com.google.errorprone.annotations.ThreadSafe
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * A [BlindSignAuth.Attester] that uses Android Keystore for attestation when enabled.
 *
 * When the device attestation flag is disabled, the attestation is simply a copy of the provided
 * challenge.
 */
@ThreadSafe
class AndroidKeystoreAttester
@Inject
constructor(
  @ApplicationContext context: Context,
  @field:ThreadSafe.Suppress(reason = "DeviceAttestationFlag is thread-safe.")
  private val deviceAttestationFlag: DeviceAttestationFlag,
  private val androidKeystoreAttesterImpl: AndroidKeystoreAttesterImpl,
) : BlindSignAuth.Attester {
  override fun attest(challenge: ByteArray): List<ByteArray> =
    if (deviceAttestationFlag.enabled()) {
      androidKeystoreAttesterImpl
        .generateAttestation(
          challenge,
          includeDeviceProperties = deviceAttestationFlag.useDeviceProperties(),
        )
        .map { it.toByteArray() }
    } else {
      // The current expectation for an issuance server that doesn't support attestation is that the
      // attestation is simply a copy of the provided challenge.
      listOf(challenge)
    }
}
