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

package com.google.android.as.oss.pd.common;

import com.google.android.as.oss.common.config.FlagNamespace;
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
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:14589082030786492895")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_14589082030786492895"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_PROTECTED_DOWNLOAD)
  static ClientConfig provideAiCoreProtectedDownloadClientConfig() {
    return ClientConfig.create("com.google.android.aicore:11791126134479005147");
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_16)
  static ClientConfig provideAiCoreClient16ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:5333321975141516928")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_5333321975141516928"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_17)
  static ClientConfig provideAiCoreClient17ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:9353767029546147385")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_9353767029546147385"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_18)
  static ClientConfig provideAiCoreClient18ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:10167985913044593434")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_10167985913044593434"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_19)
  static ClientConfig provideAiCoreClient19ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:3561907884583738100")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_3561907884583738100"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_20)
  static ClientConfig provideAiCoreClient20ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:4870111188580693201")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_4870111188580693201"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_21)
  static ClientConfig provideAiCoreClient21ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:6642565339740637386")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_6642565339740637386"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_22)
  static ClientConfig provideAiCoreClient22ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:9931783747856508885")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_9931783747856508885"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_23)
  static ClientConfig provideAiCoreClient23ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:5848825322855942324")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_5848825322855942324"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_24)
  static ClientConfig provideAiCoreClient24ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:4341791953025243445")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_4341791953025243445"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_25)
  static ClientConfig provideAiCoreClient25ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:6417633745608261729")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_6417633745608261729"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_26)
  static ClientConfig provideAiCoreClient26ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:11720962012422846819")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_11720962012422846819"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_27)
  static ClientConfig provideAiCoreClient27ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:14254786987761682043")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_14254786987761682043"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_28)
  static ClientConfig provideAiCoreClient28ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:4027292349711707490")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_4027292349711707490"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_29)
  static ClientConfig provideAiCoreClient29ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:1558569612950046780")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_1558569612950046780"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_30)
  static ClientConfig provideAiCoreClient30ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:6109265619551471570")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_6109265619551471570"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_31)
  static ClientConfig provideAiCoreClient31ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:6098232831121113138")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_6098232831121113138"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_32)
  static ClientConfig provideAiCoreClient32ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:14604084352937090483")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_14604084352937090483"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_33)
  static ClientConfig provideAiCoreClient33ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:10230360187542661313")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_10230360187542661313"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_34)
  static ClientConfig provideAiCoreClient34ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:14144884036502714237")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_14144884036502714237"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_35)
  static ClientConfig provideAiCoreClient35ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:16512701228291749612")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_16512701228291749612"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_36)
  static ClientConfig provideAiCoreClient36ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:3701923067702114378")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_3701923067702114378"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_37)
  static ClientConfig provideAiCoreClient37ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:18103149225492435673")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_18103149225492435673"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_38)
  static ClientConfig provideAiCoreClient38ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:5398059663487363370")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_5398059663487363370"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_39)
  static ClientConfig provideAiCoreClient39ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:16093837962507438679")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_16093837962507438679"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_40)
  static ClientConfig provideAiCoreClient40ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:9945587330698106851")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_9945587330698106851"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_41)
  static ClientConfig provideAiCoreClient41ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:9347763061896501379")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_9347763061896501379"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_42)
  static ClientConfig provideAiCoreClient42ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:10553225535939326565")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_10553225535939326565"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_43)
  static ClientConfig provideAiCoreClient43ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:5742606038786011969")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_5742606038786011969"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_44)
  static ClientConfig provideAiCoreClient44ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:9614928112563494806")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_9614928112563494806"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_45)
  static ClientConfig provideAiCoreClient45ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:6824732181910573706")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_6824732181910573706"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_46)
  static ClientConfig provideAiCoreClient46ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:7632259796561150258")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_7632259796561150258"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_47)
  static ClientConfig provideAiCoreClient47ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:12851944831581789857")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_12851944831581789857"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_48)
  static ClientConfig provideAiCoreClient48ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:17203260412298451912")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_17203260412298451912"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_49)
  static ClientConfig provideAiCoreClient49ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:2651730305904984656")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_2651730305904984656"))
        .build();
  }

  @Provides
  @IntoMap
  @ClientMapKey(Client.AI_CORE_CLIENT_50)
  static ClientConfig provideAiCoreClient50ClientConfig() {
    return ClientConfig.builder()
        .setClientId("com.google.android.aicore:5495164372972161668")
        .setBuildIdFlag(
            ClientConfig.BuildIdFlag.create(
                FlagNamespace.AICORE, "AicDataRelease__build_id_5495164372972161668"))
        .build();
  }

  @Provides
  @Singleton
  static ProtoConversions provideProtoConversions(Map<Client, ClientConfig> clientToClientId) {
    return new ProtoConversions(ImmutableMap.copyOf(clientToClientId));
  }

  private ProtoConversionsModule() {}
}
