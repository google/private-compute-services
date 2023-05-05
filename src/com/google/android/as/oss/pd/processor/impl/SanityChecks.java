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

package com.google.android.as.oss.pd.processor.impl;

import static com.google.common.base.Preconditions.checkArgument;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.pd.api.proto.DownloadBlobRequest;
import java.util.regex.Pattern;

/** Contains some utilities to validate some fields of download requests/responses. */
final class SanityChecks {

  @VisibleForTesting static final int EXPECTED_API_KEY_SIZE = 39;

  // Api key should contain only english letters, numbers or underscore.
  private static final Pattern API_KEY_PATTERN = Pattern.compile("^\\w+$");

  /**
   * Runs a sanity check of the validity of request structure and throws {@link
   * IllegalArgumentException} upon invalid requests.
   */
  public static void validateRequest(DownloadBlobRequest request) {
    checkArgument(request.getApiKey().length() == EXPECTED_API_KEY_SIZE, "unexpected api key size");
    checkArgument(
        API_KEY_PATTERN.matcher(request.getApiKey()).matches(),
        "api key contains illegal characters");

    checkArgument(
        !request.getMetadata().getCryptoKeys().getPublicKey().isEmpty(), "missing public key");
  }

  private SanityChecks() {}
}
