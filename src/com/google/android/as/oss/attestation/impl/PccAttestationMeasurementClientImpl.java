/*
 * Copyright 2025 Google LLC
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

package com.google.android.as.oss.attestation.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static java.nio.charset.StandardCharsets.UTF_8;

import android.annotation.SuppressLint;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Pair;
import com.google.android.as.oss.attestation.AttestationMeasurementRequest;
import com.google.android.as.oss.attestation.PccAttestationMeasurementClient;
import com.google.android.as.oss.attestation.api.proto.AttestationMeasurementResponse;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.internal.android.keyattestation.v1.Challenge;
import com.google.internal.android.keyattestation.v1.GenerateChallengeRequest;
import com.google.internal.android.keyattestation.v1.KeyAttestationServiceGrpc;
import com.google.internal.android.keyattestation.v1.KeyAttestationServiceGrpc.KeyAttestationServiceFutureStub;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Generates a key attestation record for attestation measurement in PCS.
 *
 * <p>The attestation record is generated using a challenge provided by the android key attestation
 * validation service.
 *
 * <p>Call {@link #requestAttestationMeasurement(AttestationMeasurementRequest)} to execute an
 * attestation request. If attestation is not possible, then the corresponding error is returned in
 * a failed future.
 */
public class PccAttestationMeasurementClientImpl implements PccAttestationMeasurementClient {
  private final Executor executor;
  private final ManagedChannel managedChannel;
  private final NetworkUsageLogRepository networkUsageLogRepository;

  private final PcsStatsLog pcsStatsLogger;
  private final Context context;

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
  // Alias of the entry under which the generated key will appear in Android KeyStore. This alis is
  // constant so, new keys will overwrite older keys on generation.
  private static final String ANDROID_KEY_STORE_ALIAS = "PcsAttestationKey";
  // TODO: Distinguish package names for Attestation request
  private static final String PACKAGE_NAME = "com.google.android.as";
  private static final String DEVICE_ID_FEATURE_NAME = "android.software.device_id_attestation";
  private static final int EC_KEY_SIZE = 224;
  private static final int RSA_KEY_SIZE = 2048;

  private static final ImmutableList<String> SUPPORTED_KEY_ALGORITHMS =
      ImmutableList.of(KeyProperties.KEY_ALGORITHM_EC, KeyProperties.KEY_ALGORITHM_RSA);

  public PccAttestationMeasurementClientImpl(
      Executor executor,
      ManagedChannel channel,
      NetworkUsageLogRepository networkUsageLogRepository,
      PcsStatsLog pcsStatsLogger,
      Context context) {
    this.executor = executor;
    this.managedChannel = channel;
    this.networkUsageLogRepository = networkUsageLogRepository;
    this.pcsStatsLogger = pcsStatsLogger;
    this.context = context;
  }

  /** {@inheritDoc} */
  @Override
  public ListenableFuture<AttestationMeasurementResponse> requestAttestationMeasurement(
      AttestationMeasurementRequest attestationMeasurementRequest) {

    if (attestationMeasurementRequest.keyAlgorithm().isPresent()) {
      checkArgument(
          SUPPORTED_KEY_ALGORITHMS.contains(attestationMeasurementRequest.keyAlgorithm().get()),
          "Requested key algorithm %s is not supported. Supported key algorithms are: %s",
          attestationMeasurementRequest.keyAlgorithm().get(),
          SUPPORTED_KEY_ALGORITHMS);
    }

    pcsStatsLogger.logIntelligenceCountReported(
        IntelligenceCountReported.newBuilder()
            .setCountMetricId(CountMetricId.PCC_ATTESTATION_MEASUREMENT_REQUEST)
            .build());
    attestationMeasurementRequest
        .contentBinding()
        .ifPresent(
            contentBinding ->
                checkArgument(
                    !contentBinding.isEmpty(), "Content binding should not be an empty string."));

    return Futures.transformAsync(
        requestChallenge(attestationMeasurementRequest),
        attestationChallenge ->
            requestAttestationInternal(attestationMeasurementRequest, attestationChallenge),
        executor);
  }

