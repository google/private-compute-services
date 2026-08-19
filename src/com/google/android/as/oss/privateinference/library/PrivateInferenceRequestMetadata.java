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

package com.google.android.as.oss.privateinference.library;

import com.google.android.as.oss.privateinference.service.api.proto.IpBlindingMode;
import com.google.android.as.oss.privateinference.service.api.proto.PcsPrivateInferenceFeatureName;
import java.util.Optional;

/**
 * Metadata for the Private Inference request.
 *
 * <p>This is used to allow callers to pass additional information in the request, such as user
 * credentials, client info.
 */
public interface PrivateInferenceRequestMetadata {

  /**
   * Authentication info needed to connect to Private Inference.
   *
   * <p>Only one of the fields should be set.
   */
  public static class AuthInfo {
    public Optional<String> apiKey = Optional.empty();
    public Optional<String> spatulaHeader = Optional.empty();
  }

  /** Returns the authentication info for the session. */
  public AuthInfo getAuthInfo();

  /** Returns the feature name for the session. */
  default PcsPrivateInferenceFeatureName getFeatureName() {
    return PcsPrivateInferenceFeatureName.FEATURE_NAME_UNSPECIFIED;
  }

  /**
   * Returns the IP binding mode for the session.
   *
   * <p>The default mode is IP_BLINDING_MODE_ENABLED to be consistent with the existing behavior of
   * the PAC library w/o explicit configuration..
   */
  default IpBlindingMode getIpBlindingMode() {
    return IpBlindingMode.IP_BLINDING_MODE_ENABLED;
  }
}
