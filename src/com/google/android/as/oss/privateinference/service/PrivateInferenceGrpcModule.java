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

package com.google.android.as.oss.privateinference.service;

import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceServiceGrpc;
import com.google.android.as.oss.privateinference.util.timers.Annotations.PrivateInferenceServiceTimers;
import com.google.android.as.oss.privateinference.util.timers.LatencyLoggingTimer;
import com.google.android.as.oss.privateinference.util.timers.PiDebugLogTimers;
import com.google.android.as.oss.privateinference.util.timers.TimerSet;
import com.google.android.as.oss.privateinference.util.timers.Timers;
import com.google.android.as.oss.privateinference.util.timers.TraceTimers;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import io.grpc.ServerInterceptors;
import io.grpc.binder.PeerUids;
import java.util.Set;
import javax.inject.Singleton;

/** Registers {@link PcsPrivateInferenceServiceGrpc} with the PCS gRPC service. */
@Module
@InstallIn(SingletonComponent.class)
abstract class PrivateInferenceGrpcModule {
  @Provides
  @IntoSet
  @GrpcService
  static BindableService provideBindableService(PrivateInferenceGrpcBindableService service) {
    return () ->
        ServerInterceptors.intercept(
            service,
            new FileDescriptorServerInterceptor(),
            PeerUids.newPeerIdentifyingServerInterceptor());
  }

  @Provides
  @IntoSet
  @GrpcServiceName
  static String provideServiceName() {
    return PcsPrivateInferenceServiceGrpc.SERVICE_NAME;
  }

  @Provides
  @Singleton
  @PrivateInferenceServiceTimers
  static TimerSet timers(@PrivateInferenceServiceTimers Set<Timers> timers) {
    return new TimerSet(timers);
  }

  @Binds
  @IntoSet
  @PrivateInferenceServiceTimers
  abstract Timers bindsTraceTimers(TraceTimers timers);

  @Binds
  @IntoSet
  @PrivateInferenceServiceTimers
  abstract Timers bindLatencyLoggingTimers(LatencyLoggingTimer loggingTimer);

  @Binds
  @IntoSet
  @PrivateInferenceServiceTimers
  abstract Timers bindsTimers(PiDebugLogTimers timers);
}
