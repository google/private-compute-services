/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.attestation.jobs;

import com.google.android.as.oss.attestation.config.PcsAttestationMeasurementConfig;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.federatedcompute.training.PopulationTrainingScheduler;
import com.google.android.as.oss.fl.federatedcompute.training.TrainingCriteria;
import com.google.android.as.oss.fl.populations.Population;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import java.util.Optional;

@Module
@InstallIn(SingletonComponent.class)
final class AttestationJobSchedulerModule {
  @Provides
  @IntoSet
  static Optional<TrainingCriteria> providesAttestationPopulationTrainingCriteria(
      ConfigReader<PcsAttestationMeasurementConfig> pcsAttestationMeasurementConfigConfigReader,
      BuildFlavor buildFlavor) {
    return pcsAttestationMeasurementConfigConfigReader.getConfig().scheduleAttestationJob()
        ? Optional.of(
            new TrainingCriteria() {
              @Override
              public TrainerOptions getTrainerOptions() {
                return PopulationTrainingScheduler.buildTrainerOpts(
                    Population.OPSTATS.populationName());
              }

              @Override
              public boolean canScheduleTraining() {
                return buildFlavor.isRelease()
                    && pcsAttestationMeasurementConfigConfigReader
                        .getConfig()
                        .scheduleAttestationJob();
              }
            })
        : Optional.empty();
  }

  private AttestationJobSchedulerModule() {}
}
