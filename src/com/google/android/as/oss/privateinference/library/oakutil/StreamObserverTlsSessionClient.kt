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

import com.google.common.util.concurrent.MoreExecutors
import com.google.errorprone.annotations.CanIgnoreReturnValue
import com.google.oak.client.grpc.StreamObserverSessionClient
import com.google.oak.session.tls.OakSessionTlsContext
import com.google.oak.session.tls.OakSessionTlsException
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
import kotlinx.coroutines.guava.await

/**
 * An asynchronous client for Oak TLS Session based on StreamObservers.
 *
 * Similar to [StreamObserverSessionClient], this class acts as an adapter between the underlying
 * gRPC TLS session protocol and the unencrypted byte stream observer used by the client.
 */
class StreamObserverTlsSessionClient
@Inject
constructor(
  private val oakSessionTlsContextProvider: Provider<@JvmSuppressWildcards OakSessionTlsContext>,
  private val repository: PiCertificateRepository? = null,
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
    var streamSetup = setupStream(streamStarter)

    val tlsContext = oakSessionTlsContextProvider.get()
    val initializedSession =
      try {
        tlsContext.newInitializedSession(streamSetup.send, streamSetup.receive)
      } catch (e: OakSessionTlsException) {
        // Retry with fresh OakCT certificate from repository, in case the handshake failed due to
        // a missing or invalid certificate.
        repository
          ?.getOakCtCertificate(MoreExecutors.newDirectExecutorService(), forceRefresh = true)
          ?.await()

        // Cancel old stream
        (streamSetup.requestObserver as? ClientCallStreamObserver<*>)?.cancel(
          "Handshake failed, retrying",
          e,
        )

        // Restart the stream to ensure a clean state for the retry.
        streamSetup = setupStream(streamStarter)
        tlsContext.newInitializedSession(streamSetup.send, streamSetup.receive)
      }
    val session = initializedSession.session

    (streamSetup.requestObserver as? HandshakeListener)?.onHandshakeComplete()

    val clientRequests =
      object : StreamObserver<ByteString> {
        override fun onNext(value: ByteString) {
          val encrypted = session.encrypt(value.toByteArray())
          val bytes = ByteArray(encrypted.remaining()).apply { encrypted.get(this) }
          streamSetup.requestObserver.onNext(
            tlsSessionRequest { frame = ByteString.copyFrom(bytes) }
          )
        }

        override fun onError(t: Throwable) {
          streamSetup.requestObserver.onError(t)
        }

        override fun onCompleted() {
          streamSetup.requestObserver.onCompleted()
        }
      }

    sessionStreamObserver.onSessionOpen(clientRequests)

    while (true) {
      val result = streamSetup.incomingFrames.receiveCatching()
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
        (streamSetup.requestObserver as? ClientCallStreamObserver<*>)?.cancel(message, cause)
      }
    }
  }

  private data class StreamSetup(
    val send: SendFunction,
    val receive: ReceiveFunction,
    val incomingFrames: Channel<TlsSessionResponse>,
    val requestObserver: StreamObserver<TlsSessionRequest>,
  )

  private fun setupStream(
    streamStarter: (StreamObserver<TlsSessionResponse>) -> StreamObserver<TlsSessionRequest>
  ): StreamSetup {
    // Note: If the channel capacity is ever changed to something finite, start paying attention to
    // trySend errors below.
    val channel = Channel<TlsSessionResponse>(Channel.UNLIMITED)
    val responseObserver =
      object : StreamObserver<TlsSessionResponse> {
        override fun onNext(response: TlsSessionResponse) {
          // Ignoring trySend return value (unused) is OK because of the unlimited channel size.
          val unused = channel.trySend(response)
        }

        override fun onError(t: Throwable) {
          channel.close(t)
        }

        override fun onCompleted() {
          channel.close()
        }
      }

    val observer = streamStarter(responseObserver)

    val send = SendFunction { data ->
      observer.onNext(tlsSessionRequest { frame = ByteString.copyFrom(data) })
    }
    val receive = ReceiveFunction {
      val result = channel.receiveCatching()
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
    return StreamSetup(send, receive, channel, observer)
  }
}
