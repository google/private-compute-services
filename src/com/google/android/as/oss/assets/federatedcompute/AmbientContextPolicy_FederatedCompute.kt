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

package com.google.android.libraries.pcc.policies.federatedcompute

/** FederatedCompute policy. */
val AmbientContextPolicy_FederatedCompute =
  flavoredPolicies(
    name = "AmbientContextPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To measure and improve the quality of Ambient Context.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(AMBIENT_CONTEXT_STATUS_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "serviceStatus" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "eventTypes" { rawUsage(UsageType.ANY) }
    }

    target(AMBIENT_CONTEXT_RESULT_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "resultType" { rawUsage(UsageType.ANY) }
      "durationMillis" { ConditionalUsage.Bucketed.whenever(UsageType.ANY) }
      "confidenceBucket" { rawUsage(UsageType.ANY) }
      "densityBucket" { rawUsage(UsageType.ANY) }
      "modelId" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }
    target(AMBIENT_CONTEXT_INTERNAL_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "eventType" { rawUsage(UsageType.ANY) }
      "durationMillis" { ConditionalUsage.Bucketed.whenever(UsageType.ANY) }
      "modelId" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }
  }
