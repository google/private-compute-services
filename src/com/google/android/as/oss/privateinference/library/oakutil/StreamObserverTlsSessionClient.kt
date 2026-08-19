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

import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.oak.client.grpc.StreamObserverSessionClient
import com.google.oak.session.tls.OakSessionTlsContext
import com.google.oak.session.tls.ReceiveFunction
import com.google.oak.session.tls.SendFunction
import com.google.protobuf.ByteString
import com.google.search.mdi.privatearatea.proto.TlsSessionRequest
import com.google.search.mdi.privatearatea.proto.TlsSessionResponse
import com.google.search.mdi.privatearatea.proto.tlsSessionRequest
import io.grpc.Status
import io.grpc.stub.ClientCallStreamObserver
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.channels.Channel

/**
 * An asynchronous client for Oak TLS Session based on StreamObservers.
 *
 * Similar to [StreamObserverSessionClient], this class acts as an adapter between the underlying
 * gRPC TLS session protocol and the unencrypted byte stream observer used by the client.
 */
class StreamObserverTlsSessionClient
@Inject
constructor(
  private val oakSessionTlsContextProvider: Provider<@JvmSuppressWildcards OakSessionTlsContext>
) {
  /** A listener interface to notify hooks when the TLS handshake has completed. */
  interface HandshakeListener {
    fun onHandshakeComplete()
  }

  /**
   * Starts a new TLS Session.
   *
   * @param sessionStreamObserver the observer that will receive decrypted responses from the
   *   server, and that will be given an observer for sending application requests once the TLS
   *   handshake completes.
   * @param streamStarter is used to start the underlying gRPC bidirectional TLS stream.
   */
  @CanIgnoreReturnValue
  suspend fun startSession(
    sessionStreamObserver: StreamObserverSessionClient.OakSessionStreamObserver,
    streamStarter: (StreamObserver<TlsSessionResponse>) -> StreamObserver<TlsSessionRequest>,
  ): StreamObserverSessionClient.SessionHandle {
    // Note: If the channel capacity is ever changed to something finite, start paying attention to
    // trySend errors below.
    val incomingFrames = Channel<TlsSessionResponse>(Channel.UNLIMITED)
    val requestObserver: StreamObserver<TlsSessionRequest>?
    val responseObserver =
      object : StreamObserver<TlsSessionResponse> {
        override fun onNext(response: TlsSessionResponse) {
          // Ignoring trySend return value (unused) is OK because of the unlimited channel size.
          val unused = incomingFrames.trySend(response)
        }

        override fun onError(t: Throwable) {
          incomingFrames.close(t)
        }

        override fun onCompleted() {
          incomingFrames.close()
        }
      }

    requestObserver = streamStarter(responseObserver)

    val send = SendFunction { data ->
      requestObserver.onNext(tlsSessionRequest { frame = ByteString.copyFrom(data) })
    }
    val receive = ReceiveFunction {
      val result = incomingFrames.receiveCatching()
      if (result.isClosed) {
        val ex = result.exceptionOrNull()
        val status =
          if (ex != null) {
            Status.fromThrowable(ex).withDescription("Failed to read TLS frame").withCause(ex)
          } else {
            Status.ABORTED.withDescription("TLS stream closed prematurely without error")
          }
        throw status.asRuntimeException()
      }
      result.getOrThrow().frame.toByteArray()
    }

    val tlsContext = oakSessionTlsContextProvider.get()
    val initializedSession = tlsContext.newInitializedSession(send, receive)
    val session = initializedSession.session

    (requestObserver as? HandshakeListener)?.onHandshakeComplete()

    val clientRequests =
      object : StreamObserver<ByteString> {
        override fun onNext(value: ByteString) {
          val encrypted = session.encrypt(value.toByteArray())
          val bytes = ByteArray(encrypted.remaining()).apply { encrypted.get(this) }
          requestObserver.onNext(tlsSessionRequest { frame = ByteString.copyFrom(bytes) })
        }

        override fun onError(t: Throwable) {
          requestObserver.onError(t)
        }

        override fun onCompleted() {
          requestObserver.onCompleted()
        }
      }

    sessionStreamObserver.onSessionOpen(clientRequests)

    while (true) {
      val result = incomingFrames.receiveCatching()
      if (result.isClosed) {
        val ex = result.exceptionOrNull()
        if (ex != null) {
          sessionStreamObserver.onError(ex)
        } else {
          sessionStreamObserver.onCompleted()
        }
        break
      }
      val frame = result.getOrThrow().frame.toByteArray()
      if (frame.isEmpty()) continue
      val decrypted = session.decrypt(frame)
      if (decrypted.remaining() == 0) continue
      val decryptedBytes = ByteArray(decrypted.remaining()).apply { decrypted.get(this) }
      sessionStreamObserver.onNext(ByteString.copyFrom(decryptedBytes))
    }

    return object : StreamObserverSessionClient.SessionHandle {
      override fun cancel(message: String?, cause: Throwable?) {
        (requestObserver as? ClientCallStreamObserver<*>)?.cancel(message, cause)
      }
    }
  }
}
