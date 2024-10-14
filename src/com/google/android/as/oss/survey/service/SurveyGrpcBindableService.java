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

package com.google.android.as.oss.survey.service;

import static com.google.android.as.oss.survey.api.proto.ClientCapability.CLIENT_CAPABILITY_UNKNOWN;
import static com.google.android.as.oss.survey.api.proto.ClientContext.DeviceInfo.MobileInfo.OsType.OS_TYPE_ANDROID;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.apps.miphone.astrea.grpc.GrpcStatusProto;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.survey.api.proto.ClientContext;
import com.google.android.as.oss.survey.api.proto.ClientContext.DeviceInfo;
import com.google.android.as.oss.survey.api.proto.ClientContext.DeviceInfo.MobileInfo;
import com.google.android.as.oss.survey.api.proto.ClientContext.LibraryInfo;
import com.google.android.as.oss.survey.api.proto.ClientContext.LibraryInfo.Platform;
import com.google.android.as.oss.survey.api.proto.Duration;
import com.google.android.as.oss.survey.api.proto.HttpProperty;
import com.google.android.as.oss.survey.api.proto.HttpSurveyRecordEventRequest;
import com.google.android.as.oss.survey.api.proto.HttpSurveyRecordEventRequestList;
import com.google.android.as.oss.survey.api.proto.HttpSurveyResponse;
import com.google.android.as.oss.survey.api.proto.HttpSurveyStartupConfigRequest;
import com.google.android.as.oss.survey.api.proto.HttpSurveyTriggerRequest;
import com.google.android.as.oss.survey.api.proto.ResponseBodyChunk;
import com.google.android.as.oss.survey.api.proto.ResponseHeaders;
import com.google.android.as.oss.survey.api.proto.Session;
import com.google.android.as.oss.survey.api.proto.StartupConfigId;
import com.google.android.as.oss.survey.api.proto.SurveyAppId;
import com.google.android.as.oss.survey.api.proto.SurveyRecordEventRequest;
import com.google.android.as.oss.survey.api.proto.SurveyServiceGrpc;
import com.google.android.as.oss.survey.api.proto.SurveyStartupConfigRequest;
import com.google.android.as.oss.survey.api.proto.SurveyTriggerId;
import com.google.android.as.oss.survey.api.proto.SurveyTriggerRequest;
import com.google.android.as.oss.survey.api.proto.SurveyTriggerResponse;
import com.google.android.as.oss.survey.api.proto.TriggerContext;
import com.google.android.as.oss.survey.api.proto.UnrecognizedUrlException;
import com.google.android.as.oss.survey.config.PcsSurveyConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status.Code;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/** Bindable Service that handles Survey Https requests to Private Compute Services. */
public class SurveyGrpcBindableService extends SurveyServiceGrpc.SurveyServiceImplBase {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String LIVE_CAPTION_SURVEY_OVERALL_TRIGGER_ID = "";
  private static final String LIVE_CAPTION_STARTUP_CONFIG_KEY = "";

  private static final String SYSTEM_INTELLEGENCE_APP_ID = "com.google.android.as";
  private static final String SYSTEM_INTELLEGENCE_APP_NAME = "Android System Intelligence";

  private final OkHttpClient client;
  private final NetworkUsageLogRepository networkUsageLogRepository;
  private final Executor ioExecutor;
  private final PcsStatsLog pcsStatsLogger;
  private final ConfigReader<PcsSurveyConfig> surveyConfig;
  private final BuildFlavor buildFlavor;
  private final Map<SurveyTriggerId, Session> surveySessionMap;

  @Inject
  SurveyGrpcBindableService(
      OkHttpClient client,
      NetworkUsageLogRepository networkUsageLogRepository,
      @IoExecutorQualifier Executor ioExecutor,
      PcsStatsLog pcsStatsLogger,
      ConfigReader<PcsSurveyConfig> surveyConfig,
      BuildFlavor buildFlavor) {
    this.client = client;
    this.networkUsageLogRepository = networkUsageLogRepository;
    this.pcsStatsLogger = pcsStatsLogger;
    this.surveyConfig = surveyConfig;
    this.buildFlavor = buildFlavor;
    this.ioExecutor = ioExecutor;
    this.surveySessionMap = new HashMap<>();
  }

