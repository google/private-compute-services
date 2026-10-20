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

package com.google.android.`as`.oss.availability.service.module

import android.content.Context
import android.os.Build
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceSecurityPolicy
import com.google.android.`as`.oss.availability.api.FeatureAvailabilityServiceGrpcKt
import com.google.android.`as`.oss.availability.config.FeatureAvailabilityConfig
import com.google.android.`as`.oss.availability.service.impl.FeatureAvailabilityServiceImpl
import com.google.android.`as`.oss.availability.service.impl.UnsupportedFeatureAvailabilityServiceImpl
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
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
import io.grpc.binder.PeerUids
import io.grpc.binder.SecurityPolicies
import io.grpc.binder.SecurityPolicy

/** Registers the Feature Availability GRPC service in PCS. */
@Module
@InstallIn(SingletonComponent::class)
internal object FeatureAvailabilityServiceGrpcModule {
  @Provides
  @IntoSet
  @GrpcService
  fun provideBindableService(
    impl: FeatureAvailabilityServiceImpl,
    unsupportedImpl: UnsupportedFeatureAvailabilityServiceImpl,
    configReader: ConfigReader<FeatureAvailabilityConfig>,
  ): BindableService {
    if (isSdkSupported() && configReader.config.isFeatureAvailabilityEnabled) {
      return BindableService {
        ServerInterceptors.intercept(impl, PeerUids.newPeerIdentifyingServerInterceptor())
      }
    }
    return unsupportedImpl
  }

  @Provides
  @IntoMap
  @GrpcServiceSecurityPolicy
  @StringKey(FeatureAvailabilityServiceGrpcKt.SERVICE_NAME)
  fun provideSecurityPolicy(
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
  ): SecurityPolicy {
    val psiPolicy =
      SecurityPolicyUtils.makeSecurityPolicy(
        pccSecurityConfigReader.config.psiPackageSecurityInfo(),
        context,
        /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
      )
    val asiPolicy =
      SecurityPolicyUtils.makeSecurityPolicy(
        pccSecurityConfigReader.config.asiPackageSecurityInfo(),
        context,
        /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
      )
    val policies = mutableListOf<SecurityPolicy>()
    psiPolicy?.let { policies.add(it) }
    asiPolicy?.let { policies.add(it) }
    if (policies.isEmpty()) {
      return SecurityPolicies.permissionDenied("No valid security policies configured")
    }
    if (policies.size == 1) {
      return policies[0]
    }
    return SecurityPolicies.anyOf(*policies.toTypedArray())
  }

  @Provides
  @IntoSet
  @GrpcServiceName
  fun provideServiceName(): String = FeatureAvailabilityServiceGrpcKt.SERVICE_NAME

  private fun isSdkSupported(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
  }
}
