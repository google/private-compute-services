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

package com.google.android.as.oss.common.jobs;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.UserManager;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.BuildCompat;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.config.impl.PcsCommonConfig;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.fl.api.proto.TrainerOptions;
import com.google.android.as.oss.fl.federatedcompute.statsd.StatsdExampleStoreConnector;
import com.google.android.as.oss.fl.federatedcompute.statsd.config.StatsdConfig;
import com.google.android.as.oss.fl.federatedcompute.training.PopulationTrainingScheduler;
import com.google.android.as.oss.fl.federatedcompute.training.TrainingCriteria;
import com.google.android.as.oss.fl.federatedcompute.training.TrainingSchedulerCallback;
import com.google.android.as.oss.fl.populations.Population;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceValueReported;
import com.google.android.as.oss.logging.PcsStatsEnums.ValueMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.AndroidEntryPoint;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;

/**
 * Heartbeat service which (re-)schedules pcs-managed components that need periodic rescheduling.
 */
@AndroidEntryPoint(JobService.class)
public class HeartbeatService extends Hilt_HeartbeatService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final Duration JOB_INTERVAL = Duration.ofDays(1L);
  @VisibleForTesting static final int JOB_ID = 532808520;

  @Inject PopulationTrainingScheduler populationScheduler;
  @Inject PcsStatsLog pcsStatsLogger;
  @Inject ConfigReader<PcsCommonConfig> pcsCommonConfigReader;
  @Inject StatsdExampleStoreConnector statsdExampleStoreConnector;
  @Inject BuildFlavor buildFlavor;
  @Inject @ApplicationContext Context context;
  @Inject ConfigReader<StatsdConfig> statsdConfigReader;

  @Override
  public boolean onStartJob(JobParameters params) {
    logger.atFine().log("Scheduling jobs for configured population criteria.");
    if (!pcsCommonConfigReader.getConfig().enableHeartBeatJob()) {
      JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
      jobScheduler.cancel(params.getJobId());
      return false;
    }
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
            }),
        Optional.of(getAdditionalTrainingCriteria()));
    logScheduledJobsCount();
    return true;
  }

  private Set<TrainingCriteria> getAdditionalTrainingCriteria() {
    if (!BuildCompat.isAtLeastU()
        || !statsdConfigReader.getConfig().enableMetricWisePopulations()) {
      return ImmutableSet.of();
    }
    Set<TrainingCriteria> trainingCriteria = new HashSet<>();
    List<Long> restrictedMetricIds = statsdExampleStoreConnector.getRestrictedMetricIds();
    for (Long restrictedMetricId : restrictedMetricIds) {
      trainingCriteria.add(
          new TrainingCriteria() {
            @Override
            public TrainerOptions getTrainerOptions() {
              return PopulationTrainingScheduler.buildTrainerOpts(
                  String.format(
                      "%s/%s",
                      buildFlavor.isRelease()
                          ? Population.PLATFORM_LOGGING.populationName()
                          : Population.PLATFORM_LOGGING_DEV.populationName(),
                      restrictedMetricId));
            }

            @Override
            public boolean canScheduleTraining() {
              UserManager userManager = context.getSystemService(UserManager.class);
              if (userManager == null) {
                return false;
              }
              return BuildCompat.isAtLeastU()
                  && userManager.isSystemUser()
                  && statsdConfigReader.getConfig().enablePlatformLogging()
                  && statsdConfigReader.getConfig().enableMetricWisePopulations();
            }
          });
    }
    return trainingCriteria;
  }

  private void logScheduledJobsCount() {
    JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
    pcsStatsLogger.logIntelligenceValueReported(
        IntelligenceValueReported.newBuilder()
            .setValueMetricId(ValueMetricId.PCS_NUM_JOBS_SCHEDULED_COUNT)
            .setValue(jobScheduler.getAllPendingJobs().size())
            .build());
  }

  @Override
  public boolean onStopJob(JobParameters params) {
    return false; // Do not retry
  }

  /** Schedules the heartbeat service if it hasn't been scheduled already. */
  public static void considerSchedule(Context context) {
    JobScheduler jobScheduler =
        (JobScheduler) Preconditions.checkNotNull(context.getSystemService(JOB_SCHEDULER_SERVICE));

    if (jobScheduler.getPendingJob(JOB_ID) != null) {
      logger.atFine().log("HeartbeatService already scheduled.");
      return;
    }

    int resultCode =
        jobScheduler.schedule(
            new JobInfo.Builder(JOB_ID, new ComponentName(context, HeartbeatService.class))
                .setPeriodic(JOB_INTERVAL.toMillis())
                .setRequiresDeviceIdle(true)
                .setRequiresCharging(true)
                .setPersisted(true)
                .build());

    if (resultCode == JobScheduler.RESULT_SUCCESS) {
      logger.atInfo().log("Scheduled HeartbeatService");
    } else {
      logger.atWarning().log(
          "Failed to schedule HeartbeatService with error code = %d", resultCode);
    }
  }
}