  /**
   * Requests survey from HaTS server.
   *
   * @param request the request which includes survey's triggerId and API key.
   * @param responseObserver observer for handling the response results.
   */
  @Override
  public void requestSurvey(
      HttpSurveyTriggerRequest request, StreamObserver<HttpSurveyResponse> responseObserver) {

    ListenableFuture<HttpSurveyResponse> requestSurveyFuture =
        handleSurveyRequestInternal(
            request.getUrl(),
            request.getRequestPropertyList(),
            generateSurveyTriggerRequest(request),
            surveyConfig,
            networkUsageLogRepository,
            pcsStatsLogger,
            buildFlavor,
            client);

    Futures.addCallback(
        requestSurveyFuture,
        new FutureCallback<HttpSurveyResponse>() {
          @Override
          public void onSuccess(HttpSurveyResponse httpSurveyResponse) {
            responseObserver.onNext(httpSurveyResponse);
            responseObserver.onCompleted();
            updateSurveySession(request.getSurveyTriggerId(), httpSurveyResponse);
          }

          @Override
          public void onFailure(Throwable t) {
            responseObserver.onError(t);
          }
        },
        directExecutor());
  }

  /**
   * Startup configs before requesting survey data.
   *
   * @param request request with survey startup configs.
   * @param responseObserver observer for handling the response results.
   */
  @Override
  public void startupConfig(
      HttpSurveyStartupConfigRequest request, StreamObserver<HttpSurveyResponse> responseObserver) {

    ListenableFuture<HttpSurveyResponse> startupConfigFuture =
        handleSurveyRequestInternal(
            request.getUrl(),
            request.getRequestPropertyList(),
            generateSurveyStartupConfigRequest(request),
            surveyConfig,
            networkUsageLogRepository,
            pcsStatsLogger,
            buildFlavor,
            client);

    Futures.addCallback(
        startupConfigFuture,
        new FutureCallback<HttpSurveyResponse>() {
          @Override
          public void onSuccess(HttpSurveyResponse httpSurveyResponse) {
            responseObserver.onNext(httpSurveyResponse);
            responseObserver.onCompleted();
          }

          @Override
          public void onFailure(Throwable t) {
            responseObserver.onError(t);
          }
        },
        directExecutor());
  }

  /**
   * Uploads the survey results to server.
   *
   * @param surveyTriggerId the trigger id of the survey.
   * @param requestList the request list which includes the survey results.
   * @param responseObserver observer for handling the response results.
   */
  void uploadSurvey(
      SurveyTriggerId surveyTriggerId,
      HttpSurveyRecordEventRequestList requestList,
      StreamObserver<HttpSurveyResponse> responseObserver) {

    if (surveyTriggerId == null || !surveySessionMap.containsKey(surveyTriggerId)) {
      responseObserver.onError(new IllegalArgumentException("Survey session not found"));
      return;
    }

    ImmutableList<ListenableFuture<HttpSurveyResponse>> requestFutures =
        requestList.getRequestsList().stream()
            .map(
                request ->
                    Futures.submitAsync(
                        () ->
                            handleSurveyRequestInternal(
                                request.getUrl(),
                                request.getRequestPropertyList(),
                                generateSurveyRecordEventRequest(
                                    request, surveySessionMap.get(surveyTriggerId)),
                                surveyConfig,
                                networkUsageLogRepository,
                                pcsStatsLogger,
                                buildFlavor,
                                client),
                        ioExecutor))
            .collect(toImmutableList());

    ListenableFuture<List<HttpSurveyResponse>> futureList = Futures.allAsList(requestFutures);
    Futures.addCallback(
        futureList,
        new FutureCallback<List<HttpSurveyResponse>>() {
          @Override
          public void onSuccess(List<HttpSurveyResponse> result) {
            responseObserver.onCompleted();
          }

          @Override
          public void onFailure(Throwable t) {
            responseObserver.onError(t);
          }
        },
        directExecutor());
  }

