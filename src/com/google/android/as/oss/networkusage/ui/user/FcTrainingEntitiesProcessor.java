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

package com.google.android.as.oss.networkusage.ui.user;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.FC_CONNECTION_TYPES;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

import android.util.Pair;
import com.google.android.as.oss.networkusage.api.proto.ConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.FlConnectionKey;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link EntityListProcessor} that merges FC training items of the same task.
 *
 * <p>A group of FC related {@link NetworkUsageItemWrapper} is valid if items with the same runId
 * have the same feature and package names. A group of items without any feature/package name pair
 * is also valid and these are classified as check in items.
 */
class FcTrainingEntitiesProcessor implements EntityListProcessor {

  @Override
  public ImmutableList<NetworkUsageItemWrapper> process(
      ImmutableList<NetworkUsageItemWrapper> items) {
    // Non Fc related items such as HTTP and Pir network Logs
    ImmutableList<NetworkUsageItemWrapper> nonFcTrainingItems =
        items.stream().filter(item -> !isPartialFcItem(item)).collect(toImmutableList());

    ImmutableList<NetworkUsageItemWrapper> partialFcTrainingItems =
        items.stream()
            .filter(FcTrainingEntitiesProcessor::isPartialFcItem)
            .collect(toImmutableList());

    ImmutableList<NetworkUsageItemWrapper> mergedFcTrainingItems =
        Multimaps.index(getAllEntities(partialFcTrainingItems), NetworkUsageEntity::fcRunId)
            .asMap()
            .values()
            .stream()
            .map(FcTrainingEntitiesProcessor::mergeFcItems)
            .collect(toImmutableList());

    return ImmutableList.<NetworkUsageItemWrapper>builder()
        .addAll(mergedFcTrainingItems)
        .addAll(nonFcTrainingItems)
        .build();
  }

  /**
   * Merges FC training items of the same task.
   *
   * @param fcEntitiesOfSameTask a collection of FC training items of the same task.
   */
  private static NetworkUsageItemWrapper mergeFcItems(
      Collection<NetworkUsageEntity> fcEntitiesOfSameTask) {
    Set<Pair<String, String>> featureSet = extractConnectionDetails(fcEntitiesOfSameTask);
    checkArgument(validateFcEntityGroup(featureSet), "Unexpected logs found for FC entities.");

    ConnectionDetails.Builder baseConnectionDetails = getBaseConnectionDetails(featureSet);

    ImmutableList.Builder<NetworkUsageEntity> builder = ImmutableList.builder();
    // Sync connection details
    if (featureSet.isEmpty()) {
      builder.addAll(
          fcEntitiesOfSameTask.stream()
              .map(
                  entity ->
                      entity.toBuilder()
                          .setConnectionDetails(baseConnectionDetails.build())
                          .build())
              .collect(toImmutableList()));
    } else {
      builder.addAll(
          fcEntitiesOfSameTask.stream()
              .map(
                  entity ->
                      entity.toBuilder()
                          .setConnectionDetails(
                              baseConnectionDetails
                                  .setType(entity.connectionDetails().type())
                                  .build())
                          .build())
              .collect(toImmutableList()));
    }
    return new NetworkUsageItemWrapper(builder.build());
  }

  private static boolean validateFcEntityGroup(Set<Pair<String, String>> featureSet) {
    return featureSet.size() <= 1;
  }

  // Returns the base {@link ConnectionDetails} for a group of FC items with the same run ID.
  private static ConnectionDetails.Builder getBaseConnectionDetails(
      Set<Pair<String, String>> featureSet) {
    return !featureSet.isEmpty()
        ? ConnectionDetails.builder()
            .setConnectionKey(
                ConnectionKey.newBuilder()
                    .setFlConnectionKey(
                        FlConnectionKey.newBuilder()
                            .setFeatureName(featureSet.iterator().next().first)
                            .build())
                    .build())
            .setPackageName(featureSet.iterator().next().second)
        : NetworkUsageLogUtils.createFcCheckInConnectionDetails().toBuilder();
  }

  // Returns all pairs of feature names and package in a group of FC items with the same run ID.
  private static Set<Pair<String, String>> extractConnectionDetails(
      Collection<NetworkUsageEntity> fcEntitiesOfSameTask) {
    Set<Pair<String, String>> featureSet = new HashSet<>();
    for (NetworkUsageEntity entity : fcEntitiesOfSameTask) {
      if (entity.connectionDetails().connectionKey().getFlConnectionKey().hasFeatureName()
          && !entity
              .connectionDetails()
              .connectionKey()
              .getFlConnectionKey()
              .getFeatureName()
              .isEmpty()) {
        featureSet.add(
            Pair.create(
                entity.connectionDetails().connectionKey().getFlConnectionKey().getFeatureName(),
                entity.connectionDetails().packageName()));
      }
    }
    return featureSet;
  }

  // Returns all NetworkUsageEntity related to federated computations
  private static ImmutableList<NetworkUsageEntity> getAllEntities(
      ImmutableList<NetworkUsageItemWrapper> itemsWrappers) {
    ImmutableList.Builder<NetworkUsageEntity> entityList = ImmutableList.builder();
    itemsWrappers.forEach(itemWrapper -> entityList.addAll(itemWrapper.networkUsageEntities()));
    return entityList.build();
  }

  private static boolean isPartialFcItem(NetworkUsageItemWrapper item) {
    return FC_CONNECTION_TYPES.contains(item.connectionDetails().type());
  }
}
