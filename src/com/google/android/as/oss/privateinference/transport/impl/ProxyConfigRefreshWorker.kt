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

package com.google.android.`as`.oss.privateinference.transport.impl

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.work.Constraints
import androidx.work.CoroutineWorker
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
import com.google.common.flogger.GoogleLogger
import java.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** WorkManager worker which refreshes the proxy config. */
class ProxyConfigRefreshWorker(
  appContext: Context,
  private val workerParams: WorkerParameters,
  private val proxyConfigControl: ProxyConfigControlPlane,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    return try {
      proxyConfigControl.refresh()
      logger.atInfo().log("Worker refreshed proxy config successfully")
      Result.success()
    } catch (e: Exception) {
      logger.atSevere().withCause(e).log("Failed to refresh proxy config")
      Result.failure()
    }
  }

  class Factory(private val proxyConfigControl: ProxyConfigControlPlane) : WorkerFactory() {
    override fun createWorker(
      appContext: Context,
      workerClassName: String,
      workerParameters: WorkerParameters,
    ): ListenableWorker? {
      if (workerClassName != ProxyConfigRefreshWorker::class.java.name) {
        return null
      }
      return ProxyConfigRefreshWorker(appContext, workerParameters, proxyConfigControl)
    }
  }

  class Initializer(
    val appContext: Context,
    val configReader: ConfigReader<PrivateInferenceConfig>,
    val coroutineScope: CoroutineScope,
  ) : PcsInitializer {

    @VisibleForTesting val latestOperation = MutableStateFlow<Operation?>(null)
    @VisibleForTesting val latestRequest = MutableStateFlow<WorkRequest?>(null)

    override fun getPriority(): Int = PcsInitializer.PRIORITY_LOW

    override fun run() {
      coroutineScope.launch {
        configReader
          .asFlow()
          .map { it.proxyConfigRefreshIntervalMinutes() }
          .distinctUntilChanged()
          .collectLatest { intervalMinutes -> onConfig(intervalMinutes) }
      }
    }

    private fun onConfig(intervalMinutes: Int) {
      val workManager = WorkManager.getInstance(appContext)

      if (intervalMinutes == PrivateInferenceConfig.CACHE_REFRESH_INTERVAL_NEVER) {
        latestOperation.value = workManager.cancelUniqueWork(WORKER_NAME)
        latestRequest.value = null
      } else {
        val request =
          PeriodicWorkRequestBuilder<ProxyConfigRefreshWorker>(
              repeatInterval = Duration.ofMinutes(intervalMinutes.toLong())
            )
            .setConstraints(
              Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .build()
            )
            .setInitialDelay(Duration.ZERO)
            .setTraceTag(WORKER_NAME)
            .build()
        latestOperation.value =
          workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = WORKER_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request,
          )
        latestRequest.value = request
      }
    }
  }

  companion object {
    @VisibleForTesting const val WORKER_NAME = "ProxyConfigRefreshWorker"

    private val logger = GoogleLogger.forEnclosingClass()
  }
}
