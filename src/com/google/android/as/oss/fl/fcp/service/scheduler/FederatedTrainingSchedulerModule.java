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

package com.google.android.as.oss.fl.fc.service.scheduler;

import android.content.Context;
import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.federatedcompute.config.PcsFcFlags;
import com.google.fcp.client.InAppTraining;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.util.Optional;
import java.util.concurrent.Executor;

@Module
@InstallIn(SingletonComponent.class)
abstract class FederatedTrainingSchedulerModule {
  @BindsOptionalOf
  abstract PcsFcFlags bindPcsFcFlags();

  @Provides
  static TrainingScheduler provideTrainingScheduler(
      Optional<PcsFcFlags> fcFlags,
      @ApplicationContext Context context,
      @FlExecutorQualifier Executor executor) {
    return new FederatedTrainingScheduler(
        executor, context, fcFlags, InAppTraining::getInAppTrainer);
  }
}
