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

package com.google.android.`as`.oss.feedback.serviceclient.serviceconnection

import android.content.Context
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.feedback.api.dataservice.FeedbackDataServiceGrpcKt
import com.google.android.`as`.oss.feedback.serviceclient.serviceconnection.Annotations.FeedbackDataService
import com.google.android.`as`.oss.feedback.serviceclient.serviceconnection.QuartzAnnotations.QuartzFeedbackDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.Channel
import io.grpc.CompressorRegistry
import io.grpc.DecompressorRegistry
import io.grpc.binder.AndroidComponentAddress
import io.grpc.binder.BinderChannelBuilder
import io.grpc.binder.InboundParcelablePolicy
import java.util.concurrent.TimeUnit.MINUTES
import javax.inject.Singleton

/** A module for providing the feedback data service connection. */
@Module
@InstallIn(SingletonComponent::class)
internal object ServiceConnectionModule {

  @Provides
  @FeedbackDataService
  fun feedbackDataServiceAddress(): AndroidComponentAddress =
    AndroidComponentAddress.forRemoteComponent(
      DATA_SERVICE_PROVIDER_PACKAGE_NAME,
      DATA_SERVICE_CLASS_NAME,
    )

  @Provides
  @FeedbackDataService
  @Singleton
  fun feedbackDataServiceChannel(
    @ApplicationContext context: Context,
    @FeedbackDataService address: AndroidComponentAddress,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
  ): Channel {
    return BinderChannelBuilder.forAddress(address, context)
      .securityPolicy(
        SecurityPolicyUtils.makeSecurityPolicy(
          pccSecurityConfigReader.config.psiPackageSecurityInfo(),
          context,
          /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
        )
      )
      .inboundParcelablePolicy(
        InboundParcelablePolicy.newBuilder().setAcceptParcelableMetadataValues(true).build()
      )
      // Disable compression by default, since there's little benefit when all communication is
      // on-device, and it means sending supported-encoding headers with every call.
      .decompressorRegistry(DecompressorRegistry.emptyInstance())
      .compressorRegistry(CompressorRegistry.newEmptyInstance())
      .idleTimeout(1, MINUTES)
      .build()
  }

  @Provides
  fun feedbackDataServiceStub(
    @FeedbackDataService channel: Channel
  ): FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub =
    FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub(channel)

  @Provides
  @QuartzFeedbackDataService
  fun quartzFeedbackDataServiceAddress(): AndroidComponentAddress =
    AndroidComponentAddress.forRemoteComponent(
      QUARTZ_DATA_SERVICE_PROVIDER_PACKAGE_NAME,
      QUARTZ_DATA_SERVICE_CLASS_NAME,
    )

  @Provides
  @QuartzFeedbackDataService
  @Singleton
  fun quartzFeedbackDataServiceChannel(
    @ApplicationContext context: Context,
    @QuartzFeedbackDataService address: AndroidComponentAddress,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
  ): Channel {
    return BinderChannelBuilder.forAddress(address, context)
      .securityPolicy(
        SecurityPolicyUtils.makeSecurityPolicy(
          pccSecurityConfigReader.config.asiPackageSecurityInfo(),
          context,
          /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
        )
      )
      .inboundParcelablePolicy(
        InboundParcelablePolicy.newBuilder().setAcceptParcelableMetadataValues(true).build()
      )
      .decompressorRegistry(DecompressorRegistry.emptyInstance())
      .compressorRegistry(CompressorRegistry.newEmptyInstance())
      .idleTimeout(1, MINUTES)
      .build()
  }

  @Provides
  @QuartzFeedbackDataService
  fun quartzFeedbackDataServiceStub(
    @QuartzFeedbackDataService channel: Channel
  ): FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub =
    FeedbackDataServiceGrpcKt.FeedbackDataServiceCoroutineStub(channel)

  const val DATA_SERVICE_PROVIDER_PACKAGE_NAME = "com.google.android.apps.pixel.psi"
  const val DATA_SERVICE_CLASS_NAME =
    "com.google.android.apps.pixel.psi.service.FeedbackDataService"

  const val QUARTZ_DATA_SERVICE_PROVIDER_PACKAGE_NAME = "com.google.android.as"
  const val QUARTZ_DATA_SERVICE_CLASS_NAME =
    "com.google.android.apps.miphone.aiai.echo.notificationintelligence.smartnotification.feedback.service.FeedbackDataServiceEndpoint"
}
