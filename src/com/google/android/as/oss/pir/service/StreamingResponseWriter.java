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

import com.google.android.as.oss.pir.api.pir.proto.BytesDownloadedEvent;
import com.google.android.as.oss.pir.api.pir.proto.DeletePartialDownloadEvent;
import com.google.android.as.oss.pir.api.pir.proto.PirDownloadResponse;
import com.google.android.as.oss.pir.service.PirGrpcBindableService.PirDownloadStatus;
import com.google.private_retrieval.pir.ResponseWriter;
import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

class StreamingResponseWriter implements ResponseWriter {
  private final StreamObserver<PirDownloadResponse> responseObserver;
  private final DelegatingDownloadListener downloadListener;
  private final PirDownloadStatus pirDownloadStatus;
  private final String url;

  public StreamingResponseWriter(
      StreamObserver<PirDownloadResponse> responseObserver,
      DelegatingDownloadListener downloadListener,
      PirDownloadStatus pirDownloadStatus,
      String url) {
    this.responseObserver = responseObserver;
    this.downloadListener = downloadListener;
    this.pirDownloadStatus = pirDownloadStatus;
    this.url = url;
  }

  @Override
  public void writeResponse(
      byte[] data, long downloadOffsetBytes, int dataBufferOffsetBytes, int countBytes) {
    pirLogger.logDebug("Writing [%d] bytes to response stream.", countBytes);
    if (pirDownloadStatus.getOperationCompleted().get()) {
      pirLogger.logDebug(
          "Stream is already finalized, dropping response data: %d bytes", countBytes);
      return;
    }
    pirDownloadStatus.getNumDownloadedBytes().set(downloadOffsetBytes + countBytes);
    try {
      responseObserver.onNext(
          PirDownloadResponse.newBuilder()
              .setBytesDownloadedEvent(
                  BytesDownloadedEvent.newBuilder()
                      .setOffset(downloadOffsetBytes)
                      .setPayload(ByteString.copyFrom(data, dataBufferOffsetBytes, countBytes)))
              .build());
    } catch (StatusRuntimeException e) {
      downloadListener.onStatusRuntimeException(url, e);
    }
  }

  @Override
  public void deleteOutput() {
    pirLogger.logDebug("Sending deletion event to PIR client.");
    if (pirDownloadStatus.getOperationCompleted().get()) {
      pirLogger.logDebug("Stream is already finalized, dropping deletion event.");
      return;
    }
    pirDownloadStatus.getNumDownloadedBytes().set(0);
    try {
      responseObserver.onNext(
          PirDownloadResponse.newBuilder()
              .setDeletePartialDownloadEvent(DeletePartialDownloadEvent.getDefaultInstance())
              .build());
    } catch (StatusRuntimeException e) {
      downloadListener.onStatusRuntimeException(url, e);
    }
  }

  @Override
  public long getNumExistingBytes() {
    return pirDownloadStatus.getNumDownloadedBytes().get();
  }
}