  @VisibleForTesting
  @Nullable
  Session getSurveySession(SurveyTriggerId surveyTriggerId) {
    if (surveyTriggerId == null) {
      return null;
    }
    return surveySessionMap.get(surveyTriggerId);
  }

  private static ListenableFuture<HttpSurveyResponse> handleSurveyRequestInternal(
      String url,
      List<HttpProperty> httpPropertyList,
      byte[] requestBody,
      ConfigReader<PcsSurveyConfig> surveyConfig,
      NetworkUsageLogRepository networkUsageLogRepository,
      PcsStatsLog pcsStatsLogger,
      BuildFlavor buildFlavor,
      OkHttpClient client) {

    SettableFuture<HttpSurveyResponse> settableFuture = SettableFuture.create();

    if (!isConfigEnabled(settableFuture, surveyConfig) || !isValidHttpsUrl(url, settableFuture)) {
      return settableFuture;
    }
    // Log Unrecognized requests
    logUnrecognizedRequest(url, networkUsageLogRepository, pcsStatsLogger, buildFlavor);
    if (shouldRejectRequest(url, settableFuture, networkUsageLogRepository)) {
      return settableFuture;
    }

    Request okRequest = buildOkHttpRequest(url, httpPropertyList, requestBody);
    Response response;
    try {
      response = client.newCall(okRequest).execute();

      if (response.isSuccessful()) {

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
          insertNetworkUsageLogRow(networkUsageLogRepository, url, Status.FAILED, 0L);
          settableFuture.setException(new Exception("Survey response is null"));
        } else {
          byte[] responseBytes = responseBody.bytes();
          insertNetworkUsageLogRow(
              networkUsageLogRepository, url, Status.SUCCEEDED, responseBytes.length);
          settableFuture.set(
              HttpSurveyResponse.newBuilder()
                  .setResponseHeaders(buildResponseHeaders(response))
                  .setResponseBodyChunk(
                      ResponseBodyChunk.newBuilder()
                          .setResponseBytes(ByteString.copyFrom(responseBytes))
                          .build())
                  .build());
        }
      } else {
        insertNetworkUsageLogRow(networkUsageLogRepository, url, Status.FAILED, 0L);
        settableFuture.setException(new Exception("Survey request failed"));
      }

    } catch (IOException e) {
      insertNetworkUsageLogRow(networkUsageLogRepository, url, Status.FAILED, 0L);
      settableFuture.setException(e);
    }
    return settableFuture;
  }

  private static boolean isConfigEnabled(
      SettableFuture<HttpSurveyResponse> settableFuture,
      ConfigReader<PcsSurveyConfig> surveyConfig) {
    if (!surveyConfig.getConfig().enableSurvey()) {
      logger.atWarning().log("Survey feature is not enabled");
      settableFuture.setException(new IllegalArgumentException("Survey feature is not enabled."));
      return false;
    }
    return true;
  }

  private static boolean isValidHttpsUrl(
      String url, SettableFuture<HttpSurveyResponse> settableFuture) {
    boolean isValidHttpsUrl = url.startsWith("https://");
    if (!isValidHttpsUrl) {
      logger.atWarning().log("Rejected non HTTPS url request to PCS");
      settableFuture.setException(
          new IllegalArgumentException(String.format("Rejecting non HTTPS url: '%s'", url)));
    }
    return isValidHttpsUrl;
  }

  private static void logUnrecognizedRequest(
      String url,
      NetworkUsageLogRepository networkUsageLogRepository,
      PcsStatsLog pcsStatsLogger,
      BuildFlavor buildFlavor) {
    if (!networkUsageLogRepository.isKnownConnection(ConnectionType.SURVEY_REQUEST, url)) {
      pcsStatsLogger.logIntelligenceCountReported(
          // Unrecognised request
          IntelligenceCountReported.newBuilder()
              .setCountMetricId(CountMetricId.PCS_NETWORK_USAGE_LOG_UNRECOGNISED_REQUEST)
              .build());

      if (buildFlavor.isInternal()) {
        // Log the exact key that is unrecognized
        pcsStatsLogger.logIntelligenceUnrecognisedNetworkRequestReported(
            IntelligenceUnrecognisedNetworkRequestReported.newBuilder()
                .setConnectionType(
                    IntelligenceUnrecognisedNetworkRequestReported.ConnectionType.HTTP)
                .setConnectionKey(url)
                .build());
      }
      logger.atInfo().log("Network usage log unrecognised HTTPS request for %s", url);
    }
  }

  private static boolean shouldRejectRequest(
      String url,
      SettableFuture<HttpSurveyResponse> settableFuture,
      NetworkUsageLogRepository networkUsageLogRepository) {
    if (networkUsageLogRepository.shouldRejectRequest(ConnectionType.SURVEY_REQUEST, url)) {
      UnrecognizedNetworkRequestException exception =
          UnrecognizedNetworkRequestException.forUrl(url);
      logger.atWarning().withCause(exception).log("Rejected unknown HTTPS request to PCS");

      com.google.rpc.Status.Builder statusProtoBuilder =
          com.google.rpc.Status.newBuilder()
              .setCode(Code.INVALID_ARGUMENT.value())
              .addDetails(
                  GrpcStatusProto.packIntoAny(
                      UnrecognizedUrlException.newBuilder().setUrl(url).build()));
      if (exception.getMessage() != null) {
        statusProtoBuilder.setMessage(exception.getMessage());
      }

      settableFuture.setException(
          GrpcStatusProto.toStatusRuntimeException(statusProtoBuilder.build()));
      return true;
    }
    return false;
  }

  private static Request buildOkHttpRequest(
      String url, List<HttpProperty> httpPropertyList, byte[] requestBody) {
    Request.Builder okRequest =
        new Request.Builder()
            .url(url)
            .post(RequestBody.create(MediaType.parse("application/x-protobuf"), requestBody));

    for (HttpProperty property : httpPropertyList) {
      for (String value : property.getValueList()) {
        okRequest.addHeader(property.getKey(), value);
      }
    }
    return okRequest.build();
  }

  private static ResponseHeaders buildResponseHeaders(Response okHttpResponse) {
    ResponseHeaders.Builder responseHeaders =
        ResponseHeaders.newBuilder().setResponseCode(okHttpResponse.code());
    for (String name : okHttpResponse.headers().names()) {
      responseHeaders.addHeader(
          HttpProperty.newBuilder()
              .setKey(name)
              .addAllValue(okHttpResponse.headers().values(name))
              .build());
    }
    return responseHeaders.build();
  }

  private static void insertNetworkUsageLogRow(
      NetworkUsageLogRepository networkUsageLogRepository, String url, Status status, long size) {
    if (!networkUsageLogRepository.shouldLogNetworkUsage(ConnectionType.SURVEY_REQUEST, url)
        || networkUsageLogRepository.getContentMap().isEmpty()) {
      return;
    }

    ConnectionDetails connectionDetails =
        networkUsageLogRepository.getContentMap().get().getSurveyConnectionDetails(url).get();
    NetworkUsageEntity entity =
        NetworkUsageLogUtils.createSurveyNetworkUsageEntity(connectionDetails, status, size, url);

    networkUsageLogRepository.insertNetworkUsageEntity(entity);
  }

  private void updateSurveySession(SurveyTriggerId surveyTriggerId, HttpSurveyResponse response) {
    try {
      SurveyTriggerResponse surveyTriggerResponse =
          SurveyTriggerResponse.parseFrom(
              response.getResponseBodyChunk().getResponseBytes(),
              ExtensionRegistryLite.getGeneratedRegistry());
      surveySessionMap.put(surveyTriggerId, surveyTriggerResponse.getSession());

    } catch (InvalidProtocolBufferException e) {
      logger.atWarning().withCause(e).log("Failed to parse survey trigger response.");
    }
  }

  private static byte[] generateSurveyTriggerRequest(HttpSurveyTriggerRequest request) {

    String timezone = TimeZone.getDefault().getID();
    long timezoneOffsetInSeconds =
        MILLISECONDS.toSeconds(TimeZone.getTimeZone(timezone).getRawOffset());
    Duration duration = Duration.newBuilder().setSeconds(timezoneOffsetInSeconds).build();

    return SurveyTriggerRequest.newBuilder()
        .setTriggerContext(
            TriggerContext.newBuilder()
                .setTriggerId(getTriggerId(request.getSurveyTriggerId()))
                .addLanguage(Locale.getDefault().toLanguageTag())
                .setTestingMode(request.getTestingMode())
                .build())
        .setClientContext(
            ClientContext.newBuilder()
                .setDeviceInfo(
                    DeviceInfo.newBuilder()
                        .setMobileInfo(
                            MobileInfo.newBuilder()
                                .setDeviceModel(Build.MODEL)
                                .setOsVersion(Build.VERSION.RELEASE)
                                .setOsType(OS_TYPE_ANDROID)
                                .setAppId(getSurveyAppId(request.getSurveyAppId()))
                                .setAppName(getSurveyAppName(request.getSurveyAppId()))
                                .setAppVersion(String.valueOf(request.getAppVersion())))
                        .setTimezoneOffset(duration))
                .setLibraryInfo(
                    LibraryInfo.newBuilder()
                        .setPlatform(Platform.PLATFORM_ANDROID)
                        .addSupportedCapability(CLIENT_CAPABILITY_UNKNOWN)
                        .setLibraryVersionInt(request.getLibraryVersion())))
        .build()
        .toByteArray();
  }

  private static byte[] generateSurveyRecordEventRequest(
      HttpSurveyRecordEventRequest request, Session surveySession) {
    return SurveyRecordEventRequest.newBuilder()
        .setSession(surveySession)
        .setEvent(request.getEvent())
        .build()
        .toByteArray();
  }

  private static byte[] generateSurveyStartupConfigRequest(HttpSurveyStartupConfigRequest request) {
    return SurveyStartupConfigRequest.newBuilder()
        .setApiKey(getStartUpConfigKey(request.getStartupConfigId()))
        .setPlatform(Platform.PLATFORM_ANDROID)
        .build()
        .toByteArray();
  }

  private static String getTriggerId(SurveyTriggerId surveyTriggerId) {
    switch (surveyTriggerId) {
      case SURVEY_TRIGGER_ID_LIVE_CAPTION_OVERALL_SATISFACTION:
        return LIVE_CAPTION_SURVEY_OVERALL_TRIGGER_ID;
      default:
        throw new IllegalArgumentException("Unknown survey trigger id");
    }
  }

  private static String getSurveyAppId(SurveyAppId surveyAppId) {
    switch (surveyAppId) {
      case SURVEY_APP_ID_ASI:
        return SYSTEM_INTELLEGENCE_APP_ID;
      default:
        throw new IllegalArgumentException("Unknown survey app id for getSurveyAppId");
    }
  }

  private static String getSurveyAppName(SurveyAppId surveyAppId) {
    switch (surveyAppId) {
      case SURVEY_APP_ID_ASI:
        return SYSTEM_INTELLEGENCE_APP_NAME;
      default:
        throw new IllegalArgumentException("Unknown survey app id for getSurveyAppName");
    }
  }

  private static String getStartUpConfigKey(StartupConfigId startUpConfigId) {
    switch (startUpConfigId) {
      case STARTUP_CONFIG_ID_LIVE_CAPTION:
        return LIVE_CAPTION_STARTUP_CONFIG_KEY;
      default:
        throw new IllegalArgumentException("Unknown startup config id");
    }
  }
}
