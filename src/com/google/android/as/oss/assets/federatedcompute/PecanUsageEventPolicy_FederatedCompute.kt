/*
 * Copyright 2024 Google LLC
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
val PecanUsageEventPolicy_FederatedCompute =
  flavoredPolicies(
    name = "PecanUsageEventPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      Monitor usage statistics for People and Conversations. Usage statistics will
      help improve the People and Conversations infrastructure.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """
        .trimIndent()
    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(PECAN_USAGE_EVENT_GENERATED_DTD, Duration.ofDays(7)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "eventId" { rawUsage(UsageType.JOIN) }
      "packageName" {
        conditionalUsage("top2000PackageNamesWith2000Wau", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "timestamp" {
        conditionalUsage("truncatedToDays", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }
  }
