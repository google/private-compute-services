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

package com.google.android.`as`.oss.privateinference.util

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive

/**
 * Coroutine-safe version of [runCatching] which throws any [CancellationException] and [Error]
 * instances it observes to preserve structured concurrency and avoid catching catastrophic
 * failures, respectively.
 */
suspend inline fun <T> runSuspendCatching(block: () -> T): Result<T> =
  runCatching(block).reThrowCancellation().reThrowError()

/**
 * Coroutine-safe version of [Result.mapCatching] which throws any [CancellationException] and
 * [Error] instances it observes to preserve structured concurrency and avoid catching catastrophic
 * failures, respectively.
 */
suspend inline fun <T, R> Result<T>.mapSuspendCatching(block: (T) -> R): Result<R> =
  mapCatching(block).reThrowCancellation().reThrowError()

/**
 * Re-throws [CancellationException] throwables.
 *
 * If the receiving [Result] observes a [CancellationException], it should re-throw that exception
 * so that the coroutine observing the result is correctly cancelled.
 */
suspend fun <T> Result<T>.reThrowCancellation(): Result<T> = onFailure {
  if (it is CancellationException && !currentCoroutineContext().isActive) throw it
}

/**
 * Re-throws [Error] throwables.
 *
 * [Error] throwables should not be caught the same way that [Exceptions] and other [Throwables]
 * generally can be. For example, if [OutOfMemoryError] is observed, there is no real recovery that
 * could be done.
 */
fun <T> Result<T>.reThrowError(): Result<T> = onFailure { if (it is Error) throw it }
