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

import com.google.android.`as`.oss.privateinference.util.timers.Timers
import com.google.oak.session.v1.SessionRequest
import com.google.oak.session.v1.SessionResponse
import com.google.search.mdi.privatearatea.proto.PrivateArateaServiceGrpc
import com.google.search.mdi.privatearatea.proto.PrivateTlsServiceGrpc
import com.google.search.mdi.privatearatea.proto.TlsSessionRequest
import com.google.search.mdi.privatearatea.proto.TlsSessionResponse
import io.grpc.stub.StreamObserver
import java.util.function.Consumer

class RequestLoggingHelpers(private val timers: Timers) {
  private var handshakeTimer: Timers.Timer? = null
  private var attestTimer: Timers.Timer? = null
  private var tlsHandshakeComplete = false

  fun startSessionWithHandshakeLogging(
    asyncStub: PrivateArateaServiceGrpc.PrivateArateaServiceStub,
    responseObserver: StreamObserver<SessionResponse>,
  ): StreamObserver<SessionRequest> {
    return StreamObserverHook(
      asyncStub.startNoiseSession(StreamObserverHook(responseObserver, this::logHandshakeResponse)),
      this::logHandshakeRequest,
    )
  }

  fun startTlsSessionWithHandshakeLogging(
    tlsStub: PrivateTlsServiceGrpc.PrivateTlsServiceStub,
    responseObserver: StreamObserver<TlsSessionResponse>,
  ): StreamObserver<TlsSessionRequest> {
    return TlsStreamObserverHook(
      tlsStub.tlsSession(StreamObserverHook(responseObserver, this::logTlsHandshakeResponse)),
      this::logTlsHandshakeRequest,
      this::onTlsHandshakeComplete,
    )
  }

  private fun logHandshakeRequest(request: SessionRequest) {
    when (request.requestCase) {
      SessionRequest.RequestCase.HANDSHAKE_REQUEST ->
        handshakeTimer =
          timers.start(PrivateInferenceClientTimerNames.OAK_SESSION_PERFORM_HANDSHAKE_STEP)
      SessionRequest.RequestCase.ATTEST_REQUEST ->
        attestTimer =
          timers.start(PrivateInferenceClientTimerNames.OAK_SESSION_EXCHANGE_ATTESTATION_EVIDENCE)
      else -> {}
    }
  }

  private fun logHandshakeResponse(response: SessionResponse) {
    when (response.responseCase) {
      SessionResponse.ResponseCase.HANDSHAKE_RESPONSE -> {
        handshakeTimer?.stop()
        handshakeTimer = null
      }
      SessionResponse.ResponseCase.ATTEST_RESPONSE -> {
        attestTimer?.stop()
        attestTimer = null
      }
      else -> {}
    }
  }

  private fun logTlsHandshakeRequest(request: TlsSessionRequest) {
    // if (!tlsHandshakeComplete) {
    //   handshakeTimer =
    //     timers.start(<TLS_HANDSHAKE_TIMER_NAME>)
    // }
  }

  private fun logTlsHandshakeResponse(response: TlsSessionResponse) {
    if (!tlsHandshakeComplete) {
      handshakeTimer?.stop()
      handshakeTimer = null
    }
  }

  private fun onTlsHandshakeComplete() {
    tlsHandshakeComplete = true
    handshakeTimer?.stop()
    handshakeTimer = null
  }

  /** A small utility to allow us to inspect requests and responses for logging. */
  private class StreamObserverHook<T>(val delegate: StreamObserver<T>, val hook: Consumer<T>) :
    StreamObserver<T> {
    override fun onNext(value: T) {
      hook.accept(value)
      delegate.onNext(value)
    }

    override fun onError(t: Throwable) {
      delegate.onError(t)
    }

    override fun onCompleted() {
      delegate.onCompleted()
    }
  }

  private class TlsStreamObserverHook(
    val delegate: StreamObserver<TlsSessionRequest>,
    val hook: Consumer<TlsSessionRequest>,
    val onCompleteHook: Runnable,
  ) : StreamObserver<TlsSessionRequest>, StreamObserverTlsSessionClient.HandshakeListener {
    override fun onNext(value: TlsSessionRequest) {
      hook.accept(value)
      delegate.onNext(value)
    }

    override fun onError(t: Throwable) {
      delegate.onError(t)
    }

    override fun onCompleted() {
      delegate.onCompleted()
    }

    override fun onHandshakeComplete() {
      onCompleteHook.run()
    }
  }
}
