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

package com.google.android.as.oss.networkusage.db.impl;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.Instant;
import java.util.List;

/** Data Access Object (DAO) for {@link NetworkUsageEntity}. */
@Dao
public interface NetworkUsageEntitiesDao {
  @Query("SELECT COUNT(*) FROM NetworkUsageLog")
  int count();

  /** Returns the row number of the inserted entity. */
  @Insert
  ListenableFuture<Long> insert(NetworkUsageEntity networkUsageEntity);

  /** Returns the number of deleted rows, or -1 in case of failure. */
  @Query("DELETE FROM NetworkUsageLog WHERE creationTime <= :latestCreationTime")
  ListenableFuture<Integer> deleteAllBefore(Instant latestCreationTime);

  @Query("SELECT * FROM NetworkUsageLog WHERE id = :id")
  NetworkUsageEntity getNetworkUsageEntityWithId(int id);

  @Query("SELECT * FROM NetworkUsageLog")
  LiveData<List<NetworkUsageEntity>> getAll();
}
