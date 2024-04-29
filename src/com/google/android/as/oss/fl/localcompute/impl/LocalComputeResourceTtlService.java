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

package com.google.android.as.oss.fl.localcompute.impl;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import com.google.android.as.oss.fl.localcompute.LocalComputeResourceManager;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.Duration;
import java.util.Optional;
import javax.inject.Inject;

/** Invokes the clean-up job for leftover local compute resources periodlically */
@AndroidEntryPoint(JobService.class)
public class LocalComputeResourceTtlService extends Hilt_LocalComputeResourceTtlService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final long JOB_INTERVAL_MILLIS = Duration.ofHours(12).toMillis();
  @VisibleForTesting static final int JOB_ID = 452613538; // first committed CL number

  @Inject Optional<LocalComputeResourceManager> resourceManager;

  @Override
  public boolean onStartJob(JobParameters jobParameters) {
    logger.atInfo().log("Starting local compute resource clean-up routine job.");

    if (resourceManager.isPresent()) {
      ListenableFuture<Void> future = resourceManager.get().cleanResourceAtRoutineJob();
      future.addListener(
          () -> {
            logger.atInfo().log("Finished local compute resource clean-up routine job.");
            jobFinished(jobParameters, /* wantsReschedule= */ false);
          },
          MoreExecutors.directExecutor());
    }

    return true;
  }

  @Override
  public boolean onStopJob(JobParameters jobParameters) {
    return false; // Do not retry
  }

  static void scheduleCleanUpRoutineJob(Context context) {
    JobScheduler jobScheduler =
        (JobScheduler)
            Preconditions.checkNotNull(context.getSystemService(Context.JOB_SCHEDULER_SERVICE));

    if (jobScheduler.getPendingJob(JOB_ID) != null) {
      logger.atInfo().log("LocalComputeResourceTtlService is already scheduled.");
      return;
    }

    int result =
        jobScheduler.schedule(
            new JobInfo.Builder(
                    JOB_ID, new ComponentName(context, LocalComputeResourceTtlService.class))
                .setPeriodic(JOB_INTERVAL_MILLIS)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(true)
                .build());

    if (result == JobScheduler.RESULT_SUCCESS) {
      logger.atInfo().log("Successfully scheduled LocalComputeResourceTtlService.");
    } else {
      logger.atWarning().log("Failed to schedule LocalComputeResourceTtlService.");
    }
  }
}
