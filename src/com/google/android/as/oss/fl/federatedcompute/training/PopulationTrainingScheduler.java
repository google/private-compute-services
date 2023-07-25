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

package com.google.android.as.oss.fl.federatedcompute.training;

import android.os.Build.VERSION_CODES;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.JobType;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.TrainingMode;
import com.google.android.as.oss.fl.brella.service.scheduler.TrainingScheduler;
import com.google.android.as.oss.fl.populations.Population;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;

/** Schedules inAppTraining for populations related to platform logging. */
@RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
public class PopulationTrainingScheduler {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final Set<Optional<TrainingCriteria>> trainingCriteria;
  private final Executor executor;
  private final TrainingScheduler trainingScheduler;

  public PopulationTrainingScheduler(
      TrainingScheduler trainingScheduler,
      Set<Optional<TrainingCriteria>> trainingCriteria,
      Executor executor) {
    this.trainingCriteria = trainingCriteria;
    this.trainingScheduler = trainingScheduler;
    this.executor = executor;
  }

  /**
   * Schedules training for all training criteria.
   *
   * @param callback callback to be called when training for all criteria is scheduled.
   */
  public void schedule(Optional<TrainingSchedulerCallback> callback) {
    List<ListenableFuture<Void>> futures = new ArrayList<>();
    for (var maybeCriteria : trainingCriteria) {
      if (maybeCriteria.isPresent()) {
        TrainingCriteria criteria = maybeCriteria.get();
        if (criteria.canScheduleTraining()) {
          futures.add(registerPopulation(criteria.getTrainerOptions()));
        } else {
          futures.add(unregisterPopulation(criteria.getTrainerOptions()));
        }
      }
    }
    // Add a callback to the list of ListenableFutures that will be called when all of the futures
    // have completed
    Futures.addCallback(
        Futures.allAsList(futures),
        new FutureCallback<List<Void>>() {
          @Override
          public void onSuccess(@Nullable List<Void> results) {
            // This code will be called when all of the futures have completed without errors.
            callback.ifPresent(c -> c.onTrainingScheduleSuccess());
          }

          @Override
          public void onFailure(Throwable t) {
            callback.ifPresent(c -> c.onTrainingScheduleFailure(t));
          }
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
