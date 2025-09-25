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

package com.google.android.as.oss.networkusage.ui.content.impl;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_CHECK_IN;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_RESULT_UPLOAD;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;

import android.content.Context;
import androidx.annotation.StringRes;
import com.google.android.as.oss.networkusage.api.proto.ConnectionKey;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.user.R;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/** Implementation of {@link NetworkUsageLogContentMap}. */
// TODO: we should check the package name as well as the ConnectionKey to retrieve
//  ConnectionDetails.
@Singleton
public final class NetworkUsageLogContentMapImpl implements NetworkUsageLogContentMap {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final ImmutableMap<ConnectionDetails, ConnectionResources> entryContentMap;
  private final Context context;

  @Inject
  NetworkUsageLogContentMapImpl(
      @ApplicationContext Context context,
      ImmutableMap<ConnectionDetails, ConnectionResources> entryContentMap) {
    this.context = context;
    this.entryContentMap = entryContentMap;
  }

  @Override
  public Optional<ConnectionDetails> getHttpConnectionDetails(String url) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      if (details.connectionKey().hasHttpConnectionKey()
          && url.matches(details.connectionKey().getHttpConnectionKey().getUrlRegex())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized https request for url '%s'", url);
    return Optional.empty();
  }

  @Override
  public Optional<ConnectionDetails> getAttestationConnectionDetails(String featureName) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      if (details.connectionKey().hasAttestationConnectionKey()
          && featureName.equals(
              details.connectionKey().getAttestationConnectionKey().getFeatureName())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized Attestation request for feature name '%s'", featureName);
    return Optional.empty();
  }

  @Override
  public Optional<ConnectionDetails> getPirConnectionDetails(String url) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      if (details.connectionKey().hasPirConnectionKey()
          && url.matches(details.connectionKey().getPirConnectionKey().getUrlRegex())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized pir request for url '%s'", url);
    return Optional.empty();
  }

  @Override
  public Optional<ConnectionDetails> getSurveyConnectionDetails(String url) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      if (details.connectionKey().hasSurveyConnectionKey()
          && url.matches(details.connectionKey().getSurveyConnectionKey().getUrlRegex())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized Survey request for url '%s'", url);
    return Optional.empty();
  }

  @Override
  public Optional<ConnectionDetails> getPdConnectionDetails(String clientId) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      if (details.connectionKey().hasPdConnectionKey()
          && clientId.matches(details.connectionKey().getPdConnectionKey().getClientId())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized PD request for client Id '%s'", clientId);
    return Optional.empty();
  }

  @Override
  public Optional<ConnectionDetails> getFcStartQueryConnectionDetails(String featureName) {
    for (ConnectionDetails details : entryContentMap.keySet()) {
      ConnectionKey connectionKey = details.connectionKey();
      if (connectionKey.hasFlConnectionKey()
          && featureName.equals(connectionKey.getFlConnectionKey().getFeatureName())) {
        return Optional.of(details);
      }
    }
    logger.atWarning().log("Unauthorized FC request for feature name '%s'", featureName);
    return Optional.empty();
  }

  @Override
  public Optional<String> getFeatureName(ConnectionDetails connectionDetails) {
    if (connectionDetails.type() == FC_CHECK_IN) {
      return Optional.of(context.getString(R.string.feature_name_fc_check_in));
    }
    if (connectionDetails.type() == FC_TRAINING_RESULT_UPLOAD) {
      connectionDetails = connectionDetails.toBuilder().setType(FC_TRAINING_START_QUERY).build();
    }
    if (entryContentMap.containsKey(connectionDetails)) {
      return Optional.of(
          context.getString(entryContentMap.get(connectionDetails).featureNameStringId()));
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> getDescription(ConnectionDetails connectionDetails) {
    if (connectionDetails.type() == FC_CHECK_IN) {
      return Optional.of(context.getString(R.string.description_fc_check_in));
    }
    if (connectionDetails.type() == FC_TRAINING_RESULT_UPLOAD) {
      connectionDetails = connectionDetails.toBuilder().setType(FC_TRAINING_START_QUERY).build();
    }
    if (entryContentMap.containsKey(connectionDetails)) {
      return Optional.of(
          context.getString(entryContentMap.get(connectionDetails).descriptionStringId()));
    }
    return Optional.empty();
  }

  @Override
  public String getMechanismName(ConnectionDetails connectionDetails) {
    return switch (connectionDetails.type()) {
      case HTTP -> context.getString(R.string.connection_type_http);
      case PIR -> context.getString(R.string.connection_type_pir);
      case FC_CHECK_IN, FC_TRAINING_START_QUERY, FC_TRAINING_RESULT_UPLOAD ->
          context.getString(R.string.connection_type_fc);
      case PD -> context.getString(R.string.connection_type_ap);
      case ATTESTATION_REQUEST -> context.getString(R.string.connection_type_attestation);
      case SURVEY_REQUEST -> context.getString(R.string.connection_type_survey);
      default ->
          throw new UnsupportedOperationException(
              String.format("Unsupported connection type '%s'", connectionDetails.type().name()));
    };
  }

  /** Contains resource ids of strings to be used in the Network Usage Log UI. */
  @AutoValue
  public abstract static class ConnectionResources {
    @StringRes
    public abstract int featureNameStringId();

    @StringRes
    public abstract int descriptionStringId();

    public static Builder builder() {
      return new AutoValue_NetworkUsageLogContentMapImpl_ConnectionResources.Builder();
    }

    /** AutoValue.Builder for ConnectionResources. */
    @AutoValue.Builder
    public abstract static class Builder {
      public abstract Builder setFeatureNameStringId(@StringRes int id);

      public abstract Builder setDescriptionStringId(@StringRes int id);

      public abstract ConnectionResources build();
    }
  }
}
