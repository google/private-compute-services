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

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Client;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.ClientGroup;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.DeviceTier;
import com.google.android.as.oss.pd.api.proto.BlobConstraints.Variant;
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
  private final Context context;
  private final ProtoConversions protoConversions;

  public BlobProtoUtils(ProtoConversions protoConversions, Context context) {
    this.protoConversions = protoConversions;
    this.context = context;
  }

  @VisibleForTesting
  static final ImmutableMap<String, String> DEFAULT_LABELS = ImmutableMap.of("language_code", "en");

  @VisibleForTesting static final String CLIENT_GROUP_LABEL_KEY = "client_group";
  @VisibleForTesting static final String DEVICE_TIER_LABEL_KEY = "device_tier";
  @VisibleForTesting static final long CLIENT_VERSION = 4L;
  @VisibleForTesting static final String VARIANT_LABEL_KEY = "variant";

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

  /**
   * Decrypts with external encryption and re-encrypts using internalEncryption, if re-encryption is
   * requested and internal encryption is provided. If the externalEncryption does not have a
   * private key then no encryption can be done and we just return the unmodified encryptedData.
   */
  private static ByteString replaceEncryption(
      ByteString encryptedData,
      EncryptionHelper externalEncryption,
      Optional<EncryptionHelper> internalEncryption,
      byte[] associatedData,
      boolean reencryptData) {
    if (!externalEncryption.hasPrivateKey()) {
      return encryptedData;
    }
    byte[] decrypted;
    try {
      decrypted = externalEncryption.decrypt(encryptedData.toByteArray(), associatedData);
    } catch (GeneralSecurityException e) {
      throw new IllegalArgumentException(e);
    }
    if (!reencryptData) {
      return ByteString.copyFrom(decrypted);
    }
    byte[] reencryptedBytes =
        internalEncryption
            .map(
                enc -> {
                  try {
                    return enc.encrypt(decrypted, associatedData);
                  } catch (GeneralSecurityException e) {
                    throw new IllegalArgumentException(e);
                  }
                })
            .orElse(decrypted);
    return ByteString.copyFrom(reencryptedBytes);
  }

  /** Converts a server download blob response to PCS download blob response. */
  public static DownloadBlobResponse toInternalResponse(
      com.google.android.as.oss.pd.service.api.proto.DownloadBlobResponse externalResponse,
      EncryptionHelper externalEncryption,
      Optional<EncryptionHelper> internalEncryption,
      byte[] associatedData)
      throws GeneralSecurityException {
    DownloadBlobResponse.Builder builder =
        DownloadBlobResponse.newBuilder()
            .setProtectionProofV2(toInternalProof(externalResponse.getProtectionProofV2()))
            .setNextPageToken(externalResponse.getNextPageToken())
            .setDownloadStatusValue(externalResponse.getDownloadStatus().getNumber());

    ByteString outerBlob = externalResponse.getBlob();
    boolean hasOuterBlob = !outerBlob.isEmpty();

    if (hasOuterBlob) {
      ByteString convertedOuterBlob =
          replaceEncryption(
              outerBlob,
              externalEncryption,
              internalEncryption,
              associatedData,
              /* reencryptData= */ true);
      builder.setBlob(convertedOuterBlob);
    }

    for (com.google.android.as.oss.pd.service.api.proto.ProtectionComponent externalComponent :
        externalResponse.getProtectionComponentsList()) {
      ByteString convertedBlob =
          replaceEncryption(
              externalComponent.getBlob(),
              externalEncryption,
              internalEncryption,
              associatedData,
              /* reencryptData= */ hasOuterBlob);

      builder.addProtectionComponents(
          ProtectionComponent.newBuilder()
              .setTypeValue(externalComponent.getType().getNumber())
              .setIsPartialUpdate(externalComponent.getIsPartialUpdate())
              .setPartialUpdateIndex(externalComponent.getPartialUpdateIndex())
              .setBlob(convertedBlob)
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

  public long getClientVersionVersion(Metadata metadata) {
    if (metadata.getBlobConstraints().getClientVersion().getType()
        == com.google.android.as.oss.pd.api.proto.BlobConstraints.ClientVersion.Type.TYPE_ANDROID) {
      return CLIENT_VERSION;
    }
    // Query PackageManager for the client application's version code.
    String packageName = getClientId(metadata).split(":", 2)[0];
    String[] versionParts = null;
    try {
      // Attempt to extract version number from versionName of the form: %s.%s.<VERSION>.
      String versionName =
          context
              .getPackageManager()
              .getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
              .versionName;
      if (versionName == null) {
        // Fallback to hardcoded CLIENT_VERSION.
        return CLIENT_VERSION;
      }
      versionParts = versionName.split("\\.", 0);
    } catch (NameNotFoundException e) {
      // Fallback to hardcoded CLIENT_VERSION.
      return CLIENT_VERSION;
    }
    try {
      return Long.parseLong(versionParts[versionParts.length - 1], 10);
    } catch (NumberFormatException e) {
      // Fallback to hardcoded CLIENT_VERSION.
      return CLIENT_VERSION;
    }
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
    BlobConstraints.Builder constraintsBuilder =
        BlobConstraints.newBuilder()
            .setClientId(getClientId(metadata))
            .setDeviceTier(getDeviceTier(metadata))
            .setClientVersion(
                ClientVersion.newBuilder()
                    .setType(getClientVersionType(metadata))
                    .setVersion(getClientVersionVersion(metadata))
                    .build())
            .addAllLabel(toLabels(labels))
            .addLabel(toLabel(CLIENT_GROUP_LABEL_KEY, getClientGroup(metadata)));
    getVariant(metadata)
        .ifPresent(value -> constraintsBuilder.addLabel(toLabel(VARIANT_LABEL_KEY, value)));
    return com.google.android.as.oss.pd.service.api.proto.Metadata.newBuilder()
        .setCryptoKeys(CryptoKeys.newBuilder().setPublicKey(publicKey).setUseClientIdSeed(true))
        .setBlobConstraints(constraintsBuilder)
        .setCounters(Counters.getDefaultInstance())
        .build();
  }

  @VisibleForTesting
  ManifestConfigConstraints buildConstraints(
      com.google.android.as.oss.pd.api.proto.BlobConstraints constraints) {
    ManifestConfigConstraints.Builder constraintsBuilder =
        ManifestConfigConstraints.newBuilder()
            .setClientId(getClientId(constraints))
            .setClientVersion(
                com.google.android.as.oss.pd.manifest.api.proto.ClientVersion.newBuilder()
                    .setVersion(CLIENT_VERSION)
                    .build())
            .addLabel(
                com.google.android.as.oss.pd.manifest.api.proto.Label.newBuilder()
                    .setAttribute(CLIENT_GROUP_LABEL_KEY)
                    .setValue(getClientGroup(constraints))
                    .build())
            .addLabel(
                com.google.android.as.oss.pd.manifest.api.proto.Label.newBuilder()
                    .setAttribute(DEVICE_TIER_LABEL_KEY)
                    .setValue(getDeviceTier(constraints))
                    .build());
    getVariant(constraints)
        .ifPresent(
            value ->
                constraintsBuilder.addLabel(
                    com.google.android.as.oss.pd.manifest.api.proto.Label.newBuilder()
                        .setAttribute(VARIANT_LABEL_KEY)
                        .setValue(value)
                        .build()));
    return constraintsBuilder.build();
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

  private static Optional<String> getVariant(Metadata metadata) {
    return getVariant(metadata.getBlobConstraints());
  }

  private static Optional<String> getVariant(
      com.google.android.as.oss.pd.api.proto.BlobConstraints blobConstraints) {
    Variant tag = blobConstraints.getVariant();
    if (tag == Variant.VARIANT_UNSPECIFIED) {
      return Optional.empty();
    }
    return Optional.of(
        ProtoConversions.toVariantString(tag)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        String.format("unable to convert %s to string", tag.name()))));
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
