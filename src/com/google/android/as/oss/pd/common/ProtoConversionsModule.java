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
import com.google.common.collect.ImmutableMap;
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
  @interface ClientMapKey {
    Client value();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.PLAY_PROTECT_SERVICE)
  static ClientConfig providePlayProtectClientConfig() {
    return ClientConfig.create("com.google.android.odad");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.PLAY_PROTECT_SERVICE_CORE_DEFAULT)
  static ClientConfig providePlayProtectCoreDefaultClientConfig() {
    return ClientConfig.create("com.google.android.odad:2793571637033546290");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.PLAY_PROTECT_SERVICE_PVM_DEFAULT)
  static ClientConfig providePlayProtectPvmDefaultClientConfig() {
    return ClientConfig.create("com.google.android.odad:2525461103339185322");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_TEXT_INPUT)
  static ClientConfig provideAiCoreTextInputClientConfig() {
    return ClientConfig.create("com.google.android.aicore:3649180271731021675");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_TEXT_OUTPUT)
  static ClientConfig provideAiCoreTextOutputClientConfig() {
    return ClientConfig.create("com.google.android.aicore:7923848966216590666");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_IMAGE_INPUT)
  static ClientConfig provideAiCoreImageInputClientConfig() {
    return ClientConfig.create("com.google.android.aicore:6120135725815620389");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_IMAGE_OUTPUT)
  static ClientConfig provideAiCoreImageOutputClientConfig() {
    return ClientConfig.create("com.google.android.aicore:16223496253676012401");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_MESSAGES_TEXT)
  static ClientConfig provideAiCoreMessagesTextClientConfig() {
    return ClientConfig.create("com.google.android.aicore:4970947506931743799");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CHROME_SUMMARIZATION_OUTPUT)
  static ClientConfig provideAiCoreChromeSummarizationOutputClientConfig() {
    return ClientConfig.create("com.google.android.aicore:8519285862245230442");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_PROTECTED_DOWNLOAD)
  static ClientConfig provideAiCoreProtectedDownloadClientConfig() {
    return ClientConfig.create("com.google.android.aicore:11791126134479005147");
  }

  @Provides
  @Singleton
  static ProtoConversions provideProtoConversions(Map<Client, ClientConfig> clientToClientId) {
    return new ProtoConversions(ImmutableMap.copyOf(clientToClientId));
  }

  private ProtoConversionsModule() {}
}
