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

package com.google.android.as.oss.pd.common;

import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.common.collect.ImmutableBiMap;
import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import java.util.Map;
import javax.inject.Singleton;

/** Convenience module for providing {@link ProtoConversions}. */
@Module
@InstallIn(SingletonComponent.class)
final class ProtoConversionsModule {

  @MapKey
  @interface ClientBiMapKey {
    Client value();
  }

  @Provides
  @IntoMap
  @ClientBiMapKey(Client.PLAY_PROTECT_SERVICE)
  static String providePlayProtectClientId() {
    return "com.google.android.odad";
  }

  @Provides
  @IntoMap
  @ClientBiMapKey(Client.SUSPICIOUS_MESSAGE_ALERTS)
  static String provideAsiClientId() {
    return "com.google.android.as";
  }

  @Provides
  @Singleton
  static ProtoConversions provideProtoConversions(Map<Client, String> clientToClientId) {
    return new ProtoConversions(ImmutableBiMap.copyOf(clientToClientId));
  }

  private ProtoConversionsModule() {}
}
