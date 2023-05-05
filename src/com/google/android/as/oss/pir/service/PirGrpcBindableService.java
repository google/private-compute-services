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

package com.google.android.as.oss.pir.service;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.PirExecutorQualifier;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.pir.api.pir.proto.PirDownloadRequest;
import com.google.android.as.oss.pir.api.pir.proto.PirDownloadResponse;
import com.google.android.as.oss.pir.api.pir.proto.PirServiceGrpc;
import com.google.android.as.oss.pir.service.PirGrpcModule.PirDownloadTaskBuilderFactoryServerSide;
import com.google.android.libraries.base.Logger;
import com.google.common.flogger.android.AndroidFluentLogger;
import com.google.errorprone.annotations.FormatMethod;
import com.google.errorprone.annotations.FormatString;
import com.google.private_retrieval.pir.AndroidPirUriParser;
import com.google.private_retrieval.pir.PirDownloadException;
import com.google.private_retrieval.pir.PirDownloadTask;
import com.google.private_retrieval.pir.PirDownloadTask.Builder.PirDownloadTaskBuilderFactory;
import com.google.private_retrieval.pir.PirUri;
import com.google.private_retrieval.pir.PirUriParser;
import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Bindable Service that handles PIR requests to PCS. */
public class PirGrpcBindableService extends PirServiceGrpc.PirServiceImplBase {
  private static final AndroidFluentLogger baseLogger = AndroidFluentLogger.create("SP.PIR");
  static final Logger pirLogger =
      new Logger("") {
        @Override
        public Logger withTag(String tag) {
          // This wrapper ignores tags.
          return this;
        }

        @Override
        @FormatMethod
        public void printLog(
            Level level,
            String tag,
            @Nullable Throwable t,
            @FormatString String message,
            Object... args) {
          baseLogger.at(level).withCause(t).logVarargs(message, args);
        }
      };

  private final PirDownloadTaskBuilderFactory pirDownloadTaskBuilderFactory;
  private final Executor executor;
  private final NetworkUsageLogRepository networkUsageLogRepository;

  @Inject
  @VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
  public PirGrpcBindableService(
      @PirDownloadTaskBuilderFactoryServerSide
          PirDownloadTaskBuilderFactory pirDownloadTaskBuilderFactory,
      @PirExecutorQualifier Executor executor,
      NetworkUsageLogRepository networkUsageLogRepository) {
    this.pirDownloadTaskBuilderFactory = pirDownloadTaskBuilderFactory;
    this.executor = executor;
    this.networkUsageLogRepository = networkUsageLogRepository;
  }

  @Override
  public void download(
      PirDownloadRequest request, StreamObserver<PirDownloadResponse> responseObserver) {
    // Log Unrecognized requests
    if (!networkUsageLogRepository.isKnownConnection(ConnectionType.PIR, request.getUrl())) {
      baseLogger.atInfo().log(
          "Network usage log unrecognised PIR request for %s", request.getUrl());
    }
    if (networkUsageLogRepository.shouldRejectRequest(ConnectionType.PIR, request.getUrl())) {
      pirLogger.logWarn(
          UnrecognizedNetworkRequestException.forUrl(request.getUrl()),
          "Rejected unknown PIR request to PCS");
      responseObserver.onError(UnrecognizedNetworkRequestException.forUrl(request.getUrl()));
      return;
    }
    Optional<PirDownloadTask> task = setupTask(request, responseObserver);
    if (!task.isPresent()) {
      return;
    }

    Context.current().addListener(context -> task.ifPresent(PirDownloadTask::cancel), executor);
    // Async execution is necessary for GRPC flow-control & client-side cancellations.
    // TODO: Task should be run by PirDownloadExecutor to support constraints.
    executor.execute(task.get());
  }

  private Optional<PirDownloadTask> setupTask(
      PirDownloadRequest request, StreamObserver<PirDownloadResponse> responseObserver) {
    PirUriParser uriParser = new AndroidPirUriParser();
    PirUri uri = uriParser.parse(request.getUrl());
    PirDownloadStatus downloadStatus = new PirDownloadStatus();
    DelegatingDownloadListener downloadListener =
        new DelegatingDownloadListener(
            responseObserver, networkUsageLogRepository, downloadStatus, request.getUrl());
    StreamingResponseWriter responseWriter =
        new StreamingResponseWriter(
            responseObserver, downloadListener, downloadStatus, request.getUrl());
    try {
      return Optional.of(
          pirDownloadTaskBuilderFactory
              .create()
              .setApiKey(request.getApiKey())
              .setLogger(pirLogger)
              .setNumChunksPerRequest(request.getNumChunksPerRequest())
              .setPirUri(uri)
              .setTaskId(request.getTaskId())
              // TODO: Support network constraints.
              .setResponseWriter(responseWriter)
              .setPirDownloadListener(downloadListener)
              .build());
    } catch (PirDownloadException e) {
      downloadListener.onFailure(uri, new Exception("Could not initialize PIR library.", e));
      return Optional.empty();
    }
  }

  static class PirDownloadStatus {
    private final AtomicBoolean operationCompleted = new AtomicBoolean(false);
    private final AtomicLong numDownloadedBytes = new AtomicLong(0);

    AtomicBoolean getOperationCompleted() {
      return operationCompleted;
    }

    AtomicLong getNumDownloadedBytes() {
      return numDownloadedBytes;
    }
  }
}
