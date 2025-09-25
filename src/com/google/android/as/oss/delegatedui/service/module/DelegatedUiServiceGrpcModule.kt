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

package com.google.android.`as`.oss.delegatedui.service.module

import android.content.Context
import android.os.Build
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceSecurityPolicy
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceParcelableKeys
import com.google.android.`as`.oss.delegatedui.config.DelegatedUiConfig
import com.google.android.`as`.oss.delegatedui.service.impl.DelegatedUiServiceImpl
import com.google.android.`as`.oss.delegatedui.service.impl.UnsupportedDelegatedUiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import io.grpc.BindableService
import io.grpc.ServerInterceptors
import io.grpc.binder.SecurityPolicies
import io.grpc.binder.SecurityPolicy

/** Registers the Delegated UI GRPC service in PCS. */
@Module
@InstallIn(SingletonComponent::class)
internal object DelegatedUiServiceGrpcModule {
  @Provides
  @IntoSet
  @GrpcService
  fun provideBindableService(
    impl: DelegatedUiServiceImpl,
    unsupportedImpl: UnsupportedDelegatedUiServiceImpl,
    configReader: ConfigReader<DelegatedUiConfig>,
  ): BindableService {
    if (isSdkSupported() && configReader.config.isDelegatedUiEnabled) {
      return BindableService {
        ServerInterceptors.intercept(impl, DelegatedUiServiceParcelableKeys.SERVER_INTERCEPTOR)
      }
    }
    return unsupportedImpl
  }

  @Provides
  @IntoMap
  @GrpcServiceSecurityPolicy
  @StringKey(DelegatedUiServiceGrpcKt.SERVICE_NAME)
  fun provideSecurityPolicy(
    @ApplicationContext context: Context,
    configReader: ConfigReader<DelegatedUiConfig>,
  ): SecurityPolicy {
    val securityPolicies =
      configReader.config.clientAllowlist.packageSecurityInfosList
        .map {
          SecurityPolicyUtils.makeSecurityPolicy(
            it,
            context,
            /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
            /* usePermissionDeniedForInvalidPolicy= */ false,
          )
        }
        .filterNotNull()
        .toTypedArray()

    return if (securityPolicies.isEmpty()) {
      SecurityPolicies.permissionDenied("No valid security policies configured")
    } else {
      SecurityPolicies.anyOf(*securityPolicies)
    }
  }

  @Provides
  @IntoSet
  @GrpcServiceName
  fun provideServiceName(): String = DelegatedUiServiceGrpcKt.SERVICE_NAME

  private fun isSdkSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  }
}
