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

import androidx.annotation.NonNull;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsEnums.ValueMetricId;
import com.google.android.as.oss.privateinference.library.BaseOakServerStreamResponseObserver;
import com.google.android.as.oss.privateinference.library.oakutil.AttestationVerificationException;
import com.google.android.as.oss.privateinference.logging.PcsStatsLogger;
import com.google.android.as.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceFeatureName;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionResponse;
import com.google.android.as.oss.privateinference.util.timers.Timers;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link StreamObserver} for responses from the Oak server.
 *
 * <p>This observer extends {@link BaseOakServerStreamResponseObserver} and adds PCS-specific
 * logging and network usage tracking.
 */
public final class PcsOakServerStreamResponseObserver extends BaseOakServerStreamResponseObserver {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final PrivateInferenceNetworkUsageLogHelper networkUsageLogHelper;
  private final PcsStatsLogger pcsStatsLogger;

  private final long inferenceStartMillis;
  private final Timers.Timer inferenceSessionTimer;
  private final AtomicLong totalRequestSize;
  private final AtomicLong totalResponseSize;
  private final AtomicReference<PcsPrivateInferenceFeatureName> featureName;

  private final LoggingMetricIdProvider loggingMetricIdProvider;

  private final String callingPackageName;

  public PcsOakServerStreamResponseObserver(
      StreamObserver<PrivateInferenceSessionResponse> clientSessionResponseObserver,
      PrivateInferenceNetworkUsageLogHelper networkUsageLogHelper,
      PcsStatsLogger pcsStatsLogger,
      long inferenceStartMillis,
      Timers.Timer inferenceSessionTimer,
      AtomicLong totalRequestSize,
      AtomicLong totalResponseSize,
      AtomicReference<PcsPrivateInferenceFeatureName> featureName,
      AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver,
      LoggingMetricIdProvider loggingMetricIdProvider,
      String callingPackageName) {
    super(clientSessionResponseObserver, directOakClientRequestObserver);
    this.networkUsageLogHelper = networkUsageLogHelper;
    this.pcsStatsLogger = pcsStatsLogger;
    this.inferenceStartMillis = inferenceStartMillis;
    this.inferenceSessionTimer = inferenceSessionTimer;
    this.totalRequestSize = totalRequestSize;
    this.totalResponseSize = totalResponseSize;
    this.featureName = featureName;
    this.loggingMetricIdProvider = loggingMetricIdProvider;
    this.callingPackageName = callingPackageName;
  }

  @Override
  public void onSessionOpen(@NonNull StreamObserver<ByteString> clientRequestsStreamObserver) {
    logger.atInfo().log("[startInferenceSession] Oak Noise session is set up.");
    super.onSessionOpen(clientRequestsStreamObserver);
  }

  @Override
  public void onNext(ByteString oakResponse) {
    totalResponseSize.getAndAdd(oakResponse.size());
    super.onNext(oakResponse);
    logger.atInfo().log(
        "[startInferenceSession] Received response from server with size: %d.", oakResponse.size());
  }

  @Override
  public void onError(Throwable t) {
    inferenceSessionTimer.stop();
    long latencyMs = System.currentTimeMillis() - inferenceStartMillis;
    logger.atWarning().log(
        "[startInferenceSession] onError[%s] from server for feature: %s.",
        t.getMessage(), featureName.get().name());
    logInferenceFailureLatency(featureName.get(), latencyMs);
    networkUsageLogHelper.logPrivateInferenceRequest(
        featureName.get().name(),
        callingPackageName,
        /* isSuccess= */ false,
        totalRequestSize.get(),
        totalResponseSize.get());
    logInferenceFailureEvent(featureName.get());
    logInferenceFailureErrorCode(t);
    super.onError(t);
  }

  @Override
  public void onCompleted() {
    logger.atInfo().log(
        "[startInferenceSession] onCompleted from server for feature: %s.",
        featureName.get().name());
    // Will integrate with the Timer API to measure the latency.
    logInferenceSuccessLatency(
        featureName.get(), System.currentTimeMillis() - inferenceStartMillis);
    networkUsageLogHelper.logPrivateInferenceRequest(
        featureName.get().name(),
        callingPackageName,
        /* isSuccess= */ true,
        totalRequestSize.get(),
        totalResponseSize.get());
    logInferenceSuccessEvent(featureName.get());
    inferenceSessionTimer.stop();
    super.onCompleted();
  }

  // TODO: Merge duplicate helper methods.
  private void logInferenceSuccessEvent(PcsPrivateInferenceFeatureName featureName) {
    CountMetricId countMetricId =
        loggingMetricIdProvider.getInferenceSuccessCountMetricId(featureName);
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

  private void logInferenceFailureEvent(PcsPrivateInferenceFeatureName featureName) {
    CountMetricId countMetricId =
        loggingMetricIdProvider.getInferenceFailureCountMetricId(featureName);
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
