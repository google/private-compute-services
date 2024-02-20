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

import static com.google.android.as.oss.pd.processor.impl.BlobProtoUtils.toInternalResponse;
import static com.google.android.as.oss.pd.processor.impl.SanityChecks.validateRequest;
import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.ExecutorAnnotations.ProtectedDownloadExecutorQualifier;
import com.google.android.as.oss.common.time.TimeSource;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.pd.api.proto.BlobConstraints;
import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import com.google.android.as.oss.pd.api.proto.DownloadBlobResponse;
import com.google.android.as.oss.pd.api.proto.GetManifestConfigRequest;
import com.google.android.as.oss.pd.api.proto.GetManifestConfigResponse;
import com.google.android.as.oss.pd.attestation.AttestationClient;
import com.google.android.as.oss.pd.channel.ChannelProvider;
import com.google.android.as.oss.pd.keys.EncryptionHelper;
import com.google.android.as.oss.pd.keys.EncryptionHelperFactory;
import com.google.android.as.oss.pd.manifest.api.proto.ProtectedDownloadServiceGrpc;
import com.google.android.as.oss.pd.manifest.api.proto.ProtectedDownloadServiceGrpc.ProtectedDownloadServiceFutureStub;
import com.google.android.as.oss.pd.networkusage.PDNetworkUsageLogHelper;
import com.google.android.as.oss.pd.persistence.ClientPersistentState;
import com.google.android.as.oss.pd.persistence.PersistentStateManager;
import com.google.android.as.oss.pd.processor.ProtectedDownloadProcessor;
import com.google.android.as.oss.pd.service.api.proto.ClientVersion;
import com.google.android.as.oss.pd.service.api.proto.ProgramBlobServiceGrpc;
import com.google.android.as.oss.pd.service.api.proto.ProgramBlobServiceGrpc.ProgramBlobServiceFutureStub;
import com.google.auto.value.AutoValue;
import com.google.common.flogger.GoogleLogger;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.ByteString;
import com.google.protobuf.MessageLite;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * A {@link ProtectedDownloadProcessor} that communicates with Google server to download blobs to
 * the device.
 */
@Singleton
final class ProtectedDownloadProcessorImpl implements ProtectedDownloadProcessor {

  @VisibleForTesting
  static final Metadata.Key<String> GRPC_API_KEY =
      Metadata.Key.of("X-GOOG-API-KEY", Metadata.ASCII_STRING_MARSHALLER);

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final ClientPersistentState DEFAULT_PERSISTENT_STATE =
      ClientPersistentState.newBuilder().setPageToken(ByteString.empty()).build();

  private final ListeningExecutorService pdExecutor;
  private final Executor ioExecutor;
  private final ChannelProvider channelProvider;
  private final TimeSource timeSource;
  private final PersistentStateManager persistenceManager;
  private final EncryptionHelperFactory encryptionHelperFactory;
  private final PDNetworkUsageLogHelper networkLogHelper;
  private final BlobProtoUtils blobProtoUtils;
  private final AttestationClient attestationClient;

  @Inject
  ProtectedDownloadProcessorImpl(
      ChannelProvider channelProvider,
      @ProtectedDownloadExecutorQualifier ListeningExecutorService pdExecutor,
      @IoExecutorQualifier Executor ioExecutor,
      TimeSource timeSource,
      PersistentStateManager persistenceManager,
      EncryptionHelperFactory encryptionHelperFactory,
      PDNetworkUsageLogHelper networkLogHelper,
      BlobProtoUtils blobProtoUtils,
      AttestationClient attestationClient) {
    this.channelProvider = channelProvider;
    this.pdExecutor = pdExecutor;
    this.ioExecutor = ioExecutor;
    this.timeSource = timeSource;
    this.persistenceManager = persistenceManager;
    this.encryptionHelperFactory = encryptionHelperFactory;
    this.networkLogHelper = networkLogHelper;
    this.blobProtoUtils = blobProtoUtils;
    this.attestationClient = attestationClient;
  }

