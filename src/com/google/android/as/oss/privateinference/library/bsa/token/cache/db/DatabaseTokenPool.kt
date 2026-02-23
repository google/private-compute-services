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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache.db

import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.CacheableArateaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.TokenPool
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.TokenPoolSource
import com.google.android.`as`.oss.privateinference.library.bsa.token.crypto.BsaTokenCipher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Implementation of [TokenPool] which manages its pools of tokens using a [BsaTokenDatabase].
 *
 * A single pool is maintained for each unique instance of [BsaTokenParams] either passed as
 * [refreshParams] or to the [draw] method.
 *
 * @param refreshParams List of [BsaTokenParams] to use when making requests to a [TokenPoolSource]
 *   when pre-filling the pool to the [preferredPoolSize].
 * @param minPoolSize The minimum size a pool is allowed to fall-to before the fallback
 *   [TokenPoolSource] is used to re-populate a pool during a [draw] call.
 * @param preferredPoolSize The size a pool is filled-to when a [TokenPoolSource] is used to re-fill
 *   it.
 */
class DatabaseTokenPool<T : BsaToken>(
  private val refreshParams: List<BsaTokenParams<T>>,
  private val minPoolSize: Int,
  private val preferredPoolSize: Int,
  private val cipher: BsaTokenCipher,
  private val daoProvider: () -> BsaTokenDao,
  private val timeSource: TimeSource,
) : TokenPool<T> {
  private val dbLock = Mutex()

  override suspend fun draw(
    params: BsaTokenParams<T>,
    count: Int,
    fallbackSource: TokenPoolSource<T>,
  ): List<T> =
    dbLock.withLock {
      val (dbTokens, poolSizePostFetch, _) =
        daoProvider().drawTokens(params, count, timeSource.now())
      val resultTokens = dbTokens.mapNotNull { decrypt(it) }.toMutableList()

      val resultTokensNeeded = count - resultTokens.size
      val dbTokensNeeded =
        if (poolSizePostFetch < minPoolSize) {
          preferredPoolSize - poolSizePostFetch
        } else {
          0
        }

      val totalTokensNeeded = resultTokensNeeded + dbTokensNeeded
      if (totalTokensNeeded > 0) {
        val newTokens =
          fallbackSource(params, totalTokensNeeded, BsaTokenDao.tokenValidator(timeSource))
        resultTokens += newTokens.take(resultTokensNeeded)
        if (dbTokensNeeded > 0) {
          daoProvider()
            .insertAll(newTokens.drop(resultTokensNeeded).mapNotNull { encrypt(params, it) })
        }
      }
      return@withLock resultTokens
    }

  override suspend fun clear() = dbLock.withLock { daoProvider().deleteAll(refreshParams) }

  override suspend fun refresh(refillSource: TokenPoolSource<T>) =
    dbLock.withLock {
      daoProvider().deleteAll(refreshParams)
      val toInsert =
        refreshParams.flatMap { params ->
          refillSource(params, preferredPoolSize, BsaTokenDao.tokenValidator(timeSource))
            .mapNotNull { encrypt(params, it) }
        }
      daoProvider().insertAll(toInsert)
    }

  private suspend fun encrypt(params: BsaTokenParams<T>, token: T): BsaTokenEntity? {
    val expiration =
      requireNotNull(token.expirationTime) {
        "BsaTokens must have expiration values to be cached in the database."
      }
    return BsaTokenEntity(
      tokenParams = params,
      encryptedTokenData = cipher.encrypt(token.bytes) ?: return null,
      expiration = expiration,
    )
  }

  @Suppress("UNCHECKED_CAST")
  private suspend fun decrypt(entity: BsaTokenEntity): T? {
    val params = entity.tokenParams
    val data = cipher.decrypt(entity.encryptedTokenData) ?: return null
    return when (params) {
      is ArateaTokenParams -> ArateaToken(data)
      is ProxyTokenParams -> ProxyToken(data, entity.expiration)
      is CacheableArateaTokenParams -> ArateaTokenWithoutChallenge(data, entity.expiration)
    }
      as T
  }
}
