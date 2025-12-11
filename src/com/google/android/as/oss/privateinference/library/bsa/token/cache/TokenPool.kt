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

/** Predicate used to determine if a token is still valid. */
typealias TokenValidityPredicate<T> = (T) -> Boolean

/**
 * Function used to fetch a minimum number of tokens (`atLeast`) according to provided parameters
 * (`params`), where the result list has at least that amount of tokens and all tokens satisfy the
 * predicate test.
 */
typealias TokenPoolSource<T> =
  suspend (params: BsaTokenParams<T>, atLeast: Int, predicate: TokenValidityPredicate<T>) -> List<T>

interface TokenPool<T : BsaToken> {
  /**
   * Draws [count] items from the pool.
   *
   * If there aren't enough items in the pool, or the pool would be below its low water mark after
   * the draw, the [fallbackSource] will be used to make up the difference. After this call, it will
   * be guaranteed that the pool will have at least its low-water mark of items again.
   *
   * @param fallbackSource Callback used to fetch items when topping-up the pool after a draw drains
   *   it below the low-water mark. This callback should NOT call any other methods on the pool.
   */
  suspend fun draw(
    params: BsaTokenParams<T>,
    count: Int,
    fallbackSource: TokenPoolSource<T>,
  ): List<T>

  /** Clears all items from the pool. */
  suspend fun clear()

  /**
   * Clears all items and refills the pool with its preferred amount of items using the provided
   * [refillSource].
   *
   * @param refillSource Callback used to fetch items when refilling. This callback should NOT call
   *   any other methods on the pool.
   */
  suspend fun refresh(refillSource: TokenPoolSource<T>)
}
