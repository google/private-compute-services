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

package com.google.android.as.oss;

import android.app.Application;
import android.content.Context;
import androidx.work.Configuration;
import androidx.work.DelegatingWorkerFactory;
import androidx.work.WorkerFactory;
import com.google.android.as.oss.common.ExecutorAnnotations.WorkManagerExecutorQualifier;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

/** Main application class for Private Compute Services. */
@HiltAndroidApp(Application.class)
public class PrivateComputeServicesApplication extends Hilt_PrivateComputeServicesApplication
    implements Configuration.Provider {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Inject @ApplicationContext Context context;
  @Inject Set<PcsInitializer> initializers;
  @Inject Set<WorkerFactory> workerFactories;
  @Inject @WorkManagerExecutorQualifier Executor workManagerExecutor;

  @Override
  public void onCreate() {
    super.onCreate();

    logger.atInfo().log("PrivateComputeServicesApplication#onCreate");

    // Run initializers in order of their priorities.
    initializers.stream()
        .sorted(Comparator.comparing(PcsInitializer::getPriority).reversed())
        .forEach(PcsInitializer::run);
  }

  /** Loads the configuration used by WorkManager. */
  @Override
  public Configuration getWorkManagerConfiguration() {
    DelegatingWorkerFactory delegatingFactory = new DelegatingWorkerFactory();
    workerFactories.forEach(delegatingFactory::addFactory);
    return new Configuration.Builder()
        .setWorkerFactory(delegatingFactory)
        .setExecutor(workManagerExecutor)
        .build();
  }
}
