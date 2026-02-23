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

package com.google.android.`as`.oss.privateinference.library.bsa.jni

import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth.Attester
import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth.MessageInterface
import com.google.common.flogger.GoogleLogger
import com.google.common.util.concurrent.ListenableFuture
import com.google.errorprone.annotations.ThreadSafe
import com.google.protobuf.ByteString
import io.grpc.Status
import io.grpc.StatusException
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * A wrapper around the native BlindSignAuth library.
 *
 * This provides access to the functions in a C++ BlindSignAuth object that are needed to acquire
 * tokens for Private Inference.
 *
 * There is not a 1:1 mapping between instances of this class and instances of the underlying C++
 * class. Since that class is stateless, we avoid the need to manage instances across the JNI
 * boundary by having the methods create a new instance each time.
 *
 * @param messageInterfaceFactory A factory that generates a new MessageInterface for a scope.
 */
@ThreadSafe
class BlindSignAuthJniBridge(val messageInterface: MessageInterface) {
  /**
   * The proxy layer that we need tokens for.
   *
   * This matches the ProxyLayer enum in the C++ library.
   */
  enum class ProxyLayer {
    /** Not used by our scheme. */
    PROXY_A,
    /** The proxy that we transit through to reach our server. */
    PROXY_B,
    /** Tokens for our server. */
    TERMINAL_LAYER,
  }

  /**
   * The request type used by the [MessageInterface].
   *
   * This matches the BlindSignMessageRequestType enum in the C++ library.
   */
  private enum class BlindSignMessageRequestType {
    UNKNOWN,
    /** Request a challenge. */
    GET_INITIAL_DATA,
    /** Request tokens. Not used by our implementation. */
    AUTH_AND_SIGN,
    /** Request attested tokens. */
    ATTEST_AND_SIGN,
  }

  /** A common interface for callbacks that report errors via absl::Status. */
  private interface ErrorCallback {
    /**
     * A callback representing an absl::Status error.
     *
     * @param errorCode The error code from the C++ status.
     * @param errorMessage The error message from the C++ status.
     */
    fun onError(throwable: Throwable)
  }

  /**
   * A token returned by the signing logic.
   *
   * This is the counterpart to the BlindSignToken in the C++ library.
   *
   * Our use case doesn't use the geo_hint field, so we don't expose it here.
   */
  class BlindSignToken(val token: ByteArray, val expiration: Instant) {
    constructor(
      token: ByteArray,
      expiration_epoch_milli: Long,
    ) : this(token, Instant.ofEpochMilli(expiration_epoch_milli))
  }

  /**
   * A callback from the signing library that provides the signed attestated tokens.
   *
   * This is the counterpart to the SignedTokenCallback in the C++ library.
   *
   * This is a single-use callback, so calling it more than once will throw an exception.
   */
  private interface SignedTokenCallback : ErrorCallback {
    /**
     * Called when the signed tokens are ready.
     *
     * @param tokens The signed tokens.
     */
    fun onSignedTokens(tokens: List<BlindSignToken>)
  }

  /**
   * A callback from the signing library that requests attestation for the given challenge.
   *
   * This is the counterpart to the SignedTokenCallback in the C++ library.
   *
   * This is a single-use callback, so calling it more than once will throw an exception.
   */
  private interface AttestationDataCallback {
    fun onChallengeData(challenge: ByteArray, callback: NativeAttestAndSignCallback)
  }

  /**
   * A callback into C++ from AttestationDataCallback.
   *
   * This is the mechanism for an asynchronous response to the AttestationDataCallback. After
   * generating attestation data for the provided challenge, the result can be provided using the
   * `onAttestationData` method of this callback.
   *
   * Instance of this class are holding a pointer a a C++ function that must be freed.
   *
   * Calling either [onAttestationData] or [release] will free the pointer.
   *
   * This is a single-use callback, so calling it more than once will throw an exception.
   */
  private class NativeAttestAndSignCallback(contextPtr: Long) {
    private val contextPtr = AtomicLong(contextPtr)

