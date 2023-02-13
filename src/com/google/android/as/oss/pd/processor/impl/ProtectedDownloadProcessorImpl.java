/*
 * Copyright 2021 Google LLC
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

import static com.google.android.as.oss.pd.processor.impl.BlobProtoUtils.getClientId;
import static com.google.android.as.oss.pd.processor.impl.BlobProtoUtils.toExternalRequest;
import static com.google.android.as.oss.pd.processor.impl.BlobProtoUtils.toInternalResponse;
import static com.google.android.as.oss.pd.processor.impl.SanityChecks.validateRequest;
import static java.nio.charset.StandardCharsets.UTF_8;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.ExecutorAnnotations.ProtectedDownloadExecutorQualifier;
import com.google.android.as.oss.common.time.TimeSource;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import com.google.android.as.oss.pd.api.proto.DownloadBlobResponse;
import com.google.android.as.oss.pd.channel.ChannelProvider;
import com.google.android.as.oss.pd.keys.EncryptionHelper;
import com.google.android.as.oss.pd.keys.EncryptionHelperFactory;
import com.google.android.as.oss.pd.networkusage.PDNetworkUsageLogHelper;
import com.google.android.as.oss.pd.persistence.ClientPersistentState;
import com.google.android.as.oss.pd.persistence.PersistentStateManager;
import com.google.android.as.oss.pd.processor.ProtectedDownloadProcessor;
import com.google.android.as.oss.pd.service.api.proto.ProgramBlobServiceGrpc;
import com.google.android.as.oss.pd.service.api.proto.ProgramBlobServiceGrpc.ProgramBlobServiceFutureStub;
import com.google.auto.value.AutoValue;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.protobuf.ByteString;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.io.IOException;
import java.security.GeneralSecurityException;
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

  @Inject
  ProtectedDownloadProcessorImpl(
      ChannelProvider channelProvider,
      @ProtectedDownloadExecutorQualifier ListeningExecutorService pdExecutor,
      @IoExecutorQualifier Executor ioExecutor,
      TimeSource timeSource,
      PersistentStateManager persistenceManager,
      EncryptionHelperFactory encryptionHelperFactory,
      PDNetworkUsageLogHelper networkLogHelper) {
    this.channelProvider = channelProvider;
    this.pdExecutor = pdExecutor;
    this.ioExecutor = ioExecutor;
    this.timeSource = timeSource;
    this.persistenceManager = persistenceManager;
    this.encryptionHelperFactory = encryptionHelperFactory;
    this.networkLogHelper = networkLogHelper;
  }

  @Override
  public ListenableFuture<DownloadBlobResponse> download(DownloadBlobRequest request) {
    try {
      validateRequest(request);
    } catch (RuntimeException e) {
      return Futures.immediateFailedFuture(e);
    }

    String clientId = getClientId(request.getMetadata());
    try {
      networkLogHelper.checkAllowedRequest(clientId);
    } catch (UnrecognizedNetworkRequestException e) {
      return Futures.immediateFailedFuture(e);
    }

    Channel channel =
        channelProvider.getChannel(request.getMetadata().getBlobConstraints().getClient());

    return FluentFuture.from(readOrCreatePersistentState(clientId))
        .transformAsync(state -> downloadBlob(channel, clientId, request, state), pdExecutor)
        .transformAsync(this::finalizeDownload, pdExecutor);
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

  private ListenableFuture<DownloadResult> downloadBlob(
      Channel channel,
      String clientId,
      DownloadBlobRequest request,
      ClientPersistentState persistentState)
      throws GeneralSecurityException, IOException {

    ClientPersistentState clientPersistentState = persistentState;
    ProgramBlobServiceFutureStub serviceStub = createServiceStub(channel, request.getApiKey());

    EncryptionHelper internalEncryption =
        encryptionHelperFactory.createFromPublicKey(
            request.getMetadata().getCryptoKeys().getPublicKey().toByteArray());

    EncryptionHelper externalEncryption;
    if (clientPersistentState.hasExternalKeySet()) {
      externalEncryption =
          encryptionHelperFactory.createFromEncryptedKeySet(
              persistentState.getExternalKeySet().toByteArray());
    } else {
      logger.atInfo().log("generating new key set for a new client persistent state");
      externalEncryption = encryptionHelperFactory.generateEncryptedKeySet();
      clientPersistentState =
          clientPersistentState.toBuilder()
              .setExternalKeySet(ByteString.copyFrom(externalEncryption.toEncryptedKeySet()))
              .build();
    }

    ClientPersistentState finalClientPersistentState = clientPersistentState;
    return FluentFuture.from(
            serviceStub.downloadBlob(
                toExternalRequest(
                    request,
                    externalEncryption.publicKey(),
                    clientPersistentState.getPageToken().toByteArray())))
        .catchingAsync(
            Exception.class,
            error -> {
              // The response message was not received, hence the size of the download is unknown
              /* size= */ networkLogHelper.logDownloadIfNeeded(clientId, Status.FAILED, 0);
              return Futures.immediateFailedFuture(error);
            },
            pdExecutor)
        .transformAsync(
            externalResponse -> {
              int approximatedSize = externalResponse.getSerializedSize();
              try {
                byte[] newBlob =
                    replaceEncryption(
                        externalEncryption,
                        internalEncryption,
                        getClientId(request.getMetadata()).getBytes(UTF_8),
                        externalResponse.getBlob().toByteArray());
                return Futures.immediateFuture(
                    DownloadResult.create(
                        toInternalResponse(externalResponse, newBlob),
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

  private ProgramBlobServiceFutureStub createServiceStub(Channel channel, String apiKey) {
    Metadata metadata = new Metadata();
    metadata.put(GRPC_API_KEY, apiKey);
    return ProgramBlobServiceGrpc.newFutureStub(channel)
        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
        .withExecutor(ioExecutor);
  }

  private ClientPersistentState updatePersistentState(
      ClientPersistentState oldState, DownloadBlobResponse response) {
    return oldState.toBuilder()
        .setLastCompletionTimeMillis(timeSource.now().toEpochMilli())
        .setPageToken(response.getNextPageToken())
        .build();
  }

  private ListenableFuture<DownloadBlobResponse> finalizeDownload(DownloadResult result) {
    return FluentFuture.from(
            persistenceManager.writeState(
                result.clientId(), updatePersistentState(result.state(), result.response())))
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

  private byte[] replaceEncryption(
      EncryptionHelper externalEncryption,
      EncryptionHelper internalEncryption,
      byte[] associatedData,
      byte[] encryptedData)
      throws GeneralSecurityException {
    byte[] decrypted = externalEncryption.decrypt(encryptedData, associatedData);
    return internalEncryption.encrypt(decrypted, associatedData);
  }

  /** An internal data class passed between async operations during the blob download. */
  @AutoValue
  abstract static class DownloadResult {
    public static DownloadResult create(
        DownloadBlobResponse response,
        ClientPersistentState state,
        int downloadSize,
        String clientId) {
      return new AutoValue_ProtectedDownloadProcessorImpl_DownloadResult(
          response, state, downloadSize, clientId);
    }

    /** The {@link DownloadBlobResponse} to return at the end of the download operation. */
    public abstract DownloadBlobResponse response();

    /** The {@link ClientPersistentState} to store at the end of the download operation. */
    public abstract ClientPersistentState state();

    /** A close approximation to the actual bytes downloaded in the request. */
    public abstract int approximatedDownloadSize();

    /** The client requested the blob. */
    public abstract String clientId();
  }
}
