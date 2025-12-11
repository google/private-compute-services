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

package com.google.android.`as`.oss.privateinference.config.impl

import com.google.common.flogger.GoogleLogger
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor

/** Device information for Private Inference. */
data class DeviceInfo(
  val appVersionName: String? = null,
  val appVersionCode: String? = null,
  val hardwareRevision: String? = null,
) {

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    fun PcsVersionInterceptor(deviceInfo: DeviceInfo?) =
      deviceInfo?.let {
        newAttachHeadersInterceptor(
          /*extraHeaders=*/ Metadata().apply {
            deviceInfo.appVersionName?.let { appVersionName ->
              logger.atFine().log("Adding app version name: %s", appVersionName)
              put(APP_VERSION_NAME_HEADER, appVersionName)
            }
            deviceInfo.appVersionCode?.let { appVersionCode ->
              logger.atFine().log("Adding app version code: %s", appVersionCode)
              put(APP_VERSION_CODE_HEADER, appVersionCode)
            }
          }
        )
      }

    fun HardwareRevisionInterceptor(deviceInfo: DeviceInfo?) =
      deviceInfo?.let {
        newAttachHeadersInterceptor(
          /*extraHeaders=*/ Metadata().apply {
            deviceInfo.hardwareRevision?.let { hardwareRevision ->
              logger.atFine().log("Adding hardware revision: %s", hardwareRevision)
              put(HARDWARE_REVISION_HEADER, hardwareRevision)
            }
          }
        )
      }

    private val APP_VERSION_NAME_HEADER: Metadata.Key<String> =
      Metadata.Key.of("X-App-Version-Name", Metadata.ASCII_STRING_MARSHALLER)

    private val APP_VERSION_CODE_HEADER: Metadata.Key<String> =
      Metadata.Key.of("X-App-Version-Code", Metadata.ASCII_STRING_MARSHALLER)

    private val HARDWARE_REVISION_HEADER: Metadata.Key<String> =
      Metadata.Key.of("X-Hardware-Revision", Metadata.ASCII_STRING_MARSHALLER)
  }
}
