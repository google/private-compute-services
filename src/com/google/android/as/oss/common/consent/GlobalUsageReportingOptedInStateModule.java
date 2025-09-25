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

package com.google.android.as.oss.common.consent;

import android.content.Context;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/** The module to provide {@link UsageReportingOptedInState}. */
@Module
@InstallIn(SingletonComponent.class)
abstract class GlobalUsageReportingOptedInStateModule {
  @Binds
  @Singleton
  abstract UsageReportingOptedInState provideUsageReportingOptedInState(
      GlobalUsageReportingOptedInStateImpl usageReportingOptedInStateImpl);

  @Provides
  @Singleton
  static GlobalUsageReportingOptedInStateImpl provideGlobalUsageReportingOptedInStateImpl(
      @ApplicationContext Context context) {
    return new GlobalUsageReportingOptedInStateImpl(context);
  }
}
