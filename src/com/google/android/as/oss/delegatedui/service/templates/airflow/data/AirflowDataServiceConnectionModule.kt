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

package com.google.android.`as`.oss.delegatedui.service.templates.airflow.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.delegatedui.service.templates.airflow.data.Annotations.AirflowDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.grpc.Channel
import io.grpc.binder.AndroidComponentAddress
import io.grpc.binder.BinderChannelBuilder
import io.grpc.binder.InboundParcelablePolicy
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object AirflowDataServiceConnectionModule {

  @Provides
  @AirflowDataService
  @Singleton
  fun airflowDataServiceChannel(
    @ApplicationContext context: Context,
    pccSecurityConfigReader: ConfigReader<PccSecurityConfig>,
  ): Channel {
    val serverIntent =
      Intent()
        .setAction("io.grpc.action.BIND")
        .setPackage("com.google.android.as")
        .setData(Uri.parse("grpc:///com.google.android.apps.miphone.aiai.IAirflowDataService"))
    val asiPackageSecurityInfo = pccSecurityConfigReader.config.asiPackageSecurityInfo()
    return BinderChannelBuilder.forAddress(
        AndroidComponentAddress.forBindIntent(serverIntent),
        context,
      )
      .securityPolicy(
        SecurityPolicyUtils.makeSecurityPolicy(
          asiPackageSecurityInfo,
          context,
          /* allowTestKeys= */ !SecurityPolicyUtils.isUserBuild(),
        )
      )
      .inboundParcelablePolicy(InboundParcelablePolicy.DEFAULT)
      .build()
  }

  @Provides
  @AirflowDataService
  fun airflowDataServiceStub(
    @AirflowDataService channel: Channel
  ): AirflowDataServiceGrpcKt.AirflowDataServiceCoroutineStub =
    AirflowDataServiceGrpcKt.AirflowDataServiceCoroutineStub(channel)
}
