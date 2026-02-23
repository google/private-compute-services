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
import io.grpc.stub.AbstractStub
import io.grpc.stub.MetadataUtils
import java.util.concurrent.atomic.AtomicReference

interface ParcelableOverRpcUtils {

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
    delegate.setValue(key) { headerCapture.get()?.get(key.metadataKey) }
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
    delegate.setValue(key) { headerCapture.get()?.getAll(key.metadataKey)?.toList() }
    return withInterceptors(
      MetadataUtils.newCaptureMetadataInterceptor(headerCapture, AtomicReference())
    )
  }

  /**
   * Helper to be called on the server for receiving single parcelables over request headers, throws
   * an exception if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> receiveParcelableFromRequest(
    key: SingleParcelableKey<P>,
    context: Context = Context.current(),
  ): P = receiveParcelableOrNullFromRequest(key, context) ?: throw NoSuchParcelableException(key)

  /**
   * Helper to be called on the server for receiving single parcelables over request headers,
   * returns null if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> receiveParcelableOrNullFromRequest(
    key: SingleParcelableKey<P>,
    context: Context = Context.current(),
  ): P? = key.contextKey.get(context)?.firstOrNull()

  /**
   * Helper to be called on the server for receiving repeated parcelables over request headers,
   * throws an exception if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> receiveParcelablesFromRequest(
    key: RepeatedParcelableKey<P>,
    context: Context = Context.current(),
  ): List<P> =
    receiveParcelablesOrNullFromRequest(key, context) ?: throw NoSuchParcelableException(key)

  /**
   * Helper to be called on the server for receiving repeated parcelables over request headers,
   * returns null if not found.
   */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> receiveParcelablesOrNullFromRequest(
    key: RepeatedParcelableKey<P>,
    context: Context = Context.current(),
  ): List<P>? = key.contextKey.get(context)

  /** Helper to be called on the server for sending single parcelables over response headers. */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> attachParcelableToResponse(
    key: SingleParcelableKey<P>,
    value: P,
    context: Context = Context.current(),
  ) = key.responseHeaderContextKey.get(context).set(listOf(value))

  /** Helper to be called on the server for sending repeated parcelables over response headers. */
  @Suppress("UnusedReceiverParameter")
  fun <P : Parcelable> attachParcelablesToResponse(
    key: RepeatedParcelableKey<P>,
    values: List<P>,
    context: Context = Context.current(),
  ) = key.responseHeaderContextKey.get(context).set(values)
}

/** Thrown to indicate that the parcelable for the given key was not found. */
class NoSuchParcelableException(key: ParcelableKey<*>?) :
  RuntimeException("No parcelables found for key: ${key ?: "UNKNOWN"}.")

/** Holds some value [T]. */
class ParcelableOverRpcDelegate<T> internal constructor() {
  private var key: ParcelableKey<*>? = null
  private var provider: () -> T? = { null }

  /** Returns the value held by the delegate, or null if there is no value held by the delegate. */
  val valueOrNull: T?
    get() = provider()

  /**
   * Returns the value held by the delegate.
   *
   * @throws NoSuchParcelableException Throws if there is no valid value held by the delegate.
   */
  val value: T
    get() = valueOrNull ?: throw NoSuchParcelableException(key)

  /** Sets the value held by the delegate. */
  internal fun setValue(key: ParcelableKey<*>? = null, valueProvider: () -> T?) {
    this.key = key
    this.provider = valueProvider
  }

  override fun toString(): String {
    return "ParcelableOverRpcDelegate($valueOrNull)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is ParcelableOverRpcDelegate<*>) return false

    if (valueOrNull != other.valueOrNull) return false

    return true
  }

  override fun hashCode(): Int {
    return valueOrNull?.hashCode() ?: 0
  }

  companion object {
    /** Creates an empty value holder for receiving Parcelables over RPC. */
    fun <T : Parcelable> delegateOf(): ParcelableOverRpcDelegate<T> = ParcelableOverRpcDelegate()

    /** Creates a value holder for receiving Parcelables over RPC. */
    fun <T : Parcelable> T?.delegateOf(): ParcelableOverRpcDelegate<T> {
      val value = this@delegateOf
      return ParcelableOverRpcDelegate<T>().apply { value?.let { this.setValue { it } } }
    }

    /** Creates an empty value holder for receiving Parcelables over RPC, structured as a list. */
    fun <T : Parcelable> delegateListOf(): ParcelableOverRpcDelegate<List<T>> =
      ParcelableOverRpcDelegate()

    /** Creates a value holder for receiving Parcelables over RPC, structured as a list. */
    fun <T : Parcelable> List<T>?.delegateListOf(): ParcelableOverRpcDelegate<List<T>> {
      val value = this@delegateListOf
      return ParcelableOverRpcDelegate<List<T>>().apply { value?.let { this.setValue { it } } }
    }
  }
}
