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

package com.google.android.as.oss.pd.networkusage;

import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.UnrecognizedNetworkRequestException;

/**
 * Provides helper methods to check the validity of PD network requests and log them in the network
 * usage log.
 */
public interface PDNetworkUsageLogHelper {

  /** Throws an exception if PD requests with the given client Id should be rejected. */
  void checkAllowedRequest(String clientId) throws UnrecognizedNetworkRequestException;

  /** Writes an PD entry to the usage log with the given parameters if required by configuration. */
  void logDownloadIfNeeded(String clientId, Status status, int estimatedSize);
}