  @Override
  public ListenableFuture<DownloadBlobResponse> download(DownloadBlobRequest request) {
    try {
      validateRequest(request);
    } catch (RuntimeException e) {
      return Futures.immediateFailedFuture(e);
    }

    String clientId = blobProtoUtils.getClientId(request.getMetadata());
    try {
      networkLogHelper.checkAllowedRequest(clientId);
    } catch (UnrecognizedNetworkRequestException e) {
      return Futures.immediateFailedFuture(e);
    }

    Channel channel =
        channelProvider.getChannel(request.getMetadata().getBlobConstraints().getClient());

    ContentBindingHashFunction contentBindingHashFunction =
        keyBytes -> blobProtoUtils.metadataHash(keyBytes, request.getMetadata());
    boolean useVmKey =
        request.getMetadata().getBlobConstraints().getClientVersion().getType()
            == BlobConstraints.ClientVersion.Type.TYPE_ANDROID_CORE_ATTESTED_PKVM;
    return FluentFuture.from(readOrCreatePersistentState(clientId))
        .transformAsync(
            state -> integrityCheck(state, contentBindingHashFunction, useVmKey), pdExecutor)
        .transformAsync(
            integrityResponse -> downloadBlob(channel, clientId, request, integrityResponse),
            pdExecutor)
        .transformAsync(this::finalizeDownload, pdExecutor);
  }

  @Override
  public ListenableFuture<GetManifestConfigResponse> getManifestConfig(
      GetManifestConfigRequest request) {
    try {
      validateRequest(request);
    } catch (RuntimeException e) {
      return Futures.immediateFailedFuture(e);
    }

    String clientId = blobProtoUtils.getClientId(request.getConstraints());
    try {
      networkLogHelper.checkAllowedRequest(clientId);
    } catch (UnrecognizedNetworkRequestException e) {
      return Futures.immediateFailedFuture(e);
    }

    Channel channel = channelProvider.getChannel(request.getConstraints().getClient());

    ContentBindingHashFunction contentBindingHashFunction =
        keyBytes ->
            blobProtoUtils.getManifestConfigMetadataHash(keyBytes, request.getConstraints());
    return FluentFuture.from(readOrCreatePersistentState(clientId))
        .transformAsync(
            state -> integrityCheck(state, contentBindingHashFunction, /* useVmKey= */ false),
            pdExecutor)
        .transformAsync(
            state -> downloadManifestConfig(channel, clientId, request, state), pdExecutor)
        .transformAsync(this::finalizeDownload, pdExecutor);
  }

  private ListenableFuture<IntegrityResponse> integrityCheck(
      ClientPersistentState persistentState,
      ContentBindingHashFunction contentBindingHashFunction,
      boolean useVmKey)
      throws GeneralSecurityException, IOException {
    ListenableFuture<EncryptionHelper> externalEncryptionFuture;
    if (useVmKey) {
      externalEncryptionFuture = readVmKey();
    } else if (persistentState.hasExternalKeySet()) {
      externalEncryptionFuture =
          Futures.immediateFuture(
              encryptionHelperFactory.createFromEncryptedKeySet(
                  persistentState.getExternalKeySet().toByteArray()));
    } else {
      logger.atInfo().log("generating new key set for a new client persistent state");
      EncryptionHelper encryptionHelper = encryptionHelperFactory.generateEncryptedKeySet();
      externalEncryptionFuture = Futures.immediateFuture(encryptionHelper);
      persistentState =
          persistentState.toBuilder()
              .setExternalKeySet(ByteString.copyFrom(encryptionHelper.toEncryptedKeySet()))
              .build();
    }

    ClientPersistentState finalClientPersistentState = persistentState;

    // NOTE: Even if attestation fails, we continue execution. Server will make a decision what to
    // do with the download.
    return FluentFuture.from(externalEncryptionFuture)
        .transformAsync(
            externalEncryption -> {
              String contentBinding =
                  contentBindingHashFunction.apply(externalEncryption.publicKey());
              ListenableFuture<ByteString> attestationFuture =
                  attestationClient.requestMeasurementWithContentBinding(contentBinding);
              return FluentFuture.from(attestationFuture)
                  .transform(
                      attestationToken ->
                          IntegrityResponse.create(
                              finalClientPersistentState,
                              externalEncryption,
                              Optional.of(attestationToken)),
                      pdExecutor)
                  .catching(
                      Exception.class,
                      e ->
                          IntegrityResponse.create(
                              finalClientPersistentState, externalEncryption, Optional.empty()),
                      pdExecutor);
            },
            pdExecutor);
  }

  private ListenableFuture<EncryptionHelper> readVmKey() {
    return FluentFuture.from(persistenceManager.readState(PersistentStateManager.VM_CLIENT_ID))
        .transform(
            state -> {
              ByteString vmKeyBytes =
                  state
                      .orElseThrow(() -> new IllegalStateException("No key available for VM"))
                      .getExternalKeySet();
              try {
                return encryptionHelperFactory.createFromPublicKey(vmKeyBytes.toByteArray());
              } catch (GeneralSecurityException | IOException e) {
                throw new IllegalStateException("Could not convert VM public key bytes", e);
              }
            },
            pdExecutor);
  }

