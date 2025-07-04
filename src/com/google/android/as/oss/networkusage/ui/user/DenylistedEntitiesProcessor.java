/*
 * Copyright 2024 Google LLC
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

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;

/** Filters entities on denylist from the {@link NetworkUsageItemWrapper} list. */
class DenylistedEntitiesProcessor implements EntityListProcessor {
  static final String GPPS_PACKAGE_NAME = "com.google.android.odad";
  static final ImmutableList<String> DENYLISTED_PACKAGE_NAMES = ImmutableList.of(GPPS_PACKAGE_NAME);

  @Override
  public ImmutableList<NetworkUsageItemWrapper> process(
      ImmutableList<NetworkUsageItemWrapper> networkUsageItems) {
    return networkUsageItems.stream()
        .filter(
            entity -> !DENYLISTED_PACKAGE_NAMES.contains(entity.connectionDetails().packageName()))
        .collect(toImmutableList());
  }
}
