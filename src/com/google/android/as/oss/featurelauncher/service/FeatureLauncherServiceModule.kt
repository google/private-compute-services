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

package com.google.android.`as`.oss.featurelauncher.service

import android.content.Context
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceSecurityPolicy
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.featurelauncher.api.proto.PcsFeatureLauncherServiceGrpcKt
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

/** Dagger module that provides dependencies for the Pcs Feature Launcher service. */
@Module
@InstallIn(SingletonComponent::class)
internal interface FeatureLauncherServiceModule {
  @Binds
  @IntoSet
  @GrpcService
  fun bindFeatureLauncherService(impl: FeatureLauncherServiceImpl): BindableService

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()

    @Provides
    @IntoSet
    @GrpcServiceName
    fun provideFeatureLauncherServiceName(): String = PcsFeatureLauncherServiceGrpcKt.SERVICE_NAME

    @Provides
    @IntoMap
    @GrpcServiceSecurityPolicy
    @StringKey("com.google.android.as.oss.featurelauncher.api.PcsFeatureLauncherService")
    fun provideAuthPolicy(
      @ApplicationContext context: Context,
      pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
    ): SecurityPolicy {
      val asiPolicy =
        SecurityPolicyUtils.makeSecurityPolicy(
          pccSecurityConfigReader.config.asiPackageSecurityInfo(),
          context,
          !SecurityPolicyUtils.isUserBuild(),
        )
      return asiPolicy ?: SecurityPolicies.permissionDenied("No valid ASI security policy")
    }
  }
}
