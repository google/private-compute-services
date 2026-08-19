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

package com.google.android.as.oss.fl.server;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.util.concurrent.Futures.immediateFailedFuture;

import android.content.Context;
import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.api.proto.ClientStreamMessage;
import com.google.android.as.oss.fl.api.proto.GetDataRequest;
import com.google.android.as.oss.fl.api.proto.GetDataResponse;
import com.google.android.as.oss.fl.api.proto.PrivateLoggingServiceGrpc;
import com.google.android.as.oss.fl.api.proto.ServiceStreamMessage;
import com.google.android.as.oss.fl.api.proto.UploadFinishedResponse;
import com.google.android.as.oss.fl.api.proto.UploadOutcome;
import com.google.android.as.oss.fl.api.proto.UploadRequest;
import com.google.android.as.oss.fl.fc.service.scheduler.Annotations.PrivateLoggerFcpInvoker;
import com.google.android.as.oss.fl.fc.service.scheduler.FcpContributionResultInfo;
import com.google.android.as.oss.fl.fc.service.scheduler.FcpInvocation;
import com.google.android.as.oss.fl.fc.service.scheduler.FcpInvocationCallback;
import com.google.android.as.oss.fl.fc.service.scheduler.FcpInvocationOptions;
import com.google.android.as.oss.fl.fc.service.scheduler.FcpLoggerInvoker;
import com.google.android.as.oss.fl.fc.service.scheduler.endorsementoptions.EndorsementClientType;
import com.google.android.as.oss.fl.fc.service.scheduler.endorsementoptions.EndorsementOptionsProvider;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.android.as.oss.proto.PcsFeatureEnum.FeatureName;
import com.google.fcp.client.tasks.Task;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.SettableFuture;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import com.google.fcp.client.privatelogger.LogEntry;
import com.google.fcp.client.privatelogger.impl.DataProvider;
import com.google.protobuf.ByteString;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A gRPC service that handles log uploads for the Private Logger.
 *
 * <p>This service enables cross-process communication for the Private Logger API, allowing PCC apps
 * to upload data held in their app via the FCP library running in PCS. It acts as a go-between for
 * {@link com.google.fcp.client.privatelogger.PrivateLogger} running in the PCC app and PCS's upload
 * mechanism via FCP. It mirrors the {@link DataProvider} interface but adapts it for gRPC, handling
 * the {@link DataProvider} callback via bidi-streaming.
 *
 * <p>When a client (a PCC app) initiates an upload, this service uses {@link FcpLoggerInvoker} to
 * start an upload task in PCS. When FCP requests data during the upload, this service sends a
 * {@link GetDataRequest} to the client via the stream, and expects a {@link GetDataResponse} in
 * return. Once the FCP upload is complete, the service sends an {@link UploadFinishedResponse}
 * containing {@link UploadOutcome}s indicating whether the task contribution was successful, and
 * then closes the stream.
 */
