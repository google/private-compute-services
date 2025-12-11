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

package com.google.android.as.oss.privateinference.service;

import android.content.Context;
import android.os.ParcelFileDescriptor;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsEnums.ValueMetricId;
import com.google.android.as.oss.privateinference.config.PrivateInferenceConfig;
import com.google.android.as.oss.privateinference.library.PrivateInferenceRequestMetadata;
import com.google.android.as.oss.privateinference.library.oakutil.AttestationVerificationException;
import com.google.android.as.oss.privateinference.library.oakutil.PrivateInferenceOakAsyncClient;
import com.google.android.as.oss.privateinference.logging.PcsStatsLogger;
import com.google.android.as.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper;
import com.google.android.as.oss.privateinference.service.api.MetadataParcelFileDescriptorKeys;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceFeatureName;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceRequest;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceResponse;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceServiceGrpc;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferencePrepareRequest;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferencePrepareResponse;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionRequest;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionResponse;
import com.google.android.as.oss.privateinference.util.timers.Annotations.PrivateInferenceServiceTimers;
import com.google.android.as.oss.privateinference.util.timers.TimerSet;
import com.google.android.as.oss.privateinference.util.timers.Timers;
import com.google.common.flogger.GoogleLogger;
import com.google.oak.client.grpc.StreamObserverSessionClient;
import com.google.protobuf.ByteString;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusException;
import io.grpc.binder.PeerUid;
import io.grpc.binder.PeerUids;
import io.grpc.stub.StreamObserver;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;

