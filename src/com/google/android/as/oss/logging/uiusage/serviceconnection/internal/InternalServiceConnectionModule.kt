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

package com.google.android.`as`.oss.logging.uiusage.serviceconnection.internal

import android.content.Context
import com.google.android.`as`.oss.logging.uiusage.api.UsageDataServiceGrpcKt
import com.google.android.`as`.oss.logging.uiusage.serviceconnection.Annotations.UiUsageDataService
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
import io.grpc.binder.UntrustedSecurityPolicies
import java.util.concurrent.TimeUnit.MINUTES
import javax.inject.Singleton

/**
 * An internal module for providing the UI usage data service connection.
 *
 * We use an internal module with some quick shortcuts like hardcoding the package name and service
 * name for now. The prod module will do this in a more robust way, as recommended in [redacted]
 */
@Module
@InstallIn(SingletonComponent::class)
internal object InternalServiceConnectionModule {

  @Provides
  @UiUsageDataService
  fun uiUsageDataServiceAddress(): AndroidComponentAddress =
    AndroidComponentAddress.forRemoteComponent(
      DATA_SERVICE_PROVIDER_PACKAGE_NAME,
      DATA_SERVICE_CLASS_NAME,
    )

  @Provides
  @UiUsageDataService
  @Singleton
  fun uiUsageDataServiceChannel(
    @ApplicationContext context: Context,
    @UiUsageDataService address: AndroidComponentAddress,
  ): Channel {
    return BinderChannelBuilder.forAddress(address, context)
      .securityPolicy(UntrustedSecurityPolicies.untrustedPublic())
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
  fun uiUsageDataServiceStub(
    @UiUsageDataService channel: Channel
  ): UsageDataServiceGrpcKt.UsageDataServiceCoroutineStub =
    UsageDataServiceGrpcKt.UsageDataServiceCoroutineStub(channel)

  const val DATA_SERVICE_PROVIDER_PACKAGE_NAME = "com.google.android.apps.pixel.psi"
  const val DATA_SERVICE_CLASS_NAME =
    "com.google.android.apps.pixel.psi.service.LogUsageDataService"
}
