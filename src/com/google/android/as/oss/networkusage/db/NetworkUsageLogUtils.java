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

package com.google.android.as.oss.networkusage.db;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import arcs.core.data.proto.PolicyProto;
import com.google.android.as.oss.networkusage.api.proto.ConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.FlConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.HttpConnectionKey;
import com.google.android.as.oss.networkusage.api.proto.PirConnectionKey;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.common.base.Strings;
import java.time.Instant;

/** Utility class for creating NetworkUsageLog entities. */
public final class NetworkUsageLogUtils {

  public static ConnectionDetails createHttpConnectionDetails(String urlRegex, String packageName) {
    checkArgument(!Strings.isNullOrEmpty(urlRegex));
    return getDefaultConnectionDetailsBuilder(ConnectionType.HTTP, packageName)
        .setConnectionKey(
            ConnectionKey.newBuilder()
                .setHttpConnectionKey(HttpConnectionKey.newBuilder().setUrlRegex(urlRegex).build())
                .build())
        .build();
  }

  public static ConnectionDetails createPirConnectionDetails(String urlRegex, String packageName) {
    checkArgument(!Strings.isNullOrEmpty(urlRegex));
    return getDefaultConnectionDetailsBuilder(ConnectionType.PIR, packageName)
        .setConnectionKey(
            ConnectionKey.newBuilder()
                .setPirConnectionKey(PirConnectionKey.newBuilder().setUrlRegex(urlRegex).build())
                .build())
        .build();
  }

  public static ConnectionDetails createFcCheckInConnectionDetails(String packageName) {
    return getDefaultConnectionDetailsBuilder(ConnectionType.FC_CHECK_IN, packageName).build();
  }

  public static ConnectionDetails createFcTrainingResultUploadConnectionDetails(
      String packageName) {
    return getDefaultConnectionDetailsBuilder(ConnectionType.FC_TRAINING_RESULT_UPLOAD, packageName)
        .build();
  }

  public static ConnectionDetails createFcTrainingStartQueryConnectionDetails(
      String featureName, String packageName) {
    checkArgument(!Strings.isNullOrEmpty(featureName));
    return getDefaultConnectionDetailsBuilder(ConnectionType.FC_TRAINING_START_QUERY, packageName)
        .setConnectionKey(
            ConnectionKey.newBuilder()
                .setFlConnectionKey(
                    FlConnectionKey.newBuilder().setFeatureName(featureName).build())
                .build())
        .build();
  }

  public static NetworkUsageEntity createHttpNetworkUsageEntity(
      ConnectionDetails connectionDetails, Status status, long size, String url) {
    checkArgument(connectionDetails.connectionKey().hasHttpConnectionKey());
    checkArgument(
        url.matches(connectionDetails.connectionKey().getHttpConnectionKey().getUrlRegex()));
    checkArgument(connectionDetails.type() == ConnectionType.HTTP);
    return getNetworkUsageEntityForUrl(connectionDetails, status, size, url);
  }

  public static NetworkUsageEntity createPirNetworkUsageEntity(
      ConnectionDetails connectionDetails, Status status, long size, String url) {
    checkArgument(connectionDetails.type() == ConnectionType.PIR);
    checkArgument(connectionDetails.connectionKey().hasPirConnectionKey());
    checkArgument(
        url.matches(connectionDetails.connectionKey().getPirConnectionKey().getUrlRegex()));
    return getNetworkUsageEntityForUrl(connectionDetails, status, size, url);
  }

  public static NetworkUsageEntity createFcCheckInNetworkUsageEntity(
      ConnectionDetails connectionDetails, long size) {
    checkArgument(connectionDetails.type() == ConnectionType.FC_CHECK_IN);
    return getNetworkUsageEntityBuilder(connectionDetails, Status.SUCCEEDED, size).build();
  }

  public static NetworkUsageEntity createFcTrainingResultUploadNetworkUsageEntity(
      ConnectionDetails connectionDetails, Status status, long size) {
    checkArgument(connectionDetails.type() == ConnectionType.FC_TRAINING_RESULT_UPLOAD);
    return getNetworkUsageEntityBuilder(connectionDetails, status, size).build();
  }

  public static NetworkUsageEntity createFcTrainingStartQueryNetworkUsageEntity(
      ConnectionDetails connectionDetails, Status status, PolicyProto policyProto) {
    checkArgument(connectionDetails.type() == ConnectionType.FC_TRAINING_START_QUERY);
    checkArgument(connectionDetails.connectionKey().hasFlConnectionKey());
    checkNotNull(policyProto);
    checkArgument(policyProto.isInitialized());
    return getNetworkUsageEntityBuilder(connectionDetails, status, /* size= */ 0)
        .setPolicyProto(policyProto)
        .build();
  }

  private static NetworkUsageEntity getNetworkUsageEntityForUrl(
      ConnectionDetails connectionDetails, Status status, long size, String url) {
    checkArgument(!Strings.isNullOrEmpty(url));
    return getNetworkUsageEntityBuilder(connectionDetails, status, size).setUrl(url).build();
  }

  private static NetworkUsageEntity.Builder getNetworkUsageEntityBuilder(
      ConnectionDetails connectionDetails, Status status, long size) {
    checkNotNull(connectionDetails);
    checkNotNull(status);
    checkArgument(size >= 0);

    return NetworkUsageEntity.builder()
        .setId(0) // Allows room to auto-generate ids
        .setCreationTime(Instant.now())
        .setConnectionDetails(connectionDetails)
        .setStatus(status)
        .setSize(size)
        .setUrl("")
        .setPolicyProto(PolicyProto.getDefaultInstance());
  }

  private static ConnectionDetails.Builder getDefaultConnectionDetailsBuilder(
      ConnectionType type, String packageName) {
    checkArgument(!Strings.isNullOrEmpty(packageName));
    return ConnectionDetails.builder()
        .setType(type)
        .setPackageName(packageName)
        .setConnectionKey(ConnectionKey.getDefaultInstance());
  }

  private NetworkUsageLogUtils() {}
}
