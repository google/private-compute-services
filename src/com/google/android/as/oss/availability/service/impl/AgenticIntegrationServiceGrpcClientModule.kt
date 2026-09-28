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

package com.google.android.`as`.oss.availability.service.impl

import android.content.Context
import android.os.Build
import com.google.android.`as`.oss.availability.api.agenticintegration.AgenticIntegrationServiceGrpcKt
import com.google.android.`as`.oss.availability.config.FeatureAvailabilityConfig
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import dagger.Module
import dagger.Provides
import dagger.Reusable
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.Channel
import io.grpc.CompressorRegistry
import io.grpc.DecompressorRegistry
import io.grpc.binder.AndroidComponentAddress
import io.grpc.binder.BindServiceFlags
import io.grpc.binder.BinderChannelBuilder
import io.grpc.binder.InboundParcelablePolicy
import java.util.concurrent.TimeUnit.MINUTES
import javax.inject.Qualifier
import javax.inject.Singleton

/** Module for providing gRPC client components for AgenticIntegrationService. */
@Module
@InstallIn(SingletonComponent::class)
internal object AgenticIntegrationServiceGrpcClientModule {

  @Qualifier annotation class AgenticIntegrationService

  @Provides
  @AgenticIntegrationService
  fun serverAddress(
    configReader: ConfigReader<FeatureAvailabilityConfig>
  ): AndroidComponentAddress =
    AndroidComponentAddress.forRemoteComponent(
      configReader.config.agenticIntegrationPackageName,
      configReader.config.agenticIntegrationGrpcServiceClass,
    )

  @Provides
  @Singleton
  @AgenticIntegrationService
  fun channel(
    @AgenticIntegrationService androidComponentAddress: AndroidComponentAddress,
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
  ): Channel {
    val builder = BinderChannelBuilder.forAddress(androidComponentAddress, context)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      builder.setBindServiceFlags(
        BindServiceFlags.DEFAULTS.toBuilder().setAllowActivityStarts(true).build()
      )
    }
    return builder
      .securityPolicy(
        SecurityPolicyUtils.makeSecurityPolicy(
          pccSecurityConfigReader.config.agsaPackageSecurityInfo(),
          context,
          /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
        )
      )
      .inboundParcelablePolicy(InboundParcelablePolicy.DEFAULT)
      // Disable compression by default, since there's little benefit when all communication is
      // on-device, and it means sending supported-encoding headers with every call.
      .decompressorRegistry(DecompressorRegistry.emptyInstance())
      .compressorRegistry(CompressorRegistry.newEmptyInstance())
      .idleTimeout(1, MINUTES)
      .build()
  }

  /** Coroutine-based stub for Kotlin clients. */
  @Provides
  @Reusable
  fun coroutineStub(
    @AgenticIntegrationService channel: Channel
  ): AgenticIntegrationServiceGrpcKt.AgenticIntegrationServiceCoroutineStub =
    AgenticIntegrationServiceGrpcKt.AgenticIntegrationServiceCoroutineStub(channel)
}
