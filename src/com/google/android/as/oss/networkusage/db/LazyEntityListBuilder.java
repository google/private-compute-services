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

package com.google.android.as.oss.networkusage.db;

import static com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils.createFcCheckInConnectionDetails;
import static com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils.createFcCheckInNetworkUsageEntity;
import static com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils.createFcTrainingResultUploadConnectionDetails;
import static com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils.createFcTrainingResultUploadNetworkUsageEntity;
import static java.lang.Math.max;

import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A builder for the list of entities shown in the NetworkUsageLog UI, which caches the latest
 * values of DB queries atomically and constructs the new list when requested.
 */
public final class LazyEntityListBuilder {

  private final AtomicLong totalFcCheckInDownloadSize = new AtomicLong(-1);
  private final AtomicLong totalFcResultUploadSize = new AtomicLong(-1);
  private final AtomicBoolean isNonAggregatedEntitySetInitialized = new AtomicBoolean(false);
  private final SortedSet<NetworkUsageEntity> nonAggregatedEntitySet =
      Collections.synchronizedSortedSet(new TreeSet<>(NetworkUsageEntity.BY_LATEST_TIMESTAMP));
  private final MutableLiveData<ImmutableList<NetworkUsageEntity>> mergedEntityListLiveData;

  public static LazyEntityListBuilder create(
      LiveData<List<NetworkUsageEntity>> nonAggregatedEntityListLiveData,
      LiveData<Long> totalFcCheckInDownloadSizeLiveData,
      LiveData<Long> totalFcResultUploadSizeLiveData) {

    MediatorLiveData<ImmutableList<NetworkUsageEntity>> liveDataMerger = new MediatorLiveData<>();
    LazyEntityListBuilder entityListBuilder = new LazyEntityListBuilder(liveDataMerger);

    liveDataMerger.addSource(
        nonAggregatedEntityListLiveData, entityListBuilder::updateNonAggregatedEntityList);
    liveDataMerger.addSource(
        totalFcCheckInDownloadSizeLiveData, entityListBuilder::updateTotalFcCheckInDownloadSize);
    liveDataMerger.addSource(
        totalFcResultUploadSizeLiveData, entityListBuilder::updateTotalFcResultUploadSize);

    return entityListBuilder;
  }

  public LiveData<ImmutableList<NetworkUsageEntity>> getMergedEntityListLiveData() {
    return mergedEntityListLiveData;
  }

  @VisibleForTesting
  void updateNonAggregatedEntityList(List<NetworkUsageEntity> newList) {
    if (newList != null) {
      nonAggregatedEntitySet.addAll(newList);
    }
    isNonAggregatedEntitySetInitialized.set(true);
    considerNotifyObservers();
  }

  @VisibleForTesting
  void updateTotalFcCheckInDownloadSize(Long newValue) {
    totalFcCheckInDownloadSize.updateAndGet(
        current -> max(current, newValue == null ? 0 : newValue));
    considerNotifyObservers();
  }

  @VisibleForTesting
  void updateTotalFcResultUploadSize(Long newValue) {
    totalFcResultUploadSize.updateAndGet(current -> max(current, newValue == null ? 0 : newValue));
    considerNotifyObservers();
  }

  // Checks that each of the joining values has been assigned at least once.
  @VisibleForTesting
  boolean isInitialized() {
    return totalFcCheckInDownloadSize.get() >= 0
        && totalFcResultUploadSize.get() >= 0
        && isNonAggregatedEntitySetInitialized.get();
  }

  // Builds the list of NetworkUsageEntities that should be shown in the UI.
  @VisibleForTesting
  ImmutableList<NetworkUsageEntity> buildList() {
    ImmutableList.Builder<NetworkUsageEntity> builder = ImmutableList.builder();

    if (totalFcResultUploadSize.get() > 0) {
      builder.add(
          createFcTrainingResultUploadNetworkUsageEntity(
              createFcTrainingResultUploadConnectionDetails("all"),
              Status.SUCCEEDED,
              totalFcResultUploadSize.get()));
    }

    if (totalFcCheckInDownloadSize.get() > 0) {
      builder.add(
          createFcCheckInNetworkUsageEntity(
              createFcCheckInConnectionDetails("all"), totalFcCheckInDownloadSize.get()));
    }

    builder.addAll(nonAggregatedEntitySet);

    return builder.build();
  }

  // Notify observers of the mergedEntityListLiveData of changes.
  private void considerNotifyObservers() {
    if (isInitialized() && mergedEntityListLiveData != null) {
      mergedEntityListLiveData.setValue(buildList());
    }
  }

  private LazyEntityListBuilder(
      MediatorLiveData<ImmutableList<NetworkUsageEntity>> mergedEntityListLiveData) {
    this.mergedEntityListLiveData = mergedEntityListLiveData;
  }
}
