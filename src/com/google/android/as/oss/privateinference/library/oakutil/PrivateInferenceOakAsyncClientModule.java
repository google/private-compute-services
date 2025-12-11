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

package com.google.android.as.oss.privateinference.library.oakutil;

import com.google.android.as.oss.common.ExecutorAnnotations.PiExecutorQualifier;
import com.google.android.as.oss.common.time.TimeSource;
import com.google.android.as.oss.privateinference.util.timers.Annotations.PrivateInferenceClientTimers;
import com.google.android.as.oss.privateinference.util.timers.PiDebugLogTimers;
import com.google.android.as.oss.privateinference.util.timers.TimerSet;
import com.google.android.as.oss.privateinference.util.timers.Timers;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.oak.client.grpc.StreamObserverSessionClient;
import com.google.oak.remote_attestation.AttestationVerificationClock;
import com.google.oak.session.OakSessionConfigBuilder;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import java.util.Set;
import javax.inject.Provider;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
abstract class PrivateInferenceOakAsyncClientModule {
  @Binds
  @Singleton
  @PiExecutorQualifier
  abstract ListeningExecutorService bindsListeningExecutorService(
      @PiExecutorQualifier ListeningScheduledExecutorService executor);

  @Provides
  @Singleton
  static StreamObserverSessionClient provideStreamObserverSessionClient(
      Provider<OakSessionConfigBuilder> configProvider) {
    return new StreamObserverSessionClient(configProvider);
  }

  @Binds
  abstract PrivateInferenceOakAsyncClient.DeviceAttestationGenerator
      bindsDeviceAttestationGenerator(AndroidKeystoreAttestationGenerator generator);

  @Provides
  @Singleton
  static AttestationVerificationClock provideAttestationVerificationClock(TimeSource timeSource) {
    AttestationVerificationClock clock =
        new AttestationVerificationClock() {
          @Override
          // This method is called by the native code, so don't strip it.
          public long millisecondsSinceEpoch() {
            return timeSource.now().toEpochMilli();
          }
        };
    // Reference the clock to make sure it is not stripped by proguard.
    return clock;
  }

  @Provides
  @Singleton
  @PrivateInferenceClientTimers
  static TimerSet providesTimerSet(@PrivateInferenceClientTimers Set<Timers> timers) {
    return new TimerSet(timers);
  }

  @Binds
  @IntoSet
  @PrivateInferenceClientTimers
  abstract Timers bindsLogTimers(PiDebugLogTimers timers);
}
