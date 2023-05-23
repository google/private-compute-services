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

import com.google.android.as.oss.networkusage.db.NetworkUsageEntityTtl;
import com.google.common.collect.ImmutableList;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.time.Duration;
import javax.inject.Singleton;

/** The module that provides post-processors for the entity list fetched from the database. */
@Module
@InstallIn(SingletonComponent.class)
abstract class EntityListProcessorsModule {

  @Provides
  @Singleton
  static ImmutableList<EntityListProcessor> provideEntityListProcessors(
      @NetworkUsageEntityTtl Duration entitiesTtl) {
    // Note: order of processors matters
    return ImmutableList.of(
        new MostRecentEntitiesProcessor(entitiesTtl),
        new FcTrainingEntitiesProcessor(),
        new AsiOnlyEntitiesProcessor(),
        new MergeSimilarEntitiesPerDayProcessor());
  }
}
