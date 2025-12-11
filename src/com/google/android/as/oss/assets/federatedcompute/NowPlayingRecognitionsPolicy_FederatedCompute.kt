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

package com.google.android.libraries.pcc.policies.federatedcompute

/** Data policy. */
val NowPlayingRecognitionsPolicy_FederatedCompute =
  flavoredPolicies(
    name = "NowPlayingRecognitionsPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To provide usage statistics to monitor/improve Now Playing.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
      ALLOWED USAGES: Federated analytics.
      """
        .trimIndent()

    // The population is defined for Pixel 4+ devices per country. Most
    // countries (besides the US) have a smaller population and hence the min
    // round size is set to 500.
    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 500, minSecAggRoundSize = 500) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(NOW_PLAYING_TRACK_RECOGNITION_GENERATED_DTD, Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "trackId" { rawUsage(UsageType.ANY) }
    }
  }
