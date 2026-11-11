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

package com.google.android.as.oss.asr.service;

import static com.google.frameworks.client.data.android.server.contrib.parcelables.ParcelableOverMetadataClientInterceptorKt.withParcelableOverMetadata;

import android.annotation.SuppressLint;
import android.content.AttributionSource;
import android.content.Context;
import android.content.ContextParams;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.annotation.RequiresApi;
import com.google.android.as.oss.asr.api.AsrEnhancementPolicy;
import com.google.android.as.oss.asr.api.AudioSourceConfig;
import com.google.android.as.oss.asr.api.AutoLanguageSwitchConfig;
import com.google.android.as.oss.asr.api.ClientInfo;
import com.google.android.as.oss.asr.api.FeatureConfig;
import com.google.android.as.oss.asr.api.FormattingConfig;
import com.google.android.as.oss.asr.api.GoogleAsrServiceGrpc;
import com.google.android.as.oss.asr.api.GoogleAsrServiceParcelables;
import com.google.android.as.oss.asr.api.InitialSrsgRequest;
import com.google.android.as.oss.asr.api.LanguageDetectionConfig;
import com.google.android.as.oss.asr.api.ModelAvailabilityRequest;
import com.google.android.as.oss.asr.api.ModelAvailabilityResponse;
import com.google.android.as.oss.asr.api.ModelDownloadRequest;
import com.google.android.as.oss.asr.api.ModelDownloadResponse;
import com.google.android.as.oss.asr.api.MultiSegmentConfig;
import com.google.android.as.oss.asr.api.RecognitionRequest;
import com.google.android.as.oss.asr.api.RecognitionRequest.StartAction;
import com.google.android.as.oss.asr.api.RecognitionResponse;
import com.google.android.as.oss.asr.api.RecognizerMode;
import com.google.android.as.oss.asr.api.SerializedProtoEvent;
import com.google.android.as.oss.asr.api.SrsgModelAvailabilityRequest;
import com.google.android.as.oss.asr.api.SrsgModelAvailabilityResponse;
import com.google.android.as.oss.asr.api.SrsgModelDownloadRequest;
import com.google.android.as.oss.asr.api.SrsgModelDownloadResponse;
import com.google.android.as.oss.asr.api.SrsgProxyServiceGrpc;
import com.google.android.as.oss.asr.api.SrsgRequest;
import com.google.android.as.oss.asr.api.SrsgResponse;
import com.google.common.flogger.GoogleLogger;
import com.google.frameworks.client.data.android.server.contrib.parcelables.ParcelableOverMetadataKeys;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * A gRPC service that acts as a proxy between a client (e.g., Live Caption) and the
 * GoogleASRService (SRSG).
 *
 * <p>This proxy is responsible for:
 *
 * <ul>
 *   <li>Receiving {@link SrsgRequest} messages from the client.
 *   <li>Extracting a {@link ParcelFileDescriptor} from the incoming gRPC metadata, which represents
 *       the audio stream.
 *   <li>Transforming {@link SrsgRequest} messages into {@link RecognitionRequest} messages suitable
 *       for the GoogleASRService.
 *   <li>Forwarding the transformed requests and the audio PFD (via metadata) to the
 *       GoogleASRService.
 *   <li>Receiving {@link RecognitionResponse} messages from the GoogleASRService.
 *   <li>Transforming these responses back into {@link SrsgResponse} messages.
 *   <li>Sending the transformed responses back to the client.
 *   <li>Handling stream lifecycle and error propagation between the client and GoogleASRService.
 * </ul>
 */
