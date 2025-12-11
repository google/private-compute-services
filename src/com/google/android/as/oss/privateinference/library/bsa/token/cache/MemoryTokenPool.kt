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

import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of [TokenPool] which manages its pool in-memory as a mapping from [BsaTokenParams]
 * to queues of tokens.
 */
class MemoryTokenPool<T : BsaToken>(
  private val refreshParams: List<BsaTokenParams<T>>,
  private val minPoolSize: Int,
  private val preferredPoolSize: Int,
  private val tokenValidityPredicate: TokenValidityPredicate<T>,
) : TokenPool<T> {
  private val lock = Mutex()
  private val poolsByParams = mutableMapOf<BsaTokenParams<T>, List<T>>()

  override suspend fun draw(
    params: BsaTokenParams<T>,
    count: Int,
    fallbackSource: TokenPoolSource<T>,
  ): List<T> =
    lock.withLock {
      val validPool = poolsByParams[params]?.filter(tokenValidityPredicate) ?: emptyList()
      val result = validPool.take(count).toMutableList()
      val newPool = validPool.drop(count).toMutableList()

      val remainingForResult = count - result.size
      val refillAmount =
        if (newPool.size < minPoolSize) {
          // Only try to refill if the new pool will have dropped below the minimum pool size.
          maxOf(preferredPoolSize - newPool.size, 0)
        } else {
          0
        }

      val toFetch = remainingForResult + refillAmount
      if (toFetch > 0) {
        val fetched = fallbackSource(params, toFetch, tokenValidityPredicate)
        result.addAll(fetched.take(remainingForResult))
        if (refillAmount > 0) {
          // Only add to the updated pool list if we explicitly needed to top it up, otherwise it's
          // possible we could have an ever-increasing pool if the upstream refill provider's batch
          // size is large.
          newPool.addAll(fetched.drop(remainingForResult))
        }
      }

      // Update the pool
      poolsByParams[params] = newPool
      return result.toList()
    }

  override suspend fun clear() = lock.withLock { poolsByParams.clear() }

  override suspend fun refresh(refillSource: TokenPoolSource<T>) =
    lock.withLock {
      poolsByParams.clear()

      // Consider parallelizing this.
      for (params in refreshParams) {
        poolsByParams[params] = refillSource(params, preferredPoolSize, tokenValidityPredicate)
      }
    }
}
