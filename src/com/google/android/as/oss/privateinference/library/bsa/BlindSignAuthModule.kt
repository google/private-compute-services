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

package com.google.android.`as`.oss.privateinference.library.bsa

import com.google.android.`as`.oss.privateinference.Annotations.TokenIssuanceServerGrpcChannel
import com.google.android.`as`.oss.privateinference.config.impl.DeviceInfo
import com.google.android.`as`.oss.privateinference.library.bsa.impl.AndroidKeystoreAttester
import com.google.android.`as`.oss.privateinference.library.bsa.impl.BlindSignAuthImpl
import com.google.android.`as`.oss.privateinference.library.bsa.impl.PhosphorGrpcMessageInterface
import com.google.android.`as`.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper
import com.google.android.`as`.oss.privateinference.util.timers.Annotations.PrivateInferenceClientTimers
import com.google.android.`as`.oss.privateinference.util.timers.TimerSet
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.grpc.ManagedChannel
import java.util.Optional
import javax.inject.Singleton

/** Module that provides the BlindSignAuth implementation. */
@Module
@InstallIn(SingletonComponent::class)
internal object BlindSignAuthModule {

  @Provides
  @Singleton
  fun provideBlindSignAuth(
    @TokenIssuanceServerGrpcChannel managedChannel: Lazy<ManagedChannel>,
    networkUsageLogHelper: PrivateInferenceNetworkUsageLogHelper,
    androidKeystoreAttester: AndroidKeystoreAttester,
    @PrivateInferenceClientTimers timerSet: TimerSet,
    deviceInfo: Optional<DeviceInfo>,
  ): BlindSignAuth {
    return BlindSignAuthImpl(
      PhosphorGrpcMessageInterface(managedChannel, networkUsageLogHelper, timerSet, deviceInfo),
      androidKeystoreAttester,
      timerSet,
    )
  }
}
