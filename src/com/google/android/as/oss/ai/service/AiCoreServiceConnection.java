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

package com.google.android.as.oss.ai.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.concurrent.futures.CallbackToFutureAdapter.Completer;
import com.google.android.apps.aicore.aidl.AiCoreServiceProviderErrorCode;
import com.google.android.apps.aicore.aidl.IAICoreService;
import com.google.android.apps.aicore.aidl.IAiCoreServiceProvider;
import com.google.android.apps.aicore.aidl.IAiCoreServiceProviderCallback;
import com.google.android.as.oss.ai.config.PcsAiConfig;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Object to handle connection with the AICore service. One object represents only one unique binder
 * connection, established as part of initialization. Once the connection dies for any reason, this
 * class will not establish a new connection. To reconnect, users should create a new instance.
 */
final class AiCoreServiceConnection implements ServiceConnection {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String AICORE_PACKAGE_NAME = "com.google.android.aicore";
  private static final String AICORE_SERVICE_NAME =
      "com.google.android.apps.aicore.service.multiuser.AiCoreMultiUserService";
  private static final String PCS_AICORE_CONNECTION_STOPPED =
      "com.google.android.as.oss.AiCoreServiceConnection_STOPPED";

  private final AtomicBoolean disconnected = new AtomicBoolean(false);
  private final Context context;

  private Completer<IAICoreService> completer;
  private ListenableFuture<IAICoreService> serviceFuture;

  public static AiCoreServiceConnection create(
      Context context,
      ListeningScheduledExecutorService executorService,
      ConfigReader<PcsAiConfig> configReader) {
    return new AiCoreServiceConnection(context).initConnect(executorService, configReader);
  }

  private AiCoreServiceConnection(Context context) {
    this.context = context;
  }

  private AiCoreServiceConnection initConnect(
      ListeningScheduledExecutorService executorService, ConfigReader<PcsAiConfig> configReader) {
    ListenableFuture<IAICoreService> localFuture =
        CallbackToFutureAdapter.getFuture(
            completer -> {
              this.completer = completer;
              completer.addCancellationListener(
                  () -> disconnect("AICore<>PCS Connection cancelled, likely timed-out.", false),
                  executorService);

              String action = IAICoreService.class.getCanonicalName();
              Intent aiCoreIntent = new Intent(action);
              aiCoreIntent.setComponent(
                  new ComponentName(AICORE_PACKAGE_NAME, AICORE_SERVICE_NAME));
              if (!context.bindService(aiCoreIntent, this, Context.BIND_AUTO_CREATE)) {
                disconnect("Unable to find/start AICoreService", false);
              }
              return "AiCoreServiceConnection";
            });
    this.serviceFuture =
        Futures.withTimeout(
            localFuture,
            configReader.getConfig().genAiServiceConnectionTimeoutMs(),
            MILLISECONDS,
            executorService);
    return this;
  }

  private boolean isPending() {
    return !serviceFuture.isDone();
  }

  public boolean isValid() {
    return isPending() || isConnected();
  }

  public ListenableFuture<IAICoreService> getServiceFuture() {
    return serviceFuture;
  }

  private boolean isConnected() {
    // No point making all of this atomic since the caller doesn't share the same lock anyway.
    var service = getServiceIfConnected();
    return !disconnected.get()
        && service != null
        && service.asBinder().isBinderAlive()
        && service.asBinder().pingBinder();
  }

  @Nullable
  private IAICoreService getServiceIfConnected() {
    if (disconnected.get() || !serviceFuture.isDone()) {
      return null;
    }
    try {
      return Futures.getDone(serviceFuture);
    } catch (ExecutionException e) {
      return null;
    }
  }

  @Override
  public void onServiceConnected(ComponentName name, IBinder providerService) {
    logger.atFine().log("PCS<>AICore connected");
    try {
      providerService.linkToDeath(() -> disconnect("PCS<>AICore binder died", true), 0);
      IAiCoreServiceProvider provider = IAiCoreServiceProvider.Stub.asInterface(providerService);
      provider.get(
          new IAiCoreServiceProviderCallback.Stub() {
            @Override
            public void onServiceProviderSuccess(IAICoreService service) {
              completer.set(service);
              try {
                service
                    .asBinder()
                    .linkToDeath(() -> disconnect("PCS<>AICore binder died", true), 0);
              } catch (RemoteException e) {
                logger.atWarning().withCause(e).log("Unable to set death callback.");
              }
            }

            @Override
            public void onServiceProviderFailure(
                @AiCoreServiceProviderErrorCode int errorCode, String errorMessage) {
              disconnect(
                  String.format(
                      Locale.US,
                      "Failed to get AICoreService. Error code [%d], error message [%s]",
                      errorCode,
                      errorMessage),
                  true);
            }
          });
    } catch (RemoteException e) {
      disconnect(
          String.format(
              Locale.US,
              "Encountered error while connecting to AICoreService. Error: [%s]",
              e.getMessage()),
          true);
    }
  }

  @Override
  public void onServiceDisconnected(ComponentName name) {
    disconnect("PCS<>AICore disconnected", true);
  }

  @Override
  public void onBindingDied(ComponentName name) {
    disconnect("PCS<>AICore binding died", true);
  }

  @Override
  public void onNullBinding(ComponentName name) {
    disconnect("Received null binding for AICoreService", false);
  }

  public void disconnect(String errorMessage, boolean notifyClient) {
    if (disconnected.getAndSet(true)) {
      // Already disconnected earlier.
      return;
    }
    logger.atFine().log("%s", errorMessage);
    completer.setException(new RemoteException(errorMessage));
    try {
      context.unbindService(this);
    } catch (RuntimeException e) {
      logger.atInfo().withCause(e).log("Failed to unbind.");
    }
    if (notifyClient) {
      context.sendBroadcast(new Intent(PCS_AICORE_CONNECTION_STOPPED));
    }
  }
}
