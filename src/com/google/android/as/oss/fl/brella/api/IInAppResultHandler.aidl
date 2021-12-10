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

package com.google.android.as.oss.fl.brella.api;

import com.google.android.as.oss.fl.brella.api.IStatusCallback;
import com.google.android.as.oss.fl.brella.api.InAppTrainerOptions;
import com.google.android.as.oss.fl.brella.api.ExampleConsumption;

/**
 * The interface used to communicate with apps' implementation of
 * ResultHandlingService.
 */
interface IInAppResultHandler {

  // reserved transcation number 0;

  /**
   * Returns the version of the interface.
   * NOTE: getInterfaceVersion is reserved internally, and unfortunately we
   * don't have access to it.
   */
  int getVersion() = 1;

  /**
   * The app implements this method to handle results.
   * When this method is available, getVersion() returns a value >= 1.
   *
   * @param trainerOptions InAppTrainerOptions to identify a training task
   * @param success Whether the training task has succeeded or not
   * @param exampleConsumptionList A list of ExampleConsumption which records
   *        which examples have been used in the training
   * @param IStatusCallback A callback which is used to tell federated compute
   *        if the user has finished handling the output files or an error has
   *        encountered
   *
   * NOTE: Oneway since we do not control the implementation running in the app.
   * Without oneway calls, the app could throw a RuntimeException that could
   * bring the training process down. Without 'oneway', an app could also block
   * the training thread, making it harder to properly implement timeout
   * behavior.
   */
  oneway void handleResult(in InAppTrainerOptions trainerOptions,
      boolean success, in List<ExampleConsumption> exampleConsumptionList,
      in IStatusCallback statusCallback) = 2;
}
