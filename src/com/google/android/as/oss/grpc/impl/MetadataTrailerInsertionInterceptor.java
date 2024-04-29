/*
 * Copyright 2024 Google LLC
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

package com.google.android.as.oss.grpc.impl;

import android.annotation.TargetApi;
import android.os.Build;
import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import java.util.concurrent.atomic.AtomicReference;

/** An interceptor for copying data from the RPC context to the response trailer metadata. */
@TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class MetadataTrailerInsertionInterceptor<T> implements ServerInterceptor {
  private final Context.Key<AtomicReference<T>> contextKey;
  private final Metadata.Key<T> metadataKey;

  /**
   * @param contextKey The key for the AtomicReference that will be copied from.
   * @param metadataKey The key used for placement of a value into the trailer metadata.
   */
  public MetadataTrailerInsertionInterceptor(
      Context.Key<AtomicReference<T>> contextKey, Metadata.Key<T> metadataKey) {
    this.contextKey = contextKey;
    this.metadataKey = metadataKey;
  }

  @Override
  public <ReqT, RespT> Listener<ReqT> interceptCall(
      ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
    AtomicReference<T> ref = new AtomicReference<T>();
    System.err.println("Seeding context with ref: " + System.identityHashCode(ref));
    Context context = Context.current().withValue(contextKey, ref);
    ServerCall<ReqT, RespT> wrappedCall =
        new SimpleForwardingServerCall<ReqT, RespT>(call) {
          @Override
          public void close(Status status, Metadata trailers) {
            T val = ref.get();
            System.err.println("val in interceptor: " + val);
            if (val != null) {
              trailers.put(metadataKey, val);
            }
            super.close(status, trailers);
          }
        };
    return Contexts.interceptCall(context, wrappedCall, headers, next);
  }
}
