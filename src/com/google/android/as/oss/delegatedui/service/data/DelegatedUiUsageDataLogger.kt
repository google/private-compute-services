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

package com.google.android.`as`.oss.delegatedui.service.data

import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiClientId
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiDataProviderInfo.DelegatedUiDataProvider
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.delegatedUiLogUsageDataRequest
import com.google.android.`as`.oss.delegatedui.service.data.serviceconnection.Annotations.DelegatedUiDataService
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import javax.inject.Inject

/** A logger for delegated UI usage data. */
interface DelegatedUiUsageDataLogger {

  /** Logs the usage data into the delegated UI data service. */
  suspend fun logUsageData(
    sessionUuid: String,
    clientId: DelegatedUiClientId,
    dataProvider: DelegatedUiDataProvider,
    usageData: DelegatedUiUsageData,
  )
}

class DelegatedUiUsageDataLoggerImpl
@Inject
internal constructor(
  @DelegatedUiDataService
  private val services:
    Map<
      DelegatedUiDataProvider,
      @JvmSuppressWildcards
      DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub,
    >
) : DelegatedUiUsageDataLogger {

  override suspend fun logUsageData(
    sessionUuid: String,
    clientId: DelegatedUiClientId,
    dataProvider: DelegatedUiDataProvider,
    usageData: DelegatedUiUsageData,
  ) {
    val service = services[dataProvider]
    if (service == null) {
      throw IllegalStateException("No service found for data provider: ${dataProvider}")
    }

    try {
      val request = delegatedUiLogUsageDataRequest {
        this.sessionUuid = sessionUuid
        this.clientId = clientId
        this.usageData = usageData
      }
      val unused = service.logDelegatedUiUsageData(request)
    } catch (e: Exception) {
      logger.atWarning().withCause(e).withStackTrace(StackSize.SMALL).log("Failed to logUsageData.")
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
