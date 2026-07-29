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

package com.google.android.`as`.oss.features.optin.service

import android.content.Context
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceSecurityPolicy
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.features.optin.api.proto.FeatureOptInServiceGrpcKt
import com.google.common.flogger.GoogleLogger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.IntoSet
import dagger.multibindings.StringKey
import io.grpc.BindableService
import io.grpc.binder.SecurityPolicies
import io.grpc.binder.SecurityPolicy

/**
 * Hilt module for providing [FeatureOptInServiceImpl] and its security policy to Pcs's gRPC server.
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface FeatureOptInServiceModule {
  /** Binds [FeatureOptInServiceImpl] into the Pcs gRPC server's set of services. */
  @Binds
  @IntoSet
  @GrpcService
  fun bindFeatureOptInService(impl: FeatureOptInServiceImpl): BindableService

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    /** Provides the service name for [FeatureOptInService]. */
    @Provides
    @IntoSet
    @GrpcServiceName
    fun provideFeatureOptInServiceName(): String = FeatureOptInServiceGrpcKt.SERVICE_NAME

    /**
     * Provides the client-side [SecurityPolicy] for [FeatureOptInService]. This policy ensures that
     * only authorized clients (in this case, PSI and ASI) are allowed to call the service. Pcs
     * requires all services to define such a policy.
     */
    @Provides
    @IntoMap
    @GrpcServiceSecurityPolicy
    @StringKey(FeatureOptInServiceGrpcKt.SERVICE_NAME)
    fun provideAuthPolicy(
      @ApplicationContext context: Context,
      pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
    ): SecurityPolicy {
      val policies =
        listOf(
            pccSecurityConfigReader.config.psiPackageSecurityInfo(),
            pccSecurityConfigReader.config.asiPackageSecurityInfo(),
            pccSecurityConfigReader.config.blueflaxPackageSecurityInfo(),
            pccSecurityConfigReader.config.agsaPackageSecurityInfo(),
          )
          .map {
            checkNotNull(
              SecurityPolicyUtils.makeSecurityPolicy(
                it,
                context,
                /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
              )
            ) {
              "Failed to create security policy for ${FeatureOptInServiceGrpcKt.SERVICE_NAME}"
            }
          }
      return SecurityPolicies.anyOf(*policies.toTypedArray())
    }
  }
}
