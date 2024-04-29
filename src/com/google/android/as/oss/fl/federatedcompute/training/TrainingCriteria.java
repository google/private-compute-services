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

import com.google.android.as.oss.fl.api.proto.TrainerOptions;

/** Interface to define conditions under which a population's training can be scheduled. */
public interface TrainingCriteria {
  /** Returns the {@link TrainerOptions} that should be used to schedule the training. */
  public TrainerOptions getTrainerOptions();

  /** Returns whether the training can be scheduled. */
  public boolean canScheduleTraining();
}
