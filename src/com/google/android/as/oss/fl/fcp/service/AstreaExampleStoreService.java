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

package com.google.android.as.oss.fl.fc.service;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;
import static java.util.concurrent.TimeUnit.SECONDS;

import android.os.IInterface;
import android.os.RemoteException;
import androidx.annotation.VisibleForTesting;
import androidx.core.os.BuildCompat;
import arcs.core.data.proto.PolicyProto;
import com.google.android.as.oss.common.ExecutorAnnotations.FlExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.consent.UsageReportingOptedInState;
import com.google.android.as.oss.common.consent.config.PolicyConfig;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.fl.Annotations.AsiPackageName;
import com.google.android.as.oss.fl.Annotations.ExampleStoreClientsInfo;
import com.google.android.as.oss.fl.Annotations.GppsPackageName;
import com.google.android.as.oss.fl.fc.api.IExampleStore;
import com.google.android.as.oss.fl.fc.api.StartQueryCallback;
import com.google.android.as.oss.fl.fc.api.proto.TrainingError;
import com.google.android.as.oss.fl.fc.service.ConnectionManager.ConnectionType;
import com.google.android.as.oss.fl.fc.service.util.PolicyConstants;
import com.google.android.as.oss.fl.fc.service.util.PolicyFinder;
import com.google.android.as.oss.fl.federatedcompute.config.PcsFcFlags;
import com.google.android.as.oss.fl.federatedcompute.statsd.ExampleStoreConnector;
import com.google.android.as.oss.fl.federatedcompute.statsd.config.StatsdConfig;
import com.google.android.as.oss.fl.localcompute.FileCopyStartQuery;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.proto.PcsProtos.PcsQuery;
import com.google.fcp.client.ExampleStoreService;
import com.google.android.as.oss.policies.api.Policy;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.intelligence.fcp.client.QueryTimeComputationProperties.ExampleIteratorOutputFormat;
import com.google.intelligence.fcp.client.SelectorContext;
import com.google.protobuf.Any;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeoutException;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An implementation of federated compute's {@link ExampleStoreService} that forwards calls to the
 * appropriate delegate in client based on the passed-in query.
 */
@AndroidEntryPoint(ExampleStoreService.class)
public final class PcsExampleStoreService extends Hilt_PcsExampleStoreService {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final String FILECOPY_COLLECTION_PREFIX = "/filecopy";

  @Inject @ExampleStoreClientsInfo ImmutableMap<String, String> packageToActionMap;

  @Inject ExampleStoreConnector statsdConnector;
  @Inject ConfigReader<StatsdConfig> statsdConfigReader;
  @Inject ConfigReader<PolicyConfig> policyConfigReader;
  @Inject Multimap<String, PolicyProto> installedPolicies;
  @Inject @AsiPackageName String asiPackageName;
  @Inject @GppsPackageName String gppsPackageName;
  @Inject NetworkUsageLogRepository networkUsageLogRepository;
  @Inject UsageReportingOptedInState usageReportingState;
  @Inject @FlExecutorQualifier Executor flExecutor;
  @Inject @FlExecutorQualifier ListeningScheduledExecutorService flExecutorService;
  @Inject PcsFcFlags pcsFcFlags;
  @Inject PcsStatsLog pcsStatsLogger;
  @Inject java.util.Optional<FileCopyStartQuery> fileCopyStartQuery;
  @Inject BuildFlavor buildFlavor;

  @VisibleForTesting ConnectionManager connectionManager;

  @Override
  public void onCreate() {
    super.onCreate();
    logger.atFine().log("PcsExampleStoreService.onCreate()");
    connectionManager =
        new ConnectionManager(
            this,
            packageToActionMap,
            ConnectionType.EXAMPLE_STORE,
            pcsStatsLogger,
            asiPackageName,
            gppsPackageName);
  }

  @Override
  public void onDestroy() {
    logger.atFine().log("PcsExampleStoreService.onDestroy()");
    connectionManager.unbindService();
    super.onDestroy();
  }

