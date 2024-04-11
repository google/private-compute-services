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
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_12)
  static ClientConfig provideAiCoreClient12ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:418124939180967388");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_13)
  static ClientConfig provideAiCoreClient13ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:15018369527000359173");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_14)
  static ClientConfig provideAiCoreClient14ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:10085173703611871103");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_15)
  static ClientConfig provideAiCoreClient15ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:14589082030786492895");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_16)
  static ClientConfig provideAiCoreClient16ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:5333321975141516928");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_17)
  static ClientConfig provideAiCoreClient17ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:9353767029546147385");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_18)
  static ClientConfig provideAiCoreClient18ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:10167985913044593434");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_19)
  static ClientConfig provideAiCoreClient19ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:3561907884583738100");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_20)
  static ClientConfig provideAiCoreClient20ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:4870111188580693201");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_21)
  static ClientConfig provideAiCoreClient21ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:6642565339740637386");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_22)
  static ClientConfig provideAiCoreClient22ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:9931783747856508885");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_23)
  static ClientConfig provideAiCoreClient23ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:5848825322855942324");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_24)
  static ClientConfig provideAiCoreClient24ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:4341791953025243445");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_25)
  static ClientConfig provideAiCoreClient25ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:6417633745608261729");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_26)
  static ClientConfig provideAiCoreClient26ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:11720962012422846819");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_27)
  static ClientConfig provideAiCoreClient27ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:14254786987761682043");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_28)
  static ClientConfig provideAiCoreClient28ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:4027292349711707490");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_29)
  static ClientConfig provideAiCoreClient29ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:1558569612950046780");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_30)
  static ClientConfig provideAiCoreClient30ClientConfig() {
    return ClientConfig.create("com.google.android.aicore:6109265619551471570");
  }

  @Provides
  @Singleton
  static ProtoConversions provideProtoConversions(Map<Client, ClientConfig> clientToClientId) {
    return new ProtoConversions(ImmutableMap.copyOf(clientToClientId));
  }

  private ProtoConversionsModule() {}
}
