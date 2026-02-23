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

/**
 * The names of the timers used by the client to measure the latency of setting up the Private
 * Inference (Pcs) channel.
 *
 * Private Inference Channel Setup Process
 *
 * The setup process generally follows this sequence:
 * 1. **Proxy Setup:** Fetch configuration and establish tokens (`IPP_FETCH_PROXY_CONFIG`,
 *    `IPP_CREATE_PROXY_TOKEN`).
 * 2. **Tunneling:** Set up the MASQUE tunnel (`IPP_MASQUE_TUNNEL_SETUP`).
 * 3. **Oak Session Establishment:**
 *     * Fetch and verify attestation evidence (`OAK_SESSION_EXCHANGE_ATTESTATION_EVIDENCE`).
 *     * Perform the noise handshake to exchange keys (`OAK_SESSION_PERFORM_HANDSHAKE_STEP`).
 *     * These combined steps are measured by `OAK_SESSION_ESTABLISH_STREAM`.
 * 4. **Authentication:** Authenticate the terminal token (`IPP_ANONYMOUS_TOKEN_AUTH`).
 * 5. **Inference:** The channel is ready for the first inference request (`INFERENCE_FIRST`).
 */
object PrivateInferenceClientTimerNames {
  /**
   * Timer for the entire process of setting up the Private Inference channel. This measures the
   * total time from initiation until the client is ready to make its first inference request.
   */
  const val END_TO_END_PI_CHANNEL_SETUP = "END_TO_END_PI_CHANNEL_SETUP"

  /**
   * Timer for establishing an encrypted, attested channel (The Oak Session).
   *
   * This encompasses two sub-phases:
   * 1. **Attestation Step:** Peers exchange identity evidence.
   * 2. **Noise Handshake Step:** Peers exchange encryption keys.
   */
  const val OAK_SESSION_ESTABLISH_STREAM = "OAK_SESSION_ESTABLISH_STREAM"

  /**
   * Timer for the first phase of the oak session establishment. Measures the time spent fetching
   * the attestation evidence from the server and verifying it.
   */
  const val OAK_SESSION_EXCHANGE_ATTESTATION_EVIDENCE = "OAK_SESSION_EXCHANGE_ATTESTATION_EVIDENCE"

  /**
   * Timer for the second phase of the oak session establishment. Measures the time spent performing
   * the peer encryption key exchange.
   */
  const val OAK_SESSION_PERFORM_HANDSHAKE_STEP = "OAK_SESSION_PERFORM_HANDSHAKE_STEP"

  /**
   * Timer for terminal token authentication.
   *
   * This occurs after the Oak session is open. It is effectively the first request sent in the
   * opened session on behalf of the client before the client actually receives the stream.
   */
  const val IPP_ANONYMOUS_TOKEN_AUTH = "IPP_ANONYMOUS_TOKEN_AUTH"

  /** Timer for generating a key pair with attestation. */
  const val DEVICE_ATTESTATION_GENERATE_KEY_PAIR = "GENERATE_KEY_PAIR_WITH_ATTESTATION_AUTH"

  /** Timer for getting initial data. */
  const val IPP_GET_INITIAL_DATA = "IPP_GET_INITIAL_DATA"

  /** Timer for attesting and signing. */
  const val IPP_ATTEST_AND_SIGN = "IPP_ATTEST_AND_SIGN"

  /** Timer for creating a terminal token. */
  const val IPP_CREATE_TERMINAL_TOKEN = "IPP_CREATE_TERMINAL_TOKEN"

  /**
   * Timer for creating a proxy token. This operation currently occurs in the background off the
   * critical session establishment path.
   */
  const val IPP_CREATE_PROXY_TOKEN = "IPP_CREATE_PROXY_TOKEN"

  /** Timer for getting a proxy token. */
  const val IPP_GET_PROXY_TOKEN = "IPP_GET_PROXY_TOKEN"

  /**
   * Timer for getting the proxy configuration. This operation currently occurs when the proxy
   * tunnel is being setup. This is usually once per the PI service lifetime on the first call to
   * perform private inference.
   */
  const val IPP_GET_PROXY_CONFIG = "IPP_GET_PROXY_CONFIG"

  /** Timer for fetching the proxy configuration. This should also happen at tunnel setup time. */
  const val IPP_FETCH_PROXY_CONFIG = "IPP_FETCH_PROXY_CONFIG"

  /** Timer for setting up the MASQUE tunnel. */
  const val IPP_MASQUE_TUNNEL_SETUP = "IPP_MASQUE_TUNNEL_SETUP"

  /** Timer for the latency of the first inference. */
  const val INFERENCE_FIRST = "INFERENCE_FIRST"

  /** Timer for the latency of stable inferences. */
  const val INFERENCE_STABLE = "INFERENCE_STABLE"
}
