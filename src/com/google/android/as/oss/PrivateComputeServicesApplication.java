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

package com.google.android.as.oss;

import android.app.Application;
import android.content.Context;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.HiltAndroidApp;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;

@HiltAndroidApp(Application.class)
public class PrivateComputeServicesApplication extends Hilt_PrivateComputeServicesApplication {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Inject @ApplicationContext Context context;
  @Inject Set<PcsInitializer> initializers;

  @Override
  public void onCreate() {
    super.onCreate();

    logger.atInfo().log("PrivateComputeServicesApplication#onCreate");

    // Run initializers in order of their priorities.
    initializers.stream()
        .sorted(Comparator.comparing(PcsInitializer::getPriority).reversed())
        .forEach(PcsInitializer::run);
  }
}
