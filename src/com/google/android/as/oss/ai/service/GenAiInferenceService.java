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

package com.google.android.as.oss.ai.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import com.google.android.apps.aicore.aidl.AIFeature;
import com.google.android.apps.aicore.aidl.AIFeatureStatus;
import com.google.android.apps.aicore.aidl.DownloadFailureStatus;
import com.google.android.apps.aicore.aidl.DownloadRequestStatus;
import com.google.android.apps.aicore.aidl.IAICoreService;
import com.google.android.apps.aicore.aidl.IDownloadListener;
import com.google.android.apps.aicore.aidl.IDownloadListener2;
import com.google.android.as.oss.ai.aidl.IGenAiInferenceService;
import com.google.android.as.oss.ai.aidl.PccLlmService;
import com.google.android.as.oss.ai.aidl.PccSmartReplyService;
import com.google.android.as.oss.ai.aidl.PccSummarizationService;
import com.google.android.as.oss.ai.aidl.PccTextEmbeddingService;
import com.google.android.as.oss.ai.config.PcsAiConfig;
import com.google.android.as.oss.common.ExecutorAnnotations.GenAiExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;

/**
 * On-device LLM service delegating to the real on-device provider.
 *
 * <p>Note that AICore keeps data for all clients isolated from each other. So data from PCS's
 * requests is never shared with any other client.
 */
