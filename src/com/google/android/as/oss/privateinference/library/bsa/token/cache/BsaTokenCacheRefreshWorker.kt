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

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.NetworkType
import androidx.work.Operation
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.config.asFlow
import com.google.android.`as`.oss.common.initializer.PcsInitializer
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import com.google.android.`as`.oss.privateinference.library.bsa.token.stableTokenClassName
import com.google.common.flogger.GoogleLogger
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** WorkManager worker which refreshes the token cache. */
class BsaTokenCacheRefreshWorker(
  appContext: Context,
  private val workerParams: WorkerParameters,
  private val cacheControl: BsaTokenCacheControlPlane,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    return try {
      cacheControl.invalidateAndRefill()
      logger.atInfo().log("Refreshed token cache")
      Result.success()
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to refresh token cache")
      Result.failure()
    }
  }

  /**
   * [WorkerFactory] implementation which can create a [BsaTokenCacheRefreshWorker] associated with
   * a particular [BsaToken] type that calls the provided [BsaTokenCacheControlPlane] when
   * triggered.
   */
  class Factory(val tokenClass: Class<out BsaToken>, val cacheControl: BsaTokenCacheControlPlane) :
    WorkerFactory() {
    override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters,
    ): ListenableWorker? {
      if (workerClassName != BsaTokenCacheRefreshWorker::class.java.name) return null
      val inputName = workerParameters.inputData.getString(INPUT_DATA_KEY_TOKEN_TYPE_NAME)
      if (inputName != tokenClass.stableTokenClassName) return null
      return BsaTokenCacheRefreshWorker(
        appContext = appContext,
        workerParams = workerParameters,
        cacheControl = cacheControl,
      )
    }
  }

  /**
   * Private Compute Services Initializer. Will be run at application launch time to register
   * [BsaTokenCacheRefreshWorker] jobs.
   */
  class Initializer(
    val appContext: Context,
    val configReader: ConfigReader<PrivateInferenceConfig>,
    val tokenClass: Class<out BsaToken>,
    val coroutineScope: CoroutineScope,
  ) : PcsInitializer {
    @VisibleForTesting val latestOperation = MutableStateFlow<Operation?>(null)
    @VisibleForTesting val latestRequest = MutableStateFlow<WorkRequest?>(null)

    override fun getPriority(): Int = PcsInitializer.PRIORITY_LOW

    override fun run() {
      coroutineScope.launch {
        configReader
          .asFlow()
          .map { config ->
            when (tokenClass) {
              ArateaTokenWithoutChallenge::class.java,
              ArateaToken::class.java -> config.arateaTokenCacheRefreshIntervalMinutes()
              ProxyToken::class.java -> config.proxyTokenCacheRefreshIntervalMinutes()
              else ->
                throw IllegalArgumentException(
                  "Bad tokenClass value for BsaTokenCacheRefreshWorker initializer"
                )
            }
          }
          .distinctUntilChanged()
          .collectLatest { intervalMinutes -> onConfig(intervalMinutes) }
      }
    }

    private fun onConfig(intervalMinutes: Int) {
      val workManager = WorkManager.getInstance(appContext)
      val uniqueWorkName = createWorkName(tokenClass)
      if (intervalMinutes == PrivateInferenceConfig.CACHE_REFRESH_INTERVAL_NEVER) {
        latestOperation.value = workManager.cancelUniqueWork(uniqueWorkName = uniqueWorkName)
        latestRequest.value = null
      } else {
        val request =
          PeriodicWorkRequestBuilder<BsaTokenCacheRefreshWorker>(
              repeatInterval = Duration.ofMinutes(intervalMinutes.toLong())
            )
            .setConstraints(
              Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            )
            .setInitialDelay(Duration.ZERO)
            .setInputData(
              Data.Builder()
                .putString(INPUT_DATA_KEY_TOKEN_TYPE_NAME, tokenClass.stableTokenClassName)
                .build()
            )
            .setTraceTag(uniqueWorkName)
            .build()
        latestOperation.value =
          workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = uniqueWorkName,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request,
          )
        latestRequest.value = request
      }
    }
  }

  companion object {
    @VisibleForTesting const val INPUT_DATA_KEY_TOKEN_TYPE_NAME = "token_type_name"

    private val logger = GoogleLogger.forEnclosingClass()

    @VisibleForTesting
    fun createWorkName(tokenClass: Class<out BsaToken>): String =
      "${tokenClass.stableTokenClassName}CacheRefreshWorker"
  }
}
