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

package com.google.android.as.oss.fl.brella.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.IInterface;
import androidx.annotation.GuardedBy;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.base.SafeInitializer;
import com.google.android.as.oss.fl.brella.api.IExampleStore;
import com.google.android.as.oss.fl.brella.api.IInAppResultHandler;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.HashMap;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;

/** Manages client connections for federated compute services in PCS. */
class ConnectionManager {
  enum ConnectionType {
    EXAMPLE_STORE,
    RESULT_HANDLER
  }

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @GuardedBy("this")
  private final Map<String, SafeInitializer<IInterface>> clientInitializerMap;

  @GuardedBy("this")
  private final Map<String, @Nullable ServiceConnection> clientActiveServiceConnectionMap;

  private final ImmutableMap<String, String> packageToActionMap;
  private final Context context;
  private final ConnectionType connectionType;
  private final String asiPackageName;
  private final String gppsPackageName;
  private final PcsStatsLog pcsStatsLogger;

  ConnectionManager(
      Context context,
      ImmutableMap<String, String> packageToActionMap,
      ConnectionType connectionType,
      PcsStatsLog pcsStatsLogger,
      String asiPackageName,
      String gppsPackageName) {
    this.context = context;
    this.packageToActionMap = packageToActionMap;
    this.asiPackageName = asiPackageName;
    this.gppsPackageName = gppsPackageName;
    this.clientActiveServiceConnectionMap = new HashMap<>();
    this.clientInitializerMap = new HashMap<>();
    this.connectionType = connectionType;
    this.pcsStatsLogger = pcsStatsLogger;
  }

  @VisibleForTesting
  ConnectionManager(
      Context context,
      ImmutableMap<String, String> packageToActionMap,
      Map<String, @Nullable ServiceConnection> clientActiveServiceConnectionMap,
      Map<String, SafeInitializer<IInterface>> clientInitializerMap,
      ConnectionType connectionType,
      PcsStatsLog pcsStatsLogger,
      String asiPackageName,
      String gppsPackageName) {
    this.context = context;
    this.packageToActionMap = packageToActionMap;
    this.clientActiveServiceConnectionMap = clientActiveServiceConnectionMap;
    this.clientInitializerMap = clientInitializerMap;
    this.connectionType = connectionType;
    this.pcsStatsLogger = pcsStatsLogger;
    this.asiPackageName = asiPackageName;
    this.gppsPackageName = gppsPackageName;
  }

  synchronized ListenableFuture<IInterface> initializeServiceConnection(String clientName) {
    clientInitializerMap.putIfAbsent(clientName, SafeInitializer.forClass(IInterface.class));
    return clientInitializerMap.get(clientName).initialize(() -> this.initialize(clientName));
  }

  synchronized void unbindService() {
    for (ServiceConnection connection : clientActiveServiceConnectionMap.values()) {
      if (connection != null) {
        // Always unbind if you call bindService: http://[redacted]
        // Sometimes it ends up throwing RemoteException or IllegalStateException.
        try {
          context.unbindService(connection);
        } catch (Throwable ex) {
          logger.atWarning().withCause(ex).log("Unbinding service failed.");
        }
      }
    }
  }

  synchronized void resetClient(String clientName) {
    if (clientInitializerMap.get(clientName) != null) {
      clientInitializerMap.get(clientName).reset();
    }
    @Nullable ServiceConnection activeServiceConnection =
        clientActiveServiceConnectionMap.getOrDefault(clientName, null);
    if (activeServiceConnection != null) {
      // Always unbind if you call bindService: http://[redacted]
      // Sometimes it ends up throwing RemoteException or IllegalStateException.
      try {
        context.unbindService(activeServiceConnection);
      } catch (Throwable ex) {
        logger.atWarning().withCause(ex).log("Unbinding service failed.");
      }
    }
  }

  boolean isClientSupported(String clientName) {
    return packageToActionMap.containsKey(clientName);
  }

