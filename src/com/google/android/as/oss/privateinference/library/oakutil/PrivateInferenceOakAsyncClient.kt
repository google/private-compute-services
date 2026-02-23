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

import com.google.android.`as`.oss.common.ExecutorAnnotations.PiExecutorQualifier
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.logging.PcsStatsEnums.ValueMetricId
import com.google.android.`as`.oss.privateinference.config.impl.ArateaAuthFlag
import com.google.android.`as`.oss.privateinference.library.PrivateInferenceRequestMetadata
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.CacheableArateaTokenParams
import com.google.android.`as`.oss.privateinference.logging.MetricIdMap
import com.google.android.`as`.oss.privateinference.logging.PcsStatsLogger
import com.google.android.`as`.oss.privateinference.util.timers.Annotations.PrivateInferenceClientTimers
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import com.google.android.`as`.oss.privateinference.util.timers.Timers
import com.google.common.flogger.GoogleLogger
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.oak.client.grpc.StreamObserverSessionClient
import com.google.privacy.ppn.proto.PrivacyPassTokenData
import com.google.protobuf.ByteString
import com.google.search.mdi.privatearatea.proto.androidKeyStoreAttestationEvidence
import com.google.search.mdi.privatearatea.proto.anonymousTokenRequest
import com.google.search.mdi.privatearatea.proto.deviceAttestationRequest
import com.google.search.mdi.privatearatea.proto.pcsPrivateArateaRequest
import io.grpc.Status
import io.grpc.StatusException
import io.grpc.stub.StreamObserver
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** An asynchronous client for Private Inference based on StreamObservers. */
// Open to allow mocking.
open class PrivateInferenceOakAsyncClient
@Inject
internal constructor(
  @PiExecutorQualifier private val backgroundExecutor: ListeningExecutorService,
  private val streamObserverSessionClient: StreamObserverSessionClient,
  private val stubFactory: PrivateInferenceServiceStubFactory,
  private val deviceAttestationGenerator: DeviceAttestationGenerator,
  private val deviceAttestationFlag: DeviceAttestationFlag,
  private val arateaAuthFlag: ArateaAuthFlag,
  private val bsaArateaTokenProvider: BsaTokenProvider<@JvmSuppressWildcards ArateaToken>,
  private val bsaCacheableArateaTokenProvider:
    BsaTokenProvider<@JvmSuppressWildcards ArateaTokenWithoutChallenge>,
  @param:PrivateInferenceClientTimers private val timers: TimerSet,
  private val pcsStatsLogger: PcsStatsLogger,
) {
  private val dispatcher by lazy { backgroundExecutor.asCoroutineDispatcher() }
  private val nextSessionId = atomic(1)

  /** A component that can generate device attestation. */
  fun interface DeviceAttestationGenerator {
    /**
     * Generate device attestation against a server-provided challenge.
     *
     * @param attestationChallenge The challenge provided by the server or session context.
     * @return The generated attestation data. In most cases, this will be a certificate chain, with
     *   each item in the returned List being a single encoded certificate in the chain.
     */
    fun generateAttestation(
      attestationChallenge: ByteArray,
      includeDeviceProperties: Boolean,
    ): List<ByteString>
  }

  /**
   * Opens a channel with the Oak server and performs initialization and handshake asynchronously.
   *
   * Once the handshake is complete, the [OakSessionOpenObserver] will be notified with a
   * StreamObserver that can be used to send requests.
   *
   * @param requestMetadata The request metadata to use for the gRPC call.
   * @param sessionStreamObserver The observer to receive responses from the server, and that will
   *   receive the [StreamObserver] to use for sending requests, once the stream is open.
   */
  // Open to allow mocking.
  open fun startNoiseSession(
    requestMetadata: PrivateInferenceRequestMetadata,
    sessionStreamObserver: StreamObserverSessionClient.OakSessionStreamObserver,
  ) {
    logger
      .atFine()
      .log(
        "PrivateInferenceOakAsyncClient.startNoiseSession with" +
          " device_attestation_mode flag mode %s",
        deviceAttestationFlag.mode(),
      )
    // Some session initialization may do file I/O, so we need to run it in the background.
    // We create a new CoroutineScope for each `startNoiseSession` and treat the lifecycle of the
    // Session as the CoroutineScope's lifecycle.
    // The scope is cancelled by PrivateInferenceStreamObserver when the session is completed or is
    // stopped due to an error.
    CoroutineScope(
        context = dispatcher + CoroutineName("StartNoiseSession_${nextSessionId.getAndIncrement()}")
      )
      .launch {
        val asyncStub = stubFactory.createStub(requestMetadata.authInfo)
        try {
          streamObserverSessionClient.startSession(
            sessionStreamObserver =
              PrivateInferenceSessionStreamObserver(
                scope = this@launch,
                wrapped = sessionStreamObserver,
                deviceAttestationGenerator = deviceAttestationGenerator,
                deviceAttestationFlag = deviceAttestationFlag,
                arateaAuthFlag = arateaAuthFlag,
                timers = timers,
                backgroundExecutor = backgroundExecutor,
                bsaArateaTokenProvider = bsaArateaTokenProvider,
                bsaCacheableArateaTokenProvider = bsaCacheableArateaTokenProvider,
                pcsStatsLogger = pcsStatsLogger,
              ),
            streamStarter = { observer ->
              RequestLoggingHelpers(timers)
                .startSessionWithHandshakeLogging(
                  asyncStub = asyncStub,
                  responseObserver = observer,
                )
            },
          )
        } catch (e: Exception) {
          logger.atSevere().withCause(e).log("Failed to start noise session.")
          sessionStreamObserver.onError(e)
        }
      }
  }

  /** A wrapper around the client-provided observer for logging and device attestation purposes. */
  private class PrivateInferenceSessionStreamObserver(
    private val scope: CoroutineScope,
    private val wrapped: StreamObserverSessionClient.OakSessionStreamObserver,
    private val deviceAttestationGenerator: DeviceAttestationGenerator,
    private val deviceAttestationFlag: DeviceAttestationFlag,
    private val arateaAuthFlag: ArateaAuthFlag,
    private val timers: Timers,
    private val backgroundExecutor: ListeningExecutorService,
    private val bsaArateaTokenProvider: BsaTokenProvider<@JvmSuppressWildcards ArateaToken>,
    private val bsaCacheableArateaTokenProvider:
      BsaTokenProvider<@JvmSuppressWildcards ArateaTokenWithoutChallenge>,
    private val pcsStatsLogger: PcsStatsLogger,
    private val e2ePiSessionStartTimer: Timers.Timer =
      timers.start(PrivateInferenceClientTimerNames.END_TO_END_PI_CHANNEL_SETUP),
    private val oakSessionOpenTimer: Timers.Timer =
      timers.start(PrivateInferenceClientTimerNames.OAK_SESSION_ESTABLISH_STREAM),
  ) : StreamObserverSessionClient.OakSessionStreamObserver {
    private val inferenceTimers: InferenceTimers = InferenceTimers(timers)
    private val onErrorCompleted = AtomicBoolean(false)

    override fun onSessionOpen(clientRequests: StreamObserver<ByteString>) {
      oakSessionOpenTimer.stop()

      // The final logic to run in both attested and unattested cases.
      fun openSessionAndStartTimers() {
        e2ePiSessionStartTimer.stop()
        inferenceTimers.startFirst()
        wrapped.onSessionOpen(clientRequests)
      }

      val sessionBindingToken =
        (clientRequests as StreamObserverSessionClient.ClientSessionAccess)
          .oakClientSession
          .getSessionBindingToken(PRIVATE_INFERENCE_SESSION_BINDING_TOKEN_INFO)

      when (arateaAuthFlag.mode()) {
        ArateaAuthFlag.Mode.DEVICE_ATTESTATION -> {
          if (deviceAttestationFlag.enabled()) {
            // The attestation generation is done in a background thread because it may do file I/O
            // and key generation can be slow.
            backgroundExecutor.execute {
              val generateKeyPairTimer =
                timers.start(PrivateInferenceClientTimerNames.DEVICE_ATTESTATION_GENERATE_KEY_PAIR)
              val certificateChain =
                deviceAttestationGenerator.generateAttestation(
                  sessionBindingToken,
                  deviceAttestationFlag.useDeviceProperties(),
                )
              logger
                .atFine()
                .log(
                  "Sending device attestation to server with %d certificates.",
                  certificateChain.size,
                )
              clientRequests.onNext(
                pcsPrivateArateaRequest {
                    deviceAttestationRequest = deviceAttestationRequest {
                      androidKeyStoreEvidence = androidKeyStoreAttestationEvidence {
                        this.certificateChain += certificateChain
                      }
                    }
                  }
                  .toByteString()
              )
              generateKeyPairTimer.stop()
              // Call openSessionAndStartTimers() once, *after* any attestation has occurred.
              openSessionAndStartTimers()
              // The background task is complete now.
            }
          } else {
            logger.atFine().log("Device attestation is disabled.")
            // Call openSessionAndStartTimers() once, *after* any attestation has occurred.
            openSessionAndStartTimers()
          }
        }
        ArateaAuthFlag.Mode.ANONYMOUS_TOKEN -> {
          logger.atFine().log("Fetching anonymous token from server.")
          val terminalTokenAuthTimer =
            timers.start(PrivateInferenceClientTimerNames.IPP_ANONYMOUS_TOKEN_AUTH)
          // Get the token from the BsaTokenProvider and send it to the server.
          try {
            val token = fetchArateaToken(sessionBindingToken)
            logger
              .atFine()
              .log(
                "Received anonymous token from server: {token: %s, encodedExtensions: %s}",
                token.token,
                token.encodedExtensions,
              )
            clientRequests.onNext(
              pcsPrivateArateaRequest {
                  anonymousTokenRequest = anonymousTokenRequest {
                    anonymousToken = ByteString.copyFrom(token.toByteArray())
                    encodedExtensions = ByteString.copyFromUtf8(token.encodedExtensions)
                  }
                }
                .toByteString()
            )
            // Call openSessionAndStartTimers() once, *after* any attestation has occurred.
            openSessionAndStartTimers()
            terminalTokenAuthTimer.stop()
          } catch (e: ExecutionException) {
            logger.atSevere().withCause(e).log("Failed to fetch anonymous token")
            val errorStatus =
              if (e.cause is StatusException) {
                (e.cause as StatusException).status
              } else {
                Status.UNKNOWN.withDescription("Failed to fetch anonymous token")
              }
            // Propagate the specific exception to the wrapped observer.
            wrapped.onError(errorStatus.asRuntimeException())
            onErrorCompleted.set(true)
            // Terminate the clientRequests stream to ensure gRPC stream is closed.
            clientRequests.onCompleted()
          }
        }
      }
    }

    private fun fetchArateaToken(sessionBindingToken: ByteArray): PrivacyPassTokenData {
      return pcsStatsLogger.getResultAndLogStatus(METRIC_ID_MAP) {
        val tokenBytes =
          if (arateaAuthFlag.isCacheEnabled()) {
            bsaCacheableArateaTokenProvider
              .fetchTokenFuture(backgroundExecutor, CacheableArateaTokenParams())
              .get()
              .bytes
          } else {
            bsaArateaTokenProvider
              .fetchTokenFuture(backgroundExecutor, ArateaTokenParams(sessionBindingToken))
              .get()
              .bytes
          }
        PrivacyPassTokenData.parseFrom(tokenBytes.toByteArray())
      }
    }

    override fun onNext(response: ByteString) {
      inferenceTimers.response()
      wrapped.onNext(response)
    }

    override fun onError(t: Throwable) {
      inferenceTimers.stop()
      // Avoid double onError calls on [wrapped]
      if (!onErrorCompleted.get()) {
        wrapped.onError(t)
      }
      scope.cancel("OnError during Noise Session", t)
    }

    override fun onCompleted() {
      inferenceTimers.stop()
      wrapped.onCompleted()
      scope.cancel()
    }
  }

  /**
   * A helper for managing our desired inference timer logic.
   *
   * It tracks the first inference, to account for warm-up time, and then counts the request of the
   * inferences as a single group.
   */
  private class InferenceTimers(private val timers: Timers) {
    private var firstInferenceTimer: Timers.Timer? = null
    private var stableInferenceTimer: Timers.Timer? = null

    fun startFirst() {
      if (firstInferenceTimer != null || stableInferenceTimer != null) {
        logger.atWarning().log("InferenceTimers.startFirst() called multiple times.")
      }

      firstInferenceTimer = timers.start(PrivateInferenceClientTimerNames.INFERENCE_FIRST)
    }

    fun response() {
      if (stableInferenceTimer == null) {
        firstInferenceTimer?.stop()
        stableInferenceTimer = timers.start(PrivateInferenceClientTimerNames.INFERENCE_STABLE)
      }
    }

    fun stop() {
      firstInferenceTimer?.stop()
      stableInferenceTimer?.stop()
    }
  }

  companion object {
    init {
      System.loadLibrary("pi_client_session_config_jni")
    }

    private val logger = GoogleLogger.forEnclosingClass()

    private val METRIC_ID_MAP =
      MetricIdMap(
        CountMetricId.PCS_PI_IPP_GET_TERMINAL_TOKEN_SUCCESS,
        CountMetricId.PCS_PI_IPP_GET_TERMINAL_TOKEN_FAILURE,
        ValueMetricId.PCS_PI_IPP_GET_TERMINAL_TOKEN_SUCCESS_LATENCY_MS,
        ValueMetricId.PCS_PI_IPP_GET_TERMINAL_TOKEN_FAILURE_LATENCY_MS,
      )

    // ****** DO NOT CHANGE THIS VALUE UNLESS YOU REALLY KNOW WHAT YOU ARE DOING.
    // Changing this value will break all existing clients.
    // The number after the descriptive section is random and meaningless.
    @JvmField
    val PRIVATE_INFERENCE_SESSION_BINDING_TOKEN_INFO =
      "private-inference-oak-session-binding-15646121867115887062".toByteArray()
  }
}