@AndroidEntryPoint(Service.class)
public class GenAiInferenceService extends Hilt_GenAiInferenceService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String ASI_PACKAGE_NAME = "com.google.android.as";
  private static final ImmutableSet<String> CLIENT_PKG_ALLOWLIST =
      ImmutableSet.of(ASI_PACKAGE_NAME);

  @Inject ConfigReader<PcsAiConfig> configReader;
  @Inject @GenAiExecutorQualifier ListeningScheduledExecutorService executorService;

  private final Object connectionLock = new Object();

  @Nullable
  @GuardedBy("connectionLock")
  private AiCoreServiceConnection serviceConnection = null;

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    // Note that onBind is only called once in the lifetime of this Service, and the return value
    // is cached. So we cannot do ACL checks here. However this does not mean that onBind cannot be
    // called twice. For instance if we get a new bind after onUnbind had been called but the
    // service had not been destroyed yet, onBind would be called again. In those cases, we need to
    // re-initialize a connection to AICore.
    if (!isEnabled()) {
      logger.atSevere().log("AICore service forwarding is currently disabled.");
      return null;
    }
    // Initialize connection.
    checkServiceOrReconnect();
    // Cannot wait here for connection since it's only allowed after we return from onBind
    return new GenAiServiceBinderStub();
  }

  @Override
  public boolean onUnbind(Intent intent) {
    // All clients have unbound with unbindService()
    logger.atInfo().log("onUnbind");
    disconnect();
    return false;
  }

  private void disconnect() {
    synchronized (connectionLock) {
      if (serviceConnection == null) {
        return;
      }
      serviceConnection.disconnect(
          "Force disconnecting old connection either because the service is being"
              + " destroyed or trying to reconnect.");
      serviceConnection = null;
    }
  }

  /** Check if we have an non-dead connection, otherwise reconnect. */
  @CanIgnoreReturnValue
  private ListenableFuture<IAICoreService> checkServiceOrReconnect() {
    synchronized (connectionLock) {
      if (serviceConnection == null || !serviceConnection.isValid()) {
        // Clean up old connection, and establish a new one.
        disconnect();
        serviceConnection = AiCoreServiceConnection.create(this, executorService, configReader);
      }
      return serviceConnection.getServiceFuture();
    }
  }

  private boolean isEnabled() {
    return configReader.getConfig().genAiInferenceServiceEnabled();
  }

  private void validateRequest() throws RemoteException {
    if (!isEnabled()) {
      throw new RemoteException("AICore service forwarding is currently disabled.");
    }
    if (!CLIENT_PKG_ALLOWLIST.contains(
        Preconditions.checkNotNull(getPackageManager().getPackagesForUid(Binder.getCallingUid()))[
            0])) {
      throw new RemoteException("Caller is not allow-listed for AICore service forwarding.");
    }
  }

  private IAICoreService getServiceOrThrow() throws RemoteException {
    // First verify flags & ACL.
    validateRequest();

    // Check active/pending connection, or establish a new one.
    return Futures.getChecked(checkServiceOrReconnect(), RemoteException.class);
  }

  private class GenAiServiceBinderStub extends IGenAiInferenceService.Stub {
    @Override
    public AIFeature[] listFeatures() throws RemoteException {
      return getServiceOrThrow().listFeatures();
    }

    @Override
    @Nullable
    public AIFeature getFeature(@AIFeature.Id int id) throws RemoteException {
      return getServiceOrThrow().getFeature(id);
    }

    @Override
    @DownloadRequestStatus
    public int requestDownloadableFeatureWithDownloadListener(
        AIFeature feature, IDownloadListener listener) throws RemoteException {
      Preconditions.checkNotNull(feature);
      Preconditions.checkNotNull(listener);
      try {
        return getServiceOrThrow()
            .requestDownloadableFeatureWithDownloadListener(feature, listener);
      } catch (RemoteException e) {
        logger.atWarning().withCause(e).log("Failed to request downloadable feature");
        listener.onDownloadFailed(
            feature.getName(), String.format("Failed to request download: %s", e.getMessage()));
        return DownloadRequestStatus.UNAVAILABLE;
      }
    }

    @Override
    @DownloadRequestStatus
    public int requestDownloadableFeatureWithDownloadListener2(
        AIFeature feature, IDownloadListener2 listener) throws RemoteException {
      Preconditions.checkNotNull(feature);
      Preconditions.checkNotNull(listener);
      try {
        return getServiceOrThrow()
            .requestDownloadableFeatureWithDownloadListener2(feature, listener);
      } catch (RemoteException e) {
        logger.atWarning().withCause(e).log("Failed to request downloadable feature");
        listener.onDownloadFailed(
            feature.getName(),
            DownloadFailureStatus.UNKNOWN,
            String.format("Failed to request download: %s", e.getMessage()));
        return DownloadRequestStatus.UNAVAILABLE;
      }
    }

    @Override
    @DownloadRequestStatus
    public int requestDownloadableFeature(AIFeature feature) throws RemoteException {
      Preconditions.checkNotNull(feature);
      try {
        return getServiceOrThrow().requestDownloadableFeature(feature);
      } catch (RemoteException e) {
        logger.atWarning().withCause(e).log("Failed to request downloadable feature");
        return DownloadRequestStatus.UNAVAILABLE;
      }
    }

    @Override
    @AIFeatureStatus
    public int getFeatureStatus(AIFeature feature) throws RemoteException {
      Preconditions.checkNotNull(feature);
      try {
        return getServiceOrThrow().getFeatureStatus(feature);
      } catch (RemoteException e) {
        logger.atWarning().withCause(e).log("Failed to get feature status");
        return AIFeatureStatus.UNAVAILABLE;
      }
    }

    @Override
    public PccTextEmbeddingService getTextEmbeddingService(AIFeature feature)
        throws RemoteException {
      Preconditions.checkNotNull(feature);
      return GenAiServiceUtils.createTextEmbeddingServiceStub(getServiceOrThrow(), feature);
    }

    @Override
    public PccLlmService getLLMService(AIFeature feature) throws RemoteException {
      Preconditions.checkNotNull(feature);
      return GenAiServiceUtils.createLlmServiceStub(getServiceOrThrow(), feature);
    }

    @Override
    public PccSmartReplyService getSmartReplyService(AIFeature feature) throws RemoteException {
      Preconditions.checkNotNull(feature);
      return GenAiServiceUtils.createSmartReplyServiceStub(getServiceOrThrow(), feature);
    }

    @Override
    public PccSummarizationService getSummarizationService(AIFeature feature)
        throws RemoteException {
      Preconditions.checkNotNull(feature);
      return GenAiServiceUtils.createSummarizationServiceStub(getServiceOrThrow(), feature);
    }

    @Override
    public boolean isPersistentModeEnabled() throws RemoteException {
      return getServiceOrThrow().isPersistentModeEnabled();
    }
  }
}