/** Bindable Service that handles requests to Private Inference service via PCS. */
public final class PrivateInferenceGrpcBindableService
    extends PcsPrivateInferenceServiceGrpc.PcsPrivateInferenceServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Context context;
  private final PrivateInferenceOakAsyncClient oakAsyncClient;
  private final ConfigReader<PrivateInferenceConfig> configReader;
  private final PrivateInferenceNetworkUsageLogHelper networkUsageLogHelper;
  private final PcsStatsLogger pcsStatsLogger;
  private final BuildFlavor buildFlavor;
  private final TimerSet timers;
  private final LoggingMetricIdProvider loggingMetricIdProvider;

  @Inject
  PrivateInferenceGrpcBindableService(
      @ApplicationContext Context context,
      PrivateInferenceOakAsyncClient oakAsyncClient,
      ConfigReader<PrivateInferenceConfig> configReader,
      PrivateInferenceNetworkUsageLogHelper networkUsageLogHelper,
      PcsStatsLogger pcsStatsLogger,
      BuildFlavor buildFlavor,
      @PrivateInferenceServiceTimers TimerSet timers,
      LoggingMetricIdProvider loggingMetricIdProvider) {
    this.context = context;
    this.oakAsyncClient = oakAsyncClient;
    this.configReader = configReader;
    this.networkUsageLogHelper = networkUsageLogHelper;
    this.pcsStatsLogger = pcsStatsLogger;
    this.buildFlavor = buildFlavor;
    this.timers = timers;
    this.loggingMetricIdProvider = loggingMetricIdProvider;
  }

  @Override
  public void prepareInferenceSession(
      PrivateInferencePrepareRequest request,
      StreamObserver<PrivateInferencePrepareResponse> responseObserver) {
    logger.atInfo().log(
        "[Prepare Inference Session] Cold start latency: %d ms.",
        Instant.now().minusMillis(request.getClientTimestampMillis()).toEpochMilli());
    responseObserver.onNext(PrivateInferencePrepareResponse.getDefaultInstance());
    responseObserver.onCompleted();
  }

  @Override
  public StreamObserver<PrivateInferenceSessionRequest> startInferenceSession(
      StreamObserver<PrivateInferenceSessionResponse> sessionResponseObserver) {
    long startTime = System.currentTimeMillis();
    InputStream parcelInputStream =
        new ParcelFileDescriptor.AutoCloseInputStream(
            MetadataParcelFileDescriptorKeys.FILE_DESCRIPTOR_CONTEXT_KEY.get());
    final Timers.Timer inferenceSessionTimer =
        timers.start(PrivateInferenceGrpcTimerNames.PRIVATE_INFERENCE_SESSION_TIMER_NAME);
    final String callingPackageName = getCallingPackageName();

    // These items are shared between the request and response processors.
    AtomicLong totalRequestSize = new AtomicLong(0);
    AtomicLong totalResponseSize = new AtomicLong(0);
    AtomicReference<PcsPrivateInferenceFeatureName> featureName =
        new AtomicReference<>(PcsPrivateInferenceFeatureName.FEATURE_NAME_UNSPECIFIED);
    AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver =
        new AtomicReference<>();

    StreamObserverSessionClient.OakSessionStreamObserver pcsOakServerStreamResponseObserver =
        new PcsOakServerStreamResponseObserver(
            sessionResponseObserver,
            networkUsageLogHelper,
            pcsStatsLogger,
            startTime,
            inferenceSessionTimer,
            totalRequestSize,
            totalResponseSize,
            featureName,
            directOakClientRequestObserver,
            loggingMetricIdProvider,
            callingPackageName);

    return new PcsOakServerStreamRequestReader(
        oakAsyncClient,
        pcsOakServerStreamResponseObserver,
        directOakClientRequestObserver,
        parcelInputStream,
        configReader,
        buildFlavor,
        sessionResponseObserver,
        totalRequestSize,
        featureName);
  }

  @Override
  public void performInference(
      PcsPrivateInferenceRequest request,
      StreamObserver<PcsPrivateInferenceResponse> responseObserver) {
    long startTime = System.currentTimeMillis();
    if (!configReader.getConfig().enabled() && !buildFlavor.isInternal()) {
      logger.atFine().log("Rejecting request since the feature is disabled");
      responseObserver.onError(
          new StatusException(Status.FAILED_PRECONDITION.withDescription("Feature disabled")));
      pcsStatsLogger.logEventCount(CountMetricId.PCS_PI_ERROR_FEATURE_DISABLED);
      return;
    }

    final Timers.Timer inferenceTimer =
        timers.start(PrivateInferenceGrpcTimerNames.PRIVATE_INFERENCE_TIMER_NAME);
    final String callingPackageName = getCallingPackageName();
    String featureName = request.getFeatureName().name();
    long requestSize = request.getData().size();
    AtomicLong responseSize = new AtomicLong(0);
    // Log unrecognized request
    if (!networkUsageLogHelper.isKnownFeature(featureName)) {
      pcsStatsLogger.logEventCount(
          // Unrecognised request
          CountMetricId.PCS_NETWORK_USAGE_LOG_UNRECOGNISED_REQUEST);
      logger.atInfo().log(
          "[performInference] Network usage log unrecognised Private Inference request for %s",
          featureName);
    }
    StreamObserverSessionClient.OakSessionStreamObserver privateInferenceResponseObserver =
        new StreamObserverSessionClient.OakSessionStreamObserver() {

          @Override
          public void onSessionOpen(StreamObserver<ByteString> clientStreamObserver) {
            logger.atInfo().log("[performInference] Oak Noise session is set up.");
            clientStreamObserver.onNext(request.getData());
            clientStreamObserver.onCompleted(); // Signal no more requests after this one.
            logger.atInfo().log(
                "[performInference] Sent request to server with size: %d.", requestSize);
          }

          @Override
          public void onNext(ByteString response) {
            responseSize.set(response.size());
            PcsPrivateInferenceResponse pcsPrivateInferenceResponse =
                PcsPrivateInferenceResponse.newBuilder().setData(response).build();
            responseObserver.onNext(pcsPrivateInferenceResponse);
            logger.atInfo().log(
                "[performInference] Received response from server with size: %d.", response.size());
          }

          @Override
          public void onError(Throwable t) {
            inferenceTimer.stop();
            long latencyMs = System.currentTimeMillis() - startTime;
            logger.atWarning().log(
                "[performInference] onError[%s] from server for feature: %s.",
                t.getMessage(), featureName);
            networkUsageLogHelper.logPrivateInferenceRequest(
                featureName,
                callingPackageName,
                /* isSuccess= */ false,
                requestSize,
                responseSize.get());
            logInferenceFailureLatency(request.getFeatureName(), latencyMs);
            logInferenceFailureEvent(request.getFeatureName());
            logInferenceFailureErrorCode(t);

            responseObserver.onError(wrapIfAttestationFailure(t));
          }

          @Override
          public void onCompleted() {
            logger.atInfo().log(
                "[performInference] onCompleted from server for feature: %s.", featureName);
            logInferenceSuccessLatency(
                request.getFeatureName(), System.currentTimeMillis() - startTime);
            networkUsageLogHelper.logPrivateInferenceRequest(
                featureName,
                callingPackageName,
                /* isSuccess= */ true,
                requestSize,
                responseSize.get());
            logInferenceSuccessEvent(request.getFeatureName());
            inferenceTimer.stop();
            responseObserver.onCompleted();
          }
        };

    oakAsyncClient.startNoiseSession(
        buildPrivateInferenceRequestMetadata(request), privateInferenceResponseObserver);
  }

  private Throwable wrapIfAttestationFailure(Throwable t) {
    Status status = Status.fromThrowable(t);
    if (status.getCause() instanceof AttestationVerificationException) {
      return Status.INTERNAL
          .withDescription("Server attestation verification failed (public key expired)")
          .withCause(t)
          .asRuntimeException();
    } else if (status.getCode() == Code.PERMISSION_DENIED) {
      return Status.INTERNAL
          .withDescription("Device attestation verification failed")
          .withCause(t)
          .asRuntimeException();
    } else {
      return t;
    }
  }

  private PrivateInferenceRequestMetadata buildPrivateInferenceRequestMetadata(
      PcsPrivateInferenceRequest request) {
    return new PrivateInferenceRequestMetadata() {
      @Override
      public AuthInfo getAuthInfo() {
        AuthInfo authInfo = new AuthInfo();
        if (request.hasApiKey()) {
          authInfo.apiKey = Optional.of(request.getApiKey());
        } else if (request.hasSpatulaHeader()) {
          authInfo.spatulaHeader = Optional.of(request.getSpatulaHeader());
        }
        return authInfo;
      }
    };
  }

  // PeerUid is used here only for logging. It is NOT used for any security checks.
  private String getCallingPackageName() {
    String callingPackageName = "";
    PeerUid remotePeer = PeerUids.REMOTE_PEER.get();
    if (remotePeer == null
        || PeerUids.getInsecurePackagesForUid(context.getPackageManager(), remotePeer) == null) {
      logger.atWarning().log("Calling package not set in PeerUid.");
    } else {
      callingPackageName =
          PeerUids.getInsecurePackagesForUid(context.getPackageManager(), remotePeer)[0];
      logger.atFine().log("Received call from package %s", callingPackageName);
    }
    return callingPackageName;
  }

  private void logInferenceSuccessEvent(PcsPrivateInferenceFeatureName featureName) {
    CountMetricId countMetricId =
        loggingMetricIdProvider.getInferenceSuccessCountMetricId(featureName);
    pcsStatsLogger.logEventCount(countMetricId);
  }

  private void logInferenceFailureEvent(PcsPrivateInferenceFeatureName featureName) {
    CountMetricId countMetricId =
        loggingMetricIdProvider.getInferenceFailureCountMetricId(featureName);
    pcsStatsLogger.logEventCount(countMetricId);
  }

  private void logInferenceFailureErrorCode(Throwable t) {
    CountMetricId countMetricId = null;
    Status status = Status.fromThrowable(t);

    if (status.getCause() instanceof AttestationVerificationException) {
      countMetricId = CountMetricId.PCS_PI_ERROR_ATTESTATION_VERIFICATION_KEYS_EXPIRED;
    } else if (status.getCode() == Code.PERMISSION_DENIED) {
      countMetricId = CountMetricId.PCS_PI_ERROR_KEY_ATTESTATION_FAILED;
    } else {
      countMetricId =
          loggingMetricIdProvider.getInferenceFailureErrorCodeCountMetricId(status.getCode());
    }

    pcsStatsLogger.logEventCount(countMetricId);
  }

  private void logInferenceSuccessLatency(
      PcsPrivateInferenceFeatureName featureName, long latencyMs) {
    ValueMetricId valueMetricId =
        loggingMetricIdProvider.getInferenceSuccessLatencyValueMetricId(featureName);
    pcsStatsLogger.logEventLatency(valueMetricId, latencyMs);
  }

  private void logInferenceFailureLatency(
      PcsPrivateInferenceFeatureName featureName, long latencyMs) {
    ValueMetricId valueMetricId =
        loggingMetricIdProvider.getInferenceFailureLatencyValueMetricId(featureName);
    pcsStatsLogger.logEventLatency(valueMetricId, latencyMs);
  }
}
