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
val PwmPolicy_FederatedCompute =
  flavoredPolicies(
    name = "PwmPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To measure and improve the quality of People-Who-Matter service (PWM).

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics.
      """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(PERSISTED_PWM_RANKING_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "queryTimeMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "suggestionType" { rawUsage(UsageType.ANY) }
      "clientId" { rawUsage(UsageType.ANY) }
      "featureMap" { rawUsage(UsageType.ANY) }
      "score" { rawUsage(UsageType.ANY) }
    }
  }
