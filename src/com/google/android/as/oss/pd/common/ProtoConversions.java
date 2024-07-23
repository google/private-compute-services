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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;

import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.ClientGroup;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.DeviceTier;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Variant;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import java.util.Map;
import java.util.Optional;

/**
 * Provides utility methods to convert some fields in {@link DownloadBlobRequest}/ {@link
 * DownloadBlobResponse} from/to their protobuf representation.
 */
public final class ProtoConversions {

  private final ImmutableMap<Client, ClientConfig> clientToClientConfig;
  private final ImmutableMap<String, Client> clientIdToClient;
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public ProtoConversions(ImmutableMap<Client, ClientConfig> clientToClientConfig) {
    this.clientToClientConfig = clientToClientConfig;

    this.clientIdToClient =
        clientToClientConfig.entrySet().stream()
            .collect(toImmutableMap(entity -> entity.getValue().clientId(), Map.Entry::getKey));
    checkArgument(
        clientToClientConfig.size() == this.clientIdToClient.size(),
        "All ClientIds should be unique.");
  }

  private static final ImmutableBiMap<DeviceTier, String> DEVICE_TIER_TO_STRING =
      ImmutableBiMap.of(
          DeviceTier.UNKNOWN, "",
          DeviceTier.ULTRA_LOW, "Ultra Low",
          DeviceTier.LOW, "Low",
          DeviceTier.MID, "Mid",
          DeviceTier.HIGH, "High",
          DeviceTier.ULTRA, "Ultra");

  private static final ImmutableBiMap<ClientGroup, String> CLIENT_GROUP_TO_STRING =
      ImmutableBiMap.of(
          ClientGroup.ALL, "all",
          ClientGroup.BETA, "beta",
          ClientGroup.ALPHA, "alpha",
          ClientGroup.THIRD_PARTY_EAP, "third_party_eap");

  private static final ImmutableMap<String, Variant> STRING_TO_VARIANT =
      stream(Variant.values()).collect(toImmutableMap(Enum::name, variant -> variant));

  public Optional<String> toClientIdString(Client client) {
    return Optional.ofNullable(clientToClientConfig.get(client)).map(ClientConfig::clientId);
  }

  public Optional<Client> fromClientIdString(String clientId) {
    return Optional.ofNullable(clientIdToClient.get(clientId));
  }

  public static Optional<String> toDeviceTierString(DeviceTier deviceTier) {
    return Optional.ofNullable(DEVICE_TIER_TO_STRING.get(deviceTier));
  }

  public static Optional<DeviceTier> fromDeviceTierString(String deviceTier) {
    return Optional.ofNullable(DEVICE_TIER_TO_STRING.inverse().get(deviceTier));
  }

  public static Optional<String> toClientGroupString(ClientGroup clientGroup) {
    return Optional.ofNullable(CLIENT_GROUP_TO_STRING.get(clientGroup));
  }

  public static Optional<ClientGroup> fromClientGroupString(String clientGroup) {
    return Optional.ofNullable(CLIENT_GROUP_TO_STRING.inverse().get(clientGroup));
  }

  public static Optional<String> toVariantString(Variant variant) {
    if (variant == Variant.UNRECOGNIZED) {
      return Optional.empty();
    }
    if (variant == Variant.VARIANT_UNSPECIFIED) {
      return Optional.of("");
    }
    return Optional.of(variant.name());
  }

  public static Optional<Variant> fromVariantString(String variantName) {
    if (variantName.isEmpty()) {
      return Optional.of(Variant.VARIANT_UNSPECIFIED);
    }
    Variant variant = STRING_TO_VARIANT.get(variantName);
    if (variant == null) {
      logger.atWarning().log("Variant %s was not found and will be ignored.", variantName);
      return Optional.empty();
    }

    return Optional.of(variant);
  }
}
