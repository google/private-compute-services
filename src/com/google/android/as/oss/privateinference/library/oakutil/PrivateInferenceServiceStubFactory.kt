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

package com.google.android.`as`.oss.privateinference.library.oakutil

import android.content.Context
import com.google.android.`as`.oss.feedback.gateway.getCertFingerprint
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceAttachCertificateHeader
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceServerGrpcChannel
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceWaitForGrpcChannelReady
import com.google.android.`as`.oss.privateinference.config.impl.DeviceInfo
import com.google.android.`as`.oss.privateinference.library.PrivateInferenceRequestMetadata
import com.google.android.`as`.oss.privateinference.transport.ManagedChannelFactory
import com.google.common.flogger.GoogleLogger
import com.google.search.mdi.privatearatea.proto.PrivateArateaServiceGrpc
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.Metadata
import io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor
import java.util.Optional
import javax.inject.Inject
import kotlin.jvm.optionals.getOrNull

/** Factory for gRPC stub of PrivateArateaServiceGrpc.PrivateArateaServiceStub. */
open class PrivateInferenceServiceStubFactory
@Inject
internal constructor(
  @ApplicationContext private val context: Context,
  @PrivateInferenceServerGrpcChannel val managedChannelFactory: Lazy<ManagedChannelFactory>,
  @PrivateInferenceWaitForGrpcChannelReady val waitForGrpcChannelToBeReady: Boolean,
  @PrivateInferenceAttachCertificateHeader val attachCertificateHeader: Boolean,
  val deviceInfo: Optional<DeviceInfo>,
) {

  /**
   * Creates a stub for PrivateArateaServiceGrpc.PrivateArateaServiceStub
   *
   * The given API key or Spatula header in the [authInfo] is used to add the appropriate
   * interceptor to the stub if required.
   */
  open suspend fun createStub(
    authInfo: PrivateInferenceRequestMetadata.AuthInfo
  ): PrivateArateaServiceGrpc.PrivateArateaServiceStub {
    var stub =
      if (waitForGrpcChannelToBeReady) {
        logger.atInfo().log("Waiting for gRPC channel to be ready.")
        PrivateArateaServiceGrpc.newStub(managedChannelFactory.get().getInstance())
          .withWaitForReady()
      } else {
        PrivateArateaServiceGrpc.newStub(managedChannelFactory.get().getInstance())
      }
    if (attachCertificateHeader) {
      logger.atInfo().log("Attaching certificate header to the request.")
      stub =
        stub.withInterceptors(
          AndroidPackageAndCertificateInterceptor(
            context.packageName,
            getCertFingerprint(context) ?: "",
          )
        )
    }

    deviceInfo.getOrNull()?.let {
      logger.atFine().log("Attaching device info to the request.")
      stub =
        stub.withInterceptors(
          DeviceInfo.PcsVersionInterceptor(deviceInfo.getOrNull()),
          DeviceInfo.HardwareRevisionInterceptor(deviceInfo.getOrNull()),
        )
    }

    return when {
      authInfo.spatulaHeader.isPresent ->
        stub.withInterceptors(SpatulaInterceptor(authInfo.spatulaHeader.get()))
      authInfo.apiKey.isPresent -> stub.withInterceptors(ApiKeyInterceptor(authInfo.apiKey.get()))
      else -> stub
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    /** gRPC client interceptor that adds an Android package name to each outgoing request. */
    private fun AndroidPackageAndCertificateInterceptor(packageName: String, certificate: String) =
      newAttachHeadersInterceptor(
        /*extraHeaders=*/ Metadata().apply {
          put(ANDROID_PACKAGE_HEADER, packageName)
          put(ANDROID_CERT_HEADER, certificate)
        }
      )

    /** gRPC client interceptor that adds an API key to each outgoing request. */
    private fun ApiKeyInterceptor(apiKey: String) =
      newAttachHeadersInterceptor(
        /*extraHeaders=*/ Metadata().apply { put(API_KEY_METADATA_HEADER, apiKey) }
      )

    /** gRPC client interceptor that adds a Spatula header to each outgoing request. */
    private fun SpatulaInterceptor(spatula: String) =
      newAttachHeadersInterceptor(
        /*extraHeaders=*/ Metadata().apply { put(SPATULA_KEY, spatula) }
      )

    private val ANDROID_PACKAGE_HEADER: Metadata.Key<String> =
      Metadata.Key.of("X-Android-Package", Metadata.ASCII_STRING_MARSHALLER)
    private val ANDROID_CERT_HEADER: Metadata.Key<String> =
      Metadata.Key.of("X-Android-Cert", Metadata.ASCII_STRING_MARSHALLER)

    // HTTP/gRPC header for Google API keys.
    // https://cloud.google.com/apis/docs/system-parameters
    // https://cloud.google.com/docs/authentication/api-keys
    private val API_KEY_METADATA_HEADER: Metadata.Key<String> =
      Metadata.Key.of("x-goog-api-key", Metadata.ASCII_STRING_MARSHALLER)

    private val SPATULA_KEY: Metadata.Key<String> =
      Metadata.Key.of("x-goog-spatula", Metadata.ASCII_STRING_MARSHALLER)
  }
}
