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

package com.google.android.`as`.oss.delegatedui.service.data.serviceconnection

import android.content.Context
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.api.PackageSecurityInfo
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiDataProviderInfo.DelegatedUiDataProvider
import com.google.android.`as`.oss.delegatedui.api.config.DelegatedUiDataServiceConfigList
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiDataServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.config.DelegatedUiConfig
import com.google.android.`as`.oss.delegatedui.service.data.serviceconnection.Annotations.DelegatedUiDataProviderKey
import com.google.android.`as`.oss.delegatedui.service.data.serviceconnection.Annotations.DelegatedUiDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import io.grpc.Channel
import io.grpc.CompressorRegistry
import io.grpc.DecompressorRegistry
import io.grpc.binder.AndroidComponentAddress
import io.grpc.binder.BinderChannelBuilder
import io.grpc.binder.InboundParcelablePolicy
import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

/** A module for providing the delegated UI data service connection. */
@Module
@InstallIn(SingletonComponent::class)
internal object ServiceConnectionModule {

  private val asiAddress =
    AndroidComponentAddress.forRemoteComponent(
      "com.google.android.as",
      "com.google.android.apps.miphone.aiai.delegatedui.service.DelegatedUiDataServiceEndpoint",
    )

  private val psiAddress =
    AndroidComponentAddress.forRemoteComponent(
      "com.google.android.apps.pixel.psi",
      "com.google.android.apps.pixel.psi.service.DelegatedUiDataService",
    )

  @Provides
  @DelegatedUiDataService
  @IntoMap
  @DelegatedUiDataProviderKey(DelegatedUiDataProvider.DATA_PROVIDER_ASI)
  @Singleton
  fun provideAsiDelegatedUiDataServiceStub(
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
    delegatedUiConfigReader: ConfigReader<DelegatedUiConfig>,
  ): DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub {
    val channel =
      buildChannel(
        context,
        asiAddress,
        pccSecurityConfigReader.config.asiPackageSecurityInfo(),
        DelegatedUiDataProvider.DATA_PROVIDER_ASI,
        delegatedUiConfigReader.config,
      )
    return DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub(channel)
  }

  @Provides
  @DelegatedUiDataService
  @IntoMap
  @DelegatedUiDataProviderKey(DelegatedUiDataProvider.DATA_PROVIDER_PSI)
  @Singleton
  fun providePsiDelegatedUiDataServiceStub(
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
    delegatedUiConfigReader: ConfigReader<DelegatedUiConfig>,
  ): DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub {
    val channel =
      buildChannel(
        context,
        psiAddress,
        pccSecurityConfigReader.config.psiPackageSecurityInfo(),
        DelegatedUiDataProvider.DATA_PROVIDER_PSI,
        delegatedUiConfigReader.config,
      )
    return DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub(channel)
  }

  @Provides
  @DelegatedUiDataService
  @IntoMap
  @DelegatedUiDataProviderKey(DelegatedUiDataProvider.DATA_PROVIDER_UNSPECIFIED)
  @Singleton
  fun provideUnspecifiedDelegatedUiDataServiceStub(
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
    delegatedUiConfigReader: ConfigReader<DelegatedUiConfig>,
  ): DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub {
    val channel =
      buildChannel(
        context,
        psiAddress,
        pccSecurityConfigReader.config.psiPackageSecurityInfo(),
        DelegatedUiDataProvider.DATA_PROVIDER_PSI,
        delegatedUiConfigReader.config,
      )
    return DelegatedUiDataServiceGrpcKt.DelegatedUiDataServiceCoroutineStub(channel)
  }

  private fun buildChannel(
    context: Context,
    address: AndroidComponentAddress,
    packageSecurityInfo: PackageSecurityInfo,
    dataProvider: DelegatedUiDataProvider,
    delegatedUiConfig: DelegatedUiConfig,
  ): Channel {
    return BinderChannelBuilder.forAddress(address, context)
      .securityPolicy(
        SecurityPolicyUtils.makeSecurityPolicy(
          packageSecurityInfo,
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
      .idleTimeout(
        getServiceConnectionIdleTimeoutSeconds(
          delegatedUiConfig.dataServiceConfigList,
          dataProvider,
        ),
        SECONDS,
      )
      .build()
  }

  private fun getServiceConnectionIdleTimeoutSeconds(
    dataServiceConfigList: DelegatedUiDataServiceConfigList,
    dataProvider: DelegatedUiDataProvider,
  ): Long {
    val dataServiceConfig =
      dataServiceConfigList.configsList.firstOrNull { it.providerInfo.dataProvider == dataProvider }
    val configuredTimeoutSeconds = dataServiceConfig?.connectionIdleTimeoutSeconds
    return if (configuredTimeoutSeconds == null || configuredTimeoutSeconds == 0L) {
      DEFAULT_IDLE_TIMEOUT_SECONDS
    } else {
      configuredTimeoutSeconds
    }
  }

  private val DEFAULT_IDLE_TIMEOUT_SECONDS = 10.minutes.inWholeSeconds
}
