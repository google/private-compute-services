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

package com.google.android.as.oss.fl;

import com.google.android.as.oss.fl.Annotations.AsiPackageName;
import com.google.android.as.oss.fl.Annotations.ExampleStoreClientsInfo;
import com.google.android.as.oss.fl.Annotations.GppsPackageName;
import com.google.android.as.oss.fl.Annotations.ResultHandlingClientsInfo;
import com.google.common.collect.ImmutableMap;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
abstract class FederatedModule {
  private static final String ASI_CLIENT_NAME = "com.google.android.as";
  private static final String GPPS_CLIENT_NAME = "com.google.android.odad";

  @Provides
  @ExampleStoreClientsInfo
  static ImmutableMap<String, String> providePcsClientToExampleStoreActionMap() {
    return ImmutableMap.of(
        ASI_CLIENT_NAME,
        "com.google.android.apps.miphone.aiai.EXAMPLE_STORE_V1",
        GPPS_CLIENT_NAME,
        "com.google.android.apps.miphone.odad.EXAMPLE_STORE_V1");
  }

  @Provides
  @ResultHandlingClientsInfo
  static ImmutableMap<String, String> providePcsClientToResultHandlingActionMap() {
    return ImmutableMap.of(
        ASI_CLIENT_NAME,
        "com.google.android.apps.miphone.aiai.COMPUTATION_RESULT_V1",
        GPPS_CLIENT_NAME,
        "com.google.android.apps.miphone.odad.COMPUTATION_RESULT_V1");
  }

  @Provides
  @AsiPackageName
  static String provideAsiPackageName() {
    // This refers to the Android System Intelligence app.
    return ASI_CLIENT_NAME;
  }

  @Provides
  @GppsPackageName
  static String provideGppsPackageName() {
    // This refers to the Google Play Protect Service app.
    return GPPS_CLIENT_NAME;
  }
}