  private ListenableFuture<ClientPersistentState> readOrCreatePersistentState(String clientId) {
    return Futures.transform(
        persistenceManager.readState(clientId),
        optionalState -> {
          if (optionalState.isPresent()) {
            logger.atInfo().log("found persistent state for client %s", clientId);
          } else {
            logger.atInfo().log("creating new persistent state for client %s", clientId);
          }
          return optionalState.orElse(DEFAULT_PERSISTENT_STATE);
        },
        pdExecutor);
  }

  // Returns an EncryptionHelper using the public key from `metadata` if the client Type for the
  // request is `TYPE_UNKNOWN` or `TYPE_ANDROID`.
  private Optional<EncryptionHelper> getInternalEncryptionForInternalResponse(
      com.google.android.as.oss.pd.api.proto.Metadata metadata)
      throws GeneralSecurityException, IOException {
    ClientVersion.Type type = BlobProtoUtils.getClientVersionType(metadata);
    if (type == ClientVersion.Type.TYPE_UNKNOWN || type == ClientVersion.Type.TYPE_ANDROID) {
      return Optional.of(
          encryptionHelperFactory.createFromPublicKey(
              metadata.getCryptoKeys().getPublicKey().toByteArray()));
    }
    return Optional.empty();
  }

  private ListenableFuture<DownloadResult<DownloadBlobResponse>> downloadBlob(
      Channel channel,
      String clientId,
      DownloadBlobRequest request,
      IntegrityResponse integrityResponse)
      throws GeneralSecurityException, IOException {
    ClientPersistentState finalClientPersistentState = integrityResponse.state();
    EncryptionHelper externalEncryption = integrityResponse.externalEncryption();

    String apiKey = channelProvider.getServiceApiKeyOverride().orElse(request.getApiKey());
    ProgramBlobServiceFutureStub serviceStub = createServiceStub(channel, apiKey);

    com.google.android.as.oss.pd.api.proto.Metadata metadata = request.getMetadata();
    logger.atInfo().log(
        "downloading blob with public key hash %s.", externalEncryption.publicKeyHashForLogging());
    return FluentFuture.from(
            serviceStub.downloadBlob(
                blobProtoUtils.toExternalRequest(
                    request,
                    externalEncryption.publicKey(),
                    finalClientPersistentState.getPageToken().toByteArray(),
                    integrityResponse.attestationToken())))
        .catchingAsync(Exception.class, getFailureLoggingTransform(clientId), pdExecutor)
        .transformAsync(
            externalResponse -> {
              int approximatedSize = externalResponse.getSerializedSize();
              try {
                logger.atInfo().log(
                    "received protection package with protection token %s",
                    BaseEncoding.base16()
                        .lowerCase()
                        .encode(externalResponse.getProtectionToken().toByteArray()));
                return Futures.immediateFuture(
                    DownloadResult.create(
                        toInternalResponse(
                            externalResponse,
                            externalEncryption,
                            getInternalEncryptionForInternalResponse(metadata),
                            blobProtoUtils.getClientId(metadata).getBytes(UTF_8)),
                        finalClientPersistentState,
                        approximatedSize,
                        clientId));
              } catch (GeneralSecurityException e) {
                networkLogHelper.logDownloadIfNeeded(clientId, Status.FAILED, approximatedSize);
                return Futures.immediateFailedFuture(e);
              }
            },
            pdExecutor);
  }

  private ListenableFuture<DownloadResult<GetManifestConfigResponse>> downloadManifestConfig(
      Channel channel,
      String clientId,
      GetManifestConfigRequest request,
      IntegrityResponse integrityResponse)
      throws GeneralSecurityException, IOException {
    EncryptionHelper externalEncryption = integrityResponse.externalEncryption();

    String apiKey = channelProvider.getServiceApiKeyOverride().orElse(request.getApiKey());
    ProtectedDownloadServiceFutureStub serviceStub =
        createProtectedDownloadServiceStub(channel, apiKey);
    logger.atInfo().log(
        "downloading manifest with public key hash %s.",
        externalEncryption.publicKeyHashForLogging());
    return FluentFuture.from(
            serviceStub.getManifestConfig(
                blobProtoUtils.toExternalRequest(
                    request, externalEncryption.publicKey(), integrityResponse.attestationToken())))
        .catchingAsync(Exception.class, getFailureLoggingTransform(clientId), pdExecutor)
        .transformAsync(
            externalResponse -> {
              int approximatedSize = externalResponse.getSerializedSize();
              try {
                return Futures.immediateFuture(
                    DownloadResult.create(
                        toInternalResponse(
                            externalResponse,
                            externalEncryption,
                            blobProtoUtils.getClientId(request.getConstraints()).getBytes(UTF_8)),
                        integrityResponse.state(),
                        approximatedSize,
                        clientId));
              } catch (GeneralSecurityException e) {
                networkLogHelper.logDownloadIfNeeded(clientId, Status.FAILED, approximatedSize);
                return Futures.immediateFailedFuture(e);
              }
            },
            pdExecutor);
  }

