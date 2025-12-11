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

import com.google.oak.remote_attestation.AttestationVerificationClock
import com.google.oak.session.AttestationPublisher
import com.google.oak.session.OakSessionConfigBuilder

/** Helpers to get a peer-attested [OakSessionConfigBuilder] for Private Inference clients. */
object PeerAttestedClientSessionConfigBuilder {
  init {
    System.loadLibrary("pi_client_session_config_jni")
  }

  /**
   * Creates a new [OakSessionConfigBuilder] with the given dependencies.
   *
   * @param publicKeyset The public keyset for attestation verification.
   * @param clock The clock to use for attestation verification time validation.
   * @param An optional publisher to publisher attestation evidence on successful session setups.
   */
  @JvmStatic
  @JvmOverloads
  fun get(
    publicKeyset: ByteArray,
    clock: AttestationVerificationClock,
    publisher: AttestationPublisher? = null,
  ): OakSessionConfigBuilder {
    return nativeGet(publicKeyset, clock, publisher)
  }

  @JvmStatic
  private external fun nativeGet(
    publicKeyset: ByteArray,
    clock: AttestationVerificationClock,
    publisher: AttestationPublisher?,
  ): OakSessionConfigBuilder
}