  @Nullable String getClientName(InAppTrainerOptions trainerOptions) {
    String populationOrSessionName =
        trainerOptions.getFederatedPopulationName() != null
            ? trainerOptions.getFederatedPopulationName()
            : trainerOptions.getSessionName();
    // ASI population or local computation session prefix.
    if (populationOrSessionName.startsWith("aiai/")) {
      return asiPackageName;
    }

    // GPPS population or local computation session prefix.
    if (populationOrSessionName.startsWith("odad/")) {
      return gppsPackageName;
    }
    return null;
  }

  private synchronized ListenableFuture<IInterface> initialize(String clientName) {
    SettableFuture<IInterface> serviceBindingSettableFuture = SettableFuture.create();
    Intent intent = new Intent();
    intent.setAction(packageToActionMap.get(clientName)).setPackage(clientName);
    ServiceConnection serviceConnection =
        new ServiceConnection() {
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
            logger.atInfo().log(
                "Connected to service: %s.%s", name.getPackageName(), name.getClassName());
            logCounter(CountMetricId.PCS_TRAINING_BINDER_SERVICE_CONNECTED);
            if (connectionType == ConnectionType.EXAMPLE_STORE) {
              IInterface binding = IExampleStore.Stub.asInterface(service);
              serviceBindingSettableFuture.set(binding);
            } else if (connectionType == ConnectionType.RESULT_HANDLER) {
              IInterface binding = IInAppResultHandler.Stub.asInterface(service);
              serviceBindingSettableFuture.set(binding);
            } else {
              serviceBindingSettableFuture.setException(
                  new RuntimeException("Unsupported connection type"));
            }
          }

          @Override
          public void onServiceDisconnected(ComponentName name) {
            logger.atInfo().log("Disconnected from Service.");
            logCounter(CountMetricId.PCS_TRAINING_BINDER_SERVICE_DISCONNECTED);
            markConnectionInvalid(clientName, serviceBindingSettableFuture);
          }

          @Override
          public void onBindingDied(ComponentName name) {
            logger.atInfo().log("Binding to %s died", name);
            logCounter(CountMetricId.PCS_TRAINING_BINDER_DIED);
            markConnectionInvalid(clientName, serviceBindingSettableFuture);
          }

          @Override
          public void onNullBinding(ComponentName name) {
            logger.atInfo().log("Received null binding for %s", name);
            logCounter(CountMetricId.PCS_TRAINING_BINDER_NULL);
            markConnectionInvalid(clientName, serviceBindingSettableFuture);
          }
        };
    clientActiveServiceConnectionMap.put(clientName, serviceConnection);
    if (!context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
      logger.atWarning().log("Failed to bind to service");
      if (clientInitializerMap.get(clientName) != null) {
        clientInitializerMap.get(clientName).reset();
      }
      // Always unbind if you call bindService: http://[redacted]
      // Sometimes it ends up throwing RemoteException or IllegalStateException.
      try {
        context.unbindService(serviceConnection);
      } catch (Throwable ex) {
        logger.atWarning().withCause(ex).log("Unbinding service failed.");
      }
      clientActiveServiceConnectionMap.put(clientName, null);
      return Futures.immediateFailedFuture(
          new RuntimeException("Failed to bind AiAiFederatedDataService"));
    }
    return serviceBindingSettableFuture;
  }

  private void markConnectionInvalid(
      String clientName, SettableFuture<IInterface> serviceBindingSettableFuture) {
    synchronized (ConnectionManager.this) {
      if (!serviceBindingSettableFuture.isDone()) {
        serviceBindingSettableFuture.setException(
            new RuntimeException(String.format("Failed to bind: %s", clientName)));
      }
      this.resetClient(clientName);
    }
  }

  private void logCounter(CountMetricId countMetricId) {
    pcsStatsLogger.logIntelligenceCountReported(
        IntelligenceCountReported.newBuilder().setCountMetricId(countMetricId).build());
  }
}
