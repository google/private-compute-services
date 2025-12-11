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

package com.google.android.`as`.oss.privateinference.library.bsa

import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.errorprone.annotations.ThreadSafe

/**
 * Thin wrapper around [BlindSignAuth] responsible for calling the
 * [BlindSignAuth.getAttestationTokens] and [BlindSignAuth.attestAndSign] functions correctly to
 * generate batches of [ProxyToken]s or [ArateaToken]s.
 */
interface BlindSignAuth {
  /** A message interface to implement for BlindSignAuth operations. */
  @ThreadSafe
  interface MessageInterface {
    /**
     * Handle an initial data request.
     *
     * @param request The request message as raw bytes.
     * @return The response message, as raw bytes.
     */
    suspend fun initialData(request: ByteArray): ByteArray

    /**
     * Handle an attest and sign request.
     *
     * @param request The request message as raw bytes.
     * @return The response message, as raw bytes.
     */
    suspend fun attestAndSign(request: ByteArray): ByteArray
  }

  /**
   * A function that provides attestation data for the given challenge.
   *
   * If the attester throws an exception, it will be propagated to the original Java caller via an
   * absl::Status in the C++ code. To improve error reporting, you can use the StatusException class
   * to wrap your exceptions, providing the appropriate error code for the failure mode.
   */
  @ThreadSafe
  fun interface Attester {
    fun attest(challenge: ByteArray): List<ByteArray>
  }

  /** Creates and returns [numTokens] instances of [ProxyToken]. */
  suspend fun createProxyTokens(numTokens: Int): List<ProxyToken>

  /**
   * Creates and returns [numTokens] instances of [ArateaToken] using the provided [challengeData].
   */
  suspend fun createArateaTokens(numTokens: Int, challengeData: ByteArray): List<ArateaToken>
}
