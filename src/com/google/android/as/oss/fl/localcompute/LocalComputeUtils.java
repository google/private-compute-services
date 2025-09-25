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

package com.google.android.as.oss.fl.localcompute;

import android.net.Uri;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Optional;
import java.util.concurrent.Executor;

/** Provides util functions for localcompute tasks. */
public final class LocalComputeUtils {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public static ListenableFuture<Boolean> handleLocalComputeOutput(
      InAppTrainerOptions trainerOptions,
      String clientName,
      Optional<LocalComputeResourceManager> resourceManager) {
    Uri personalizationPlanUri = trainerOptions.getPersonalizationPlan();
    Uri inputDirectoryUri = trainerOptions.getInputDirectory();
    Uri outputDirectoryUri = trainerOptions.getOutputDirectory();
    String sessionName = trainerOptions.getSessionName();
    if (personalizationPlanUri != null
        && inputDirectoryUri != null
        && outputDirectoryUri != null
        && resourceManager.isPresent()) {
      return resourceManager
          .get()
          .cleanResourceAtResultHandling(sessionName, inputDirectoryUri, outputDirectoryUri);
    }

    return Futures.immediateFailedFuture(
        new IllegalArgumentException(
            "Local computation input or output is null or the resource manager is not present."));
  }

  public static void prepareLocalComputeResourcesAtScheduling(
      TrainerOptions trainerOptions,
      Optional<LocalComputeResourceManager> resourceManager,
      Executor executor) {
    String sessionName = trainerOptions.getSessionName();
    Uri originalPlanUri = Uri.parse(trainerOptions.getLocalComputationPlanUri());
    Uri originalInputDirUri = Uri.parse(trainerOptions.getInputDirectoryUri());
    FluentFuture.from(
            resourceManager
                .get()
                .prepareResourceAtScheduling(sessionName, originalPlanUri, originalInputDirUri))
        .addCallback(
            new FutureCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  logger.atFine().log(
                      "Successfully prepared resources for session %s at scheduling.", sessionName);
                } else {
                  logger.atWarning().log(
                      "Failed to prepare resources for session %s at scheduling.", sessionName);
                }
              }

              @Override
              public void onFailure(Throwable t) {
                logger.atWarning().withCause(t).log(
                    "Failed to prepare resources for session %s at scheduling.", sessionName);
              }
            },
            executor);
  }

  public static void cleanLocalComputationResourcesAtCancellation(
      TrainerOptions trainerOptions,
      Optional<LocalComputeResourceManager> resourceManager,
      Executor executor) {
    String sessionName = trainerOptions.getSessionName();
    FluentFuture.from(resourceManager.get().cleanResourceAtCancellation(sessionName))
        .addCallback(
            new FutureCallback<Boolean>() {
              @Override
              public void onSuccess(Boolean result) {
                if (result) {
                  logger.atFine().log(
                      "Successfully cleaned resources for session %s at cancellation.",
                      sessionName);
                } else {
                  logger.atWarning().log(
                      "Failed to clean resources for session %s at cancellation.", sessionName);
                }
              }

              @Override
              public void onFailure(Throwable t) {
                logger.atWarning().withCause(t).log(
                    "Failed to clean resources for session %s at cancellation.", sessionName);
              }
            },
            executor);
  }

  private LocalComputeUtils() {}
}
