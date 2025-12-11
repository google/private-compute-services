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
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceParcelableKeys.CONFIGURATION_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceParcelableKeys.IMAGE_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceParcelableKeys.PENDING_INTENT_LIST_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceParcelableKeys.REMOTE_ACTION_LIST_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiGetTemplateDataResponse
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.delegatedUiGetTemplateDataRequest
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateType
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiRenderSpec
import com.google.android.`as`.oss.delegatedui.service.data.serviceconnection.Annotations.DelegatedUiDataService
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateListOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcDelegate.Companion.delegateOf
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.withParcelablesToReceive
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.android.AndroidLogTag
import javax.inject.Inject

/** A repository for fetching delegated UI data. */
interface DelegatedUiDataRepository {

  /**
   * Fetches the template data required to render the delegated UI, and a deferred additional data
   * required to handle clicks.
   */
  suspend fun getTemplateData(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
  ): DelegatedUiDataResponses
}

/** Data class holding the template data. */
data class DelegatedUiDataResponses(
  val templateData: ResponseWithParcelables<DelegatedUiGetTemplateDataResponse>
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
    >,
  private val parcelableOverRpcUtils: ParcelableOverRpcUtils,
) : DelegatedUiDataRepository {
  private val templateEntries =
    DelegatedUiTemplateType.entries.filterNot { it == DelegatedUiTemplateType.UNRECOGNIZED }

  override suspend fun getTemplateData(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
  ): DelegatedUiDataResponses {
    val dataProvider = spec.dataProviderInfo.dataProvider
    val service =
      services[dataProvider]
        ?: throw DelegatedUiExceptions.InvalidDataProviderServiceError(dataProvider)

    val templateData = fetchTemplateData(service, spec)
    return DelegatedUiDataResponses(templateData)
  }

  private suspend fun fetchTemplateData(
    service: DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub,
    spec: DelegatedUiRenderSpec,
  ): ResponseWithParcelables<DelegatedUiGetTemplateDataResponse> {
    val request = delegatedUiGetTemplateDataRequest {
      this.sessionUuid = spec.sessionUuid
      this.clientId = spec.clientId
      this.clientIngressData = spec.dataSpec.ingressData
      this.supportedTemplates += templateEntries
    }
    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service calling getDelegatedUiTemplateData() for session: %s",
        spec.sessionUuid,
      )

    val image = delegateOf<Bitmap>()
    val pendingIntentList = delegateListOf<PendingIntent>()
    val remoteActionList = delegateListOf<RemoteAction>()
    val response =
      with(parcelableOverRpcUtils) {
        service
          .receiveParcelableFromResponse(IMAGE_KEY, image)
          .receiveParcelablesFromResponse(PENDING_INTENT_LIST_KEY, pendingIntentList)
          .receiveParcelablesFromResponse(REMOTE_ACTION_LIST_KEY, remoteActionList)
          .sendParcelableInRequest(CONFIGURATION_KEY, spec.configuration)
          .getDelegatedUiTemplateData(request)
      }
    val result =
      response.withParcelablesToReceive(
        image = image,
        pendingIntentList = pendingIntentList,
        remoteActionList = remoteActionList,
      )

    logger
      .atInfo()
      .log(
        "[DelegatedUILifecycle] DUI-Service received getDelegatedUiTemplateData() for session: %s",
        spec.sessionUuid,
      )
    return result
  }

  companion object {
    @AndroidLogTag("DelegatedUiDataRepository")
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
