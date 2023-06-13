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

package com.google.android.as.oss.fl.federatedcompute.statsd.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.Duration;
import java.util.Optional;
import javax.inject.Inject;

/** Training heartbeat service which schedules statsd populations every 24 hours. */
@AndroidEntryPoint(JobService.class)
@RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
public class StatsdTrainingSchedulerService extends Hilt_StatsdTrainingSchedulerService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final Duration JOB_INTERVAL = Duration.ofDays(1L);
  @VisibleForTesting static final int JOB_ID = 512927718;

  @Inject StatsdTrainingPopulationScheduler populationScheduler;

  @Override
  public boolean onStartJob(JobParameters params) {
    logger.atFine().log("Starting FA scheduler job for statsd.");
    populationScheduler.schedule(
        Optional.of(
            new TrainingSchedulerCallback() {
              @Override
              public void onTrainingScheduleSuccess() {
                logger.atInfo().log("Finished training schedule job.");
                jobFinished(params, /* wantsReschedule= */ false);
              }

              @Override
              public void onTrainingScheduleFailure(Throwable t) {
                logger.atWarning().withCause(t).log(
                    "Failure in scheduling training, rescheduling the job.");
                jobFinished(params, /* wantsReschedule= */ true);
              }
            }));
    return true;
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
      logger.atFine().log("StatsdTrainingSchedulerService already scheduled.");
      return;
    }

    int resultCode =
        jobScheduler.schedule(
            new JobInfo.Builder(
                    JOB_ID, new ComponentName(context, StatsdTrainingSchedulerService.class))
                .setPeriodic(JOB_INTERVAL.toMillis())
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .setPersisted(true)
                .build());

    if (resultCode == JobScheduler.RESULT_SUCCESS) {
      logger.atInfo().log("Scheduled StatsdTrainingSchedulerService");
    } else {
      logger.atWarning().log(
          "Failed to schedule StatsdTrainingSchedulerService with error code = %d", resultCode);
    }
  }
}
