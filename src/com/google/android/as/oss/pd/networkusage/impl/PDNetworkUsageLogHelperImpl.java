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

package com.google.android.as.oss.pd.networkusage.impl;

import static com.google.android.as.oss.networkusage.db.NetworkUsageLogUtils.createPdNetworkUsageEntry;

import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;
import com.google.android.as.oss.pd.networkusage.PDNetworkUsageLogHelper;
import com.google.common.flogger.GoogleLogger;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Implements the API for network usage logging/filtering using {@link NetworkUsageLogRepository}
 * and {@link NetworkUsageLogContentMap}.
 */
@Singleton
final class PDNetworkUsageLogHelperImpl implements PDNetworkUsageLogHelper {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String PD_FEATURE_PREFIX = "PD-";

  private final NetworkUsageLogRepository repository;

  @Inject
  PDNetworkUsageLogHelperImpl(NetworkUsageLogRepository repository) {
    this.repository = repository;
  }

  @Override
  public void checkAllowedRequest(String clientId) throws UnrecognizedNetworkRequestException {
    if (repository.shouldRejectRequest(ConnectionType.PD, clientId)) {
      throw UnrecognizedNetworkRequestException.forFeatureName(PD_FEATURE_PREFIX + clientId);
    }
  }

  @Override
  public void logDownloadIfNeeded(String clientId, Status status, int estimatedSize) {
    if (!repository.shouldLogNetworkUsage(ConnectionType.PD, clientId)
        || !repository.getContentMap().isPresent()) {
      return;
    }

    // The app package is not available, so we retrieve it from the content map
    Optional<ConnectionDetails> apConnectionDetails =
        repository.getContentMap().get().getPdConnectionDetails(clientId);
    if (!apConnectionDetails.isPresent()) {
      // This should never happen since previous check 'shouldLogNetworkUsage' should return
      // false and return immediately.
      logger.atWarning().log("Unknown clientId '%s' for logging AP network usage", clientId);
      return;
    }

    repository.insertNetworkUsageEntity(
        createPdNetworkUsageEntry(apConnectionDetails.get(), status, estimatedSize, clientId));
  }
}
