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

package com.google.android.as.oss.networkusage.db.impl;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntityTtl;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;

/** Cleanup service for the network usage database */
@AndroidEntryPoint(JobService.class)
public class NetworkUsageLogTtlService extends Hilt_NetworkUsageLogTtlService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final Duration JOB_INTERVAL = Duration.ofDays(1L);
  @VisibleForTesting static final int JOB_ID = 412882778;

  @Inject @NetworkUsageEntityTtl Duration entitiesTtl;
  @Inject NetworkUsageLogRepositoryImpl networkUsageLogRepository;

  @Override
  public boolean onStartJob(JobParameters params) {
    logger.atInfo().log("Starting DB clean-up job");

    Instant latestInstant;
    if (networkUsageLogRepository.isNetworkUsageLogEnabled()
        && networkUsageLogRepository.isUserOptedIn()) {
      latestInstant = Instant.now().minus(entitiesTtl);
    } else {
      latestInstant = Instant.now();
    }

    networkUsageLogRepository.deleteAllBefore(
        latestInstant,
        new FutureCallback<Integer>() {
          @Override
          public void onSuccess(Integer result) {
            if (result != -1) {
              logger.atFine().log("Successfully removed %d entities", result);
              jobFinished(params, /* wantsReschedule= */ false);
            } else {
              logger.atWarning().log("Failed to delete old entities");
              jobFinished(params, /* wantsReschedule= */ true);
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().withCause(t).log("Failed to delete entities.");
          }
        });

    return true; // This job is executing on a separate thread, so return true
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    return false; // Do not retry
  }

  static void considerSchedule(Context context) {
    JobScheduler jobScheduler =
        (JobScheduler)
            Preconditions.checkNotNull(context.getSystemService(Context.JOB_SCHEDULER_SERVICE));

    if (jobScheduler.getPendingJob(JOB_ID) != null) {
      logger.atInfo().log("NetworkUsageLogTtlService already scheduled.");
      return;
    }

    int resultCode =
        jobScheduler.schedule(
            new JobInfo.Builder(JOB_ID, new ComponentName(context, NetworkUsageLogTtlService.class))
                .setPeriodic(JOB_INTERVAL.toMillis())
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .build());

    if (resultCode == JobScheduler.RESULT_SUCCESS) {
      logger.atInfo().log("Scheduled NetworkUsageLogTtlService");
    } else {
      logger.atWarning().log(
          "Failed to schedule NetworkUsageLogTtlService with error code = %d", resultCode);
    }
  }
}
