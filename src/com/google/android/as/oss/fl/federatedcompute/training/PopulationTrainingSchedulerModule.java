/*
 * Copyright 2024 Google LLC
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

package com.google.android.as.oss.fl.federatedcompute.training;

import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import com.google.android.as.oss.fl.brella.service.scheduler.TrainingScheduler;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
abstract class PopulationTrainingSchedulerModule {
  @Provides
  @Singleton
  static PopulationTrainingScheduler provideTrainingSchedulerModule(
      TrainingScheduler trainingScheduler,
      Set<Optional<TrainingCriteria>> trainingCriterion,
      @FlExecutorQualifier Executor executor) {
    return new PopulationTrainingScheduler(trainingScheduler, trainingCriterion, executor);
  }

  @Provides
  @IntoSet
  static PcsInitializer providePcsInitializer(
      PopulationTrainingScheduler populationTrainingScheduler) {
    return () -> populationTrainingScheduler.schedule(Optional.empty());
  }
}