  /**
   * Helper method to request attestation measurement. Requests key attestation and generates a
   * corresponding {@link AttestationMeasurementResponse}.
   *
   * @param attestationMeasurementRequest: Attestation measurement request parameters.
   * @param challenge: Attestation challenge obtained from the attestation validation service.
   */
  private ListenableFuture<AttestationMeasurementResponse> requestAttestationInternal(
      AttestationMeasurementRequest attestationMeasurementRequest, Challenge challenge) {
    // Record challenge request in network usage log
    if (attestationMeasurementRequest.contentBinding().isEmpty()) {
      insertNetworkUsageLogRow(challenge.getSerializedSize());
    }

    byte[] attestationChallenge = challenge.getChallenge().toByteArray();

    AttestationMeasurementResponse.Builder attestationResponseBuilder =
        AttestationMeasurementResponse.newBuilder();

    // Try to generate an attestation response
    try {
      String keyAlgorithm =
          attestationMeasurementRequest.keyAlgorithm().orElse(KeyProperties.KEY_ALGORITHM_RSA);
      Pair<KeyPair, List<Certificate>> keyPairWithAttestation =
          generateKeyPairWithAttestation(
              attestationMeasurementRequest, attestationChallenge, keyAlgorithm);
      KeyPair keyPair = keyPairWithAttestation.first;
      // Add public key to response
      attestationResponseBuilder.setPublicKey(
          ByteString.copyFrom(Objects.requireNonNull(keyPair.getPublic().getEncoded())));

      List<Certificate> attestationRecord = keyPairWithAttestation.second;
      // Encode attestation record
      List<ByteString> encodedAttestationRecord = encodeCertificate(attestationRecord);
      attestationResponseBuilder.addAllKeyAttestationCertificateChain(encodedAttestationRecord);

      // Sign content binding if it is provided.
      if (attestationMeasurementRequest.contentBinding().isPresent()) {
        // Sign attestation payload
        byte[] signature =
            signPayload(
                keyPair, attestationMeasurementRequest.contentBinding().get(), keyAlgorithm);
        attestationResponseBuilder
            .setPayload(attestationMeasurementRequest.contentBinding().get())
            .setSignatureBytes(ByteString.copyFrom(signature));
        attestationResponseBuilder.setContentBindingAsChallenge(true);
      }
    } catch (GeneralSecurityException e) {
      logger.atWarning().withCause(e).log(
          "Encountered a security exception while performing attestation measurement.");
      return Futures.immediateFailedFuture(e);
    } catch (IOException e) {
      logger.atWarning().withCause(e).log(
          "Encountered an IO exception while performing attestation measurement.");
      return Futures.immediateFailedFuture(e);
    }
    pcsStatsLogger.logIntelligenceCountReported(
        IntelligenceCountReported.newBuilder()
            .setCountMetricId(CountMetricId.PCC_ATTESTATION_RECORD_GENERATED)
            .build());
    return Futures.immediateFuture(attestationResponseBuilder.build());
  }

  /** Helper method to initiate a grpc request for an attestation challenge. */
  private ListenableFuture<Challenge> requestChallenge(
      AttestationMeasurementRequest attestationRequest) {
    if (attestationRequest.contentBinding().isPresent()) {
      return Futures.immediateFuture(
          Challenge.newBuilder()
              .setChallenge(
                  ByteString.copyFrom(
                      Hashing.sha384()
                          .hashString(attestationRequest.contentBinding().get(), UTF_8)
                          .asBytes()))
              .build());
    }
    KeyAttestationServiceFutureStub futureStub =
        KeyAttestationServiceGrpc.newFutureStub(managedChannel);
    return futureStub.generateChallenge(GenerateChallengeRequest.getDefaultInstance());
  }

