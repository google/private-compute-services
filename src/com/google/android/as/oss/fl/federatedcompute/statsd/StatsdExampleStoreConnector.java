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

package com.google.android.as.oss.fl.federatedcompute.statsd;

import android.app.PendingIntent;
import android.app.StatsCursor;
import android.app.StatsManager;
import android.app.StatsManager.StatsQueryException;
import android.app.StatsManager.StatsUnavailableException;
import android.app.StatsQuery;
import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION_CODES;
import android.os.OutcomeReceiver;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.android.internal.os.StatsPolicyConfigProto.StatsPolicyConfig;
import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.fl.brella.api.EmptyExampleStoreIterator;
import com.google.android.as.oss.fl.brella.api.proto.TrainingError;
import com.google.android.as.oss.proto.PcsProtos.AstreaQuery;
import com.google.android.as.oss.proto.PcsStatsquery.AstreaStatsQuery;
import com.google.fcp.client.ExampleStoreIterator;
import com.google.fcp.client.ExampleStoreService.QueryCallback;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.common.primitives.Longs;
import com.google.intelligence.fcp.client.SelectorContext;
import com.google.protobuf.Any;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.List;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Example store connector for returning examples from statsd by querying restricted metrics. */
@Singleton
public class StatsdExampleStoreConnector implements ExampleStoreConnector {
  public static final String ACTION_RESTRICTED_METRICS_CHANGED =
      "com.google.android.as.oss.ACTION_RESTRICTED_METRICS_CHANGED"; // unused, using as placeholder

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  public static final String STATSD_COLLECTION_NAME = "/statsd";
  private static final String PCS_CRITERIA_TYPE_URL =
      "type.googleapis.com/com.google.android.as.oss.proto.AstreaQuery";
  public static final String PCS_STATSQUERY_CRITERIA_TYPE_URL =
      "type.googleapis.com/com.google.android.as.oss.proto.AstreaStatsQuery";
  private static long configKey = 175747355; // [redacted]
  private static String configPackage = "com.google.fcp.client";

  private static final String NO_TABLE_PRESENT_ERROR = "no such table: metric_";

  private final Executor executor;
  private final Context context;

  @Inject
  public StatsdExampleStoreConnector(
      @FlExecutorQualifier Executor executor, @ApplicationContext Context context) {
    this.executor = executor;
    this.context = context;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  @Override
  public void startQuery(
      String collection,
      byte[] criteria,
      byte[] resumptionToken,
      QueryCallback callback,
      SelectorContext selectorContext) {
    String sqlQuery = parseSqlQuery(criteria);
    Preconditions.checkNotNull(sqlQuery);
    StatsPolicyConfig statsPolicyConfig =
        StatsPolicyConfig.newBuilder()
            .setMinimumClientsInAggregateResult(
                selectorContext
                    .getComputationProperties()
                    .getFederated()
                    .getSecureAggregation()
                    .getMinimumClientsInServerVisibleAggregate())
            .build();
    logger.atFine().log("Sql Query: %s", sqlQuery);
    StatsQuery query =
        new StatsQuery.Builder(sqlQuery)
            .setSqlDialect(StatsQuery.DIALECT_SQLITE)
            .setMinSqlClientVersion(1)
            .setPolicyConfig(statsPolicyConfig.toByteArray())
            .build();

    try {
      StatsManager statsManager = context.getSystemService(StatsManager.class);
      statsManager.query(
          configKey,
          configPackage,
          query,
          executor,
          new OutcomeReceiver<StatsCursor, StatsQueryException>() {
            @Override
            public void onResult(StatsCursor result) {
              ExampleStoreIterator exampleStoreIterator = new CursorExampleIterator(result);
              callback.onStartQuerySuccess(exampleStoreIterator);
            }

            @Override
            public void onError(StatsQueryException error) {
              if (error.getMessage() != null
                  && error.getMessage().contains(NO_TABLE_PRESENT_ERROR)) {
                // TODO: Replace with proper checks using status code when API is
                // available.
                callback.onStartQuerySuccess(EmptyExampleStoreIterator.create());
              } else {
                callback.onStartQueryFailure(
                    TrainingError.TRAINING_ERROR_START_QUERY_RUNTIME_EXCEPTION_VALUE,
                    "Statsd query failed: " + error.getLocalizedMessage());
              }
            }
          });
    } catch (StatsUnavailableException | RuntimeException e) {
      callback.onStartQueryFailure(
          TrainingError.TRAINING_ERROR_START_QUERY_RUNTIME_EXCEPTION_VALUE,
          "Statsd query failed: " + e.getLocalizedMessage());
    }
  }

  public List<Long> getRestrictedMetricIds() {
    StatsManager statsManager = context.getSystemService(StatsManager.class);
    PendingIntent pi =
        PendingIntent.getBroadcast(
            context,
            0,
            new Intent(ACTION_RESTRICTED_METRICS_CHANGED),
            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
    try {
      return Longs.asList(
          statsManager.setRestrictedMetricsChangedOperation(
              getConfigId(), getConfigPackageName(), pi));
    } catch (StatsUnavailableException | RuntimeException e) {
      return ImmutableList.of();
    }
  }

  @Nullable
  private static String parseSqlQuery(byte[] criteria) {
    try {
      Any parsedCriteria = Any.parseFrom(criteria, ExtensionRegistryLite.getGeneratedRegistry());
      if (!parsedCriteria.getTypeUrl().equals(PCS_CRITERIA_TYPE_URL)) {
        return null;
      }
      AstreaQuery query =
          AstreaQuery.parseFrom(
              parsedCriteria.getValue(), ExtensionRegistryLite.getGeneratedRegistry());
      if (query.hasDataSelectionCriteria()
          && query
              .getDataSelectionCriteria()
              .getTypeUrl()
              .equals(PCS_STATSQUERY_CRITERIA_TYPE_URL)) {
        AstreaStatsQuery statsQuery =
            AstreaStatsQuery.parseFrom(
                query.getDataSelectionCriteria().getValue(),
                ExtensionRegistryLite.getGeneratedRegistry());
        return statsQuery.getSqlQuery();
      }
    } catch (InvalidProtocolBufferException e) {
      logger.atWarning().withCause(e).log("Couldn't parse criteria.");
    }
    return null;
  }

  public static String getConfigPackageName() {
    return configPackage;
  }

  public static long getConfigId() {
    return configKey;
  }

  @VisibleForTesting
  static void setConfigPackageName(String packageName) {
    configPackage = packageName;
  }

  @VisibleForTesting
  static void setConfigId(long configId) {
    configKey = configId;
  }
}
