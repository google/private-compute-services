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

package com.google.android.as.oss.networkusage.ui.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import android.util.Pair;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import java.time.LocalDate;
import java.util.Collection;

/**
 * Merges groups of similar items in the list into one {@link NetworkUsageEntity} that has the total
 * size and latest creationDate of the group. Items are considered similar if they have the same
 * {@link ConnectionDetails}. This eliminates unnecessarily repeated entries for a single update,
 * for example when an update consists of multiple downloads.
 */
class MergeSimilarEntitiesPerDayProcessor implements EntityListProcessor {

  @Override
  public ImmutableList<NetworkUsageItemWrapper> process(
      ImmutableList<NetworkUsageItemWrapper> networkUsageItems) {
    ImmutableListMultimap<Pair<ConnectionDetails, LocalDate>, NetworkUsageItemWrapper>
        groupedItems =
            Multimaps.index(
                networkUsageItems,
                item ->
                    Pair.create(
                        item.connectionDetails(),
                        NetworkUsageItemUtils.getLocalDate(item.latestCreationTime())
                            .atStartOfDay()
                            .toLocalDate()));

    return groupedItems.asMap().values().stream().map(this::mergeItems).collect(toImmutableList());
  }

  // TODO: display the group of merged entities in NetworkUsageItemDetailsActivity
  NetworkUsageItemWrapper mergeItems(Collection<NetworkUsageItemWrapper> networkUsageItems) {
    checkArgument(!networkUsageItems.isEmpty());
    return networkUsageItems.stream()
        .reduce(
            (item1, item2) ->
                new NetworkUsageItemWrapper(
                    ImmutableList.<NetworkUsageEntity>builder()
                        .addAll(item1.networkUsageEntities())
                        .addAll(item2.networkUsageEntities())
                        .build()))
        .orElseThrow(AssertionError::new);
  }
}