  /**
   * This creates a key generation request, specifying a key alias and key generation parameters for
   * an RSA key pair. It also set the attestationChallenge provided by the validation server, to
   * indicate that attestation is requested.
   *
   * <p>This is a synchronous method since we generate key pairs under the same alias
   * (ANDROID_KEY_STORE_ALIAS).
   *
   * <p>Once the generation request is complete, the {@link KeyPair} is obtained as well as the
   * attestation {@link Certificate}. The {@link Certificate} is stored in the AndroidKeyStore.
   */
  @SuppressLint("NewApi")
  private synchronized Pair<KeyPair, List<Certificate>> generateKeyPairWithAttestation(
      AttestationMeasurementRequest attestationMeasurementRequest,
      byte[] attestationChallenge,
      String keyAlgorithm)
      throws GeneralSecurityException, IOException {

    boolean includeDeviceProperties =
        (context.getPackageManager().hasSystemFeature(DEVICE_ID_FEATURE_NAME)
            && attestationMeasurementRequest.includeIdAttestation().orElse(false));

    KeyGenParameterSpec keyGenParameterSpec =
        getKeyGenParameterSpec(attestationChallenge, includeDeviceProperties, keyAlgorithm);
    KeyPairGenerator keyPairGenerator = getKeyPairGeneratorInstance(keyAlgorithm);
    keyPairGenerator.initialize(keyGenParameterSpec);

    // Generate the key pair. This will result in calls to both generate_key() and
    // attest_key() at the keymaster2 HAL.
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    // Get the certificate chain
    KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
    keyStore.load(null);
    Certificate[] certs = keyStore.getCertificateChain(ANDROID_KEY_STORE_ALIAS);
    List<Certificate> attestationRecord;
    if (certs == null) {
      attestationRecord = new ArrayList<>();
    } else {
      attestationRecord = Arrays.asList(certs);
    }

    return Pair.create(keyPair, attestationRecord);
  }

  private KeyGenParameterSpec getKeyGenParameterSpec(
      byte[] attestationChallenge, boolean includeDeviceProperties, String keyAlgorithm) {
    KeyGenParameterSpec.Builder keyGenParameterSpecBuilder =
        new KeyGenParameterSpec.Builder(
                ANDROID_KEY_STORE_ALIAS, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
            .setDigests(KeyProperties.DIGEST_SHA256)
            // Request ID Attestation
            .setDevicePropertiesAttestationIncluded(includeDeviceProperties)
            // Request an attestation with challenge
            .setAttestationChallenge(attestationChallenge);
    if (keyAlgorithm.equals(KeyProperties.KEY_ALGORITHM_EC)) {
      keyGenParameterSpecBuilder.setKeySize(EC_KEY_SIZE);
    } else {
      keyGenParameterSpecBuilder
          .setSignaturePaddings(
              KeyProperties.SIGNATURE_PADDING_RSA_PSS, KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
          .setKeySize(RSA_KEY_SIZE);
    }
    return keyGenParameterSpecBuilder.build();
  }

  private KeyPairGenerator getKeyPairGeneratorInstance(String keyAlgorithm)
      throws GeneralSecurityException {
    KeyPairGenerator keyPairGenerator;
    if (keyAlgorithm.equals(KeyProperties.KEY_ALGORITHM_EC)) {
      keyPairGenerator =
          KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEY_STORE);
    } else {
      keyPairGenerator =
          KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
    }
    return keyPairGenerator;
  }

  /**
   * Sign the {@code attestationPayload}, using the {@link PrivateKey} from the generated asymmetric
   * {@link KeyPair}. The {@link Signature} instance used to sign the payload corresponds to the key
   * generator parameters for the RSA {@link KeyPair}.
   */
  private byte[] signPayload(KeyPair keyPair, String attestationPayload, String keyAlgorithm)
      throws GeneralSecurityException {
    PrivateKey privateKey = keyPair.getPrivate();
    String signatureAlgorithm =
        keyAlgorithm.equals(KeyProperties.KEY_ALGORITHM_EC) ? "SHA256withECDSA" : "SHA256withRSA";
    Signature signer = Signature.getInstance(signatureAlgorithm);
    signer.initSign(privateKey);
    signer.update(attestationPayload.getBytes(UTF_8));

    return signer.sign();
  }

  /** Helper method to encode an array of {@link Certificate} to {@link String} format. */
  private List<ByteString> encodeCertificate(List<Certificate> attestationRecord)
      throws CertificateEncodingException {
    List<ByteString> encodedAttestationRecord = new ArrayList<>();
    for (Certificate cert : attestationRecord) {
      encodedAttestationRecord.add(ByteString.copyFrom(cert.getEncoded()));
    }
    return encodedAttestationRecord;
  }

  /** Helper method to insert download into network usage log. */
  private void insertNetworkUsageLogRow(long downloadSize) {
    NetworkUsageEntity networkUsageEntity =
        NetworkUsageLogUtils.createAttestationNetworkUsageEntity(PACKAGE_NAME, downloadSize);
    networkUsageLogRepository.insertNetworkUsageEntity(networkUsageEntity);
  }
}
