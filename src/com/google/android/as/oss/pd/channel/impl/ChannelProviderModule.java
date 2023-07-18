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

package com.google.android.as.oss.pd.channel.impl;

import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.channel.ChannelProvider;
import com.google.common.collect.ImmutableMap;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Singleton;

/** Registers a {@link ChannelProvider} using a predefined map of host names. */
@Module
@InstallIn(SingletonComponent.class)
abstract class ChannelProviderModule {

  private static final ImmutableMap<Client, String> HOST_NAMES =
      ImmutableMap.of(
          Client.SUSPICIOUS_MESSAGE_ALERTS, "ondevicesafety-pa.googleapis.com",
          Client.PLAY_PROTECT_SERVICE, "ondevicesafety-pa.googleapis.com",
          Client.PLAY_PROTECT_SERVICE_CORE_DEFAULT, "ondevicesafety-pa.googleapis.com");

  @Singleton
  @Provides
  static ChannelProvider provideChannelProvider() {
    return new ChannelProviderImpl(HOST_NAMES);
  }
}
