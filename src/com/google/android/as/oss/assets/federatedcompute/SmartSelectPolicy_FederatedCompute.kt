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

package com.google.android.libraries.pcc.policies.federatedcompute

/** SmartSelect FederatedCompute policy. */
val SmartSelectPolicy_FederatedCompute =
  flavoredPolicies(
    name = "SmartSelectPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To train and improve SmartSelect ML models that correctly select and classify actionable text.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """.trimIndent()

    // Smart select needs a smaller round size due to:
    // 1) The population size is small because only users with entity selections
    // are selected to participate in a round.
    // 2) Population is divided into sub-populations to search hyperparameters.
    // 3) Model's convergence is slow with a round size of 1000.
    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 200, minSecAggRoundSize = 100) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(
      PERSISTED_SMART_SELECT_SELECTION_EVENT_ENTITY_GENERATED_DTD,
      maxAge = Duration.ofDays(14)
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "entityType" { rawUsage(UsageType.ANY) }
      "leftContext" { rawUsage(UsageType.ANY) }
      "entityText" { rawUsage(UsageType.ANY) }
      "rightContext" { rawUsage(UsageType.ANY) }
      "numOfEntityTokens" { rawUsage(UsageType.ANY) }
      "userAction" { rawUsage(UsageType.ANY) }
      "originApp" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "destinationApp" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }
  }
