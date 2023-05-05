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

package com.google.android.as.oss.fl.client;

import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.api.proto.TrainerResponse;
import com.google.android.as.oss.fl.api.proto.TrainingServiceGrpc;
import com.google.common.flogger.GoogleLogger;
import io.grpc.Channel;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Client class encapsulating low-level transport logic and providing a high-level API for
 * scheduling in-app training through PCS.
 */
@Singleton
public class TrainerClient {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Channel channel;
  private final Executor executor;

  @Inject
  TrainerClient(Channel channel, @FlExecutorQualifier Executor executor) {
    this.channel = channel;
    this.executor = executor;
  }

  public void scheduleTraining(TrainerOptions trainerOptions, TrainerCallback callback) {
    logger.atInfo().log(
        "Preparing to schedule training for population:%s session_name:%s",
        trainerOptions.getPopulationName(), trainerOptions.getSessionName());

    TrainingServiceGrpc.TrainingServiceStub stub = TrainingServiceGrpc.newStub(channel);
    stub.scheduleFederatedComputation(
        trainerOptions,
        new StreamObserver<TrainerResponse>() {
          @Override
          public void onNext(TrainerResponse value) {
            executor.execute(
                () -> {
                  switch (value.getResponseCode()) {
                    case RESPONSE_CODE_SUCCESS:
                      callback.onSuccess();
                      break;
                    case RESPONSE_CODE_UNSUPPORTED_JOB_TYPE:
                      callback.onError(
                          new IllegalArgumentException(
                              "Unsupported job type" + trainerOptions.getJobType().name()));
                      break;
                    default:
                      callback.onError(
                          new IllegalArgumentException(
                              "Unsupported response code" + value.getResponseCode().getNumber()));
                  }
                });
          }

          @Override
          public void onError(Throwable t) {
            executor.execute(() -> callback.onError(t));
          }

          @Override
          public void onCompleted() {}
        });
  }
}
