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

package com.google.android.as.oss.fl.federatedcompute.training;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.util.concurrent.Futures.immediateFuture;

import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.JobType;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.TrainingMode;
import com.google.android.as.oss.fl.fc.service.scheduler.TrainingScheduler;
import com.google.android.as.oss.fl.populations.Population;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

/** Schedules inAppTraining for populations related to platform logging. */
@RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PopulationTrainingScheduler {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final ImmutableList<TrainingCriteria> trainingCriteria;
  private final Executor executor;
  private final TrainingScheduler trainingScheduler;

  public PopulationTrainingScheduler(
      TrainingScheduler trainingScheduler,
      Set<Optional<TrainingCriteria>> trainingCriteria,
      Executor executor) {
    this.trainingCriteria =
        trainingCriteria.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toImmutableList());
    this.trainingScheduler = trainingScheduler;
    this.executor = executor;
  }

  /**
   * Schedules training for all training criteria.
   *
   * @param additionalTrainingCriteria an optional future of additional training criteria to
   *     schedule.
   */
  @CanIgnoreReturnValue
  public ListenableFuture<Void> schedule(
      Optional<ListenableFuture<Set<TrainingCriteria>>> additionalTrainingCriteria) {
    return FluentFuture.from(
            additionalTrainingCriteria.isPresent()
                ? additionalTrainingCriteria.get()
                : immediateFuture(ImmutableSet.of()))
        .transformAsync(
            additionalCriteria -> {
              Set<TrainingCriteria> trainingCriteriaToSchedule =
                  new HashSet<>(PopulationTrainingScheduler.this.trainingCriteria);
              trainingCriteriaToSchedule.addAll(additionalCriteria);
              return Futures.allAsList(
                  trainingCriteriaToSchedule.stream()
                      .map(
                          criteria -> {
                            if (criteria.canScheduleTraining()) {
                              return registerPopulation(criteria.getTrainerOptions());
                            } else {
                              return unregisterPopulation(criteria.getTrainerOptions());
                            }
                          })
                      .collect(toImmutableList()));
            },
            executor)
        .transform(
            results -> {
              return null;
            },
            executor);
  }

  private ListenableFuture<Void> registerPopulation(TrainerOptions trainerOpts) {
    return CallbackToFutureAdapter.getFuture(
        completer -> {
          logger.atFine().log(
              "Requesting Training for populationName: %s", trainerOpts.getPopulationName());
          trainingScheduler.scheduleTraining(
              trainerOpts,
              (Void v) -> {
                logger.atFine().log(
                    "Training successfully scheduled for the populationName %s",
                    trainerOpts.getPopulationName());
                completer.set(null);
              },
              (Exception error) -> {
                logger.atWarning().withCause(error).log(
                    "Scheduling training failed [populationName=%s]",
                    trainerOpts.getPopulationName());
                completer.setException(error);
              });

          // This value is used only for debug purposes: it will be used in
          // toString() of returned future or error cases.
          return "Register Population Future";
        });
  }

  /**
   * Builds a {@link TrainerOptions} for the given population name.
   *
   * @param populationName the name of the population to be trained.
   */
  public static TrainerOptions buildTrainerOpts(String populationName) {
    int jobSchedulerId = Population.getHashByPopulationName(populationName);
    if (jobSchedulerId > 0) {
      // Ensure the jobScheduleId is negative to reduce likelihood of colliding with
      // non-TrainingManager jobs.
      jobSchedulerId = jobSchedulerId * -1;
    }

    return TrainerOptions.newBuilder()
        .setSessionName(populationName)
        .setTrainerJobId(jobSchedulerId)
        .setTrainingMode(TrainingMode.TRAINING_MODE_FEDERATION)
        .setPopulationName(populationName)
        .setJobType(JobType.JOB_TYPE_SCHEDULE)
        .build();
  }

  private ListenableFuture<Void> unregisterPopulation(TrainerOptions trainerOpts) {
    return CallbackToFutureAdapter.getFuture(
        completer -> {
          logger.atFine().log(
              "Cancelling Training for populationName: %s", trainerOpts.getPopulationName());
          trainingScheduler.disableTraining(
              trainerOpts,
              (Void v) -> {
                logger.atFine().log(
                    "Cancelled training for the populationName %s",
                    trainerOpts.getPopulationName());
                completer.set(null);
              },
              (Exception error) -> {
                logger.atWarning().withCause(error).log(
                    "Cancel training failed for [populationName=%s]",
                    trainerOpts.getPopulationName());
                completer.setException(error);
              });

          // This value is used only for debug purposes: it will be used in
          // toString() of returned future or error cases.
          return "Unregister Population Future";
        });
  }
}
