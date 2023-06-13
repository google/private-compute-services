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

package com.google.android.as.oss.fl.federatedcompute.statsd.scheduler;

import static java.nio.charset.StandardCharsets.UTF_8;

import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.JobType;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.TrainingMode;
import com.google.android.as.oss.fl.brella.service.scheduler.TrainingScheduler;
import com.google.android.as.oss.fl.federatedcompute.statsd.config.StatsdConfig;
import com.google.android.as.oss.fl.populations.Population;
import com.google.common.flogger.GoogleLogger;
import com.google.common.hash.Hashing;
import java.util.Optional;
import javax.inject.Inject;

@RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
class StatsdTrainingPopulationScheduler {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final TrainingScheduler trainingScheduler;
  private final ConfigReader<StatsdConfig> statsdConfigReader;
  private final BuildFlavor buildFlavor;

  @Inject
  StatsdTrainingPopulationScheduler(
      TrainingScheduler trainingScheduler,
      ConfigReader<StatsdConfig> statsdConfigReader,
      BuildFlavor buildFlavor) {
    this.trainingScheduler = trainingScheduler;
    this.statsdConfigReader = statsdConfigReader;
    this.buildFlavor = buildFlavor;
  }

  // Schedules inAppTraining for populations related to platform logging.
  public void schedule(Optional<TrainingSchedulerCallback> callback) {
    if (statsdConfigReader.getConfig().enablePlatformLogging()) {
      registerPopulation(Population.PLATFORM_LOGGING.populationName(), callback);
    } else {
      unregisterPopulation(Population.PLATFORM_LOGGING.populationName(), callback);
    }

    if (buildFlavor.isInternal() && statsdConfigReader.getConfig().enablePlatformLoggingTesting()) {
      registerPopulation(Population.PLATFORM_LOGGING_TEST.populationName(), callback);
    } else {
      unregisterPopulation(Population.PLATFORM_LOGGING_TEST.populationName(), callback);
    }
  }

  private void registerPopulation(
      String populationName, Optional<TrainingSchedulerCallback> callback) {
    logger.atFine().log("Requesting Training for populationName: %s", populationName);
    trainingScheduler.scheduleTraining(
        buildTrainerOpts(populationName),
        (Void v) -> {
          logger.atFine().log(
              "Training successfully scheduled for the populationName %s", populationName);
          if (callback.isPresent()) {
            callback.get().onTrainingScheduleSuccess();
          }
        },
        (Exception error) -> {
          logger.atWarning().withCause(error).log(
              "Scheduling training failed [populationName=%s]", populationName);
          if (callback.isPresent()) {
            callback.get().onTrainingScheduleFailure(error);
          }
        });
  }

  private void unregisterPopulation(
      String populationName, Optional<TrainingSchedulerCallback> callback) {
    logger.atFine().log("Cancelling Training for populationName: %s", populationName);
    trainingScheduler.disableTraining(
        buildTrainerOpts(populationName),
        (Void v) -> {
          logger.atFine().log("Cancelled training for the populationName %s", populationName);
          if (callback.isPresent()) {
            callback.get().onTrainingScheduleSuccess();
          }
        },
        (Exception error) -> {
          logger.atWarning().withCause(error).log(
              "Cancel training failed for [populationName=%s]", populationName);
          if (callback.isPresent()) {
            callback.get().onTrainingScheduleFailure(error);
          }
        });
  }

  private static TrainerOptions buildTrainerOpts(String populationName) {
    int jobSchedulerId = Hashing.farmHashFingerprint64().hashString(populationName, UTF_8).asInt();
    if (jobSchedulerId > 0) {
      // Ensure the jobScheduleId is negative to reduce likelihood of colliding with
      // non-TrainingManager jobs.
      jobSchedulerId = jobSchedulerId * -1;
    }

    TrainerOptions.Builder builder =
        TrainerOptions.newBuilder()
            .setSessionName(populationName)
            .setTrainerJobId(jobSchedulerId)
            .setTrainingMode(TrainingMode.TRAINING_MODE_FEDERATION)
            .setPopulationName(populationName);

    return builder.setJobType(JobType.JOB_TYPE_SCHEDULE).build();
  }
}
