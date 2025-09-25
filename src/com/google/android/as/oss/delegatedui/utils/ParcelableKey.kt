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
import io.grpc.Metadata
import io.grpc.binder.ParcelableUtils
import java.util.concurrent.atomic.AtomicReference

/** Parcelable Key used for sending/receiving parcelables over gRPC. */
sealed class ParcelableKey<P : Parcelable>(

  /** Metadata key for parcelable to be used to transport over grpc. */
  val metadataKey: Metadata.Key<P>,

  /**
   * The context key to be populated with data retrieved from header metadata, structured as a list
   * as multiple values are allowed for the same metadata key.
   */
  val contextKey: Context.Key<List<P>>,

  /**
   * The context key to be checked for placing data into response headers metadata, structured as a
   * list as multiple values are allowed for the same metadata key.
   */
  val responseHeaderContextKey: Context.Key<AtomicReference<List<P>?>>,

  /**
   * The context key to be checked for placing data into response trailers metadata, structured as a
   * list as multiple values are allowed for the same metadata key.
   */
  val responseTrailerContextKey: Context.Key<AtomicReference<List<P>?>>,
) {
  constructor(
    name: String,
    creator: Parcelable.Creator<P>,
  ) : this(
    metadataKey = ParcelableUtils.metadataKey(getStringKey(name), creator),
    contextKey = Context.key("REQ-${getStringKey(name)}"),
    responseHeaderContextKey = Context.key("RESH-${getStringKey(name)}"),
    responseTrailerContextKey = Context.key("REST-${getStringKey(name)}"),
  )

  companion object {
    /**
     * Returns a string key for the parcelable. All metadata and context keys should be derived from
     * this string key.
     *
     * NEVER CHANGE this key format, it is used by clients and servers to identify the parcelable.
     */
    private fun getStringKey(name: String) = "$name-bin"
  }
}

/** Key for sending/receiving a single parcelable over gRPC. */
class SingleParcelableKey<P : Parcelable>(val name: String, val creator: Parcelable.Creator<P>) :
  ParcelableKey<P>(name, creator) {}

/**
 * Key for sending/receiving list of parcelables (repeated values) over gRPC.
 *
 * NOTE: Under the hood, both SimpleParcelableKey and RepeatedParcelableKey have the same
 * implementation, since gRPC metadata transport natively supports repeated values for the same key,
 * we just offer two separate types for easier use by clients.
 */
class RepeatedParcelableKey<P : Parcelable>(val name: String, val creator: Parcelable.Creator<P>) :
  ParcelableKey<P>(name, creator) {}
