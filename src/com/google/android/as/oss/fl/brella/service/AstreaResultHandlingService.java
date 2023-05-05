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

package com.google.android.as.oss.fl.brella.service;

import static com.google.android.as.oss.fl.federatedcompute.util.ClassConversionUtils.copyExampleConsumptionList;
import static com.google.android.as.oss.fl.federatedcompute.util.ClassConversionUtils.copyTrainerOptions;

import android.os.IInterface;
import android.os.RemoteException;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.Annotations.AsiPackageName;
import com.google.android.as.oss.fl.Annotations.GppsPackageName;
import com.google.android.as.oss.fl.Annotations.ResultHandlingClientsInfo;
import com.google.android.as.oss.fl.brella.api.IInAppResultHandler;
import com.google.android.as.oss.fl.brella.api.StatusCallback;
import com.google.android.as.oss.fl.brella.service.ConnectionManager.ConnectionType;
import com.google.android.as.oss.fl.localcompute.LocalComputeResourceManager;
import com.google.android.as.oss.fl.localcompute.LocalComputeUtils;
import com.google.android.as.oss.fl.localcompute.PathConversionUtils;
import com.google.fcp.client.common.api.Status;
import com.google.fcp.client.ExampleConsumption;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.fcp.client.ResultHandlingService;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of federated compute's {@link ResultHandlingService} that forwards calls to the
 * appropriate delegate that forwards calls to the appropriate delegate in client based on the
 * passed-in options.
 */
@AndroidEntryPoint(ResultHandlingService.class)
public class AstreaResultHandlingService extends Hilt_AstreaResultHandlingService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Inject @ResultHandlingClientsInfo ImmutableMap<String, String> packageToActionMap;
  @Inject @AsiPackageName String asiPackageName;
  @Inject @GppsPackageName String gppsPackageName;
  @VisibleForTesting @Inject @FlExecutorQualifier Executor flExecutor;
  @Inject Optional<LocalComputeResourceManager> resourceManager;

  @VisibleForTesting ConnectionManager connectionManager;

  @Override
  public void onCreate() {
    super.onCreate();
    logger.atFine().log("AstreaResultHandlingService.onCreate()");
    connectionManager =
        new ConnectionManager(
            this,
            packageToActionMap,
            ConnectionType.RESULT_HANDLER,
            asiPackageName,
            gppsPackageName);
  }

  @Override
  public void onDestroy() {
    connectionManager.unbindService();
    super.onDestroy();
  }

  @Override
  public void handleResult(
      InAppTrainerOptions trainerOptions,
      boolean success,
      List<ExampleConsumption> exampleConsumptionList,
      ResultHandlingCallback callback) {
    @Nullable String clientName = connectionManager.getClientName(trainerOptions);
    if (!success
        || exampleConsumptionList == null
        || exampleConsumptionList.isEmpty()
        || clientName == null) {
      logger.atFine().log("ResultHandlingService callback unsuccessful");
      callback.onResult(Status.RESULT_INTERNAL_ERROR);
      return;
    }

    if (trainerOptions.getPersonalizationPlan() != null) {
      // If it's a local computation task, copy the output files back to ASI then handle the result.
      ListenableFuture<IInterface> initializeServiceFuture =
          Futures.transformAsync(
              LocalComputeUtils.handleLocalComputeOutput(
                  trainerOptions, clientName, resourceManager),
              unused -> connectionManager.initializeServiceConnection(clientName),
              flExecutor);
      InAppTrainerOptions convertedTrainerOptions =
          PathConversionUtils.trimLocalComputePathPrefix(trainerOptions);
      handleResultAfterConnection(
          initializeServiceFuture,
          convertedTrainerOptions,
          success,
          exampleConsumptionList,
          callback,
          clientName);
    } else {
      ListenableFuture<IInterface> initializeServiceFuture =
          connectionManager.initializeServiceConnection(clientName);
      handleResultAfterConnection(
          initializeServiceFuture,
          trainerOptions,
          success,
          exampleConsumptionList,
          callback,
          clientName);
    }
  }

  private void handleResultAfterConnection(
      ListenableFuture<IInterface> initializeServiceFuture,
      InAppTrainerOptions trainerOptions,
      boolean success,
      List<ExampleConsumption> exampleConsumptionList,
      ResultHandlingCallback callback,
      String clientName) {
    Futures.addCallback(
        initializeServiceFuture,
        new FutureCallback<IInterface>() {
          @Override
          public void onSuccess(IInterface result) {
            try {
              IInAppResultHandler binder = (IInAppResultHandler) result;
              binder.handleResult(
                  copyTrainerOptions(trainerOptions),
                  success,
                  copyExampleConsumptionList(exampleConsumptionList),
                  new StatusCallback(callback));
            } catch (RemoteException e) {
              connectionManager.resetClient(clientName);
              // We don't expect client to actually throw any RemoteExceptions.
              // If it does happen for some reason then all we can do now is log the exception
              // and swallow it.
              logger.atWarning().withCause(e).log("Failed to delegate handleResult.");
              callback.onResult(Status.RESULT_INTERNAL_ERROR);
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().log("Failed to bind to service");
            connectionManager.resetClient(clientName);
            callback.onResult(Status.RESULT_INTERNAL_ERROR);
          }
        },
        flExecutor);
  }
}
