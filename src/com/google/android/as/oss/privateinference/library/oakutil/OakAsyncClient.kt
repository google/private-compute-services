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

package com.google.android.`as`.oss.privateinference.library.oakutil

import com.google.android.`as`.oss.privateinference.library.PrivateInferenceRequestMetadata
import com.google.oak.client.grpc.StreamObserverSessionClient

/**
 * An asynchronous client for Private Inference based on StreamObservers.
 *
 * This interface decouples the client from its concrete implementation, allowing for easier mocking
 * in tests without triggering JNI dependencies.
 */
interface OakAsyncClient {
  /**
   * Starts a noise session for Private Inference.
   *
   * @param requestMetadata The request metadata to use for the gRPC call.
   * @param sessionStreamObserver The observer to receive responses from the server, and that will
   *   receive the [io.grpc.stub.StreamObserver] to use for sending requests, once the stream is
   *   open.
   */
  fun startNoiseSession(
    requestMetadata: PrivateInferenceRequestMetadata,
    sessionStreamObserver: StreamObserverSessionClient.OakSessionStreamObserver,
  )

  /**
   * Starts a TLS session for Private Inference by using the Oak TLS library.
   *
   * @param requestMetadata The request metadata to use for the gRPC call.
   * @param sessionStreamObserver The observer to receive responses from the server, and that will
   *   receive the [io.grpc.stub.StreamObserver] to use for sending requests, once the stream is
   *   open.
   */
  fun startTlsSession(
    requestMetadata: PrivateInferenceRequestMetadata,
    sessionStreamObserver: StreamObserverSessionClient.OakSessionStreamObserver,
  )
}