    fun onAttestationData(attestation: Array<ByteArray>, tokenChallenge: ByteArray?) {
      val contextPtr = this.contextPtr.getAndSet(0)
      check(contextPtr != 0L) { "Callback already freed" }
      nativeOnAttestationData(contextPtr, attestation, tokenChallenge)
    }

    fun onError(throwable: Throwable) {
      val contextPtr = this.contextPtr.getAndSet(0)
      check(contextPtr != 0L) { "Callback already freed" }
      val errorCode = throwable.toStatusCode()
      val errorMessage = (throwable.message ?: "Unknown error").toByteArray()
      nativeOnAttestationDataError(contextPtr, errorCode, errorMessage)
    }

    /**
     * Release the callback reference.
     *
     * It's safe to call this defensively even if the callback has already been called or released.
     */
    fun release() {
      val contextPtr = this.contextPtr.getAndSet(0)
      if (contextPtr != 0L) {
        nativeRelease(contextPtr)
      }
    }

    companion object {
      private @JvmStatic external fun nativeOnAttestationData(
        contextPtr: Long,
        attestation: Array<ByteArray>,
        tokenChallenge: ByteArray?,
      )

      private @JvmStatic external fun nativeOnAttestationDataError(
        contextPtr: Long,
        statusCode: Int,
        errorMessage: ByteArray,
      )

      private @JvmStatic external fun nativeRelease(contextPtr: Long)
    }
  }

  /**
   * A callback into C++ from MessageInterface.
   *
   * When the C++ code needs to make a request, it will call MessageInterface.doRequest. The final
   * argument is this callback type, which contains an internal pointer to a C++ callback.
   *
   * Instance of this class are holding a pointer a a C++ function that must be freed.
   *
   * Calling either [onResponse] or [release] will free the pointer.
   *
   * This is a single-use callback, so calling it more than once will throw an exception.
   */
  private class NativeMessageCallback(contextPtr: Long) {
    private val contextPtr = AtomicLong(contextPtr)

    fun onResponse(statusCode: Int, body: ByteArray) {
      val contextPtr = this.contextPtr.getAndSet(0)
      check(contextPtr != 0L) { "Callback already freed" }
      nativeOnResponse(contextPtr, statusCode, body)
    }

    fun onError(throwable: Throwable) {
      val contextPtr = this.contextPtr.getAndSet(0)
      check(contextPtr != 0L) { "Callback already freed" }
      val errorCode = throwable.toStatusCode()
      val errorMessage = (throwable.message ?: "Unknown error").toByteArray()
      nativeOnResponseError(contextPtr, errorCode, errorMessage)
    }

    /**
     * Release the callback reference.
     *
     * It's safe to call this defensively even if the callback has already been called or released.
     */
    fun release() {
      val contextPtr = this.contextPtr.getAndSet(0)
      if (contextPtr != 0L) {
        nativeRelease(contextPtr)
      }
    }

    companion object {
      private @JvmStatic external fun nativeOnResponse(
        contextPtr: Long,
        statusCode: Int,
        body: ByteArray,
      )

      private @JvmStatic external fun nativeOnResponseError(
        contextPtr: Long,
        statusCode: Int,
        errorMessage: ByteArray,
      )

      private @JvmStatic external fun nativeRelease(contextPtr: Long)
    }
  }

