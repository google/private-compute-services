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
import io.grpc.BindableService
import io.grpc.Context
import io.grpc.Metadata
import io.grpc.stub.AbstractStub
import io.grpc.stub.MetadataUtils
import java.util.concurrent.atomic.AtomicReference

object ParcelableOverRpcUtils {

  /** Helper to be called on the client for sending single parcelables over request headers. */
  fun <Stub : AbstractStub<Stub>, P : Parcelable> AbstractStub<Stub>.sendParcelableInRequest(
    key: SingleParcelableKey<P>,
    value: P,
  ): Stub {
    val metadata = Metadata().apply { put(key.metadataKey, value) }
    return withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
  }

  /** Helper to be called on the client for sending repeated parcelables over request headers. */
  fun <Stub : AbstractStub<Stub>, P : Parcelable> AbstractStub<Stub>.sendParcelablesInRequest(
    key: RepeatedParcelableKey<P>,
    values: List<P>,
  ): Stub {
    val metadata = Metadata().apply { for (value in values) put(key.metadataKey, value) }
    return withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
  }

  /** Helper to be called on the client for receiving single parcelables over response headers. */
  fun <Stub : AbstractStub<Stub>, P : Parcelable> AbstractStub<Stub>.receiveParcelableFromResponse(
    key: SingleParcelableKey<P>,
    delegate: ParcelableOverRpcDelegate<P>,
  ): Stub {
    val headerCapture = AtomicReference<Metadata>()
    delegate.setValue { headerCapture.get()?.get(key.metadataKey) }
    return withInterceptors(
      MetadataUtils.newCaptureMetadataInterceptor(headerCapture, AtomicReference())
    )
  }

  /** Helper to be called on the client for receiving repeated parcelables over response headers. */
  fun <Stub : AbstractStub<Stub>, P : Parcelable> AbstractStub<Stub>.receiveParcelablesFromResponse(
    key: RepeatedParcelableKey<P>,
    delegate: ParcelableOverRpcDelegate<List<P>>,
  ): Stub {
    val headerCapture = AtomicReference<Metadata>()
    delegate.setValue {
      headerCapture.get()?.let { capturedMetadata ->
        val allValues = capturedMetadata.getAll(key.metadataKey)
        allValues?.map { it as P }
      }
    }
    return withInterceptors(
      MetadataUtils.newCaptureMetadataInterceptor(headerCapture, AtomicReference())
    )
  }

  /**
   * Helper to be called on the server for receiving single parcelables over request headers, throws
   * an exception if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.receiveParcelableFromRequest(
    key: SingleParcelableKey<P>,
    context: Context = Context.current(),
  ): P = receiveParcelableOrNullFromRequest(key, context) as P

  /**
   * Helper to be called on the server for receiving single parcelables over request headers,
   * returns null if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.receiveParcelableOrNullFromRequest(
    key: SingleParcelableKey<P>,
    context: Context = Context.current(),
  ): P? = key.contextKey.get(context)?.firstOrNull()

  /**
   * Helper to be called on the server for receiving repeated parcelables over request headers,
   * throws an exception if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.receiveParcelablesFromRequest(
    key: RepeatedParcelableKey<P>,
    context: Context = Context.current(),
  ): List<P> = receiveParcelablesOrNullFromRequest(key, context) as List<P>

  /**
   * Helper to be called on the server for receiving repeated parcelables over request headers,
   * returns null if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.receiveParcelablesOrNullFromRequest(
    key: RepeatedParcelableKey<P>,
    context: Context = Context.current(),
  ): List<P>? = key.contextKey.get(context)

  /** Helper to be called on the server for sending single parcelables over response headers. */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.attachParcelableToResponse(
    key: SingleParcelableKey<P>,
    value: P,
    context: Context = Context.current(),
  ) = key.responseHeaderContextKey.get(context).set(listOf(value))

  /** Helper to be called on the server for sending repeated parcelables over response headers. */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> BindableService.attachParcelablesToResponse(
    key: RepeatedParcelableKey<P>,
    values: List<P>,
    context: Context = Context.current(),
  ) = key.responseHeaderContextKey.get(context).set(values)

  /** Creates an empty value holder for receiving Parcelables over RPC. */
  fun <T : Parcelable> delegateOf(): ParcelableOverRpcDelegate<T> = ParcelableOverRpcDelegate()

  /** Creates a value holder for receiving Parcelables over RPC. */
  fun <T : Parcelable> T?.delegateOf(): ParcelableOverRpcDelegate<T> {
    val value = this@delegateOf
    return ParcelableOverRpcDelegate<T>().apply { this.setValue { value } }
  }

  /** Creates an empty value holder for receiving Parcelables over RPC, structured as a list. */
  fun <T : Parcelable> delegateListOf(): ParcelableOverRpcDelegate<List<T>> =
    ParcelableOverRpcDelegate()

  /** Creates a value holder for receiving Parcelables over RPC, structured as a list. */
  fun <T : Parcelable> List<T>?.delegateListOf(): ParcelableOverRpcDelegate<List<T>> {
    val value = this@delegateListOf
    return ParcelableOverRpcDelegate<List<T>>().apply { this.setValue { value } }
  }

  /**
   * Converts a [ParcelableOverRpcDelegate<T>] value holder to a [ParcelableOverRpcDelegate<P>]
   * value holder.
   */
  fun <T, P> ParcelableOverRpcDelegate<T>.transform(
    transform: (T?) -> P?
  ): ParcelableOverRpcDelegate<P> {
    val parentDelegate = this
    return ParcelableOverRpcDelegate<P>().apply { setValue { transform(parentDelegate.value) } }
  }
}

/** Holds some value [T]. */
class ParcelableOverRpcDelegate<T> internal constructor() {
  private var provider: () -> T? = { null }

  /**
   * Returns the value held by the delegate.
   *
   * @throws NullPointerException Throws if there is no valid value held by the delegate.
   */
  val value: T?
    get() = provider()

  /** Sets the value held by the delegate. */
  internal fun setValue(valueProvider: () -> T?) {
    provider = valueProvider
  }

  override fun toString(): String {
    return "ParcelableOverRpcDelegate($value)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ParcelableOverRpcDelegate<*>) return false

    if (value != other.value) return false

    return true
  }

  override fun hashCode(): Int {
    return value?.hashCode() ?: 0
  }
}
