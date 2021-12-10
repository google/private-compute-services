/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.http.service;

import androidx.annotation.GuardedBy;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.http.api.proto.HttpDownloadRequest;
import com.google.android.as.oss.http.api.proto.HttpDownloadResponse;
import com.google.android.as.oss.http.api.proto.HttpProperty;
import com.google.android.as.oss.http.api.proto.HttpServiceGrpc;
import com.google.android.as.oss.http.api.proto.ResponseBodyChunk;
import com.google.android.as.oss.http.api.proto.ResponseHeaders;
import com.google.android.as.oss.http.config.PcsHttpConfig;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import javax.inject.Inject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Bindable Service that handles HTTP requests to Private Compute Services. */
public class HttpGrpcBindableService extends HttpServiceGrpc.HttpServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @VisibleForTesting static final int BUFFER_LENGTH = 1_024;

  private final OkHttpClient client;
  private final Executor executor;
  private final NetworkUsageLogRepository networkUsageLogRepository;
  private final ConfigReader<PcsHttpConfig> configReader;

  @Inject
  HttpGrpcBindableService(
      OkHttpClient client,
      @IoExecutorQualifier Executor ioExecutor,
      NetworkUsageLogRepository networkUsageLogRepository,
      ConfigReader<PcsHttpConfig> httpConfigReader) {
    this.client = client;
    this.executor = ioExecutor;
    this.networkUsageLogRepository = networkUsageLogRepository;
    this.configReader = httpConfigReader;
  }

  @Override
  public void download(
      HttpDownloadRequest request, StreamObserver<HttpDownloadResponse> responseObserver) {
    logger.atInfo().log("Downloading requested for URL '%s'", request.getUrl());

    // TODO: We should reject unknown request after making sure we have covered all
    // URLs in NetworkUsageLogContentMap.
    if (networkUsageLogRepository.shouldRejectRequest(ConnectionType.HTTP, request.getUrl())) {
      logger.atWarning().log(
          "WARNING: Unknown url='%s'. All urls must be defined in NetworkUsageLogContentMap.",
          request.getUrl());
    }

    Request.Builder okRequest = new Request.Builder().url(request.getUrl());

    for (HttpProperty property : request.getRequestPropertyList()) {
      for (String value : property.getValueList()) {
        okRequest.addHeader(property.getKey(), value);
      }
    }

    Response response;
    try {
      response = client.newCall(okRequest.build()).execute();
    } catch (IOException e) {
      responseObserver.onError(e);
      insertNetworkUsageLogRow(networkUsageLogRepository, request, Status.FAILED, 0L);
      return;
    }
    ((ServerCallStreamObserver<HttpDownloadResponse>) responseObserver)
        .setOnCancelHandler(response::close);

    ResponseHeaders.Builder responseHeaders =
        ResponseHeaders.newBuilder().setResponseCode(response.code());
    for (String name : response.headers().names()) {
      responseHeaders.addHeader(
          HttpProperty.newBuilder()
              .setKey(name)
              .addAllValue(response.headers().values(name))
              .build());
    }

    logger.atInfo().log("Responding with header information for URL '%s'", request.getUrl());
    responseObserver.onNext(
        HttpDownloadResponse.newBuilder().setResponseHeaders(responseHeaders).build());

    ResponseBody body = response.body();

    if (body == null) {
      logger.atInfo().log(
          "Received an empty body for URL '%s'. Responding with fetch_completed.",
          request.getUrl());
      responseObserver.onCompleted();
      insertNetworkUsageLogRow(networkUsageLogRepository, request, Status.SUCCEEDED, 0L);
      return;
    }

    if (configReader.getConfig().onReadyHandlerEnabled()) {
      ServerCallStreamObserver<HttpDownloadResponse> serverStreamObserver =
          (ServerCallStreamObserver<HttpDownloadResponse>) responseObserver;

      Runnable onReadyHandler =
          new HttpGrpcStreamHandler(
              request,
              serverStreamObserver,
              body.byteStream(),
              executor,
              networkUsageLogRepository);
      serverStreamObserver.setOnReadyHandler(onReadyHandler);
      // First call is required to be manual as per GRPC docs.
      onReadyHandler.run();
    } else {
      executor.execute(
          new Runnable() {
            private long totalBytesRead = 0;

            @Override
            public void run() {
              try (InputStream is = body.byteStream()) {
                byte[] buffer = new byte[BUFFER_LENGTH];

                while (true) {
                  int bytesRead = is.read(buffer);
                  if (bytesRead == -1) {
                    break;
                  }

                  if (bytesRead > 0) {
                    totalBytesRead += bytesRead;
                    responseObserver.onNext(
                        HttpDownloadResponse.newBuilder()
                            .setResponseBodyChunk(
                                ResponseBodyChunk.newBuilder()
                                    .setResponseBytes(ByteString.copyFrom(buffer, 0, bytesRead))
                                    .build())
                            .build());
                  }
                }

                logger.atInfo().log(
                    "Responding with fetch_completed for URL '%s'", request.getUrl());
                responseObserver.onCompleted();
                insertNetworkUsageLogRow(
                    networkUsageLogRepository, request, Status.SUCCEEDED, totalBytesRead);
              } catch (IOException e) {
                logger.atWarning().withCause(e).log(
                    "Failed performing IO operation while handling URL '%s'", request.getUrl());
                responseObserver.onError(e);
                insertNetworkUsageLogRow(
                    networkUsageLogRepository, request, Status.FAILED, totalBytesRead);
              } catch (StatusRuntimeException e) {
                if (responseObserver instanceof ServerCallStreamObserver
                    && ((ServerCallStreamObserver) responseObserver).isCancelled()) {
                  logger.atWarning().withCause(e).log(
                      "Failed to fetch response body for URL '%s'. Call cancelled by client.",
                      request.getUrl());
                } else {
                  responseObserver.onError(e);
                  insertNetworkUsageLogRow(
                      networkUsageLogRepository, request, Status.FAILED, totalBytesRead);
                }
              }
            }
          });
    }
  }

  private static void insertNetworkUsageLogRow(
      NetworkUsageLogRepository networkUsageLogRepository,
      HttpDownloadRequest request,
      Status status,
      long size) {
    if (!networkUsageLogRepository.shouldLogNetworkUsage(ConnectionType.HTTP, request.getUrl())
        || !networkUsageLogRepository.getContentMap().isPresent()) {
      return;
    }

    ConnectionDetails connectionDetails =
        networkUsageLogRepository
            .getContentMap()
            .get()
            .getHttpConnectionDetails(request.getUrl())
            .get();
    NetworkUsageEntity entity =
        NetworkUsageLogUtils.createHttpNetworkUsageEntity(
            connectionDetails, status, size, request.getUrl());

    networkUsageLogRepository.insertNetworkUsageEntity(entity);
  }

  private static class HttpGrpcStreamHandler implements Runnable {
    private final AtomicLong totalBytesRead = new AtomicLong(0);

    @GuardedBy("this")
    private final byte[] buffer = new byte[BUFFER_LENGTH];

    @GuardedBy("this")
    private int bytesPendingToBeSent = 0;

    private final HttpDownloadRequest request;
    private final ServerCallStreamObserver<HttpDownloadResponse> responseObserver;
    private final InputStream bodyStream;
    private final Executor backgroundExecutor;
    private final NetworkUsageLogRepository networkUsageLogRepository;

    public HttpGrpcStreamHandler(
        HttpDownloadRequest request,
        ServerCallStreamObserver<HttpDownloadResponse> serverStreamObserver,
        InputStream is,
        Executor executor,
        NetworkUsageLogRepository networkUsageLogRepository) {
      this.request = request;
      this.responseObserver = serverStreamObserver;
      this.bodyStream = is;
      this.backgroundExecutor = executor;
      this.networkUsageLogRepository = networkUsageLogRepository;
    }

    @Override
    public synchronized void run() {
      logger.atFine().log(
          "onReadyHandler called for URL [%s]. Bytes sent so far: [%d].",
          request.getUrl(), totalBytesRead.get());
      boolean saveStreamToResumeWhenClientIsReadyAgain = false;
      try {
        while (bytesPendingToBeSent >= 0) {
          // Do not overwrite the buffer with new data if previous buffer has not been transmitted.
          if (bytesPendingToBeSent == 0) {
            bytesPendingToBeSent = bodyStream.read(buffer);
          }

          if (bytesPendingToBeSent == -1) {
            // stream is over
            break;
          }

          if (bytesPendingToBeSent > 0) {
            if (!responseObserver.isReady()) {
              // We have received a valid chunk, but client is busy processing previous data. We
              // return for now, but the data is saved in the member buffer to be processed next
              // time.
              saveStreamToResumeWhenClientIsReadyAgain = true;
              return;
            }
            responseObserver.onNext(
                HttpDownloadResponse.newBuilder()
                    .setResponseBodyChunk(
                        ResponseBodyChunk.newBuilder()
                            .setResponseBytes(ByteString.copyFrom(buffer, 0, bytesPendingToBeSent))
                            .build())
                    .build());
            totalBytesRead.addAndGet(bytesPendingToBeSent);
            // Data has been sent, nothing more to process for now.
            bytesPendingToBeSent = 0;
          }
        }

        logger.atInfo().log("Responding with fetch_completed for URL [%s].", request.getUrl());
        responseObserver.onCompleted();
        backgroundExecutor.execute(
            () ->
                insertNetworkUsageLogRow(
                    networkUsageLogRepository, request, Status.SUCCEEDED, totalBytesRead.get()));
      } catch (IOException e) {
        logger.atWarning().withCause(e).log(
            "Failed performing IO operation while downloading URL [%s].", request.getUrl());
        responseObserver.onError(e);
        backgroundExecutor.execute(
            () ->
                insertNetworkUsageLogRow(
                    networkUsageLogRepository, request, Status.FAILED, totalBytesRead.get()));
      } catch (StatusRuntimeException e) {
        if (responseObserver.isCancelled()) {
          logger.atWarning().withCause(e).log(
              "Failed to fetch response body for URL [%s]. Call cancelled by client.",
              request.getUrl());
        } else {
          responseObserver.onError(e);
          // TODO: Cancellation should also be logged in network usage log.
          backgroundExecutor.execute(
              () ->
                  insertNetworkUsageLogRow(
                      networkUsageLogRepository, request, Status.FAILED, totalBytesRead.get()));
        }
      } finally {
        if (!saveStreamToResumeWhenClientIsReadyAgain) {
          try {
            bodyStream.close();
          } catch (IOException e) {
            logger.atWarning().withCause(e).log(
                "Encountered an error while closing the download stream");
          }
        } else {
          logger.atFine().log("Keeping download stream open for URL: [%s]", request.getUrl());
        }
      }
    }
  }
}
