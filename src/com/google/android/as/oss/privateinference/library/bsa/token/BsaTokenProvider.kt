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

package com.google.android.`as`.oss.privateinference.library.bsa.token

import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.guava.future

/** Defines an object capable of fetching and returning [BsaToken] instances of type [T]. */
interface BsaTokenProvider<T : BsaToken> {
  /**
   * This is the maximum value allowed to be passed to [fetchTokens]. It defines the largest batch
   * of tokens a caller can request at a time.
   */
  val maxBatchSize: Int

  /**
   * [CoroutineScope] on which any asynchronous jobs should be run outside of the structured
   * concurrency used by [fetchTokens].
   */
  val coroutineScope: CoroutineScope

  /**
   * Fetches a [BsaToken] of type [T] given the specified [BsaTokenParams]. Suspends while the token
   * is being fetched.
   *
   * @return a [Result] which, on success, contains a [BsaToken]. If a failure occurred while
   *   fetching, the Result will contain that failure.
   */
  suspend fun fetchToken(params: BsaTokenParams<T>): Result<T> =
    fetchTokens(params, batchSize = 1).map { it.first() }

  /**
   * Returns a [ListenableFuture] which fetches a [BsaToken] of type [T].
   *
   * @param executor The [Executor] on which the fetch operation should run.
   */
  fun fetchTokenFuture(executor: Executor, params: BsaTokenParams<T>): ListenableFuture<T> =
    coroutineScope.future(executor.asCoroutineDispatcher()) { fetchToken(params).getOrThrow() }

  /**
   * Fetches a batch of [BsaToken] instances of type [T] where the size of the batch is defined by
   * [batchSize]
   *
   * @return a failure result with an [IllegalArgumentException] if [batchSize] is greater than
   *   [maxBatchSize].
   */
  suspend fun fetchTokens(params: BsaTokenParams<T>, batchSize: Int = maxBatchSize): Result<List<T>>

  /**
   * Injection qualifier for implementation of [BsaTokenProvider] which performs actual token
   * generation.
   */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class Authenticating

  /** Injection qualifier for implementation of [BsaTokenProvider] which caches tokens in memory. */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class MemoryCached

  /** Injection qualifier for implementation of [BsaTokenProvider] which caches tokens on disk. */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class DiskCached

  /**
   * Injection qualifier for implementation of [BsaTokenProvider] which uses both memory and disk
   * caching in a multilevel fashion.
   */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class MultilevelCached
}
