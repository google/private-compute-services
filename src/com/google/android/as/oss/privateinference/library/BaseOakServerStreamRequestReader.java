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

package com.google.android.as.oss.privateinference.library;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.android.as.oss.privateinference.library.oakutil.PrivateInferenceOakAsyncClient;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceRequest;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionRequest;
import com.google.android.as.oss.privateinference.service.api.proto.SessionInitializationRequest;
import com.google.common.flogger.GoogleLogger;
import com.google.oak.client.grpc.StreamObserverSessionClient;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * A {@link StreamObserver} for {@link PrivateInferenceSessionRequest} that reads requests from the
 * client and forwards them to the Oak server.
 *
 * <p>This class processes incoming {@link PrivateInferenceSessionRequest} messages *after* the
 * initial session setup. It primarily handles {@link PrivateInferenceRequest} messages, which
 * contain the actual inference data.
 *
 * <p>The inference data can be provided in two ways:
 *
 * <ul>
 *   <li>Directly as a {@link ByteString}.
 *   <li>Via a ParcelFileDescriptor (PFD), typically used for efficient inter-process communication
 *       (IPC) if the client is in a separate process.
 * </ul>
 *
 * <p><b>Note:</b> The initial {@link SessionInitializationRequest} is handled <i>outside</i> of
 * this class. This reader is instantiated *after* initialization, and its {@code
 * directOakClientRequestObserver} (used to forward data to Oak) must be set before any inference
 * requests are streamed.
 */
public class BaseOakServerStreamRequestReader
    implements StreamObserver<PrivateInferenceSessionRequest> {

  @Nullable public final InputStream parcelInputStream;
  private final PrivateInferenceOakAsyncClient oakAsyncClient;
  private final StreamObserverSessionClient.OakSessionStreamObserver
      oakServerStreamResponseObserver;

  private final AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver;

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public BaseOakServerStreamRequestReader(
      PrivateInferenceOakAsyncClient oakAsyncClient,
      StreamObserverSessionClient.OakSessionStreamObserver oakServerStreamResponseObserver,
      AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver,
      @Nullable InputStream parcelInputStream) {
    this.parcelInputStream = parcelInputStream;
    this.oakAsyncClient = oakAsyncClient;
    this.oakServerStreamResponseObserver = oakServerStreamResponseObserver;
    this.directOakClientRequestObserver = directOakClientRequestObserver;
  }

  @Override
  public void onNext(PrivateInferenceSessionRequest privateInferenceSessionRequest) {
    if (privateInferenceSessionRequest.hasSessionInitializationRequest()) {
      SessionInitializationRequest initializationRequest =
          privateInferenceSessionRequest.getSessionInitializationRequest();
      oakAsyncClient.startNoiseSession(
          buildPrivateInferenceRequestMetadata(initializationRequest),
          oakServerStreamResponseObserver);
    } else if (privateInferenceSessionRequest.hasInferenceRequest()) {
      // oakClientStreamObserver should have been created in during the handshake.
      if (directOakClientRequestObserver.get() == null) {
        tryCloseInputStream(parcelInputStream);
        throw new IllegalStateException(
            "oakClientStreamObserver is null, likely due to that the initialization"
                + " request is not received.");
      }

      PrivateInferenceRequest inferenceRequest =
          privateInferenceSessionRequest.getInferenceRequest();
      if (inferenceRequest.hasData()) {
        ByteString messageData = inferenceRequest.getData();
        checkNotNull(directOakClientRequestObserver.get()).onNext(messageData);
      } else if (inferenceRequest.hasPfdDataSize()) {
        if (parcelInputStream == null) {
          throw new IllegalStateException(
              "parcelInputStream is null so cannot receive PrivateArateaRequest through PFD.");
        }
        int pfdDataSize = inferenceRequest.getPfdDataSize();
        byte[] messageData = new byte[pfdDataSize];
        int totalBytesRead = 0;
        try {
          while (totalBytesRead < pfdDataSize) {
            int bytesToRead = pfdDataSize - totalBytesRead;
            int bytesRead = parcelInputStream.read(messageData, totalBytesRead, bytesToRead);
            if (bytesRead == -1) {
              throw new IOException("PFD closed prematurely");
            }
            totalBytesRead += bytesRead;
          }

          checkNotNull(directOakClientRequestObserver.get())
              .onNext(ByteString.copyFrom(messageData));
        } catch (IOException e) {
          checkNotNull(directOakClientRequestObserver.get()).onError(e);
          tryCloseInputStream(parcelInputStream);
        }
      } else {
        onError(
            new IllegalArgumentException(
                "PrivateInferenceSessionRequest must have either session_initialization_request or"
                    + " inference_request set."));
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    StreamObserver<ByteString> observer = directOakClientRequestObserver.get();
    if (observer != null) {
      observer.onError(t);
    }
    tryCloseInputStream(parcelInputStream);
  }

  @Override
  public void onCompleted() {
    StreamObserver<ByteString> observer = directOakClientRequestObserver.get();
    if (observer != null) {
      observer.onCompleted();
    }
    tryCloseInputStream(parcelInputStream);
  }

  private PrivateInferenceRequestMetadata buildPrivateInferenceRequestMetadata(
      SessionInitializationRequest request) {
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

  public void tryCloseInputStream(@Nullable InputStream inputStream) {
    if (inputStream == null) {
      return;
    }

    try {
      inputStream.close();
    } catch (IOException e) {
      logger.atWarning().withCause(e).log("Failed to close the PFD input pipe.");
    }
  }
}
