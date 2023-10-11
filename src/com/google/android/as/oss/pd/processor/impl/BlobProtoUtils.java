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
import com.google.android.as.oss.pd.api.proto.GetManifestConfigRequest;
import com.google.android.as.oss.pd.api.proto.GetManifestConfigResponse;
import com.google.android.as.oss.pd.api.proto.InclusionProof;
import com.google.android.as.oss.pd.api.proto.LogCheckpoint;
import com.google.android.as.oss.pd.api.proto.LogEntryId;
import com.google.android.as.oss.pd.api.proto.Metadata;
import com.google.android.as.oss.pd.api.proto.ProtectionComponent;
import com.google.android.as.oss.pd.api.proto.ProtectionProof;
import com.google.android.as.oss.pd.common.ProtoConversions;
import com.google.android.as.oss.pd.keys.EncryptionHelper;
import com.google.android.as.oss.pd.manifest.api.proto.ManifestConfigConstraints;
import com.google.android.as.oss.pd.service.api.proto.BlobConstraints;
import com.google.android.as.oss.pd.service.api.proto.ClientVersion;
import com.google.android.as.oss.pd.service.api.proto.Counters;
import com.google.android.as.oss.pd.service.api.proto.CryptoKeys;
import com.google.android.as.oss.pd.service.api.proto.DownloadBlobRequest.DownloadMode;
import com.google.android.as.oss.pd.service.api.proto.IntegrityResponse;
import com.google.android.as.oss.pd.service.api.proto.Label;
import com.google.android.as.oss.pd.service.api.proto.ProtectionProofConfig;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;
import com.google.protobuf.ByteString;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  @VisibleForTesting static final String DEVICE_TIER_LABEL_KEY = "device_tier";
  @VisibleForTesting static final long CLIENT_VERSION = 2;

  /** Converts the DownloadMode from PCS to a server format. */
  public static DownloadMode getDownloadMode(DownloadBlobRequest request) {
    DownloadBlobRequest.DownloadMode internalMode = request.getDownloadMode();
    DownloadMode externalMode = DownloadMode.forNumber(internalMode.getNumber());
    if (externalMode == null) {
      throw new IllegalArgumentException(
          String.format("unable to convert %d to external DownloadMode", internalMode.getNumber()));
    }
    return externalMode;
  }

  /** Converts a blob download request from PCS to a server request to download a blob. */
  public com.google.android.as.oss.pd.service.api.proto.DownloadBlobRequest toExternalRequest(
      DownloadBlobRequest request,
      byte[] publicKey,
      byte[] pageToken,
      Optional<ByteString> attestationToken) {
    return com.google.android.as.oss.pd.service.api.proto.DownloadBlobRequest.newBuilder()
        .setIntegrityResponse(getIntegrityResponse(attestationToken))
        .setMetadata(
            toExternalMetadata(
                ByteString.copyFrom(publicKey), request.getMetadata(), DEFAULT_LABELS))
        .setPageToken(ByteString.copyFrom(pageToken))
        .setProtectionProofConfig(
            ProtectionProofConfig.newBuilder().setIncludeV2Proof(true).setExcludeV1Proof(true))
        .setDownloadMode(getDownloadMode(request))
        .build();
  }

  /** Converts a manifest request from PCS to a server request to get a manifest. */
  public com.google.android.as.oss.pd.manifest.api.proto.GetManifestConfigRequest toExternalRequest(
      GetManifestConfigRequest request, byte[] publicKey, Optional<ByteString> attestationToken) {
    return com.google.android.as.oss.pd.manifest.api.proto.GetManifestConfigRequest.newBuilder()
        .setConstraints(buildConstraints(request.getConstraints()))
        .setIntegrityResponse(getManifestIntegrityResponse(attestationToken))
        .setCryptoKeys(
            com.google.android.as.oss.pd.manifest.api.proto.CryptoKeys.newBuilder()
                .setPublicKey(ByteString.copyFrom(publicKey)))
        .build();
  }

  private static IntegrityResponse getIntegrityResponse(Optional<ByteString> attestationToken) {
    if (!attestationToken.isPresent()) {
      return IntegrityResponse.getDefaultInstance();
    } else {
      return IntegrityResponse.newBuilder().setKeyAttestationToken(attestationToken.get()).build();
    }
  }

  private static com.google.android.as.oss.pd.manifest.api.proto.IntegrityResponse
      getManifestIntegrityResponse(Optional<ByteString> attestationToken) {
    if (!attestationToken.isPresent()) {
      return com.google.android.as.oss.pd.manifest.api.proto.IntegrityResponse.getDefaultInstance();
    } else {
      return com.google.android.as.oss.pd.manifest.api.proto.IntegrityResponse.newBuilder()
          .setKeyAttestationToken(attestationToken.get())
          .build();
    }
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

  /** Converts a server manifest config response to PCS manifest config response. */
  public static GetManifestConfigResponse toInternalResponse(
      com.google.android.as.oss.pd.manifest.api.proto.GetManifestConfigResponse externalResponse,
      EncryptionHelper externalEncryption,
      byte[] associatedData)
      throws GeneralSecurityException {
    return GetManifestConfigResponse.newBuilder()
        .setManifestConfig(
            ByteString.copyFrom(
                externalEncryption.decrypt(
                    externalResponse.getEncryptedManifestConfig().toByteArray(), associatedData)))
        .build();
  }

  /** Retrieves the client identifier used by the server to select the blob to provide. */
  public String getClientId(Metadata metadata) {
    return getClientId(metadata.getBlobConstraints());
  }

  /** Retrieves the client identifier used by the server to select the blob to provide. */
  public String getClientId(
      com.google.android.as.oss.pd.api.proto.BlobConstraints blobConstraints) {
    Client client = blobConstraints.getClient();
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

  /** Calculates metadata hash for Device integrity content binding. */
  public String metadataHash(byte[] publicKey, Metadata metadata) {
    com.google.android.as.oss.pd.service.api.proto.Metadata externalMetadata =
        toExternalMetadata(ByteString.copyFrom(publicKey), metadata, DEFAULT_LABELS);

    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putBytes(externalMetadata.getBlobConstraints().getDeviceTier().getBytes());
    hasher.putBytes(externalMetadata.getBlobConstraints().getClientId().getBytes());
    hasher.putBytes(publicKey);
    for (Label label : externalMetadata.getBlobConstraints().getLabelList()) {
      hasher.putBytes(label.getAttribute().getBytes());
      hasher.putBytes(label.getValue().getBytes());
    }

    return BaseEncoding.base64().encode(hasher.hash().asBytes());
  }

  /**
   * Calculates metadata hash for Device integrity content binding used for GetManifestConfig
   * requests.
   */
  public String getManifestConfigMetadataHash(
      byte[] publicKey, com.google.android.as.oss.pd.api.proto.BlobConstraints blobConstraints) {
    ManifestConfigConstraints externalConstraints = buildConstraints(blobConstraints);

    Hasher hasher = Hashing.sha256().newHasher();
    hasher.putBytes(externalConstraints.getClientId().getBytes());
    hasher.putBytes(publicKey);
    for (com.google.android.as.oss.pd.manifest.api.proto.Label label :
        externalConstraints.getLabelList()) {
      hasher.putBytes(label.getAttribute().getBytes());
      hasher.putBytes(label.getValue().getBytes());
    }

    return BaseEncoding.base64().encode(hasher.hash().asBytes());
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
  ManifestConfigConstraints buildConstraints(
      com.google.android.as.oss.pd.api.proto.BlobConstraints constraints) {
    return ManifestConfigConstraints.newBuilder()
        .setClientId(getClientId(constraints))
        .addLabel(
            com.google.android.as.oss.pd.manifest.api.proto.Label.newBuilder()
                .setAttribute(CLIENT_GROUP_LABEL_KEY)
                .setValue(getClientGroup(constraints))
                .build())
        .addLabel(
            com.google.android.as.oss.pd.manifest.api.proto.Label.newBuilder()
                .setAttribute(DEVICE_TIER_LABEL_KEY)
                .setValue(getDeviceTier(constraints))
                .build())
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
    return getDeviceTier(metadata.getBlobConstraints());
  }

  private static String getDeviceTier(
      com.google.android.as.oss.pd.api.proto.BlobConstraints blobConstraints) {
    DeviceTier tier = blobConstraints.getDeviceTier();
    return ProtoConversions.toDeviceTierString(tier)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    String.format("unable to convert %s to string", tier.name())));
  }

  private static String getClientGroup(Metadata metadata) {
    return getClientGroup(metadata.getBlobConstraints());
  }

  private static String getClientGroup(
      com.google.android.as.oss.pd.api.proto.BlobConstraints blobConstraints) {
    ClientGroup group = blobConstraints.getClientGroup();
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
