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

package com.google.android.as.oss.fl.brella.service.util;

/** Shared constants for policy. */
public class PolicyConstants {
  /** The key for the policy config section encoding Federated Compute requirements. */
  public static final String FEDERATED_COMPUTE_CONFIG_KEY = "federatedCompute";

  /** The key for the policy config section encoding user consent requirements.. */
  public static final String REQUIRED_USER_CONSENT_CONFIG_KEY = "requiredUserConsent";

  /** The key for the Android-wide OS Usage And Diagnostics Checkbox. */
  public static final String USAGE_AND_DIAGNOSTIC_CHECKBOX = "UsageAndDiagnosticsCheckbox";

  private PolicyConstants() {}
}