  private ProgramBlobServiceFutureStub createServiceStub(Channel channel, String apiKey) {
    Metadata metadata = new Metadata();
    metadata.put(GRPC_API_KEY, apiKey);
    return ProgramBlobServiceGrpc.newFutureStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
        .withExecutor(ioExecutor);
  }

  private ProtectedDownloadServiceFutureStub createProtectedDownloadServiceStub(
      Channel channel, String apiKey) {
    Metadata metadata = new Metadata();
    metadata.put(GRPC_API_KEY, apiKey);
    return ProtectedDownloadServiceGrpc.newFutureStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
        .withExecutor(ioExecutor);
  }

  private <T> AsyncFunction<Exception, T> getFailureLoggingTransform(String clientId) {
    return error -> {
      // The response message was not received, hence the size of the download is unknown
      /* size= */ networkLogHelper.logDownloadIfNeeded(clientId, Status.FAILED, 0);
      return Futures.immediateFailedFuture(error);
    };
  }

  private ClientPersistentState updatePersistentState(
      ClientPersistentState oldState, Optional<ByteString> pageToken) {
    ClientPersistentState.Builder builder =
        oldState.toBuilder().setLastCompletionTimeMillis(timeSource.now().toEpochMilli());
    pageToken.ifPresent(builder::setPageToken);
    return builder.build();
  }

  private <T extends MessageLite> ListenableFuture<T> finalizeDownload(DownloadResult<T> result) {
    Optional<ByteString> pageToken =
        result.response() instanceof DownloadBlobResponse
            ? Optional.of(((DownloadBlobResponse) result.response()).getNextPageToken())
            : Optional.empty();
    return FluentFuture.from(
            persistenceManager.writeState(
                result.clientId(), updatePersistentState(result.state(), pageToken)))
        .catchingAsync(
            Exception.class,
            error -> {
              networkLogHelper.logDownloadIfNeeded(
                  result.clientId(), Status.FAILED, result.approximatedDownloadSize());
              return Futures.immediateFailedFuture(error);
            },
            pdExecutor)
        .transform(
            ignored -> {
              networkLogHelper.logDownloadIfNeeded(
                  result.clientId(), Status.SUCCEEDED, result.approximatedDownloadSize());
              return result.response();
            },
            pdExecutor);
  }

  /** An internal data class passed between async operations during the blob download. */
  @AutoValue
  abstract static class DownloadResult<T extends MessageLite> {
    public static <T extends MessageLite> DownloadResult<T> create(
        T response, ClientPersistentState state, int downloadSize, String clientId) {
      return new AutoValue_ProtectedDownloadProcessorImpl_DownloadResult<T>(
          response, state, downloadSize, clientId);
    }

    /** The {@link DownloadBlobResponse} to return at the end of the download operation. */
    public abstract T response();

    /** The {@link ClientPersistentState} to store at the end of the download operation. */
    public abstract ClientPersistentState state();

    /** A close approximation to the actual bytes downloaded in the request. */
    public abstract int approximatedDownloadSize();

    /** The client requested the blob. */
    public abstract String clientId();
  }

  @AutoValue
  abstract static class IntegrityResponse {
    public static IntegrityResponse create(
        ClientPersistentState state,
        EncryptionHelper externalEncryption,
        Optional<ByteString> attestationToken) {
      return new AutoValue_ProtectedDownloadProcessorImpl_IntegrityResponse(
          state, externalEncryption, attestationToken);
    }

    /** The {@link ClientPersistentState} to store at the end of the download operation. */
    public abstract ClientPersistentState state();

    /** The {@link EncryptionHelper} to use for decrypting download response. */
    public abstract EncryptionHelper externalEncryption();

    /** An attestation token for download request. */
    public abstract Optional<ByteString> attestationToken();
  }

  private interface ContentBindingHashFunction {
    String apply(byte[] publicKeyBytes);
  }
}
