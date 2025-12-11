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

package com.google.android.`as`.oss.privateinference.networkusage

import com.google.android.`as`.oss.networkusage.api.proto.connectionKey
import com.google.android.`as`.oss.networkusage.api.proto.privateInferenceConnectionKey
import com.google.android.`as`.oss.networkusage.db.ConnectionDetails
import com.google.android.`as`.oss.networkusage.db.ConnectionDetails.ConnectionType
import com.google.android.`as`.oss.networkusage.db.NetworkUsageLogRepository
import com.google.android.`as`.oss.networkusage.db.NetworkUsageLogUtils
import com.google.android.`as`.oss.networkusage.db.Status
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEndpointUrl
import com.google.android.`as`.oss.privateinference.Annotations.TokenIssuanceEndpointUrl
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper.IPProtectionRequestType
import com.google.errorprone.annotations.ThreadSafe
import javax.inject.Inject

@ThreadSafe
class PrivateInferenceNetworkUsageLogHelperImpl
@Inject
internal constructor(
  @field:ThreadSafe.Suppress(reason = "NetworkUsageLogRepository is thread-safe after creation")
  private val networkUsageLogRepository: NetworkUsageLogRepository,
  @PrivateInferenceEndpointUrl private val privateInferenceEndpointUrl: String,
  @TokenIssuanceEndpointUrl private val tokenIssuanceEndpointUrl: String,
) : PrivateInferenceNetworkUsageLogHelper {
  override fun isKnownFeature(featureName: String): Boolean =
    networkUsageLogRepository.isKnownConnection(
      ConnectionType.PRIVATE_INFERENCE_REQUEST,
      featureName,
    )

  override fun logPrivateInferenceRequest(
    featureName: String,
    callingPackageName: String,
    isSuccess: Boolean,
    requestSizeLong: Long,
    responseSizeLong: Long,
  ) {
    if (!shouldLogNetworkUsage(networkUsageLogRepository, connectionKey = featureName)) {
      return
    }

    val entity =
      NetworkUsageLogUtils.createPrivateInferenceNetworkUsageEntity(
        createPrivateInferenceConnectionDetails(featureName, callingPackageName),
        if (isSuccess) Status.SUCCEEDED else Status.FAILED,
        featureName,
        /* downloadSize= */ responseSizeLong,
        /* uploadSize= */ requestSizeLong,
        privateInferenceEndpointUrl,
      )
    networkUsageLogRepository.insertNetworkUsageEntity(entity)
  }

  override fun logIPProtectionRequest(
    requestType: IPProtectionRequestType,
    isSuccess: Boolean,
    requestSizeLong: Long,
    responseSizeLong: Long,
  ) {
    if (!shouldLogNetworkUsage(networkUsageLogRepository, connectionKey = requestType.name)) {
      return
    }

    val entity =
      NetworkUsageLogUtils.createPrivateInferenceNetworkUsageEntity(
        createPrivateInferenceConnectionDetails(requestType.name, PCS_PACKAGE_NAME),
        if (isSuccess) Status.SUCCEEDED else Status.FAILED,
        /* featureName= */ requestType.name,
        /* downloadSize= */ responseSizeLong,
        /* uploadSize= */ requestSizeLong,
        tokenIssuanceEndpointUrl,
      )
    networkUsageLogRepository.insertNetworkUsageEntity(entity)
  }

  companion object {
    private const val PCS_PACKAGE_NAME = "com.google.android.as.oss"

    private fun shouldLogNetworkUsage(
      networkUsageLogRepository: NetworkUsageLogRepository,
      connectionKey: String,
    ): Boolean =
      networkUsageLogRepository.shouldLogNetworkUsage(
        ConnectionType.PRIVATE_INFERENCE_REQUEST,
        connectionKey,
      ) && !networkUsageLogRepository.contentMap.isEmpty()

    private fun createPrivateInferenceConnectionDetails(featureName: String, packageName: String) =
      ConnectionDetails.builder()
        .setConnectionKey(
          connectionKey {
            privateInferenceConnectionKey = privateInferenceConnectionKey {
              this.featureName = featureName
            }
          }
        )
        .setPackageName(packageName)
        .setType(ConnectionType.PRIVATE_INFERENCE_REQUEST)
        .build()
  }
}
