/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.pd.service;

import com.google.android.as.oss.common.ExecutorAnnotations.ProtectedDownloadExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import com.google.android.as.oss.pd.api.proto.DownloadBlobResponse;
import com.google.android.as.oss.pd.api.proto.ProtectedDownloadServiceGrpc;
import com.google.android.as.oss.pd.config.ProtectedDownloadConfig;
import com.google.android.as.oss.pd.processor.ProtectedDownloadProcessor;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningExecutorService;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Implements the PCS protected download service by connecting to remote GRPC service. */
@Singleton
class ProtectedDownloadGrpcBindableService
    extends ProtectedDownloadServiceGrpc.ProtectedDownloadServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final ConfigReader<ProtectedDownloadConfig> configReader;
  private final ProtectedDownloadProcessor downloadProcessor;
  private final ListeningExecutorService executor;
  private final BuildFlavor buildFlavor;

  @Inject
  ProtectedDownloadGrpcBindableService(
      ConfigReader<ProtectedDownloadConfig> configReader,
      ProtectedDownloadProcessor downloadProcessor,
      @ProtectedDownloadExecutorQualifier ListeningExecutorService executor,
      BuildFlavor buildFlavor) {
    this.configReader = configReader;
    this.downloadProcessor = downloadProcessor;
    this.executor = executor;
    this.buildFlavor = buildFlavor;
  }

  @Override
  public void download(
      DownloadBlobRequest request, StreamObserver<DownloadBlobResponse> responseObserver) {
    if (!configReader.getConfig().enabled() && !buildFlavor.isInternal()) {
      logger.atFine().log("Rejecting request since the feature is disabled");
      responseObserver.onError(
          new StatusException(Status.FAILED_PRECONDITION.withDescription("feature disabled")));
      return;
    }

    logger.atInfo().log("Starting download blob request");
    Futures.addCallback(
        downloadProcessor.download(request),
        new FutureCallback<DownloadBlobResponse>() {
          @Override
          public void onSuccess(DownloadBlobResponse result) {
            logger.atInfo().log("Successfully downloaded blob response");
            responseObserver.onNext(result);
            responseObserver.onCompleted();
          }

          @Override
          public void onFailure(Throwable t) {
            // Logging the error in addition to "throwing" it, because the failure reason is
            // removed while sent to the client as part of the protocol and it is useful to have
            // it logged in a PCS process.
            logger.atSevere().withCause(t).log("Failed to download blob");
            responseObserver.onError(new StatusException(toGrpcStatus(t)));
          }
        },
        executor);
  }

  private static Status toGrpcStatus(Throwable t) {
    if (t instanceof StatusException) {
      return ((StatusException) t).getStatus();
    } else if (t instanceof StatusRuntimeException) {
      return ((StatusRuntimeException) t).getStatus();
    } else if (t instanceof IllegalArgumentException) {
      return Status.INVALID_ARGUMENT;
    }
    return Status.INTERNAL.withCause(t);
  }
}
