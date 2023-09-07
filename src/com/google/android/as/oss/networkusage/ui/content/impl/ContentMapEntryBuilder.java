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

package com.google.android.as.oss.networkusage.ui.content.impl;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.StringRes;
import com.google.android.as.oss.networkusage.api.proto.AttestationConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.ConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.FlConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.HttpConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.PdConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.PirConnectionKey;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.ui.content.impl.NetworkUsageLogContentMapImpl.ConnectionResources;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.AbstractMap.SimpleImmutableEntry;

/** Builder class for NetworkUsageContentMap entries. */
final class ContentMapEntryBuilder {
  static final String ASI_PACKAGE_NAME = "com.google.android.as";
  static final String STATSD_PACKAGE_NAME = "com.android.os.statsd";

  static final String GPPS_PACKAGE_NAME = "com.google.android.odad";

  private final Context context;

  private String packageName;
  private String connectionKeyString;
  private ConnectionType connectionType;
  private int featureNameId;
  private int descriptionId;

  ContentMapEntryBuilder(Context context) {
    this.context = context;
  }

  /**
   * @param packageName The name of the package initiating this connection (currently ASI/GPPS).
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder packageName(String packageName) {
    this.packageName = packageName;
    return this;
  }

  /**
   * @param connectionType The mechanism used for this connection.
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder connectionType(ConnectionType connectionType) {
    this.connectionType = connectionType;
    return this;
  }

  /**
   * @param connectionKeyStringId String id of the ConnectionKey that distinguishes this connection
   *     from others of the same type and package name. For HTTP and PIR this is a URL regex. For FC
   *     this is the feature name.
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder connectionKeyStringId(@StringRes int connectionKeyStringId) {
    try {
      return connectionKeyString(context.getString(connectionKeyStringId));
    } catch (Resources.NotFoundException e) {
      throw new IllegalStateException("Invalid connectionKeyStringId", e);
    }
  }

  /**
   * @param connectionKeyString The ConnectionKey String that distinguishes this connection from
   *     others of the same type and package name. For HTTP and PIR this is a URL regex. For FC this
   *     is the feature name.
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder connectionKeyString(String connectionKeyString) {
    this.connectionKeyString = connectionKeyString;
    return this;
  }

  /**
   * @param featureNameId String id of the public feature name to show to users.
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder featureNameId(@StringRes int featureNameId) {
    this.featureNameId = featureNameId;
    return this;
  }

  /**
   * @param descriptionId String id of the public description to show to users.
   */
  @CanIgnoreReturnValue
  ContentMapEntryBuilder descriptionId(@StringRes int descriptionId) {
    this.descriptionId = descriptionId;
    return this;
  }

  /**
   * @return The built map entry with ConnectionDetails as key and ConnectionResources as value.
   */
  SimpleImmutableEntry<ConnectionDetails, ConnectionResources> build() {
    checkArgument(!isNullOrEmpty(packageName));
    checkNotNull(connectionType);
    checkArgument(!isNullOrEmpty(connectionKeyString));
    try {
      checkArgument(!isNullOrEmpty(context.getString(featureNameId)));
    } catch (Resources.NotFoundException e) {
      throw new IllegalStateException("Invalid featureNameId", e);
    }
    try {
      checkArgument(!isNullOrEmpty(context.getString(descriptionId)));
    } catch (Resources.NotFoundException e) {
      throw new IllegalStateException("Invalid descriptionId", e);
    }
    return new SimpleImmutableEntry<>(createConnectionDetails(), createConnectionResources());
  }

  private ConnectionResources createConnectionResources() {
    return ConnectionResources.builder()
        .setFeatureNameStringId(featureNameId)
        .setDescriptionStringId(descriptionId)
        .build();
  }

  private ConnectionDetails createConnectionDetails() {
    ConnectionKey.Builder connectionKeyBuilder = ConnectionKey.newBuilder();

    switch (connectionType) {
      case HTTP:
        connectionKeyBuilder.setHttpConnectionKey(
            HttpConnectionKey.newBuilder().setUrlRegex(connectionKeyString).build());
        break;
      case PIR:
        connectionKeyBuilder.setPirConnectionKey(
            PirConnectionKey.newBuilder().setUrlRegex(connectionKeyString).build());
        break;
      case FC_TRAINING_START_QUERY:
        connectionKeyBuilder.setFlConnectionKey(
            FlConnectionKey.newBuilder().setFeatureName(connectionKeyString).build());
        break;
      case PD:
        connectionKeyBuilder.setPdConnectionKey(
            PdConnectionKey.newBuilder().setClientId(connectionKeyString).build());
        break;
      case ATTESTATION_REQUEST:
        connectionKeyBuilder.setAttestationConnectionKey(
            AttestationConnectionKey.newBuilder().setFeatureName(connectionKeyString).build());
        break;
      default:
        throw new UnsupportedOperationException(
            String.format("Unsupported connection type '%s'", connectionType.name()));
    }

    return ConnectionDetails.builder()
        .setPackageName(packageName)
        .setType(connectionType)
        .setConnectionKey(connectionKeyBuilder.build())
        .build();
  }
}