public class PrivateLoggerGrpcBindableService
    extends PrivateLoggingServiceGrpc.PrivateLoggingServiceImplBase {
  private final EndorsementOptionsProvider endorsementOptionsProvider;
  private final ExecutorService executorService;
  private final Context context;
  private final FcpLoggerInvoker fcpLoggerInvoker;
  private final NetworkUsageLogRepository networkUsageLogRepository;
  private final CallingPackageNameProvider callingPackageNameProvider;

  @Inject
  PrivateLoggerGrpcBindableService(
      EndorsementOptionsProvider endorsementOptionsProvider,
      ScheduledExecutorService executorService,
      Context context,
      FcpLoggerInvoker fcpLoggerInvoker,
      NetworkUsageLogRepository networkUsageLogRepository,
      CallingPackageNameProvider callingPackageNameProvider) {
    this.endorsementOptionsProvider = endorsementOptionsProvider;
    this.executorService = executorService;
    this.context = context;
    this.fcpLoggerInvoker = fcpLoggerInvoker;
    this.networkUsageLogRepository = networkUsageLogRepository;
    this.callingPackageNameProvider = callingPackageNameProvider;
  }

  @Override
  public StreamObserver<ClientStreamMessage> upload(
      StreamObserver<ServiceStreamMessage> responseObserver) {
    return new UploadStreamObserver(
        context,
        endorsementOptionsProvider,
        executorService,
        responseObserver,
        fcpLoggerInvoker,
        networkUsageLogRepository,
        callingPackageNameProvider);
  }

  /**
   * An observer that manages the lifecycle of a single upload stream between a PCS client and FCP.
   *
   * <p><b>Lifecycle:</b> A new instance of {@code UploadStreamObserver} is created for each gRPC
   * {@code upload()} call received by {@link PrivateLoggerGrpcBindableService}. This corresponds to
   * one {@code PccPrivateLogger#upload()} call from a client app. The observer instance lives for
   * the duration of that specific upload stream, until {@link #onCompleted()} or {@link
   * #onError(Throwable)} is called.
   *
   * <p><b>Functionality:</b> This class acts as the bridge between the gRPC stream and the FCP
   * upload process:
   *
   * <ul>
   *   <li>It receives the initial {@link UploadRequest} from the client via {@link
   *       #onNext(ClientStreamMessage)} and triggers a FCP upload using {@link FcpLoggerInvoker}.
   *   <li>When FCP requests data, the {@link DataProvider} passed to {@link
   *       PrivateLoggerExampleStoreAdapter} is invoked. This implementation of {@code DataProvider}
   *       sends a {@link GetDataRequest} to the client via the {@code responseObserver} and pauses
   *       the FCP thread by returning a {@link SettableFuture} ({@code pendingGetDataFuture}).
   *   <li>When the client responds with a {@link GetDataResponse} via {@link
   *       #onNext(ClientStreamMessage)}, this observer completes the {@code pendingGetDataFuture}
   *       with the received data, allowing the FCP upload to resume.
   *   <li>Once the FCP upload finishes, the {@code InvocationCallback} sends the final {@link
   *       UploadFinishedResponse} to the client via {@code responseObserver} and calls {@link
   *       #onCompleted()} on it.
   * </ul>
   */
  private static class UploadStreamObserver implements StreamObserver<ClientStreamMessage> {
    private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

    private enum State {
      // Waiting for UploadRequest
      EXPECTING_UPLOAD_REQUEST,
      // Waiting for GetDataResponse
      EXPECTING_GET_DATA_RESPONSE,
      // End state
      DONE
    }

    private final Context context;
    private final EndorsementOptionsProvider endorsementOptionsProvider;
    private final ExecutorService executorService;
    private final StreamObserver<ServiceStreamMessage> responseObserver;
    private final FcpLoggerInvoker fcpLoggerInvoker;
    private final NetworkUsageLogRepository networkUsageLogRepository;
    private final CallingPackageNameProvider callingPackageNameProvider;

    /**
     * Holds the future for a pending {@code getData} request.
     *
     * <p>This future is returned by the {@link DataProvider} lambda when FCP requests data. It is
     * completed when a {@link GetDataResponse} is received from the client via {@link
     * #handleGetDataResponse(GetDataResponse)}, or when the stream is terminated via {@link
     * #onError(Throwable)} or {@link #onCompleted()}.
     */
    private final SettableFuture<List<LogEntry>> getDataFuture = SettableFuture.create();

    private final Object lock = new Object();

    @GuardedBy("lock")
    private @Nullable FcpInvocation activeInvocation;

    @GuardedBy("lock")
    private State state = State.EXPECTING_UPLOAD_REQUEST;

    UploadStreamObserver(
        Context context,
        EndorsementOptionsProvider endorsementOptionsProvider,
        ExecutorService executorService,
        StreamObserver<ServiceStreamMessage> responseObserver,
        FcpLoggerInvoker fcpLoggerInvoker,
        NetworkUsageLogRepository networkUsageLogRepository,
        CallingPackageNameProvider callingPackageNameProvider) {
      this.context = context;
      this.endorsementOptionsProvider = endorsementOptionsProvider;
      this.executorService = executorService;
      this.responseObserver = responseObserver;
      this.fcpLoggerInvoker = fcpLoggerInvoker;
      this.networkUsageLogRepository = networkUsageLogRepository;
      this.callingPackageNameProvider = callingPackageNameProvider;
    }

    @Override
    public void onNext(ClientStreamMessage value) {

      // We only process one incoming message at a time, and only release the lock after each
      // message has been fully processed.
      synchronized (lock) {
        if (state == State.DONE) {
          return;
        }
        switch (value.getMessageTypeCase()) {
          case UPLOAD_REQUEST -> {
            if (state != State.EXPECTING_UPLOAD_REQUEST) {
              state = State.DONE;
              responseObserver.onError(
                  Status.FAILED_PRECONDITION
                      .withDescription("UPLOAD_REQUEST unexpected in state " + state)
                      .asRuntimeException());
              return;
            }
            state = State.EXPECTING_GET_DATA_RESPONSE;
            handleUploadRequest(value.getUploadRequest());
          }
          case GET_DATA_RESPONSE -> {
            if (state != State.EXPECTING_GET_DATA_RESPONSE) {
              state = State.DONE;
              responseObserver.onError(
                  Status.FAILED_PRECONDITION
                      .withDescription("GET_DATA_RESPONSE unexpected in state " + state)
                      .asRuntimeException());
              return;
            }
            handleGetDataResponse(value.getGetDataResponse());
          }
          case MESSAGETYPE_NOT_SET -> {
            state = State.DONE;
            responseObserver.onError(
                Status.INVALID_ARGUMENT
                    .withDescription("ClientStreamMessage received with no message type set.")
                    .asRuntimeException());
          }
        }
      }
    }

    @Override
    public void onError(Throwable t) {
      synchronized (lock) {
        if (state == State.DONE) {
          return;
        }
        state = State.DONE;
      }
      cancelAnyActiveInvocation(t);
    }

    @Override
    public void onCompleted() {
      synchronized (lock) {
        if (state == State.DONE) {
          return;
        }
        state = State.DONE;
      }
      cancelAnyActiveInvocation(null);
      responseObserver.onCompleted();
    }

    private void cancelAnyActiveInvocation(@Nullable Throwable t) {
      FcpInvocation invocation;
      synchronized (lock) {
        invocation = activeInvocation;
        activeInvocation = null;
      }
      if (invocation != null) {
        var unused = invocation.cancel();
      }
      if (t != null) {
        getDataFuture.setException(t);
      } else {
        getDataFuture.cancel(false);
      }
    }

    private void handleUploadRequest(UploadRequest request) {
      String featureName = request.getFeatureName().name();
      String callingPackageName = callingPackageNameProvider.getCallingPackageName(context);
      // If there is no mapping in the network usage log repository for this feature and this
      // connection type, then this egress is not allowed.
      if (networkUsageLogRepository.shouldRejectRequest(FC_TRAINING_START_QUERY, featureName)) {
        synchronized (lock) {
          state = State.DONE;
        }
        responseObserver.onError(
            Status.PERMISSION_DENIED
                .withDescription(String.format("Not allowed to egress for '%s'", featureName))
                .asRuntimeException());
        return;
      }
      DataProvider grpcDataProvider =
          selectorContext -> {
            if (!selectorContext
                .getComputationProperties()
                .getFederated()
                .hasConfidentialAggregation()) {
              synchronized (lock) {
                state = State.DONE;
              }
              String errorMessage =
                  "Incorrectly configured computation attempted to read from PrivateLogger. All "
                      + "computations reading from a PrivateLogger must use confidential "
                      + "aggregation.";
              responseObserver.onError(
                  Status.FAILED_PRECONDITION.withDescription(errorMessage).asRuntimeException());
              return immediateFailedFuture(new IllegalStateException(errorMessage));
            }
            // Allowed to upload, so add to the network usage log before proceeding.
            if (networkUsageLogRepository.isNetworkUsageLogEnabled()) {
              networkUsageLogRepository
                  .getDbExecutor()
                  .ifPresent(
                      executor ->
                          executor.execute(
                              () ->
                                  networkUsageLogRepository.insertNetworkUsageEntity(
                                      NetworkUsageLogUtils
                                          .createFcTrainingStartQueryNetworkUsageEntity(
                                              NetworkUsageLogUtils
                                                  .createFcTrainingStartQueryConnectionDetails(
                                                      featureName, callingPackageName),
                                              selectorContext.getComputationProperties().getRunId(),
                                              Optional.absent() /* policyProto */))));
            }
            synchronized (lock) {
              // If FCP calls this DataProvider after the RPC session was cancelled and transitioned
              // to DONE (e.g. via onError()), we must not call responseObserver.onNext(), as the
              // observer may already be closed, which would cause an IllegalStateException. This
              // state check prevents that by returning a failed future instead.
              if (state != State.EXPECTING_GET_DATA_RESPONSE) {
                return immediateFailedFuture(
                    new IllegalStateException("FCP requested data in unexpected state " + state));
              }
              responseObserver.onNext(
                  ServiceStreamMessage.newBuilder()
                      .setGetDataRequest(
                          GetDataRequest.newBuilder().setSelectorContext(selectorContext))
                      .build());
            }
            // We don't set a timeout here because the caller of DataProvider will already properly
            // time out if no data is provided after a certain amount of time.
            return getDataFuture;
          };

      EndorsementClientType clientType =
          shouldUseCobaltEndorsementOptions(request.getFeatureName())
              ? EndorsementClientType.COBALT_KEY
              : EndorsementClientType.PRIVATE_COMPUTE_SERVICES_DEFAULT_KEY;
      byte[] endorsementOptions =
          endorsementOptionsProvider.getEndorsementOptions(context, clientType);

      FcpInvocationOptions options =
          FcpInvocationOptions.builder()
              .setSessionName(request.getPrivateLogSourceName())
              .setPopulationName(request.getPrivateLogSourceName())
              .setAccessPolicyEndorsementOptions(ByteString.copyFrom(endorsementOptions))
              .build();

      final List<UploadOutcome> outcomes = Collections.synchronizedList(new ArrayList<>());
      FcpInvocationCallback invocationCallback =
          new FcpInvocationCallback() {
            @Override
            public void onComputationCompleted(FcpContributionResultInfo resultInfo) {
              synchronized (lock) {
                // If FCP finishes a computation while we are waiting for data from the client,
                // cancel the future to ensure we don't hang.
                getDataFuture.cancel(false);
              }
              UploadOutcome.Builder outcomeBuilder =
                  UploadOutcome.newBuilder().setTaskName(resultInfo.taskName());
              if (resultInfo.isSuccess()) {
                outcomeBuilder.setStatus(UploadOutcome.Status.CONTRIBUTED);
              } else {
                outcomeBuilder.setStatus(UploadOutcome.Status.NOT_CONTRIBUTED);
              }
              outcomes.add(outcomeBuilder.build());
            }

            @Override
            public void onInvocationFinished() {
              synchronized (lock) {
                if (state == State.DONE) {
                  return;
                }
                state = State.DONE;
                UploadFinishedResponse response =
                    UploadFinishedResponse.newBuilder().addAllOutcomes(outcomes).build();
                // We've completed the invocation, so clear the `activeInvocation`.
                activeInvocation = null;
                // Return the result to the client.
                responseObserver.onNext(
                    ServiceStreamMessage.newBuilder().setUploadFinishedResponse(response).build());
                responseObserver.onCompleted();
              }
            }
          };

      // Trigger invocation.
      Task<FcpInvocation> invocationTask =
          fcpLoggerInvoker.upload(
              context, executorService, options, invocationCallback, grpcDataProvider);

      // Once the invocation has started, store a handle to it in `activeInvocation` so it can be
      // canceled later if needed.
      invocationTask.addOnCompleteListener(
          executorService,
          task -> {
            if (task.isSuccessful()) {
              synchronized (lock) {
                activeInvocation = task.getResult();
              }
            } else {
              Exception e = task.getException();
              synchronized (lock) {
                if (state == State.DONE) {
                  return;
                }
                state = State.DONE;
              }
              if (e != null) {
                responseObserver.onError(
                    Status.INTERNAL
                        .withDescription("FcpLoggerInvoker upload task failed")
                        .withCause(e)
                        .asRuntimeException());
              } else if (!task.isCanceled()) {
                responseObserver.onError(
                    Status.INTERNAL
                        .withDescription("FcpLoggerInvoker upload task failed without exception")
                        .asRuntimeException());
              }
            }
          });
    }

    private void handleGetDataResponse(GetDataResponse response) {
      if (getDataFuture.isDone()) {
        logger.atWarning().log("Received `GetDataResponse` but the future is already done.");
      }
      ImmutableList<LogEntry> entries =
          response.getEntriesList().stream()
              .map(
                  protoEntry ->
                      LogEntry.builder()
                          .setTimestamp(protoEntry.getTimestamp())
                          .setValue(protoEntry.getValue())
                          .build())
              .collect(toImmutableList());
      // Note: any GetDataResponse message received after the first such message will effectively
      // be ignored because the getDataFuture will already have been completed with that earlier
      // value and this .set() call would hence be a no-op.
      getDataFuture.set(entries);
    }

    private boolean shouldUseCobaltEndorsementOptions(FeatureName featureName) {
      boolean cobaltEndorsementOptionsEnabled = false;
      if (!cobaltEndorsementOptionsEnabled) {
        return false;
      }
      return featureName.equals(FeatureName.GPPS_FEATURE_NAME);
    }
  }
}