  @Override
  public void startQuery(
      @Nonnull String collection,
      @Nonnull byte[] criteria,
      @Nonnull byte[] resumptionToken,
      @Nonnull QueryCallback callback,
      @Nonnull SelectorContext selectorContext) {
    PcsQuery query = parseCriteria(criteria);
    if (query == null) {
      callback.onStartQueryFailure(
          TrainingError.TRAINING_ERROR_PCC_FAILED_TO_PARSE_QUERY_VALUE, "Failed to parse query.");
      return;
    }

    Optional<Policy> installedPolicy = extractInstalledPolicyOptional(callback, query);
    if (!installedPolicy.isPresent()) {
      return;
    }

    // If local computation task, can skip network and federation-policy validations
    if (hasOnlyLocalCompute(selectorContext)) {
      if (collection.startsWith(FILECOPY_COLLECTION_PREFIX) && fileCopyStartQuery.isPresent()) {
        fileCopyStartQuery
            .get()
            .startQuery(collection, criteria, resumptionToken, callback, selectorContext);
        return;
      }
    } else {
      if (!checkFederatedConfigs(query, selectorContext, installedPolicy.get())) {
        callback.onStartQueryFailure(
            TrainingError.TRAINING_ERROR_PCC_CONFIG_VALIDATION_FAILED_VALUE,
            "Training configs don't match federation configs defined in the policy.");
        return;
      }

      if (!consentPolicyValid(installedPolicy.get(), callback)) {
        return;
      }

      String featureName = query.getFeatureName().name();

      // Log Unrecognized requests
      if (!networkUsageLogRepository.isKnownConnection(FC_TRAINING_START_QUERY, featureName)) {
        logUnknownConnection(featureName);
      }

      if (networkUsageLogRepository.shouldRejectRequest(FC_TRAINING_START_QUERY, featureName)) {
        logger.atWarning()
            .withCause(UnrecognizedNetworkRequestException.forFeatureName(featureName)).log(
            "Rejected unknown FC request to PCS");
        callback.onStartQueryFailure(
            TrainingError.TRAINING_ERROR_PCC_CLIENT_NOT_SUPPORTED_VALUE,
            String.format("Unknown PCS request for feature %s", featureName));
        return;
      }
      insertNetworkUsageLogRowForTrainingEvent(
          query, selectorContext.getComputationProperties().getRunId());
    }

    if (isValidStatsdQuery(collection)) {
      statsdConnector.startQuery(collection, criteria, resumptionToken, callback, selectorContext);
      return;
    }

    initializeConnectionAndStartQuery(
        collection, criteria, resumptionToken, callback, query, selectorContext);
  }

  private boolean consentPolicyValid(Policy installedPolicy, QueryCallback callback) {
    if (!policyConfigReader.getConfig().enableConsentCheckInPcs()) {
      return true;
    }

    Map<String, String> installedConfig =
        installedPolicy
            .getConfigs()
            .getOrDefault(PolicyConstants.REQUIRED_USER_CONSENT_CONFIG_KEY, ImmutableMap.of());
    if (installedConfig.containsKey("value")
        && Objects.equals(
            installedConfig.get("value"), PolicyConstants.USAGE_AND_DIAGNOSTIC_CHECKBOX)) {
      if (!usageReportingState.isOptedIn()) {
        callback.onStartQueryFailure(
            TrainingError.TRAINING_ERROR_POLICY_MISMATCH_VALUE,
            PolicyConstants.REQUIRED_USER_CONSENT_CONFIG_KEY + ": policy section not valid.");
        return false;
      }
    }
    return true;
  }

  private boolean isValidStatsdQuery(@Nonnull String collection) {
    return statsdConnector != null
        && Ascii.equalsIgnoreCase(collection, STATSD_COLLECTION_NAME)
        && BuildCompat.isAtLeastU()
        && isPlatformLoggingEnabled();
  }

  private boolean isPlatformLoggingEnabled() {
    return statsdConfigReader.getConfig().enablePlatformLogging();
  }

  private boolean hasOnlyLocalCompute(@Nonnull SelectorContext selectorContext) {
    return selectorContext.getComputationProperties().hasLocalCompute()
        && !selectorContext.getComputationProperties().hasFederated()
        && !selectorContext.getComputationProperties().hasEligibilityEval();
  }

  private boolean taskRequiresSelectorContext(SelectorContext selectorContext) {
    return selectorContext.getComputationProperties().getExampleIteratorOutputFormat()
        == ExampleIteratorOutputFormat.EXAMPLE_QUERY_RESULT;
  }

