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

package com.google.android.as.oss.common.jobs;

import android.content.Context;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.config.impl.PcsCommonConfig;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;

@Module
@InstallIn(SingletonComponent.class)
abstract class HeartbeatServiceModule {

  @Provides
  @IntoSet
  static PcsInitializer providePcsInitializer(
      @ApplicationContext Context appContext, ConfigReader<PcsCommonConfig> configReader) {
    return () -> {
      if (configReader.getConfig().enableHeartBeatJob()) {
        HeartbeatService.considerSchedule(appContext);
      }
    };
  }
}