  /* See: BlindSignMessageInterface in BSA library. */
  @ThreadSafe
  private class BridgeMessageInterface(
    val scope: CoroutineScope,
    val messageInterface: MessageInterface,
  ) {
    /**
     * Called when a message is received.
     *
     * The provided callback *must* be called, either with [onResponse] or [onError].
     *
     * If, for some reason, you don't want to call the callback, you must call [release] on it to
     * free up the native peer resources.
     *
     * @param requestType The request type.
     * @param authorizationHeader The authorization header.
     * @param message The message.
     * @param callback The callback to be called when the request is complete.
     */
    fun doRequest(
      requestType: BlindSignMessageRequestType,
      authorizationHeader: ByteArray?,
      @ThreadSafe.Suppress(reason = "not mutated") message: ByteArray,
      @ThreadSafe.Suppress(reason = "only called once") callback: NativeMessageCallback,
    ) {
      scope.launch {
        try {
          when (requestType) {
            BlindSignMessageRequestType.GET_INITIAL_DATA ->
              messageInterface.initialData(message).let { callback.onResponse(0, it) }
            BlindSignMessageRequestType.ATTEST_AND_SIGN ->
              messageInterface.attestAndSign(message).let { callback.onResponse(0, it) }
            else ->
              callback.onError(IllegalArgumentException("Unsupported request type $requestType"))
          }
        } catch (e: Exception) {
          logger.atWarning().withCause(e).log("Failed to do request of type %s", requestType)
          callback.onError(e)
        }
      }
    }
  }

  /**
   * Request signed and attested tokens.
   *
   * This is future-based version for Java compatibility. You'll need to provide a coroutine scope
   * that will be used to execute the underlying suspending function.
   *
   * @param scope The coroutine scope to use when calling the suspending function.
   * @param numTokens The number of tokens to request.
   * @param proxyLayer The proxy layer to request tokens for.
   * @param tokenChallenge The challenge to use if this is a session-bound token (optional).
   * @param attester A function that provides attestation data for the given challenge.
   */
  fun getAttestationTokens(
    scope: CoroutineScope,
    numTokens: Int,
    proxyLayer: ProxyLayer,
    tokenChallenge: ByteString?,
    attester: Attester,
  ): ListenableFuture<List<BlindSignToken>> =
    scope.future { getAttestationTokens(numTokens, proxyLayer, tokenChallenge, attester) }

  /**
   * The call to begin the token fetching process. The callback returns a challenge for attestation.
   *
   * This is the counterpart to the GetAttestationTokens in the C++ library.
   *
   * @param numTokens The number of tokens to request.
   * @param proxyLayer The proxy layer to request tokens for.
   * @param tokenChallenge The challenge to use if this is a session-bound token (optional).
   * @param attester A function that provides attestation data for the given challenge.
   */
  suspend fun getAttestationTokens(
    numTokens: Int,
    proxyLayer: ProxyLayer,
    tokenChallenge: ByteString?,
    attester: Attester,
  ): List<BlindSignToken> = coroutineScope {
    suspendCancellableCoroutine { continuation ->
      nativeGetAttestationTokens(
        BridgeMessageInterface(this, messageInterface),
        numTokens,
        proxyLayer.ordinal,
        object : AttestationDataCallback {
          override fun onChallengeData(
            challenge: ByteArray,
            callback: NativeAttestAndSignCallback,
          ) {
            try {
              callback.onAttestationData(
                attester.attest(challenge).toTypedArray(),
                tokenChallenge?.toByteArray(),
              )
            } catch (e: Exception) {
              logger.atWarning().withCause(e).log("Failed to attest")
              callback.onError(e)
            }
          }
        },
        object : SignedTokenCallback {
          override fun onSignedTokens(tokens: List<BlindSignToken>) {
            continuation.resume(tokens)
          }

          override fun onError(throwable: Throwable) {
            continuation.resumeWithException(throwable)
          }
        },
      )
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    private fun Throwable.toStatusCode(): Int {
      return when (this) {
        is StatusException -> this.status.code.value()
        is IllegalArgumentException -> Status.Code.INVALID_ARGUMENT.value()
        else -> Status.Code.UNKNOWN.value()
      }
    }

    @JvmStatic
    private external fun nativeGetAttestationTokens(
      messageInterface: BridgeMessageInterface,
      numTokens: Int,
      proxyLayer: Int,
      attestationDataCallback: AttestationDataCallback,
      signedTokenCallback: SignedTokenCallback,
    )

    @JvmStatic
    fun createStatusException(code: Int, message: String): StatusException {
      val status = Status.fromCodeValue(code).withDescription(message)
      return StatusException(status)
    }
  }
}
