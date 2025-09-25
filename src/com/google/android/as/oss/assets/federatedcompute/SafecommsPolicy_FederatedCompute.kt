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

/** Safecomms FederatedCompute policy. */
val SafecommsPolicy_FederatedCompute =
  flavoredPolicies(
    name = "SafecommsPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
        To improve phishing detection for messaging apps.

        ALLOWED EGRESSES: FederatedCompute.
        ALLOWED USAGES: Federated analytics, federated learning.
      """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(SAFECOMMS_CONVERSATION_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        conditionalUsage("truncatedToDays", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "packageName" {
        conditionalUsage("top2000PackageNamesWith2000Wau", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "verdict" { rawUsage(UsageType.ANY) }
      "lastMessageSource" { rawUsage(UsageType.ANY) }
      "predictedValue" { rawUsage(UsageType.ANY) }
      "modelVersion" { rawUsage(UsageType.ANY) }
    }

    target(SAFECOMMS_UI_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "timestampInMillis" {
        conditionalUsage("truncatedToDays", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "conversationEntityId" { rawUsage(UsageType.ANY) }
      "uiScreen" { rawUsage(UsageType.ANY) }
      "uiEvent" { rawUsage(UsageType.ANY) }
    }

    target(SAFECOMMS_FEATURE_STATUS_CHANGE_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "timestampMillis" {
        conditionalUsage("truncatedToDays", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "featureEnabled" { rawUsage(UsageType.ANY) }
      "featureDisableReason" { rawUsage(UsageType.ANY) }
    }

    target(SAFECOMMS_ANALYZER_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "conversationEntityId" { rawUsage(UsageType.ANY) }
      "packageName" {
        conditionalUsage("top2000PackageNamesWith2000Wau", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "timestampMillis" {
        conditionalUsage("truncatedToDays", UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "verdict" { rawUsage(UsageType.ANY) }
      "predictedValue" { rawUsage(UsageType.ANY) }
      "modelVersion" { rawUsage(UsageType.ANY) }
    }
  }
