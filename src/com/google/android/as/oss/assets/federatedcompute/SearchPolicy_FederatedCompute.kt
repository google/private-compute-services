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

/** FederatedCompute policy. */
val SearchPolicy_FederatedCompute =
  flavoredPolicies(
    name = "SearchPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
        To measure and improve the quality of the search feature such as analyzing the ranking
        quality and the ranking model.

        ALLOWED EGRESSES: FederatedCompute.
        ALLOWED USAGES: Federated analytics, federated learning.
      """
        .trimIndent()

    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(PERSISTED_SEARCH_ACTION_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(2)) {
      retention(StorageMedium.DISK)

      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "packageName" {
        conditionalUsage("top2000PackageNamesWith2000Wau", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "queryLength" { rawUsage(UsageType.ANY) }
      "resultType" { rawUsage(UsageType.ANY) }
      "actionType" { rawUsage(UsageType.ANY) }
      "blockRank" { rawUsage(UsageType.ANY) }
      "positionInBlock" { rawUsage(UsageType.ANY) }
    }
  }
