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

import android.app.PendingIntent
import android.app.RemoteAction
import android.graphics.Bitmap
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiDataProviderInfo.DelegatedUiDataProvider
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceParcelableKeys
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiGetAdditionalDataResponse
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiGetTemplateDataResponse
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.delegatedUiGetAdditionalDataRequest
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.delegatedUiGetTemplateDataRequest
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateType
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiRenderSpec
import com.google.android.`as`.oss.delegatedui.service.data.serviceconnection.Annotations.DelegatedUiDataService
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils.delegateListOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils.delegateOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils.receiveParcelableFromResponse
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils.receiveParcelablesFromResponse
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils.sendParcelableInRequest
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.withParcelablesToReceive
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.android.AndroidLogTag
import javax.inject.Inject
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

/** A repository for fetching delegated UI data. */
interface DelegatedUiDataRepository {

  /**
   * Fetches the template data required to render the delegated UI, and a deferred additional data
   * required to handle clicks.
   */
  suspend fun getData(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
  ): DelegatedUiDataResponses
}

/** Data class holding the template data and the deferred additional data. */
data class DelegatedUiDataResponses(
  val templateData: ResponseWithParcelables<DelegatedUiGetTemplateDataResponse>,
  val additionalData: Deferred<ResponseWithParcelables<DelegatedUiGetAdditionalDataResponse>>?,
)

class DelegatedUiDataRepositoryImpl
@Inject
internal constructor(
  @DelegatedUiDataService
  private val services:
    Map<
      DelegatedUiDataProvider,
      @JvmSuppressWildcards
      DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub,
    >
) : DelegatedUiDataRepository {
  private val templateEntries =
    DelegatedUiTemplateType.entries.filterNot { it == DelegatedUiTemplateType.UNRECOGNIZED }

  override suspend fun getData(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
  ): DelegatedUiDataResponses {
    val dataProvider = spec.dataProviderInfo.dataProvider
    val service =
      services[dataProvider]
        ?: throw DelegatedUiExceptions.InvalidDataProviderServiceError(dataProvider)

    // First fetch the template data.
    val templateData = fetchTemplateData(service, spec)

    // Then fetch the additional data.
    val additionalData =
      if (templateData.value.hasAdditionalData) {
        lifecycle.streamScope?.async { fetchAdditionalData(service, spec) }
      } else {
        null
      }
    return DelegatedUiDataResponses(templateData, additionalData)
  }

  private suspend fun fetchTemplateData(
    service: DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub,
    spec: DelegatedUiRenderSpec,
  ): ResponseWithParcelables<DelegatedUiGetTemplateDataResponse> {
    val request = delegatedUiGetTemplateDataRequest {
      this.sessionUuid = spec.sessionUuid
      this.clientId = spec.clientId
      this.clientIngressData = spec.ingressData
      this.supportedTemplates += templateEntries
      this.measureSpecWidth = spec.measureSpecWidth
      this.measureSpecHeight = spec.measureSpecHeight
    }
    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service calling getDelegatedUiTemplateData() with request: %s",
        request.clientIngressData,
      )

    val image = delegateOf<Bitmap>()
    val pendingIntentList = delegateListOf<PendingIntent>()
    val remoteActionList = delegateListOf<RemoteAction>()
    val response =
      service
        .receiveParcelableFromResponse(DelegatedUiDataServiceParcelableKeys.IMAGE_KEY, image)
        .receiveParcelablesFromResponse(
          DelegatedUiDataServiceParcelableKeys.PENDING_INTENT_LIST_KEY,
          pendingIntentList,
        )
        .receiveParcelablesFromResponse(
          DelegatedUiDataServiceParcelableKeys.REMOTE_ACTION_LIST_KEY,
          remoteActionList,
        )
        .sendParcelableInRequest(
          DelegatedUiDataServiceParcelableKeys.CONFIGURATION_KEY,
          spec.configuration,
        )
        .getDelegatedUiTemplateData(request)
    val result =
      response.withParcelablesToReceive(
        image = image,
        pendingIntentList = pendingIntentList,
        remoteActionList = remoteActionList,
      )

    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service received getDelegatedUiTemplateData() result: %s",
        result,
      )
    return result
  }

  private suspend fun fetchAdditionalData(
    service: DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub,
    spec: DelegatedUiRenderSpec,
  ): ResponseWithParcelables<DelegatedUiGetAdditionalDataResponse> {
    val request = delegatedUiGetAdditionalDataRequest {
      this.sessionUuid = spec.sessionUuid
      this.clientId = spec.clientId
      this.clientIngressData = spec.ingressData
    }
    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service calling getDelegatedUiAdditionalData() with request: %s",
        request,
      )

    val pendingIntentList = delegateListOf<PendingIntent>()
    val remoteActionList = delegateListOf<RemoteAction>()
    val response =
      service
        .receiveParcelablesFromResponse(
          DelegatedUiDataServiceParcelableKeys.PENDING_INTENT_LIST_KEY,
          pendingIntentList,
        )
        .receiveParcelablesFromResponse(
          DelegatedUiDataServiceParcelableKeys.REMOTE_ACTION_LIST_KEY,
          remoteActionList,
        )
        .getDelegatedUiAdditionalData(request)
    val result =
      response.withParcelablesToReceive(
        pendingIntentList = pendingIntentList,
        remoteActionList = remoteActionList,
      )

    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service received getDelegatedUiAdditionalData() result: %s",
        result,
      )
    return result
  }

  companion object {
    @AndroidLogTag("DelegatedUiDataRepository")
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
