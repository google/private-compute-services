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

/** The names of the timers used by the client. */
object PrivateInferenceClientTimerNames {
  const val OPEN_NOISE_SESSION = "OpenNoiseSession"

  const val SESSION_HANDSHAKE = "SessionHandshake"

  const val SESSION_ATTESTATION = "SessionAttestation"

  const val GENERATE_KEY_PAIR_WITH_ATTESTATION = "GenerateKeyPairWithAttestation"

  const val GET_INITIAL_DATA = "GetInitialData"

  const val ATTEST_AND_SIGN = "AttestAndSign"

  const val CREATE_ARATEA_TOKEN = "CreateArateaToken"

  const val CREATE_PROXY_TOKEN = "CreateProxyToken"

  const val GET_PROXY_TOKEN = "GetProxyToken"

  const val GET_PROXY_CONFIG = "GetProxyConfig"

  const val FETCH_PROXY_CONFIG = "FetchProxyConfig"

  const val MASQUE_TUNNEL_SETUP = "MasqueTunnelSetup"

  const val FIRST_INFERENCE = "FirstInferenceLatency"

  const val STABLE_INFERENCE = "StableInferenceLatency"
}
