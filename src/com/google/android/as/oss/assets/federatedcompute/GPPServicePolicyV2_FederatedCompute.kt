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

/** Google Play Protect Harmful app detection policy. */
val GPPServicePolicyV2_FederatedCompute =
  flavoredPolicies(
    name = "GPPServicePolicyV2_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To detect or identify harmful applications.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
      """
        .trimIndent()

    flavors(Flavor.GPPS_PROD) { minRoundSize(minRoundSize = 500, minSecAggRoundSize = 500) }
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(CLASSIFICATION_RESULT_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)
    }
  }
