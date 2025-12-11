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

package com.google.android.`as`.oss.privateinference.transport.impl

import android.os.Build
import androidx.annotation.RequiresExtension
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceProxyConfiguration
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceServerGrpcChannel
import com.google.android.`as`.oss.privateinference.Annotations.TokenIssuanceServerGrpcChannel
import com.google.android.`as`.oss.privateinference.transport.ManagedChannelFactory
import com.google.android.`as`.oss.privateinference.transport.PhosphorManagedChannelFactory
import com.google.android.`as`.oss.privateinference.transport.ProxyConfigManager
import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.grpc.ManagedChannel
import javax.inject.Singleton

/** Module that provides gRPC [io.grpc.ManagedChannel] for private inference service. */
@Module
@InstallIn(SingletonComponent::class)
internal interface TransportModule {
  @PrivateInferenceProxyConfiguration
  @BindsOptionalOf
  fun bindProxyConfigManager(): ProxyConfigManager

  @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
  @Binds
  @Singleton
  @PrivateInferenceServerGrpcChannel
  fun providesPrivateInferenceManagedChannel(
    factoryImpl: PrivateInferenceManagedChannelFactory
  ): ManagedChannelFactory

  companion object {
    /**
     * Provides a gRPC channel to Phosphor.
     *
     * This is used for fetching proxy configurations and tokens.
     */
    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
    @Provides
    @Singleton
    @TokenIssuanceServerGrpcChannel
    fun providesPhosphorManagedChannel(
      phosphorManagedChannelFactory: PhosphorManagedChannelFactory
    ): ManagedChannel {
      return phosphorManagedChannelFactory.create()
    }
  }
}

@Module
@InstallIn(SingletonComponent::class)
internal interface ProxyModule {
  @PrivateInferenceProxyConfiguration
  @Binds
  @Singleton
  fun bindProxyConfigManager(impl: ProxyConfigManagerImpl): ProxyConfigManager
}
