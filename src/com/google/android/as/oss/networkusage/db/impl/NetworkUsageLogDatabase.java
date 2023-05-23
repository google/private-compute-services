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

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.RenameColumn;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.AutoMigrationSpec;
import com.google.android.as.oss.networkusage.db.Converters;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.impl.NetworkUsageLogDatabase.NulAutoMigration;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.time.Instant;

/** Room database implementation for storing PCS's Network Usage Log. */
@Database(
    entities = {NetworkUsageEntity.class},
    version = 3,
    autoMigrations = {
      @AutoMigration(from = 1, to = 2),
      @AutoMigration(from = 2, to = 3, spec = NulAutoMigration.class)
    })
@TypeConverters({Converters.class})
public abstract class NetworkUsageLogDatabase extends RoomDatabase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public abstract NetworkUsageEntitiesDao dao();

  /** Returns true if the insert operation succeeded, false otherwise. */
  // TODO: Handle exceptions properly
  ListenableFuture<Boolean> insertNetworkUsageEntity(NetworkUsageEntity entity) {
    logger.atInfo().log(
        "Inserting new NetworkUsageEntity of type %s", entity.connectionDetails().type());
    return FluentFuture.from(dao().insert(entity))
        .catching(Throwable.class, any -> -1L, MoreExecutors.directExecutor())
        .transform(rowId -> (rowId != -1L), MoreExecutors.directExecutor());
  }

  ListenableFuture<Integer> deleteAllBefore(Instant latestInstant) {
    return dao().deleteAllBefore(latestInstant);
  }

  @RenameColumn.Entries(
      @RenameColumn(
          tableName = "NetworkUsageLog",
          fromColumnName = "size",
          toColumnName = "downloadSize"))
  static class NulAutoMigration implements AutoMigrationSpec {}
}
