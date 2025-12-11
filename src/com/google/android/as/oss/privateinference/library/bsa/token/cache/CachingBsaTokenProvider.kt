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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache

import androidx.annotation.OpenForTesting
import androidx.annotation.VisibleForTesting
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.util.runSuspendCatching
import kotlinx.coroutines.CoroutineScope

/**
 * Implementation of [BsaTokenProvider] which manages a pool of tokens, responding to calls to
 * [fetchTokens] with tokens from the pool. If the pool cannot be used to completely fulfill a
 * request, the [refillDelegate] is used to to up the pool and complete the request.
 *
 * For best results, the typical request size should be smaller than the [TokenPool]'s low-water
 * mark.
 */
@OpenForTesting
open class CachingBsaTokenProvider<T : BsaToken>(
  override val coroutineScope: CoroutineScope,
  private val refillDelegate: BsaTokenProvider<T>,
  private val tokenPool: TokenPool<T>,
) : BsaTokenProvider<T>, BsaTokenCacheControlPlane {
  override val maxBatchSize: Int = Int.MAX_VALUE

  override suspend fun fetchTokens(params: BsaTokenParams<T>, batchSize: Int): Result<List<T>> {
    if (params.mustBeFresh) {
      return fetchFromDelegate(params = params, atLeast = batchSize, predicate = { true })
        .mapCatching { it.take(batchSize) }
    }

    return runSuspendCatching {
      tokenPool.draw(params, batchSize) { params, count, predicate ->
        fetchFromDelegate(params, count, predicate).getOrThrow()
      }
    }
  }

  override suspend fun invalidate() = tokenPool.clear()

  override suspend fun invalidateAndRefill() {
    val delegateCache = refillDelegate as? CachingBsaTokenProvider
    if (delegateCache == null) {
      tokenPool.refresh { params, count, predicate ->
        fetchFromDelegate(params, count, predicate).getOrThrow()
      }
    } else {
      delegateCache.invalidateAndRefill()
      tokenPool.clear()
    }
  }

  @VisibleForTesting
  suspend fun fetchFromDelegate(
    params: BsaTokenParams<T>,
    atLeast: Int,
    predicate: TokenValidityPredicate<T>,
  ): Result<List<T>> {
    val fetched = buildList {
      do {
        // Be sure to call with a batch-size of whichever value is lowest: the amount we want to
        // fetch
        // or the delegate's maxBatchSize - so that we never get a batch-size-exceeded exception
        // from
        // the delegate.
        refillDelegate
          .fetchTokens(
            params = params,
            batchSize = minOf(atLeast - this@buildList.size, refillDelegate.maxBatchSize),
          )
          .mapCatching { tokens ->
            // Only accept cacheable values. If there are none, we should fail.
            val valid = tokens.filter(predicate)
            check(valid.isNotEmpty()) { "No valid tokens found in response from refillDelegate" }
            valid
          }
          .onFailure {
            // Any failure should stop the process.
            return Result.failure(exception = it)
          }
          .onSuccess { tokens -> this@buildList.addAll(tokens) }
      } while (this@buildList.size < atLeast)
    }
    return Result.success(fetched)
  }
}
