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

package com.google.android.as.oss.networkusage.db.impl;

import android.content.Context;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.LiveData;
import androidx.preference.PreferenceManager;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.networkusage.config.NetworkUsageLogConfig;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.user.R;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import javax.inject.Inject;

/** Implementation of NetworkUsageLogRepository. */
public class NetworkUsageLogRepositoryImpl implements NetworkUsageLogRepository {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Context context;
  private final NetworkUsageLogDatabase database;
  private final NetworkUsageLogContentMap contentMap;
  private final ConfigReader<NetworkUsageLogConfig> networkUsageLogConfigReader;
  private final Executor dbExecutor;

  @Inject
  NetworkUsageLogRepositoryImpl(
      @ApplicationContext Context context,
      NetworkUsageLogDatabase database,
      NetworkUsageLogContentMap contentMap,
      ConfigReader<NetworkUsageLogConfig> networkUsageLogConfigReader,
      @IoExecutorQualifier Executor dbExecutor) {
    this.context = context;
    this.database = database;
    this.contentMap = contentMap;
    this.networkUsageLogConfigReader = networkUsageLogConfigReader;
    this.dbExecutor = dbExecutor;
  }

  @Override
  public void insertNetworkUsageEntity(NetworkUsageEntity entity) {
    insertNetworkUsageEntity(
        entity,
        new FutureCallback<Boolean>() {
          @Override
          public void onSuccess(Boolean result) {
            if (result) {
              logger.atFine().log(
                  "Logged new NetworkUsageEntity of type = %s, feature name = %s",
                  entity.connectionDetails().type().name(),
                  contentMap.getFeatureName(entity.connectionDetails()));
            } else {
              logger.atWarning().log(
                  "Failed to log NetworkUsageEntity of type = %s, feature name = %s",
                  entity.connectionDetails().type().name(),
                  contentMap.getFeatureName(entity.connectionDetails()));
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().withCause(t).log(
                "Insert future failed for NetworkUsageEntity of type = %s, feature name = %s",
                entity.connectionDetails().type().name(),
                contentMap.getFeatureName(entity.connectionDetails()));
          }
        });
  }

  @VisibleForTesting
  void insertNetworkUsageEntity(NetworkUsageEntity entity, FutureCallback<Boolean> callback) {
    if (!isNetworkUsageLogEnabled()) {
      return;
    }

    Futures.addCallback(database.insertNetworkUsageEntity(entity), callback, dbExecutor);
  }

  @Override
  public boolean isNetworkUsageLogEnabled() {
    return networkUsageLogConfigReader.getConfig().networkUsageLogEnabled() && isUserOptedIn();
  }

  @Override
  public boolean shouldRejectRequest(ConnectionType type, String connectionKeyString) {
    return networkUsageLogConfigReader.getConfig().rejectUnknownRequests()
        && !isKnownConnection(type, connectionKeyString);
  }

  @Override
  public boolean shouldLogNetworkUsage(ConnectionType type, String connectionKeyString) {
    return isNetworkUsageLogEnabled() && isKnownConnection(type, connectionKeyString);
  }

  @Override
  public boolean isKnownConnection(ConnectionType type, String connectionKeyString) {
    switch (type) {
      case HTTP:
        return contentMap.getHttpConnectionDetails(connectionKeyString).isPresent();
      case PIR:
        return contentMap.getPirConnectionDetails(connectionKeyString).isPresent();
      case FC_TRAINING_START_QUERY:
      case FC_TRAINING_RESULT_UPLOAD:
        return contentMap.getFcStartQueryConnectionDetails(connectionKeyString).isPresent();
      case FC_CHECK_IN:
        return true;
      case PD:
        return contentMap.getPdConnectionDetails(connectionKeyString).isPresent();
      case ATTESTATION_REQUEST:
        return contentMap.getAttestationConnectionDetails(connectionKeyString).isPresent();
      default:
        return false;
    }
  }

  @Override
  public Optional<Executor> getDbExecutor() {
    return Optional.of(dbExecutor);
  }

  @Override
  public Optional<NetworkUsageLogContentMap> getContentMap() {
    return Optional.of(contentMap);
  }

  @Override
  public LiveData<List<NetworkUsageEntity>> getAll() {
    return database.dao().getAll();
  }

  @Override
  public void deleteAllBefore(Instant instant, FutureCallback<Integer> callback) {
    Futures.addCallback(database.deleteAllBefore(instant), callback, dbExecutor);
  }

  boolean isUserOptedIn() {
    String preferenceKey = context.getString(R.string.pref_network_usage_log_enabled_key);
    return PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(
            preferenceKey,
            context.getResources().getBoolean(R.bool.pref_network_usage_log_enabled_default));
  }
}
