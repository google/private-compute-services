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

package com.google.android.as.oss.pir.service;

import com.google.android.as.oss.grpc.Annotations.GrpcService;
import com.google.android.as.oss.grpc.Annotations.GrpcServiceName;
import com.google.android.as.oss.pir.api.pir.proto.PirServiceGrpc;
import com.google.private_retrieval.pir.AndroidLocalPirDownloadTaskBuilderFactory;
import com.google.private_retrieval.pir.PirDownloadTask.Builder.PirDownloadTaskBuilderFactory;
import com.google.private_retrieval.pir.core.PirClientSwigFactory;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

import com.google.common.base.Ticker;
import android.os.SystemClock;

@Module
@InstallIn(SingletonComponent.class)
abstract class PirGrpcModule {

  @Binds
  @IntoSet
  @GrpcService
  abstract BindableService provideBindableService(PirGrpcBindableService impl);

  @Provides
  @IntoSet
  @GrpcServiceName
  static String provideServiceName() {
    return PirServiceGrpc.SERVICE_NAME;
  }

  @Provides
  @PirDownloadTaskBuilderFactoryServerSide
  static PirDownloadTaskBuilderFactory providePirDownloadTaskBuilderFactory() {
    return new AndroidLocalPirDownloadTaskBuilderFactory(
        new Ticker() {
          @Override
          public long read() {
            return SystemClock.elapsedRealtimeNanos();
          }
        },
        new PirClientSwigFactory());
  }

  /** Annotation for {@link PirDownloadTaskBuilderFactory} to be used by PCS service. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @interface PirDownloadTaskBuilderFactoryServerSide {}
}
