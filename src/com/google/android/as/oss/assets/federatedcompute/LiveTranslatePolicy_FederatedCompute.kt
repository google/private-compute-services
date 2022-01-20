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

/** FederatedCompute policy. */
val LiveTranslatePolicy_FederatedCompute =
  flavoredPolicies(
    name = "LiveTranslatePolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To measure and improve the quality of Live Translate.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """.trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(PERSISTED_LIVE_TRANSLATE_CHIP_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      // ConversationId are randomly generated and just used to locally count unique threads
      // translated.
      "conversationId" { rawUsage(UsageType.ANY) }
      "sourceLanguage" { rawUsage(UsageType.ANY) }
      "targetLanguage" { rawUsage(UsageType.ANY) }
      "type" { rawUsage(UsageType.ANY) }
      "action" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(
      PERSISTED_LIVE_TRANSLATE_CONFIG_CHANGED_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28)
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "type" { rawUsage(UsageType.ANY) }
      "language" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(
      PERSISTED_LIVE_TRANSLATE_COPY_TRANSLATE_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28)
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "conversationId" { rawUsage(UsageType.JOIN) }
      "sourceLanguage" { rawUsage(UsageType.ANY) }
      "targetLanguage" { rawUsage(UsageType.ANY) }
      "messageId" { rawUsage(UsageType.JOIN) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(PERSISTED_LIVE_TRANSLATE_MESSAGE_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "messageId" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "length" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(
      PERSISTED_LIVE_TRANSLATE_PAGE_TRANSLATE_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28)
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "conversationId" { rawUsage(UsageType.JOIN) }
      "sourceLanguage" { rawUsage(UsageType.ANY) }
      "targetLanguage" { rawUsage(UsageType.ANY) }
      "isAuto" { rawUsage(UsageType.ANY) }
      "messageId" { rawUsage(UsageType.JOIN) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(PERSISTED_LIVE_TRANSLATE_PREFERENCE_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "action" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(PERSISTED_LIVE_TRANSLATE_DOWNLOAD_EVENT_GENERATED_DTD, maxAge = Duration.ofDays(28)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "status" { rawUsage(UsageType.ANY) }
    }
  }
