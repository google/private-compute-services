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

package com.google.android.as.oss.fl.fc.service.scheduler.endorsementoptions;

import com.google.common.collect.ImmutableMap;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** Binds EndorsementOptionsProvider for release builds. */
@Module
@InstallIn(SingletonComponent.class)
class ReleaseModule {
  private static final ImmutableMap<EndorsementClientType, Integer> RESOURCE_ID_MAP =
      ImmutableMap.of(
          EndorsementClientType.PRIVATE_COMPUTE_SERVICES_DEFAULT_KEY,
          R.raw.pcs_release_endorsement_options);

  @Provides
  static EndorsementOptionsProvider provideEndorsementOptionsProvider() {
    return new EndorsementOptionsProviderImpl(RESOURCE_ID_MAP);
  }

  private ReleaseModule() {}
}
