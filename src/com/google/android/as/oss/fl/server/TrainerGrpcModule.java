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

package com.google.android.as.oss.fl.server;

import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.api.proto.TrainingServiceGrpc;
import com.google.android.as.oss.fl.brella.service.scheduler.TrainingScheduler;
import com.google.android.apps.miphone.astrea.grpc.Annotations.GrpcService;
import com.google.android.apps.miphone.astrea.grpc.Annotations.GrpcServiceName;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import io.grpc.BindableService;
import java.util.Optional;
import java.util.concurrent.Executor;

/** Module to provide GRPC Service used for scheduling/canceling federated jobs. */
@Module
@InstallIn(SingletonComponent.class)
abstract class TrainerGrpcModule {
  @Provides
  @IntoSet
  @GrpcService
  static BindableService provideBindableService(
      TrainingScheduler trainingScheduler, @FlExecutorQualifier Executor executor) {
    return new TrainerGrpcBindableService(trainingScheduler, executor);
  }

  @Provides
  @IntoSet
  @GrpcServiceName
  static String provideServiceName() {
    return TrainingServiceGrpc.SERVICE_NAME;
  }

  private TrainerGrpcModule() {}
}
