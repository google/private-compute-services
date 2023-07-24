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

package com.google.android.as.oss.pd.processor.impl;

import static java.util.stream.Collectors.toCollection;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.ClientGroup;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.DeviceTier;
import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import com.google.android.as.oss.pd.api.proto.DownloadBlobResponse;
import com.google.android.as.oss.pd.api.proto.InclusionProof;
import com.google.android.as.oss.pd.api.proto.LogCheckpoint;
import com.google.android.as.oss.pd.api.proto.LogEntryId;
import com.google.android.as.oss.pd.api.proto.Metadata;
import com.google.android.as.oss.pd.api.proto.ProtectionComponent;
import com.google.android.as.oss.pd.api.proto.ProtectionProof;
import com.google.android.as.oss.pd.common.ProtoConversions;
import com.google.android.as.oss.pd.keys.EncryptionHelper;
import com.google.android.as.oss.pd.service.api.proto.BlobConstraints;
import com.google.android.as.oss.pd.service.api.proto.ClientVersion;
import com.google.android.as.oss.pd.service.api.proto.Counters;
import com.google.android.as.oss.pd.service.api.proto.CryptoKeys;
import com.google.android.as.oss.pd.service.api.proto.IntegrityResponse;
import com.google.android.as.oss.pd.service.api.proto.Label;
import com.google.android.as.oss.pd.service.api.proto.ProtectionProofConfig;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains utility functions to translate between PCS-internal proto definitions and the external
 * proto API exposed by Google servers.
 */
public final class BlobProtoUtils {
  private final ProtoConversions protoConversions;

  public BlobProtoUtils(ProtoConversions protoConversions) {
    this.protoConversions = protoConversions;
  }

  @VisibleForTesting
  static final ImmutableMap<String, String> DEFAULT_LABELS = ImmutableMap.of("language_code", "en");

  @VisibleForTesting static final String CLIENT_GROUP_LABEL_KEY = "client_group";
  @VisibleForTesting static final long CLIENT_VERSION = 2;

  /** Converts a blob download request from PCS to a server request to download a blob. */
  public com.google.android.as.oss.pd.service.api.proto.DownloadBlobRequest toExternalRequest(
      DownloadBlobRequest request, byte[] publicKey, byte[] pageToken) {
    return com.google.android.as.oss.pd.service.api.proto.DownloadBlobRequest.newBuilder()
        .setIntegrityResponse(IntegrityResponse.getDefaultInstance())
        .setMetadata(
            toExternalMetadata(
                ByteString.copyFrom(publicKey), request.getMetadata(), DEFAULT_LABELS))
        .setPageToken(ByteString.copyFrom(pageToken))
        .setProtectionProofConfig(
            ProtectionProofConfig.newBuilder().setIncludeV2Proof(true).setExcludeV1Proof(true))
        .build();
  }

  /** Decrypts with external encryption and applies internal encryption if requested. */
  private static byte[] replaceEncryption(
      byte[] encryptedData,
      EncryptionHelper externalEncryption,
      EncryptionHelper internalEncryption,
      byte[] associatedData,
      boolean reencryptData)
      throws GeneralSecurityException {
    byte[] decrypted = externalEncryption.decrypt(encryptedData, associatedData);
    if (reencryptData) {
      return internalEncryption.encrypt(decrypted, associatedData);
    }
    return decrypted;
  }

  /** Converts a server download blob response to PCS download blob response. */
  public static DownloadBlobResponse toInternalResponse(
      com.google.android.as.oss.pd.service.api.proto.DownloadBlobResponse externalResponse,
      EncryptionHelper externalEncryption,
      EncryptionHelper internalEncryption,
      byte[] associatedData)
      throws GeneralSecurityException {
    DownloadBlobResponse.Builder builder =
        DownloadBlobResponse.newBuilder()
            .setProtectionProofV2(toInternalProof(externalResponse.getProtectionProofV2()))
            .setNextPageToken(externalResponse.getNextPageToken())
            .setDownloadStatusValue(externalResponse.getDownloadStatus().getNumber());

    ByteString externalBlob = externalResponse.getBlob();
    boolean reencrypt = !externalBlob.isEmpty();
    if (reencrypt) {
      builder.setBlob(
          ByteString.copyFrom(
              replaceEncryption(
                  externalBlob.toByteArray(),
                  externalEncryption,
                  internalEncryption,
                  associatedData,
                  reencrypt)));
    }
    for (com.google.android.as.oss.pd.service.api.proto.ProtectionComponent externalComponent :
        externalResponse.getProtectionComponentsList()) {
      builder.addProtectionComponents(
          ProtectionComponent.newBuilder()
              .setTypeValue(externalComponent.getType().getNumber())
              .setIsPartialUpdate(externalComponent.getIsPartialUpdate())
              .setPartialUpdateIndex(externalComponent.getPartialUpdateIndex())
              .setBlob(
                  ByteString.copyFrom(
                      replaceEncryption(
                          externalComponent.getBlob().toByteArray(),
                          externalEncryption,
                          internalEncryption,
                          associatedData,
                          reencrypt)))
              .build());
    }
    return builder.build();
  }