  private Optional<Policy> extractInstalledPolicyOptional(
      @Nonnull QueryCallback callback, @Nonnull PcsQuery query) {
    if (!query.hasPolicy()) {
      logger.atWarning().log("No policy provided in the query.");
      callback.onStartQueryFailure(
          TrainingError.TRAINING_ERROR_PCC_POLICY_NOT_PRESENT_VALUE,
          "Query does not specify a policy");
      return Optional.absent();
    }

    PolicyProto queryPolicy = query.getPolicy();
    Optional<Policy> installedPolicy =
        PolicyFinder.findCompatiblePolicy(queryPolicy, installedPolicies);
    if (!installedPolicy.isPresent()) {
      callback.onStartQueryFailure(
          TrainingError.TRAINING_ERROR_PCC_POLICY_NOT_PRESENT_VALUE,
          "Query specified policy is not installed, or the installed version is incompatible.");
      return Optional.absent();
    }

    return installedPolicy;
  }

  private void initializeConnectionAndStartQuery(
      @Nonnull String collection,
      byte[] criteria,
      byte[] resumptionToken,
      @Nonnull QueryCallback callback,
      PcsQuery query,
      SelectorContext selectorContext) {
    if (!connectionManager.isClientSupported(query.getClientName())) {
      callback.onStartQueryFailure(
          TrainingError.TRAINING_ERROR_PCC_CLIENT_NOT_SUPPORTED_VALUE,
          "Incorrect client name provided in the query.");
      return;
    }

    Futures.addCallback(
        Futures.withTimeout(
            connectionManager.initializeServiceConnection(query.getClientName()),
            pcsFcFlags.maxBinderDelaySeconds(),
            SECONDS,
            flExecutorService),
        new FutureCallback<IInterface>() {
          @Override
          public void onSuccess(IInterface result) {
            try {
              IExampleStore binder = (IExampleStore) result;
              if (binder.supportsSelectorContext()) {
                binder.startQueryWithSelectorContext(
                    collection,
                    criteria,
                    resumptionToken,
                    new StartQueryCallback(callback),
                    selectorContext.toByteArray());
              } else {
                // Note: binder returns false if method does not exist in the implementation passed
                // i.e. if the example store was built before this change.
                if (taskRequiresSelectorContext(selectorContext)) {
                  callback.onStartQueryFailure(
                      TrainingError.TRAINING_ERROR_PCC_TRAINING_DELEGATION_TO_CLIENT_FAILED_VALUE,
                      "Example store does not support SelectorContext which is required for"
                          + " lightweight client tasks.");
                  return;
                }
                binder.startQuery(
                    collection, criteria, resumptionToken, new StartQueryCallback(callback));
              }
            } catch (RemoteException e) {
              connectionManager.resetClient(query.getClientName());
              // We don't expect client to actually throw any RemoteExceptions.
              // If it does happen for some reason then all we can do now is log the exception
              // and swallow it.
              logger.atWarning().withCause(e).log("Failed to delegate startQuery.");
              callback.onStartQueryFailure(
                  TrainingError.TRAINING_ERROR_PCC_TRAINING_DELEGATION_TO_CLIENT_FAILED_VALUE,
                  "Failed to delegate startQuery.");
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().log("Failed to bind to service");
            connectionManager.resetClient(query.getClientName());
            if (t instanceof TimeoutException) {
              callback.onStartQueryFailure(
                  TrainingError.TRAINING_ERROR_PCC_BINDING_TO_CLIENT_TIMED_OUT_VALUE,
                  "Timed out while binding to service.");
            } else {
              callback.onStartQueryFailure(
                  TrainingError.TRAINING_ERROR_PCC_BINDING_TO_CLIENT_FAILED_VALUE,
                  "Failed to bind to service.");
            }
          }
        },
        flExecutor);
  }

  private void logUnknownConnection(String featureName) {
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
                  IntelligenceUnrecognisedNetworkRequestReported.ConnectionType
                      .FC_TRAINING_START_QUERY)
              .setConnectionKey(featureName)
              .build());
    }
    logger.atInfo().log("Network usage log unrecognised FC request for %s", featureName);
  }

  static boolean checkFederatedConfigs(
      PcsQuery query, SelectorContext selectorContext, Policy installedPolicy) {
    if (!installedPolicy.getConfigs().containsKey("federatedCompute")) {
      logger.atWarning().log("Policy provided doesn't have configs.");
      return false;
    }

    Map<String, String> policyConfigs = installedPolicy.getConfigs().get("federatedCompute");
    if (policyConfigs == null) {
      logger.atWarning().log("Policy provided doesn't have configs.");
      return false;
    }

    // First, check that the population name matches.
    String executingPopulationName = "";
    if (selectorContext.getComputationProperties().hasFederated()) {
      executingPopulationName =
          selectorContext.getComputationProperties().getFederated().getPopulationName();
    } else if (selectorContext.getComputationProperties().hasEligibilityEval()) {
      executingPopulationName =
          selectorContext.getComputationProperties().getEligibilityEval().getPopulationName();
    } else {
      // We will have checked that the task is not local compute before calling this method.
      logger.atWarning().log(
          "No federated or eligibility eval computation found in selector context.");
      return false;
    }
    if (!query.getPopulationName().equals(executingPopulationName)) {
      logger.atWarning().log(
          "Population in the query does not match population in the computation properties.");
      return false;
    }

    // The task must be aggregated either using legacy rounds or confidential aggregation. If the
    // task is aggregated using legacy rounds, then the policy will specify both minRoundSize and
    // minSecAggRoundSize.
    if (policyConfigs.containsKey("minSecAggRoundSize")) {
      int secAggRoundSize = Integer.parseInt(policyConfigs.getOrDefault("minSecAggRoundSize", "0"));
      if (selectorContext.getComputationProperties().hasEligibilityEval()) {
        if (secAggRoundSize > 1) {
          // EETs are not allowed to run with SecAgg policy.
          logger.atWarning().log("EETs are not allowed to run with SecAgg policy.");
          return false;
        }
        // Currently executing an EET, the population matches, and it doesn't require SecAgg.
        // Access is allowed.
        return true;
      }

      // If the minSecAggRoundSize is 0, then the task is aggregated using legacy rounds with simple
      // aggregation. If the minSecAggRoundSize is > 0, then the task is
      // aggregated using secure aggregation.
      // Secure Aggregation with size <= 1 is equivalent to disabling SecAgg.
      if (secAggRoundSize > 1) {
        if (!selectorContext.getComputationProperties().getFederated().hasSecureAggregation()) {
          logger.atWarning().log(
              "SecAgg metadata not provided by Federated Compute, but SecAgg is required by"
                  + " policy.");
          return false;
        }

        if (selectorContext
                .getComputationProperties()
                .getFederated()
                .getSecureAggregation()
                .getMinimumClientsInServerVisibleAggregate()
            < secAggRoundSize) {
          logger.atWarning().log(
              "SecAgg round size is less than the round size required by the policy.");
          return false;
        }
      }
    } else if (policyConfigs.containsKey("confidentialAgg")) {
      // Task uses confidential aggregation. Make sure the task being executed is configured to use
      // confidential aggregation, using the hint in the SelectorContext.
      if (!selectorContext.getComputationProperties().getFederated().hasConfidentialAggregation()) {
        logger.atWarning().log(
            "Policy requires task is aggregated using confidential aggregation, but the task is not"
                + " configured to use confidential aggregation.");
        return false;
      }
    } else {
      logger.atWarning().log("Policy provided doesn't have minSecAggRoundSize or confidentialAgg.");
      return false;
    }

    // Made it through all the checks without returning false, so the task meets the policy
    // requirements.
    return true;
  }

  private static @Nullable PcsQuery parseCriteria(byte[] criteria) {
    try {
      Any parsedCriteria = Any.parseFrom(criteria, ExtensionRegistryLite.getGeneratedRegistry());
      return PcsQuery.parseFrom(
          parsedCriteria.getValue(), ExtensionRegistryLite.getGeneratedRegistry());
    } catch (InvalidProtocolBufferException e) {
      logger.atWarning().withCause(e).log("Couldn't parse criteria.");
    }
    return null;
  }

  // Note: The success/failure status and the upload size in bytes, are reported in another row
  // when we receive a LogEvent of kind TRAIN_RESULT_UPLOADED or TRAIN_FAILURE_UPLOADED.
  private void insertNetworkUsageLogRowForTrainingEvent(PcsQuery query, long runId) {
    if (networkUsageLogRepository.isNetworkUsageLogEnabled()
        && networkUsageLogRepository.getDbExecutor().isPresent()) {
      networkUsageLogRepository
          .getDbExecutor()
          .get()
          .execute(
              () ->
                  networkUsageLogRepository.insertNetworkUsageEntity(
                      NetworkUsageLogUtils.createFcTrainingStartQueryNetworkUsageEntity(
                          NetworkUsageLogUtils.createFcTrainingStartQueryConnectionDetails(
                              query.getFeatureName().name(), query.getClientName()),
                          runId,
                          query.getPolicy())));
    }
  }
}
