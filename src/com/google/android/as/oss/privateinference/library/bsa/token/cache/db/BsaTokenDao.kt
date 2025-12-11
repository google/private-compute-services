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

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenParams
import com.google.android.`as`.oss.privateinference.library.bsa.token.cache.TokenValidityPredicate
import java.time.Instant

@Dao
interface BsaTokenDao {
  /**
   * Draws a batch tokens (of size [batchSize]) matching the provided [tokenParams], deletes the
   * tokens which were drawn, and cleans up any expired tokens in the database.
   *
   * @return the fetched tokens and the amount of tokens in the pool post-cleanup/deletion.
   */
  @Transaction
  suspend fun drawTokens(
    tokenParams: BsaTokenParams<*>,
    batchSize: Int,
    now: Instant,
  ): DrawTokensResponse {
    val tokens = getTokens(tokenParams, batchSize, now)
    delete(tokens)
    val itemsCleanedUp = cleanUp(now)
    return DrawTokensResponse(tokens, getPoolSize(tokenParams), itemsCleanedUp)
  }

  /** Inserts all the provided [entities] into the database. */
  @Insert(entity = BsaTokenEntity::class, onConflict = IGNORE)
  suspend fun insertAll(entities: List<BsaTokenEntity>)

  /** Deletes all of the specified [entities] from the database. */
  @Delete(entity = BsaTokenEntity::class) suspend fun delete(entities: List<BsaTokenEntity>)

  /** Deletes all entities from the database where the expiration is before [now]. */
  @Query("""DELETE FROM BsaTokenEntity WHERE expiration <= :now""")
  suspend fun cleanUp(now: Instant): Int

  /**
   * Deletes all entities from the database where their token params are one of the specified list.
   */
  @Query("""DELETE FROM BsaTokenEntity WHERE tokenParams IN (:tokenParams)""")
  suspend fun deleteAll(tokenParams: List<BsaTokenParams<*>>)

  @Query(
    """
      SELECT * FROM BsaTokenEntity 
      WHERE tokenParams = :tokenParams AND expiration > :now
      ORDER BY rowId ASC
      LIMIT :batchSize
      """
  )
  suspend fun getTokens(
    tokenParams: BsaTokenParams<*>,
    batchSize: Int,
    now: Instant,
  ): List<BsaTokenEntity>

  @Query(
    """
      SELECT COUNT(*) FROM BsaTokenEntity
      WHERE tokenParams = :tokenParams
    """
  )
  suspend fun getPoolSize(tokenParams: BsaTokenParams<*>): Int

  data class DrawTokensResponse(
    val tokens: List<BsaTokenEntity>,
    val postCleanupPoolSize: Int,
    val itemsCleanedUp: Int,
  )

  companion object {
    /**
     * Returns a [TokenValidityPredicate] which checks the expirationTime of tokens passed to it
     * against the clock's current time.
     */
    fun <T : BsaToken> tokenValidator(timeSource: TimeSource): TokenValidityPredicate<T> =
      { token ->
        token.expirationTime?.isAfter(timeSource.now()) == true
      }
  }
}
