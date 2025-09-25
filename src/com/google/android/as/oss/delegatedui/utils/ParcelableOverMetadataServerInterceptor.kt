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

package com.google.android.`as`.oss.delegatedui.utils

import android.os.Parcelable
import io.grpc.Context
import io.grpc.Contexts
import io.grpc.ForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

/**
 * Grpc server interceptor that copies the request payload provided in the metadata to grpc context
 * and does the same for response payload using response headers.
 */
class ParcelableOverMetadataServerInterceptor(private vararg val parcelableKeys: ParcelableKey<*>) :
  ServerInterceptor {
  override fun <ReqT, RespT> interceptCall(
    call: ServerCall<ReqT, RespT>,
    requestHeaders: Metadata,
    next: ServerCallHandler<ReqT, RespT>,
  ): ServerCall.Listener<ReqT> {
    var context = Context.current().withMetadataPayload(requestHeaders)
    context = context.initializeResponseKeys()
    return Contexts.interceptCall(
      context,
      object : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(call) {
        private val sendHeadersHasBeenCalled: AtomicBoolean = AtomicBoolean(false)

        override fun sendHeaders(responseHeaders: Metadata) {
          responseHeaders.putContextPayload(context, headers = true)
          sendHeadersHasBeenCalled.set(true)

          super.sendHeaders(responseHeaders)
        }

        override fun close(status: Status, responseTrailers: Metadata) {
          // Check for engineering mistake of attaching headers after response has been sent.
          if (sendHeadersHasBeenCalled.get() && context.containsResponseHeadersPayload()) {
            val statusOverride: Status =
              Status.INTERNAL.withDescription(
                "Parcelable response headers can be populated only before the first response."
              )
            super.close(statusOverride, responseTrailers)
          } else {
            responseTrailers.putContextPayload(context, headers = false)
            super.close(status, responseTrailers)
          }
        }
      },
      requestHeaders,
      next,
    )
  }

  /**
   * For each of provided [parcelableKeys] copies metadata payload into grpc context with defined
   * request key (if metadata object is provided) and initializes holder (atomic reference) in the
   * context with response key for the possible response payload.
   */
  private fun Context.withMetadataPayload(metadata: Metadata): Context {
    var context = this
    for (key in parcelableKeys) context = context.withMetadataPayload(metadata, key)
    return context
  }

  private fun <P : Parcelable> Context.withMetadataPayload(
    metadata: Metadata,
    key: ParcelableKey<P>,
  ): Context {
    var context = this
    val valuesList = metadata.getAll(key.metadataKey)?.map { it as P } ?: emptyList()
    if (valuesList.isNotEmpty()) {
      context = context.withValue(key.contextKey, valuesList)
    }
    return context
  }

  /**
   * For each of provided [parcelableKeys] initializes holder (atomic reference) in the context with
   * response key for the possible response payload.
   */
  private fun Context.initializeResponseKeys(): Context {
    var context = this
    for (key in parcelableKeys) context = context.initializeResponseKey(key)
    return context
  }

  private fun <P : Parcelable> Context.initializeResponseKey(key: ParcelableKey<P>): Context =
    withValue(key.responseHeaderContextKey, AtomicReference<List<P>?>())
      .withValue(key.responseTrailerContextKey, AtomicReference<List<P>?>())

  /**
   * For each of provided [parcelableKeys] copies payload (if populated) from grpc context using
   * response key into response metadata (headers).
   */
  private fun Metadata.putContextPayload(context: Context, headers: Boolean) {
    for (key in parcelableKeys) putContextPayload(context, key, headers)
  }

  private fun <P : Parcelable> Metadata.putContextPayload(
    context: Context,
    key: ParcelableKey<P>,
    headers: Boolean,
  ) {
    val responseKey: Context.Key<AtomicReference<List<P>?>> =
      if (headers) key.responseHeaderContextKey else key.responseTrailerContextKey
    val values: List<P>? = responseKey.get(context).getAndSet(null)
    if (values != null) {
      for (item in values) {
        put(key.metadataKey, item)
      }
    }
  }

  /** Sanity check for response headers being populated after response message has been sent. */
  private fun Context.containsResponseHeadersPayload(): Boolean {
    for (key in parcelableKeys) {
      if (key.responseHeaderContextKey.get(/* context= */ this).get() != null) {
        return true
      }
    }

    return false
  }
}
