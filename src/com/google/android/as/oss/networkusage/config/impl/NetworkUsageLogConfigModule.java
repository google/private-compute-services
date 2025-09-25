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

package com.google.android.as.oss.networkusage.config.impl;

import static com.google.android.as.oss.common.Executors.GENERAL_SINGLE_THREAD_EXECUTOR;

import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.config.FlagManagerFactory;
import com.google.android.as.oss.common.config.FlagNamespace;
import com.google.android.as.oss.networkusage.config.NetworkUsageLogConfig;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** Module that provides ConfigReader for Network Usage Log. */
@Module
@InstallIn(SingletonComponent.class)
public interface NetworkUsageLogConfigModule {
  @Provides
  static ConfigReader<NetworkUsageLogConfig> provideConfigReader(
      FlagManagerFactory flagManagerFactory) {
    return NetworkUsageLogConfigReader.create(
        flagManagerFactory.create(
            FlagNamespace.DEVICE_PERSONALIZATION_SERVICES, GENERAL_SINGLE_THREAD_EXECUTOR));
  }
}