  /** Retrieves the client identifier used by the server to select the blob to provide. */
  public String getClientId(Metadata metadata) {
    Client client = metadata.getBlobConstraints().getClient();
    return protoConversions
        .toClientIdString(client)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("unable to convert %s to string", client.name())));
  }

  /** Converts the ClientVersion.Type from PCS to a server format. */
  public static ClientVersion.Type getClientVersionType(Metadata metadata) {
    com.google.android.as.oss.pd.api.proto.BlobConstraints.ClientVersion.Type internalType =
        metadata.getBlobConstraints().getClientVersion().getType();
    ClientVersion.Type externalType = ClientVersion.Type.forNumber(internalType.getNumber());
    if (externalType == null) {
      throw new IllegalArgumentException(
          String.format(
              "unable to convert %d to internal ClientVersion.Type", internalType.getNumber()));
    }
    return externalType;
  }

  @VisibleForTesting
  com.google.android.as.oss.pd.service.api.proto.Metadata toExternalMetadata(
      ByteString publicKey, Metadata metadata, ImmutableMap<String, String> labels) {
    return com.google.android.as.oss.pd.service.api.proto.Metadata.newBuilder()
        .setCryptoKeys(CryptoKeys.newBuilder().setPublicKey(publicKey).setUseClientIdSeed(true))
        .setBlobConstraints(
            BlobConstraints.newBuilder()
                .setClientId(getClientId(metadata))
                .setDeviceTier(getDeviceTier(metadata))
                .setClientVersion(
                    ClientVersion.newBuilder()
                        .setType(getClientVersionType(metadata))
                        .setVersion(CLIENT_VERSION)
                        .build())
                .addAllLabel(toLabels(labels))
                .addLabel(toLabel(CLIENT_GROUP_LABEL_KEY, getClientGroup(metadata))))
        .setCounters(Counters.getDefaultInstance())
        .build();
  }

  @VisibleForTesting
  static ProtectionProof toInternalProof(
      com.google.android.as.oss.pd.service.api.proto.ProtectionProof proof) {
    return ProtectionProof.newBuilder()
        .setLogEntryId(
            LogEntryId.newBuilder()
                .setLeafIndex(proof.getLogEntryId().getLeafIndex())
                .setTreeId(proof.getLogEntryId().getTreeId()))
        .setLogCheckpoint(
            LogCheckpoint.newBuilder()
                .setCheckpoint(proof.getLogCheckpoint().getCheckpoint())
                .setSignature(proof.getLogCheckpoint().getSignature()))
        .setInclusionProof(
            InclusionProof.newBuilder().addAllHashes(proof.getInclusionProof().getHashesList()))
        .build();
  }

  private static String getDeviceTier(Metadata metadata) {
    DeviceTier tier = metadata.getBlobConstraints().getDeviceTier();
    return ProtoConversions.toDeviceTierString(tier)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("unable to convert %s to string", tier.name())));
  }

  private static String getClientGroup(Metadata metadata) {
    ClientGroup group = metadata.getBlobConstraints().getClientGroup();
    return ProtoConversions.toClientGroupString(group)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("unable to convert %s to string", group.name())));
  }

  private static List<Label> toLabels(ImmutableMap<String, String> labels) {
    return labels.entrySet().stream()
        .map(entry -> toLabel(entry.getKey(), entry.getValue()))
        .collect(toCollection(ArrayList::new));
  }

  private static Label toLabel(String key, String value) {
    return Label.newBuilder().setAttribute(key).setValue(value).build();
  }
}
