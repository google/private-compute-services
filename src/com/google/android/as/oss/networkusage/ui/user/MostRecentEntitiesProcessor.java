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

import static com.google.common.collect.AndroidAccessToCollectors.toImmutableList;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Filters entity list to contain only entities created after the specified duration. */
public class MostRecentEntitiesProcessor implements EntityListProcessor {
  private final Duration entitiesTtl;

  public MostRecentEntitiesProcessor(Duration entitiesTtl) {
    this.entitiesTtl = entitiesTtl;
  }

  @Override
  public ImmutableList<NetworkUsageItemWrapper> process(
      ImmutableList<NetworkUsageItemWrapper> items) {
    Instant earliestPermittedDate = Instant.now().truncatedTo(ChronoUnit.DAYS).minus(entitiesTtl);
    return items.stream()
        .map(wrapper -> wrapper.newerEntitiesOnly(earliestPermittedDate))
        .filter(filteredWrapper -> !filteredWrapper.networkUsageEntities().isEmpty())
        .collect(toImmutableList());
  }
}
