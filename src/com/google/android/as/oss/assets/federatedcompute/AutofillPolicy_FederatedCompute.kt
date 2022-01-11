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

/** Autofill FederatedCompute policy. */
val AutofillPolicy_FederatedCompute =
  flavoredPolicies(
    name = "AutofillPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To make improvements to the platform Autofill service – for example, provide suggestions for text input fields based on screen content, including smart copy & paste, smart replies and others.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """.trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(IN_MEMORY_AUTOFILL_REQUEST_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)

      "persistedId" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }

    target(IN_MEMORY_AUTOFILL_REQUEST_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(2)) {
      retention(StorageMedium.RAM)

      /** Allowed for substring/equality comparison. */
      "committedText" { rawUsage(UsageType.ANY) }
    }

    target(
      IN_MEMORY_AUTOFILL_CANDIDATE_TEMPLATE_IDX_ENTITY_GENERATED_DTD,
      maxAge = Duration.ofDays(14)
    ) {
      retention(StorageMedium.RAM)

      "persistedId" { rawUsage(UsageType.JOIN) }
      "templateIdx" { rawUsage(UsageType.ANY) }
    }

    target(PERSISTED_AUTOFILL_REQUEST_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "configId" { rawUsage(UsageType.ANY) }
      "tcVersion" { rawUsage(UsageType.ANY) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "suggestionShown" { rawUsage(UsageType.ANY) }
      "userAction" { rawUsage(UsageType.ANY) }
      "processingDurationMillis" { ConditionalUsage.Bucketed.whenever(UsageType.ANY) }
      "targetPackageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "targetActivityName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "targetResourceId" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "requestFlags" { rawUsage(UsageType.ANY) }
      "deviceLocale" { rawUsage(UsageType.ANY) }
      "languageTags" { rawUsage(UsageType.ANY) }
    }

    target(PERSISTED_AUTOFILL_CANDIDATE_ENTITY_GENERATED_DTD, maxAge = Duration.ofDays(14)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "requestId" { rawUsage(UsageType.ANY) }
      "entityType" { rawUsage(UsageType.ANY) }
      "candidateProviderType" { rawUsage(UsageType.ANY) }
      "scenarios" { rawUsage(UsageType.ANY) }
      "suppressionReasons" { rawUsage(UsageType.ANY) }
      "observationCount" { rawUsage(UsageType.ANY) }
      "lastObservationTimeMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "sourcePackageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "sourceActivityName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "sourceResourceId" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "localRank" { rawUsage(UsageType.ANY) }
      "globalRank" { rawUsage(UsageType.ANY) }
      "matchesCommittedText" { rawUsage(UsageType.ANY) }
      "suggested" { rawUsage(UsageType.ANY) }
      "selected" { rawUsage(UsageType.ANY) }
    }
  }
