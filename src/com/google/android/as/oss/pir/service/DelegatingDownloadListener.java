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

import static com.google.android.as.oss.pir.service.PirGrpcBindableService.pirLogger;

import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.pir.api.pir.proto.NumberOfChunksDeterminedEvent;
import com.google.android.as.oss.pir.api.pir.proto.PirDownloadResponse;
import com.google.android.as.oss.pir.service.PirGrpcBindableService.PirDownloadStatus;
import com.google.common.logging.privateretrieval.PirLog.PirEvent;
import com.google.private_retrieval.pir.PirDownloadListener;
import com.google.private_retrieval.pir.PirUri;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

class DelegatingDownloadListener implements PirDownloadListener {
  private final PirDownloadStatus pirDownloadStatus;
  private final StreamObserver<PirDownloadResponse> responseObserver;
  private final NetworkUsageLogRepository networkUsageLogRepository;
  private final String url;

  public DelegatingDownloadListener(
      StreamObserver<PirDownloadResponse> responseObserver,
      NetworkUsageLogRepository networkUsageLogRepository,
      PirDownloadStatus pirDownloadStatus,
      String url) {
    this.responseObserver = responseObserver;
    this.networkUsageLogRepository = networkUsageLogRepository;
    this.pirDownloadStatus = pirDownloadStatus;
    this.url = url;
  }

  @Override
  public void onPirEvent(PirEvent pirEvent) {
    pirLogger.logDebug("Received PIREvent in GRPC service.");
    if (pirDownloadStatus.getOperationCompleted().get()) {
      pirLogger.logDebug("Stream is already finalized, dropping PirEvent message: %s", pirEvent);
      return;
    }
    try {
      responseObserver.onNext(PirDownloadResponse.newBuilder().setPirEvent(pirEvent).build());
    } catch (StatusRuntimeException e) {
      onStatusRuntimeException(url, e);
    }
  }

  @Override
  public void onNumberOfChunksDetermined(int numberOfChunks) {
    pirLogger.logDebug("Received #chunks in GRPC service.");
    if (pirDownloadStatus.getOperationCompleted().get()) {
      pirLogger.logDebug("Stream is already finalized, dropping #chunks message.");
      return;
    }
    try {
      responseObserver.onNext(
          PirDownloadResponse.newBuilder()
              .setNumberOfChunksDeterminted(
                  NumberOfChunksDeterminedEvent.newBuilder().setNumberOfChunks(numberOfChunks))
              .build());
    } catch (StatusRuntimeException e) {
      onStatusRuntimeException(url, e);
    }
  }

  @Override
  public void onSuccess(PirUri pirUri) {
    pirLogger.logDebug("Received success in GRPC service for URI [%s].", pirUri.unparsedUri());
    insertNetworkUsageLogRow(
        url, pirDownloadStatus.getNumDownloadedBytes().get(), Status.SUCCEEDED);
    pirDownloadStatus.getOperationCompleted().set(true);
    responseObserver.onCompleted();
  }

  @Override
  public void onFailure(PirUri pirUri, Exception exception) {
    pirLogger.logDebug(
        exception, "Received failure in GRPC service for URI [%s].", pirUri.unparsedUri());
    insertNetworkUsageLogRow(url, pirDownloadStatus.getNumDownloadedBytes().get(), Status.FAILED);
    pirDownloadStatus.getOperationCompleted().set(true);
    responseObserver.onError(exception);
  }

  void onStatusRuntimeException(String url, StatusRuntimeException e) {
    insertNetworkUsageLogRow(url, pirDownloadStatus.getNumDownloadedBytes().get(), Status.FAILED);
    if (responseObserver instanceof ServerCallStreamObserver
        && ((ServerCallStreamObserver) responseObserver).isCancelled()) {
      pirLogger.logWarn(e, "WARNING: Call cancelled by client.");
    } else {
      responseObserver.onError(e);
    }
  }

  private void insertNetworkUsageLogRow(String url, long downloadedBytes, Status status) {
    if (!networkUsageLogRepository.shouldLogNetworkUsage(ConnectionType.PIR, url)
        || !networkUsageLogRepository.getContentMap().isPresent()) {
      return;
    }
    ConnectionDetails connectionDetails =
        networkUsageLogRepository.getContentMap().get().getPirConnectionDetails(url).get();
    NetworkUsageEntity entity =
        NetworkUsageLogUtils.createPirNetworkUsageEntity(
            connectionDetails, status, downloadedBytes, url);
    networkUsageLogRepository.insertNetworkUsageEntity(entity);
  }
}
