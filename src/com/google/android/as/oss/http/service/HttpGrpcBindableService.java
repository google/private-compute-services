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

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.http.api.proto.HttpDownloadRequest;
import com.google.android.as.oss.http.api.proto.HttpDownloadResponse;
import com.google.android.as.oss.http.api.proto.HttpProperty;
import com.google.android.as.oss.http.api.proto.HttpServiceGrpc;
import com.google.android.as.oss.http.api.proto.ResponseBodyChunk;
import com.google.android.as.oss.http.api.proto.ResponseHeaders;
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

  @Inject
  HttpGrpcBindableService(
      OkHttpClient client,
      Executor ioExecutor,
      NetworkUsageLogRepository networkUsageLogRepository) {
    this.client = client;
    this.executor = ioExecutor;
    this.networkUsageLogRepository = networkUsageLogRepository;
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
      insertNetworkUsageLogRow(request, Status.FAILED, 0L);
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
      insertNetworkUsageLogRow(request, Status.SUCCEEDED, 0L);
      return;
    }

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

              logger.atInfo().log("Responding with fetch_completed for URL '%s'", request.getUrl());
              responseObserver.onCompleted();
              insertNetworkUsageLogRow(request, Status.SUCCEEDED, totalBytesRead);
            } catch (IOException e) {
              logger.atWarning().withCause(e).log(
                  "Failed performing IO operation while handling URL '%s'", request.getUrl());
              responseObserver.onError(e);
              insertNetworkUsageLogRow(request, Status.FAILED, totalBytesRead);
            } catch (StatusRuntimeException e) {
              if (responseObserver instanceof ServerCallStreamObserver
                  && ((ServerCallStreamObserver) responseObserver).isCancelled()) {
                logger.atWarning().withCause(e).log(
                    "Failed to fetch response body for URL '%s'. Call cancelled by client.",
                    request.getUrl());
              } else {
                responseObserver.onError(e);
                insertNetworkUsageLogRow(request, Status.FAILED, totalBytesRead);
              }
            }
          }
        });
  }

  private void insertNetworkUsageLogRow(HttpDownloadRequest request, Status status, long size) {
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
}