public class SrsgProxyServiceImpl extends SrsgProxyServiceGrpc.SrsgProxyServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Provider<GoogleAsrServiceGrpc.GoogleAsrServiceStub> srsgClientProvider;

  /**
   * Key for retrieving the audio stream {@link ParcelFileDescriptor} from the gRPC Metadata
   * (headers). This key is used by the {@link ServerInterceptor} to extract the PFD sent by the
   * client. The client must attach the PFD to the Metadata using the same key name
   * "audio-pfd-key-bin".
   */
  private static final ParcelableOverMetadataKeys<ParcelFileDescriptor> PFD_METADATA_KEY =
      ParcelableOverMetadataKeys.ofParcelFileDescriptor("audio-pfd-key-bin");

  /**
   * Key for storing and accessing the extracted {@link ParcelFileDescriptor} within the gRPC {@link
   * Context}. After the interceptor pulls the PFD from the Metadata, it uses this key to make the
   * PFD available to the service method .implementation (e.g., recognitionSession) throughout the
   * lifecycle of the RPC call.
   */
  private static final io.grpc.Context.Key<ParcelFileDescriptor> CONTEXT_PFD_KEY =
      io.grpc.Context.key("context-audio-pfd");

  // Key to store the client's AttributionSource in the gRPC Context
  private static final io.grpc.Context.Key<AttributionSource> CONTEXT_CLIENT_ATTR_SRC_KEY =
      io.grpc.Context.key("context-client-attr-src");

  private final Context applicationContext;

  @Inject
  SrsgProxyServiceImpl(
      Provider<GoogleAsrServiceGrpc.GoogleAsrServiceStub> srsgClientProvider,
      @ApplicationContext Context applicationContext) {
    this.srsgClientProvider = srsgClientProvider;
    this.applicationContext = applicationContext;
  }

  /**
   * Establishes a bidirectional streaming session for recognition.
   *
   * @param clientResponseObserver The observer to send {@link SrsgResponse} messages back to the
   *     client.
   * @return A {@link StreamObserver} for the client to send {@link SrsgRequest} messages.
   */
  @Override
  public StreamObserver<SrsgRequest> recognitionSession(
      StreamObserver<SrsgResponse> clientResponseObserver) {
    // Retrieve the PFD from the current gRPC Context.
    ParcelFileDescriptor receivedPfd = CONTEXT_PFD_KEY.get();
    GoogleAsrServiceGrpc.GoogleAsrServiceStub srsgStub = srsgClientProvider.get();
    // Inject AttributionSource for Android V+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
      srsgStub = injectChainedAttributionSource(srsgStub, applicationContext);
    }
    return new ClientRequestObserver(srsgStub, clientResponseObserver, receivedPfd);
  }

  /**
   * Checks model availability. The result includes the model info and the availability state.
   *
   * @param request The {@link SrsgModelAvailabilityRequest} containing the application ID.
   * @param responseObserver The {@link StreamObserver} to send {@link
   *     SrsgModelAvailabilityResponse} messages back to the client.
   */
  @Override
  public void checkModelAvailability(
      SrsgModelAvailabilityRequest request,
      StreamObserver<SrsgModelAvailabilityResponse> responseObserver) {
    ModelAvailabilityRequest.Builder srsgRequestBuilder =
        ModelAvailabilityRequest.newBuilder()
            .setClientInfo(ClientInfo.newBuilder().setPackageName(request.getApplicationId()));

    GoogleAsrServiceGrpc.GoogleAsrServiceStub srsgStub = srsgClientProvider.get();
    srsgStub.checkModelAvailability(
        srsgRequestBuilder.build(), new AvailabilityResponseForwarder(responseObserver));
  }

  /**
   * Requests model download.
   *
   * @param request The {@link SrsgModelDownloadRequest} containing the locale and other parameters.
   * @param responseObserver The {@link StreamObserver} to send {@link SrsgModelDownloadResponse}
   *     messages back to the client.
   */
  @Override
  public void downloadModel(
      SrsgModelDownloadRequest request,
      StreamObserver<SrsgModelDownloadResponse> responseObserver) {
    logger.atInfo().log(
        "Download request for locale %s, hasAiFeatureId: %s",
        request.getLocale(), request.hasAiFeatureId());

    ModelDownloadRequest.Builder srsgRequestBuilder =
        ModelDownloadRequest.newBuilder()
            .setSkipUserConfirmation(true)
            .setEnableProgressUpdate(true)
            .setClientInfo(ClientInfo.newBuilder().setPackageName(request.getApplicationId()));

    srsgRequestBuilder.setLocale(request.getLocale());

    GoogleAsrServiceGrpc.GoogleAsrServiceStub srsgStub = srsgClientProvider.get();
    srsgStub.downloadModel(
        srsgRequestBuilder.build(), new DownloadResponseForwarder(responseObserver));
  }

  /**
   * Handles incoming messages from the client. It manages the lifecycle of the connection to the
   * downstream GoogleASRService.
   */
  private static class ClientRequestObserver implements StreamObserver<SrsgRequest> {
    private final StreamObserver<SrsgResponse> clientResponseObserver;
    private final GoogleAsrServiceGrpc.GoogleAsrServiceStub srsgStub;
    @Nullable private StreamObserver<RecognitionRequest> srsgRequestObserver;
    @Nullable private final ParcelFileDescriptor audioPfd;
    private String sessionId;

    /**
     * Constructs an observer for client requests.
     *
     * @param srsgStub The stub to connect to the downstream GoogleASRService.
     * @param clientResponseObserver The observer to send responses back to the client.
     * @param audioPfd The ParcelFileDescriptor for the audio input, extracted from metadata.
     */
    ClientRequestObserver(
        GoogleAsrServiceGrpc.GoogleAsrServiceStub srsgStub,
        StreamObserver<SrsgResponse> clientResponseObserver,
        ParcelFileDescriptor audioPfd) {
      this.srsgStub = srsgStub;
      this.clientResponseObserver = clientResponseObserver;
      this.audioPfd = audioPfd;
    }

    @SuppressWarnings("nullness")
    @Override
    public void onNext(SrsgRequest clientRequest) {
      sessionId = clientRequest.getSessionId();

      if (clientRequest.hasInitialRequest()) {
        if (srsgRequestObserver != null) {
          // Close PFD if an error occurs early
          clientResponseObserver.onError(
              new IllegalStateException("Initial request already sent."));
          return;
        }

        // Prepare the StartRecognitionRequest for GoogleAsrService
        InitialSrsgRequest initial = clientRequest.getInitialRequest();
        FeatureConfig.Builder featureConfig =
            FeatureConfig.newBuilder()
                .setAudioSourceConfig(
                    AudioSourceConfig.newBuilder()
                        .setChannelCount(initial.getChannelCount())
                        .setSamplingRate(initial.getSampleRateHz())
                        .build())
                .setMaxResults(1)
                .setEnablePartialResults(true)
                .setMaskOffensiveWords(initial.getMaskOffensiveWords())
                // Enabled the multi segment mode and set the end pointer to the audio source end.
                .setMultiSegmentConfig(
                    MultiSegmentConfig.newBuilder().setAudioSourceEnd(true).build())
                // Enabled the punctuation.
                .setFormattingConfig(FormattingConfig.newBuilder().setEnabled(true).build());

        if (initial.getAsrEnhancementEnabled()) {
          logger.atFinest().log("ASR enhancement enabled");
          featureConfig.setAsrEnhancementPolicy(
              AsrEnhancementPolicy.newBuilder()
                  .setPolicy(AsrEnhancementPolicy.Policy.FALLBACK_ALWAYS)
                  .setOptimizationMode(AsrEnhancementPolicy.OptimizationMode.LATENCY)
                  .build());
        }

        if (initial.getLanguageDetectionConfig().getEnabled()) {
          logger.atFinest().log("Language detection enabled");
          featureConfig
              .setLanguageDetectionConfig(
                  LanguageDetectionConfig.newBuilder()
                      .setEnabled(initial.getLanguageDetectionConfig().getEnabled())
                      .addAllAllowedLanguages(
                          initial.getLanguageDetectionConfig().getAllowedLanguagesList()))
              .setAutoLanguageSwitchConfig(
                  AutoLanguageSwitchConfig.newBuilder()
                      .setEnabled(true)
                      .setOptimization(
                          AutoLanguageSwitchConfig.Optimization.DEFAULT_OPTIMIZE_QUALITY)
                      .addAllAllowedLanguages(
                          initial.getAutoLanguageSwitchConfig().getAllowedLanguagesList()));
        }

        StartAction startReq =
            StartAction.newBuilder()
                .setRecognizerMode(RecognizerMode.RECOGNIZER_MODE_ONDEVICE_ONLY)
                .setPrimaryLocale(initial.getLocale())
                .setClientInfo(
                    ClientInfo.newBuilder().setPackageName(initial.getApplicationId()).build())
                .setFeatureConfig(featureConfig.build())
                .build();

        var googleAsrServiceStubWithParcel =
            withParcelableOverMetadata(srsgStub, GoogleAsrServiceParcelables.PFD_KEYS, audioPfd);
        // Start the stream to SRSG
        srsgRequestObserver =
            googleAsrServiceStubWithParcel.recognitionSession(
                new SrsgResponseObserver(clientResponseObserver, sessionId));

        // Send the initial request to SRSG
        srsgRequestObserver.onNext(
            RecognitionRequest.newBuilder().setStartAction(startReq).build());
      } else if (clientRequest.hasEndOfData()) {
        if (srsgRequestObserver != null) {
          // Cleanly close the stream to SRSG
          srsgRequestObserver.onCompleted();
        }
      }
    }

    @Override
    public void onError(Throwable t) {
      logger.atWarning().withCause(t).log(
          "Error from client stream for session %s SERVICE_NAME: %s",
          sessionId, SrsgProxyServiceGrpc.SERVICE_NAME);
      if (srsgRequestObserver != null) {
        srsgRequestObserver.onError(t);
      }
      closePfd();
    }

    @Override
    public void onCompleted() {
      logger.atInfo().log("Client stream completed for session %s", sessionId);
      closePfd();
    }

    private void closePfd() {
      try {
        if (audioPfd != null) {
          audioPfd.close();
        }
      } catch (IOException e) {
        logger.atWarning().withCause(e).log("Error closing PFD from client");
      }
    }
  }

  /**
   * Handles response messages from the GoogleASRService (SRSG). It transforms these responses into
   * {@link SrsgResponse} format for the client.
   */
  private static class SrsgResponseObserver implements StreamObserver<RecognitionResponse> {
    private final StreamObserver<SrsgResponse> clientResponseObserver;
    private final String sessionId;
    private static final String RAW_RECOGNITION_EVENT_PROTO_TYPE = "raw_recognition_event";

    /**
     * Constructs an observer for downstream service responses.
     *
     * @param clientResponseObserver The observer to send transformed responses back to the client.
     * @param sessionId The session ID for logging.
     */
    SrsgResponseObserver(StreamObserver<SrsgResponse> clientResponseObserver, String sessionId) {
      this.clientResponseObserver = clientResponseObserver;
      this.sessionId = sessionId;
    }

    @Override
    public void onNext(RecognitionResponse srsgResponse) {
      // Convert srsgResponse to SrsgResponse and send to client.
      SerializedProtoEvent serializedEvent =
          SerializedProtoEvent.newBuilder()
              .setProtoType(RAW_RECOGNITION_EVENT_PROTO_TYPE)
              .setSerializedData(srsgResponse.toByteString())
              .build();

      SrsgResponse clientResponse =
          SrsgResponse.newBuilder().setSessionId(sessionId).setProtoEvent(serializedEvent).build();
      clientResponseObserver.onNext(clientResponse);
    }

    @Override
    public void onError(Throwable t) {
      clientResponseObserver.onError(t);
    }

    @Override
    public void onCompleted() {
      clientResponseObserver.onCompleted();
    }
  }

  /**
   * Returns a {@link ServerInterceptor} designed to extract a {@link ParcelFileDescriptor} from
   * incoming gRPC call metadata. This is used to receive the audio stream from the client. It also
   * extracts the client's AttributionSource for Android V+.
   */
  public static ServerInterceptor getPfdAndAttrSrcExtractorInterceptor() {
    return new ServerInterceptor() {
      @Override
      public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
          ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {

        ParcelFileDescriptor pfd = headers.get(PFD_METADATA_KEY.getMetadataKey());
        io.grpc.Context newContext = io.grpc.Context.current();

        if (pfd != null) {
          logger.atFine().log(
              "getPfdAndAttrSrcExtractorInterceptor(): Extracted PFD from metadata");
          newContext = newContext.withValue(CONTEXT_PFD_KEY, pfd);
        } else {
          logger.atFine().log("getPfdAndAttrSrcExtractorInterceptor(): PFD is null in metadata");
        }
        // Extract Client's AttributionSource (for Android V+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
          try {
            ParcelableOverMetadataKeys<AttributionSource> clientAttrSrcMetadataKey =
                ParcelableOverMetadataKeys.ofAttributionSource("client-attr-src-bin");
            AttributionSource clientAttrSrc =
                headers.get(clientAttrSrcMetadataKey.getMetadataKey());
            if (clientAttrSrc != null) {
              newContext = newContext.withValue(CONTEXT_CLIENT_ATTR_SRC_KEY, clientAttrSrc);
            } else {
              logger.atWarning().log("No Client AttributionSource found in metadata.");
            }
          } catch (RuntimeException e) {
            logger.atWarning().withCause(e).log("Failed to extract Client AttributionSource.");
          }
        }
        return Contexts.interceptCall(newContext, call, headers, next);
      }
    };
  }

  /**
   * Returns a {@link GoogleAsrServiceGrpc.GoogleAsrServiceStub} with AttributionSource injected.
   *
   * <p>This method is used to inject AttributionSource into the stub's calls to the
   * GoogleASRService in SRSG App and chain it with the client's AttributionSource.
   */
  @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
  @SuppressLint("FlaggedApi")
  private static GoogleAsrServiceGrpc.GoogleAsrServiceStub injectChainedAttributionSource(
      GoogleAsrServiceGrpc.GoogleAsrServiceStub stub, Context applicationContext) {

    AttributionSource clientAttrSrc = CONTEXT_CLIENT_ATTR_SRC_KEY.get();

    AttributionSource pcsAttrSrc =
        applicationContext
            .createContext(
                new ContextParams.Builder()
                    .setShouldRegisterAttributionSource(true)
                    // Chain: Client -> Pcs
                    .setNextAttributionSource(clientAttrSrc)
                    .build())
            .getAttributionSource();
    AttributionSource finalAttrSrc;
    if (clientAttrSrc != null) {
      finalAttrSrc = new AttributionSource.Builder(pcsAttrSrc).build();
    } else {
      finalAttrSrc = pcsAttrSrc;
    }

    return withParcelableOverMetadata(
        stub, GoogleAsrServiceParcelables.getATTR_SRC_KEYS(), finalAttrSrc);
  }

  /**
   * Forwards {@link ModelAvailabilityResponse} from SRSG to the client as {@link
   * SrsgModelAvailabilityResponse}.
   */
  private static class AvailabilityResponseForwarder
      implements StreamObserver<ModelAvailabilityResponse> {
    private final StreamObserver<SrsgModelAvailabilityResponse> delegate;

    AvailabilityResponseForwarder(StreamObserver<SrsgModelAvailabilityResponse> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void onNext(ModelAvailabilityResponse srsgResponse) {
      delegate.onNext(SrsgProxyServiceHelper.toSrsgModelAvailabilityResponse(srsgResponse));
    }

    @Override
    public void onError(Throwable t) {
      logger.atWarning().withCause(t).log("AvailabilityResponseForwarder: Error from SRSG");
      delegate.onError(t);
    }

    @Override
    public void onCompleted() {
      delegate.onCompleted();
    }
  }

  /**
   * Forwards {@link ModelDownloadResponse} from SRSG to the client as {@link
   * SrsgModelDownloadResponse}.
   */
  private static class DownloadResponseForwarder implements StreamObserver<ModelDownloadResponse> {
    private final StreamObserver<SrsgModelDownloadResponse> delegate;

    DownloadResponseForwarder(StreamObserver<SrsgModelDownloadResponse> delegate) {
      this.delegate = delegate;
    }

    @Override
    public void onNext(ModelDownloadResponse srsgResponse) {
      delegate.onNext(SrsgProxyServiceHelper.toSrsgModelDownloadResponse(srsgResponse));
    }

    @Override
    public void onError(Throwable t) {
      logger.atWarning().withCause(t).log("DownloadResponseForwarder: Error from SRSG");
      delegate.onError(t);
    }

    @Override
    public void onCompleted() {
      delegate.onCompleted();
    }
  }
}
