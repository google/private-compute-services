/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.fl.localcompute.client;

import static java.util.concurrent.TimeUnit.MINUTES;

import android.content.Context;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.grpc.Channel;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.binder.AndroidComponentAddress;
import io.grpc.binder.BinderChannelBuilder;
import io.grpc.binder.InboundParcelablePolicy;
import io.grpc.binder.UntrustedSecurityPolicies;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;
import javax.inject.Singleton;

/** Provides OnDeviceChannel that communicates with gRPC server in ASI. */
@Module
@InstallIn(SingletonComponent.class)
abstract class ChannelModule {

  private static final String PACKAGE_NAME = "com.google.android.as";

  private static final String CLASS_NAME =
      "com.google.android.apps.miphone.aiai.common.brella.filecopy.service.FileCopyEndpointService";

  @Provides
  @AsiGrpcChannel
  @Singleton
  static Channel providesChannel(@ApplicationContext Context context) {
    return BinderChannelBuilder.forAddress(
            AndroidComponentAddress.forRemoteComponent(PACKAGE_NAME, CLASS_NAME), context)
        .securityPolicy(UntrustedSecurityPolicies.untrustedPublic())
        .inboundParcelablePolicy(
            InboundParcelablePolicy.newBuilder().setAcceptParcelableMetadataValues(true).build())
        .decompressorRegistry(DecompressorRegistry.emptyInstance())
        .compressorRegistry(CompressorRegistry.newEmptyInstance())
        .idleTimeout(1, MINUTES)
        .build();
  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface AsiGrpcChannel {}

  private ChannelModule() {}
}
