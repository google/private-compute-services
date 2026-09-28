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

package com.google.android.as.oss.http.client;

import static com.google.android.apps.miphone.pcs.grpc.ContextKeys.WRITEABLE_FILE_METADATA_KEY;
import static com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId.PCS_HTTP_DOWNLOAD_FAILURE;
import static com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId.PCS_HTTP_DOWNLOAD_SUCCESS;
import static com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId.PCS_INPROCESS_HTTP_DOWNLOAD_FAILURE;
import static com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId.PCS_INPROCESS_HTTP_DOWNLOAD_SUCCESS;

import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServicePackageName;
import com.google.android.as.oss.http.api.proto.HttpDownloadRequest;
import com.google.android.as.oss.http.api.proto.HttpDownloadResponse;
import com.google.android.as.oss.http.api.proto.HttpServiceGrpc;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import io.grpc.stub.MetadataUtils;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;

/**
 * Client class encapsulating low-level transport logic and providing a high-level API for
 * HTTP/HTTPS downloads through Pcs.
 */
// TODO: consider extending an interface for the ease of testing.
// TODO: this class has to be tested.
public class HttpDownloaderClient {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Channel onDeviceChannel;
  private final String grpcServicePackageName;
  private final PcsStatsLog pcsStatsLogger;

  @VisibleForTesting
  @Inject
  public HttpDownloaderClient(
      Channel onDeviceChannel,
      @GrpcServicePackageName String grpcServicePackageName,
      PcsStatsLog pcsStatsLogger) {
    this.onDeviceChannel = onDeviceChannel;
    this.grpcServicePackageName = grpcServicePackageName;
    this.pcsStatsLogger = pcsStatsLogger;
  }

  /** Downloads a new resource through Pcs. */
  public ListenableFuture<Void> download(
      HttpDownloadRequest httpDownloadRequest,
      @Nullable ParcelFileDescriptor pfd,
      HttpDownloaderCallback callback) {
    logger.atFine().log(
        "Preparing to start downloading from url='%s'", httpDownloadRequest.getUrl());

    final HttpServiceGrpc.HttpServiceStub stub;
    if (pfd != null) {
      Metadata headers = new Metadata();
      headers.put(WRITEABLE_FILE_METADATA_KEY, pfd);
      stub =
          HttpServiceGrpc.newStub(onDeviceChannel)
              .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    } else {
      stub = HttpServiceGrpc.newStub(onDeviceChannel);
    }

    return CallbackToFutureAdapter.getFuture(
        completer -> {
          SettableFuture<ClientCallStreamObserver<HttpDownloadRequest>> requestStreamFuture =
              SettableFuture.create();
          completer.addCancellationListener(
              () -> {
                try {
                  ClientCallStreamObserver<HttpDownloadRequest> streamObserver =
                      Futures.getDone(requestStreamFuture);
                  logger.atFine().log(
                      "Cancelling download for url [%s] requestStream [%s]",
                      httpDownloadRequest.getUrl(), streamObserver);
                  streamObserver.cancel("Client cancelled.", null);
                } catch (ExecutionException e) {
                  logger.atSevere().withCause(e).log(
                      "Invariant violated: it should be impossible to cancel before setting the"
                          + " observer for url[%s]",
                      httpDownloadRequest.getUrl());
                }
              },
              MoreExecutors.directExecutor());
          stub.download(
              httpDownloadRequest,
              new ClientResponseObserver<HttpDownloadRequest, HttpDownloadResponse>() {
                @Override
                public void beforeStart(
                    ClientCallStreamObserver<HttpDownloadRequest> requestStream) {
                  logger.atFine().log(
                      "Client call stream established for url [%s]", httpDownloadRequest.getUrl());
                  requestStreamFuture.set(requestStream);
                }

                @Override
                public void onNext(HttpDownloadResponse result) {
                  switch (result.getResponseCase()) {
                    case RESPONSE_HEADERS ->
                        callback.onDownloadStarted(result.getResponseHeaders());
                    case RESPONSE_BODY_CHUNK ->
                        callback.onDownloadedChunk(
                            result.getResponseBodyChunk().getResponseBytes().toByteArray());
                    default ->
                        callback.onError(
                            new IllegalArgumentException(
                                "Unexpected response case " + result.getResponseCase()));
                  }
                }

                @Override
                public void onError(Throwable t) {
                  logDownloadFailure();
                  callback.onError(t);
                  completer.setException(t);
                }

                // TODO: throw an IllegalArgumentException if headers haven't been returned.
                @Override
                public void onCompleted() {
                  logger.atFine().log(
                      "Download complete from url='%s'", httpDownloadRequest.getUrl());
                  logDownloadSuccess();
                  callback.onDownloadComplete();
                  completer.set(null);
                }
              });
          // Invariant: download() doesn't return before cancelation handler is installed.
          Preconditions.checkState(requestStreamFuture.isDone());
          return "HTTP download scheduled for " + httpDownloadRequest.getUrl();
        });
  }

  private void logDownloadSuccess() {
    if (isPcsApk()) {
      logCountMetricId(PCS_HTTP_DOWNLOAD_SUCCESS);
    } else {
      logCountMetricId(PCS_INPROCESS_HTTP_DOWNLOAD_SUCCESS);
    }
  }

  private void logDownloadFailure() {
    if (isPcsApk()) {
      logCountMetricId(PCS_HTTP_DOWNLOAD_FAILURE);
    } else {
      logCountMetricId(PCS_INPROCESS_HTTP_DOWNLOAD_FAILURE);
    }
  }

  private void logCountMetricId(CountMetricId countMetricId) {
    pcsStatsLogger.logIntelligenceCountReported(
        IntelligenceCountReported.newBuilder().setCountMetricId(countMetricId).build());
  }

  private boolean isPcsApk() {
    return grpcServicePackageName.equals("com.google.android.as.oss");
  }
}
