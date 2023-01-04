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

import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.JobType;
import com.google.android.as.oss.fl.api.proto.TrainerOptions.TrainingMode;
import com.google.android.as.oss.fl.api.proto.TrainerResponse;
import com.google.android.as.oss.fl.api.proto.TrainerResponse.ResponseCode;
import com.google.android.as.oss.fl.api.proto.TrainingServiceGrpc;
import com.google.android.as.oss.fl.brella.service.scheduler.TrainingScheduler;
import com.google.fcp.client.tasks.OnFailureListener;
import com.google.fcp.client.tasks.OnSuccessListener;
import com.google.common.flogger.GoogleLogger;
import io.grpc.stub.StreamObserver;
import java.util.Optional;
import java.util.concurrent.Executor;

/** Bindable GRPC Service to schedules/cancel federated jobs. */
public class TrainerGrpcBindableService extends TrainingServiceGrpc.TrainingServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final TrainingScheduler trainingScheduler;
  private final Optional<LocalComputeResourceManager> resourceManager;
  private final Executor executor;

  TrainerGrpcBindableService(
      TrainingScheduler trainingScheduler,
      Optional<LocalComputeResourceManager> resourceManager,
      Executor executor) {
    this.trainingScheduler = trainingScheduler;
    this.resourceManager = resourceManager;
    this.executor = executor;
  }

  @Override
  public void scheduleFederatedComputation(
      TrainerOptions trainerOptions, StreamObserver<TrainerResponse> responseStreamObserver) {
    if (trainerOptions.getJobType() == JobType.JOB_TYPE_SCHEDULE) {
      OnFailureListener failureListener =
          (Exception error) -> {
            logger.atWarning().withCause(error).log(
                "Scheduling training failed [population=%s, session=%s]",
                trainerOptions.getPopulationName(), trainerOptions.getSessionName());
            responseStreamObserver.onError(error);
          };
      OnSuccessListener<Void> successListener =
          (Void v) -> {
            logger.atInfo().log(
                "Scheduling training succeeded [population=%s, session=%s]",
                trainerOptions.getPopulationName(), trainerOptions.getSessionName());

            responseStreamObserver.onNext(
                TrainerResponse.newBuilder()
                    .setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS)
                    .build());
            responseStreamObserver.onCompleted();
          };

      trainingScheduler.scheduleTraining(trainerOptions, successListener, failureListener);
    } else if (trainerOptions.getJobType() == JobType.JOB_TYPE_CANCEL) {
      OnFailureListener failureListener =
          (Exception error) -> {
            logger.atWarning().withCause(error).log(
                "Cancelling training failed [population=%s, session=%s]",
                trainerOptions.getPopulationName(), trainerOptions.getSessionName());
            responseStreamObserver.onError(error);
          };
      OnSuccessListener<Void> successListener =
          (Void v) -> {
            logger.atInfo().log(
                "Cancelling training succeeded [population=%s, session=%s]",
                trainerOptions.getPopulationName(), trainerOptions.getSessionName());

            responseStreamObserver.onNext(
                TrainerResponse.newBuilder()
                    .setResponseCode(ResponseCode.RESPONSE_CODE_SUCCESS)
                    .build());
            responseStreamObserver.onCompleted();
          };

      trainingScheduler.disableTraining(trainerOptions, successListener, failureListener);
    } else {
      responseStreamObserver.onNext(
          TrainerResponse.newBuilder()
              .setResponseCode(ResponseCode.RESPONSE_CODE_UNSUPPORTED_JOB_TYPE)
              .build());
      responseStreamObserver.onCompleted();
    }
  }
}
