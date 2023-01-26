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

package com.google.android.as.oss.fl.brella.service.scheduler;

import static com.google.android.as.oss.fl.federatedcompute.util.ClassConversionUtils.schedulingModeEnumToIntDef;

import android.content.Context;
import android.net.Uri;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.TrainingMode;
import com.google.android.as.oss.fl.federatedcompute.config.PcsFcFlags;
import com.google.android.as.oss.fl.localcompute.PathConversionUtils;
import com.google.fcp.client.InAppTrainer;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.fcp.client.InAppTrainerOptions.AttestationMode;
import com.google.fcp.client.InAppTrainingConstraints;
import com.google.fcp.client.TrainingInterval;
import com.google.fcp.client.tasks.OnFailureListener;
import com.google.fcp.client.tasks.OnSuccessListener;
import com.google.fcp.client.tasks.Task;
import com.google.common.flogger.GoogleLogger;
import java.util.Optional;
import java.util.concurrent.Executor;

/** Federated training scheduler to schedule training for population. */
public class FederatedTrainingScheduler implements TrainingScheduler {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final int ATTESTATION_MODE = AttestationMode.DEFAULT;

  private final Executor executor;
  private final Context context;
  private final TrainerSupplier trainerSupplier;

  FederatedTrainingScheduler(Executor executor, Context context, TrainerSupplier trainerSupplier) {
    this.executor = executor;
    this.context = context;
    this.trainerSupplier = trainerSupplier;
  }

  @Override
  public void scheduleTraining(
      TrainerOptions trainerOptions,
      OnSuccessListener<Void> successListener,
      OnFailureListener failureListener) {
    InAppTrainerOptions.Builder inAppTrainerOptionsBuilder = buildTrainerOpts(trainerOptions);

    if (trainerOptions.hasTrainingMode()
        && trainerOptions.getTrainingMode() == TrainingMode.TRAINING_MODE_LOCAL_COMPUTATION) {
      logger.atInfo().log(
          "Scheduling local computation for session_name:%s", trainerOptions.getSessionName());
    } else {
      logger.atInfo().log(
          "Scheduling training for population:%s session_name:%s",
          trainerOptions.getPopulationName(), trainerOptions.getSessionName());
    }

    Task<InAppTrainer> trainerTask =
        trainerSupplier.get(context, executor, inAppTrainerOptionsBuilder.build());
    trainerTask
        .addOnSuccessListener(
            executor,
            (InAppTrainer trainer) ->
                trainer
                    .schedule()
                    .addOnSuccessListener(executor, successListener)
                    .addOnFailureListener(executor, failureListener))
        .addOnFailureListener(executor, failureListener);
  }

  @Override
  public void disableTraining(
      TrainerOptions trainerOptions,
      OnSuccessListener<Void> successListener,
      OnFailureListener failureListener) {
    InAppTrainerOptions.Builder inAppTrainerOptionsBuilder = buildTrainerOpts(trainerOptions);

    if (trainerOptions.hasTrainingMode()
        && trainerOptions.getTrainingMode() == TrainingMode.TRAINING_MODE_LOCAL_COMPUTATION) {
      logger.atInfo().log(
          "Cancelling local computation for session_name:%s", trainerOptions.getSessionName());
    } else {
      logger.atInfo().log(
          "Cancelling training for population:%s session_name:%s",
          trainerOptions.getPopulationName(), trainerOptions.getSessionName());
    }

    Task<InAppTrainer> trainerTask =
        trainerSupplier.get(context, executor, inAppTrainerOptionsBuilder.build());
    trainerTask
        .addOnSuccessListener(
            executor,
            (InAppTrainer trainer) ->
                trainer
                    .cancel()
                    .addOnSuccessListener(executor, successListener)
                    .addOnFailureListener(executor, failureListener))
        .addOnFailureListener(executor, failureListener);
  }

  private InAppTrainerOptions.Builder buildTrainerOpts(TrainerOptions trainerOptions) {
    InAppTrainerOptions.Builder inAppTrainerOptionsBuilder =
        InAppTrainerOptions.newBuilder()
            .setJobSchedulerJobId(trainerOptions.getTrainerJobId(), false)
            .setSessionName(trainerOptions.getSessionName());

    if (trainerOptions.hasTrainingMode()
        && trainerOptions.getTrainingMode() == TrainingMode.TRAINING_MODE_LOCAL_COMPUTATION) {
      final Uri localComputationPlanUri = Uri.parse(trainerOptions.getLocalComputationPlanUri());
      final Uri inputDirectoryUri = Uri.parse(trainerOptions.getInputDirectoryUri());
      final Uri outputDirectoryUri = Uri.parse(trainerOptions.getOutputDirectoryUri());
      inAppTrainerOptionsBuilder.setLocalComputationOptions(
          localComputationPlanUri, inputDirectoryUri, outputDirectoryUri);
    } else {
      inAppTrainerOptionsBuilder
          .setFederatedOptions(trainerOptions.getPopulationName())
          .setAttestationMode(ATTESTATION_MODE);
    }

    if (trainerOptions.hasSchedulingMode()) {
      TrainingInterval.Builder trainingIntervalBuilder = TrainingInterval.newBuilder();
      trainingIntervalBuilder.setSchedulingMode(
          schedulingModeEnumToIntDef(trainerOptions.getSchedulingMode()));

      if (trainerOptions.hasTrainingIntervalMs()) {
        trainingIntervalBuilder.setMinimumIntervalMillis(trainerOptions.getTrainingIntervalMs());
      }
      inAppTrainerOptionsBuilder.setTrainingInterval(trainingIntervalBuilder.build());
    }

    if (trainerOptions.hasContextData()) {
      inAppTrainerOptionsBuilder.setContextData(trainerOptions.getContextData().toByteArray());
    }

    return inAppTrainerOptionsBuilder;
  }
}
