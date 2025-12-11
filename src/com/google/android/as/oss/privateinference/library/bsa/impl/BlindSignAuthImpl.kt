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

import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth
import com.google.android.`as`.oss.privateinference.library.bsa.jni.BlindSignAuthJniBridge
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.oakutil.PrivateInferenceClientTimerNames
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import com.google.protobuf.ByteString

/** The real implementation of BlindSignAuth, using the JNI bridge. */
class BlindSignAuthImpl(
  val messageInterface: BlindSignAuth.MessageInterface,
  val attester: BlindSignAuth.Attester,
  private val timerSet: TimerSet,
) : BlindSignAuth {

  val blindSignAuth = BlindSignAuthJniBridge(messageInterface)

  override suspend fun createProxyTokens(numTokens: Int): List<ProxyToken> =
    timerSet.start(PrivateInferenceClientTimerNames.CREATE_PROXY_TOKEN).use {
      blindSignAuth
        .getAttestationTokens(
          numTokens = numTokens,
          proxyLayer = BlindSignAuthJniBridge.ProxyLayer.PROXY_B,
          tokenChallenge = null,
          attester = attester,
        )
        .map { token -> ProxyToken(token.token, token.expiration) }
    }

  override suspend fun createArateaTokens(
    numTokens: Int,
    challengeData: ByteArray,
  ): List<ArateaToken> =
    timerSet.start(PrivateInferenceClientTimerNames.CREATE_ARATEA_TOKEN).use {
      blindSignAuth
        .getAttestationTokens(
          numTokens = numTokens,
          proxyLayer = BlindSignAuthJniBridge.ProxyLayer.TERMINAL_LAYER,
          tokenChallenge = ByteString.copyFrom(challengeData),
          attester = attester,
        )
        .map { token -> ArateaToken(token.token) }
    }
}
