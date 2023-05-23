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

package com.google.android.as.oss.networkusage.db.impl;

import android.content.Context;
import androidx.room.Room;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntityTtl;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoSet;
import java.time.Duration;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

/** The module that provides the Room Database for PCS's Network Usage Log. */
@Module
@InstallIn(SingletonComponent.class)
abstract class NetworkUsageLogModule {
  private static final String DB_NAME = "network_usage_db";

  // Schedules a database cleanup task for expired entities at application startup.
  @Provides
  @IntoSet
  static PcsInitializer providePcsInitializer(@ApplicationContext Context appContext) {
    return () -> NetworkUsageLogTtlService.considerSchedule(appContext);
  }

  @Provides
  @Singleton
  static NetworkUsageLogDatabase provideNetworkUsageLogDatabase(
      @ApplicationContext Context context, @IoExecutorQualifier Executor ioExecutor) {
    return Room.databaseBuilder(context, NetworkUsageLogDatabase.class, DB_NAME)
        .setQueryExecutor(ioExecutor)
        .setTransactionExecutor(ioExecutor)
        // To avoid crashing, recreate the database in case the schema was upgraded without
        // providing a Migration.
        .fallbackToDestructiveMigration()
        .build();
  }

  @Provides
  @Singleton
  static NetworkUsageEntitiesDao provideNetworkUsageEntitiesDao(
      NetworkUsageLogDatabase networkUsageLogDatabase) {
    return networkUsageLogDatabase.dao();
  }

  @Binds
  abstract NetworkUsageLogRepository bindNetworkUsageLogRepository(
      NetworkUsageLogRepositoryImpl impl);

  @Provides
  @NetworkUsageEntityTtl
  static Duration provideNetworkUsageEntityTtl() {
    return Duration.ofDays(14);
  }
}
