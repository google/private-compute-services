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

import android.content.Context;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.JobType;
import com.google.android.as.oss.fl.api.proto.TrainerResponse;
import com.google.android.as.oss.fl.api.proto.TrainerResponse.ResponseCode;
import com.google.android.as.oss.fl.api.proto.TrainingServiceGrpc;
import com.google.fcp.client.InAppTrainer;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.fcp.client.InAppTrainerOptions.AttestationMode;
import com.google.fcp.client.TrainingInterval;
import com.google.fcp.client.TrainingInterval.SchedulingMode;
import com.google.fcp.client.tasks.Task;
import com.google.common.flogger.GoogleLogger;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Executor;

/** Bindable GRPC Service to schedules/cancel federated jobs. */
public class TrainerGrpcBindableService extends TrainingServiceGrpc.TrainingServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final int ATTESTATION_MODE = AttestationMode.DEFAULT;

  private final Executor executor;
  private final Context context;
  private final TrainerSupplier trainerSupplier;

  TrainerGrpcBindableService(Executor executor, Context context, TrainerSupplier trainerSupplier) {
    this.executor = executor;
    this.context = context;
    this.trainerSupplier = trainerSupplier;
  }

  @Override
  public void scheduleFederatedComputation(
      TrainerOptions trainerOptions, StreamObserver<TrainerResponse> responseStreamObserver) {
    if (trainerOptions.getJobType() == JobType.JOB_TYPE_SCHEDULE) {
      scheduleTraining(trainerOptions, responseStreamObserver);
    } else if (trainerOptions.getJobType() == JobType.JOB_TYPE_CANCEL) {
      disableTraining(trainerOptions, responseStreamObserver);
    } else {
      responseStreamObserver.onNext(
          TrainerResponse.newBuilder()
              .setResponseCode(ResponseCode.RESPONSE_CODE_UNSUPPORTED_JOB_TYPE)
              .build());
      responseStreamObserver.onCompleted();
    }
  }

  private void scheduleTraining(
      TrainerOptions trainerOptions, StreamObserver<TrainerResponse> responseStreamObserver) {

    logger.atInfo().log(
        "Scheduling training for population:%s session_name:%s",
        trainerOptions.getPopulationName(), trainerOptions.getSessionName());

    InAppTrainerOptions.Builder inAppTrainerOptionsBuilder =
        InAppTrainerOptions.newBuilder()
            .setJobSchedulerJobId(trainerOptions.getTrainerJobId(), false)
            .setSessionName(trainerOptions.getSessionName())
            .setFederatedOptions(trainerOptions.getPopulationName())
            .setAttestationMode(ATTESTATION_MODE);
    if (trainerOptions.hasTrainingIntervalMs()) {
      inAppTrainerOptionsBuilder.setTrainingInterval(
          TrainingInterval.newBuilder()
              .setSchedulingMode(SchedulingMode.RECURRENT)
              .setMinimumIntervalMillis(trainerOptions.getTrainingIntervalMs())
              .build());
    }

    Task<InAppTrainer> trainerTask =
        trainerSupplier.get(context, executor, inAppTrainerOptionsBuilder.build());
    trainerTask
        .addOnSuccessListener(
            executor,
            (InAppTrainer trainer) ->
                trainer
                    .schedule()
                    .addOnSuccessListener(
                        executor,
                        (Void v) -> {
                          logger.atInfo().log(
                              "Scheduling training succeeded [population=%s, session=%s]",
                              trainerOptions.getPopulationName(), trainerOptions.getSessionName());

                          responseStreamObserver.onNext(
                              TrainerResponse.newBuilder()
                                  .setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS)
                                  .build());
                          responseStreamObserver.onCompleted();
                        })
                    .addOnFailureListener(
                        executor,
                        (Exception error) -> {
                          logger.atWarning().withCause(error).log(
                              "Scheduling training failed [population=%s, session=%s]",
                              trainerOptions.getPopulationName(), trainerOptions.getSessionName());

                          responseStreamObserver.onError(error);
                        }))
        .addOnFailureListener(
            executor,
            (Exception error) -> {
              logger.atWarning().withCause(error).log(
                  "Scheduling training failed [population=%s, session=%s]",
                  trainerOptions.getPopulationName(), trainerOptions.getSessionName());

              responseStreamObserver.onError(error);
            });
  }

  private void disableTraining(
      TrainerOptions trainerOptions, StreamObserver<TrainerResponse> responseStreamObserver) {

    logger.atInfo().log(
        "Cancelling training for population:%s session_name:%s",
        trainerOptions.getPopulationName(), trainerOptions.getSessionName());

    InAppTrainerOptions.Builder inAppTrainerOptionsBuilder =
        InAppTrainerOptions.newBuilder()
            .setJobSchedulerJobId(trainerOptions.getTrainerJobId(), false)
            .setSessionName(trainerOptions.getSessionName())
            .setFederatedOptions(trainerOptions.getPopulationName())
            .setAttestationMode(ATTESTATION_MODE);

    Task<InAppTrainer> trainerTask =
        trainerSupplier.get(context, executor, inAppTrainerOptionsBuilder.build());
    trainerTask
        .addOnSuccessListener(
            executor,
            (InAppTrainer trainer) ->
                trainer
                    .cancel()
                    .addOnSuccessListener(
                        executor,
                        (Void v) -> {
                          logger.atInfo().log(
                              "Cancelling training succeeded [population=%s, session=%s]",
                              trainerOptions.getPopulationName(), trainerOptions.getSessionName());

                          responseStreamObserver.onNext(
                              TrainerResponse.newBuilder()
                                  .setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS)
                                  .build());
                          responseStreamObserver.onCompleted();
                        })
                    .addOnFailureListener(
                        executor,
                        (Exception error) -> {
                          logger.atWarning().withCause(error).log(
                              "Cancelling training failed [population=%s, session=%s]",
                              trainerOptions.getPopulationName(), trainerOptions.getSessionName());

                          responseStreamObserver.onError(error);
                        }))
        .addOnFailureListener(
            executor,
            (Exception error) -> {
              logger.atWarning().withCause(error).log(
                  "Cancelling training failed [population=%s, session=%s]",
                  trainerOptions.getPopulationName(), trainerOptions.getSessionName());

              responseStreamObserver.onError(error);
            });
  }

  interface TrainerSupplier {
    Task<InAppTrainer> get(Context context, Executor executor, InAppTrainerOptions opts);
  }
}
