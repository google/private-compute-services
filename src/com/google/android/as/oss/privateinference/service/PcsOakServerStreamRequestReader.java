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

import androidx.annotation.Nullable;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.privateinference.config.PrivateInferenceConfig;
import com.google.android.as.oss.privateinference.library.BaseOakServerStreamRequestReader;
import com.google.android.as.oss.privateinference.library.oakutil.PrivateInferenceOakAsyncClient;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceFeatureName;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionRequest;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionResponse;
import com.google.android.as.oss.privateinference.service.api.proto.SessionInitializationResponse;
import com.google.common.flogger.GoogleLogger;
import com.google.oak.client.grpc.StreamObserverSessionClient.OakSessionStreamObserver;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A {@link BaseOakServerStreamRequestReader} for PCS that handles {@link
 * PrivateInferenceSessionRequest} messages.
 *
 * <p>This class extends the base class to add PCS-specific logic, such as checking if the feature
 * is enabled, logging request sizes, and gating session initialization.
 */
public class PcsOakServerStreamRequestReader extends BaseOakServerStreamRequestReader {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final ConfigReader<PrivateInferenceConfig> configReader;
  private final BuildFlavor buildFlavor;
  private final StreamObserver<PrivateInferenceSessionResponse> clientSessionResponseObserver;
  private final AtomicLong totalRequestSize;
  private final AtomicReference<PcsPrivateInferenceFeatureName> featureName;

  public PcsOakServerStreamRequestReader(
      PrivateInferenceOakAsyncClient oakAsyncClient,
      OakSessionStreamObserver oakServerStreamResponseObserver,
      AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver,
      @Nullable InputStream parcelInputStream,
      ConfigReader<PrivateInferenceConfig> configReader,
      BuildFlavor buildFlavor,
      StreamObserver<PrivateInferenceSessionResponse> clientSessionResponseObserver,
      AtomicLong totalRequestSize,
      AtomicReference<PcsPrivateInferenceFeatureName> featureName) {
    super(
        oakAsyncClient,
        oakServerStreamResponseObserver,
        directOakClientRequestObserver,
        parcelInputStream);
    this.configReader = configReader;
    this.buildFlavor = buildFlavor;
    this.clientSessionResponseObserver = clientSessionResponseObserver;
    this.totalRequestSize = totalRequestSize;
    this.featureName = featureName;
  }

  @Override
  public void onNext(PrivateInferenceSessionRequest request) {
    if (request.hasSessionInitializationRequest()) {
      if (!configReader.getConfig().enabled() && !buildFlavor.isInternal()) {
        logger.atWarning().log(
            "Rejecting the session initialization request since the feature is disabled");
        super.tryCloseInputStream(parcelInputStream);
        clientSessionResponseObserver.onNext(SESSION_INITIALIZATION_DISABLED_RESPONSE);
        return;
      }
      super.onNext(request);
      logger.atInfo().log("[startInferenceSession] Started Oak Noise session");
    } else if (request.hasInferenceRequest()) {
      if (featureName.get() == PcsPrivateInferenceFeatureName.FEATURE_NAME_UNSPECIFIED) {
        featureName.set(request.getInferenceRequest().getFeatureName());
      }
      long requestSize = 0;
      if (request.getInferenceRequest().hasData()) {
        requestSize = request.getInferenceRequest().getData().size();
      } else if (request.getInferenceRequest().hasPfdDataSize()) {
        requestSize = request.getInferenceRequest().getPfdDataSize();
      }
      totalRequestSize.getAndAdd(requestSize);
      super.onNext(request);
      logger.atInfo().log(
          "[startInferenceSession] Sent request to server with size: %d.", requestSize);
    } else {
      super.onError(
          new IllegalArgumentException(
              "PrivateInferenceSessionRequest must have either session_initialization_request or"
                  + " inference_request set."));
    }
  }

  @Override
  public void onCompleted() {
    logger.atInfo().log(
        "[startInferenceSession] onCompleted from client for feature: %s.",
        featureName.get().name());
    super.onCompleted();
  }

  private static final PrivateInferenceSessionResponse SESSION_INITIALIZATION_DISABLED_RESPONSE =
      PrivateInferenceSessionResponse.newBuilder()
          .setSessionInitializationResponse(
              SessionInitializationResponse.newBuilder()
                  .setInitializationError(
                      SessionInitializationResponse.InitializationError
                          .INITIALIZATION_ERROR_FEATURE_DISABLED)
                  .setInitializationSucceeded(false))
          .build();
}
