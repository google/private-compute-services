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

package com.google.android.as.oss.logging.impl;

import com.google.android.as.oss.asi.common.logging.IntelligenceStatsLog;
import com.google.android.as.oss.logging.PcsStatsLog;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/** Convenience Sting module to provide {@link com.google.android.as.oss.logging.PcsStatsLog}. */
@Module
@InstallIn(SingletonComponent.class)
final class PcsStatsLoggerModule {
  @Provides
  @Singleton
  static PcsStatsLog provideIntelligenceStatsLog(IntelligenceStatsLog intelligenceStatsLog) {
    return new PcsStatsLoggerImpl(intelligenceStatsLog);
  }

  private PcsStatsLoggerModule() {}
}
