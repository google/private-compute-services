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

import androidx.annotation.NonNull;
import com.google.android.as.oss.privateinference.library.oakutil.AttestationVerificationException;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceResponse;
import com.google.android.as.oss.privateinference.service.api.proto.PrivateInferenceSessionResponse;
import com.google.android.as.oss.privateinference.service.api.proto.SessionInitializationResponse;
import com.google.oak.client.grpc.StreamObserverSessionClient;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An implementation of {@link StreamObserverSessionClient.OakSessionStreamObserver} for handling
 * responses from the Oak server.
 */
// TODO: Make this a kotlin class for consistency with others in this dir.
public class BaseOakServerStreamResponseObserver
    implements StreamObserverSessionClient.OakSessionStreamObserver {

  /** The observer for sending responses back to the client. */
  private final StreamObserver<PrivateInferenceSessionResponse> clientSessionResponseObserver;

  /**
   * The observer for sending requests directly to the Oak client. This is set once the session is
   * opened.
   */
  private final AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver;

  public BaseOakServerStreamResponseObserver(
      StreamObserver<PrivateInferenceSessionResponse> clientSessionResponseObserver,
      AtomicReference<StreamObserver<ByteString>> directOakClientRequestObserver) {
    this.clientSessionResponseObserver = clientSessionResponseObserver;
    this.directOakClientRequestObserver = directOakClientRequestObserver;
  }

  @Override
  public void onSessionOpen(@NonNull StreamObserver<ByteString> clientRequestsStreamObserver) {
    directOakClientRequestObserver.set(clientRequestsStreamObserver);
    clientSessionResponseObserver.onNext(
        PrivateInferenceSessionResponse.newBuilder()
            .setSessionInitializationResponse(
                SessionInitializationResponse.newBuilder().setInitializationSucceeded(true))
            .build());
  }

  @Override
  public void onNext(ByteString oakResponse) {
    clientSessionResponseObserver.onNext(
        PrivateInferenceSessionResponse.newBuilder()
            .setInferenceResponse(PrivateInferenceResponse.newBuilder().setData(oakResponse))
            .build());
  }

  @Override
  public void onError(Throwable t) {
    clientSessionResponseObserver.onError(wrapIfAttestationFailure(t));
  }

  @Override
  public void onCompleted() {
    clientSessionResponseObserver.onCompleted();
  }

  private Throwable wrapIfAttestationFailure(Throwable t) {
    Status status = Status.fromThrowable(t);
    if (status.getCause() instanceof AttestationVerificationException) {
      return Status.INTERNAL
          .withDescription("Server attestation verification failed (public key expired)")
          .withCause(t)
          .asRuntimeException();
    } else if (status.getCode() == Code.PERMISSION_DENIED) {
      return Status.INTERNAL
          .withDescription("Device attestation verification failed")
          .withCause(t)
          .asRuntimeException();
    } else {
      return t;
    }
  }
}
