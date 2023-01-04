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

import android.content.Context;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.fcp.client.InAppTrainer;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.fcp.client.tasks.OnFailureListener;
import com.google.fcp.client.tasks.OnSuccessListener;
import com.google.fcp.client.tasks.Task;
import java.util.concurrent.Executor;

/** Wrapper that schedules training for intended population. */
public interface TrainingScheduler {
  void scheduleTraining(
      TrainerOptions trainerOptions,
      OnSuccessListener<Void> successListener,
      OnFailureListener failureListener);

  void disableTraining(
      TrainerOptions trainerOptions,
      OnSuccessListener<Void> successListener,
      OnFailureListener failureListener);

  /** Supplier that provides an instance of InAppTrainer to performing training. */
  interface TrainerSupplier {
    Task<InAppTrainer> get(Context context, Executor executor, InAppTrainerOptions opts);
  }
}
