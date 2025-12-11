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
val NotificationIntelligencePolicy_FederatedCompute =
  flavoredPolicies(
    name = "NotificationIntelligencePolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To measure and improve the quality of Notification Intelligence.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics.
      """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(
      PERSISTED_SMART_NOTIFICATION_CLASSIFICATION_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "eventType" { rawUsage(UsageType.ANY) }
      "classificationMethod" { rawUsage(UsageType.ANY) }
      "finalClassificationResult" { rawUsage(UsageType.ANY) }
      "modelClassificationResult" { rawUsage(UsageType.ANY) }
      "errorType" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "isMessageStyleNotification" { rawUsage(UsageType.ANY) } // 0: false, 1: true
      "totalClassificationLatencyMillis" { rawUsage(UsageType.ANY) }
      "supportAppsCheckLatencyMillis" { rawUsage(UsageType.ANY) }
      "contactAccessLatencyMillis" { rawUsage(UsageType.ANY) }
      "notificationInfoCheckLatencyMillis" { rawUsage(UsageType.ANY) }
      "modelExecutionLatencyMillis" { rawUsage(UsageType.ANY) }
      "ruleCorrectionLatencyMillis" { rawUsage(UsageType.ANY) }
      "languageDetectionLatencyMillis" { rawUsage(UsageType.ANY) }
      "appProvidedClassifierLatencyMillis" { rawUsage(UsageType.ANY) }
      "defaultBundleLatencyMillis" { rawUsage(UsageType.ANY) }
      "systemInfoId" { rawUsage(UsageType.JOIN) }
    }

    target(
      PERSISTED_SMART_NOTIFICATION_INITIALIZATION_LATENCY_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "eventType" { rawUsage(UsageType.ANY) }
      "modelsDownloadLatencyMillis" { rawUsage(UsageType.ANY) }
      "modelsInitLatencyMillis" { rawUsage(UsageType.ANY) }
      "contactProviderAccessLatencyMillis" { rawUsage(UsageType.ANY) }
    }

    target(
      PERSISTED_SMART_NOTIFICATION_USER_FEEDBACK_EVENT_GENERATED_DTD,
      maxAge = Duration.ofDays(28),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "eventType" { rawUsage(UsageType.ANY) }
      "classificationMethod" { rawUsage(UsageType.ANY) }
      "finalClassificationResult" { rawUsage(UsageType.ANY) }
      "modelClassificationResult" { rawUsage(UsageType.ANY) }
      "errorType" { rawUsage(UsageType.ANY) }
      "packageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "feedbackCategory" { rawUsage(UsageType.ANY) }
    }

    target(
      PERSISTED_SMART_NOTIFICATION_LANGUAGE_DETECTION_GENERATED_DTD,
      maxAge = Duration.ofDays(28),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "isMessageStyleNotification" { rawUsage(UsageType.ANY) }
      "notificationLanguage" { rawUsage(UsageType.ANY) }
    }

    target(
      PERSISTED_SMART_NOTIFICATION_SUMMARIZATION_ERROR_GENERATED_DTD,
      maxAge = Duration.ofDays(28),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "errorType" { rawUsage(UsageType.ANY) }
      "modelVersion" { rawUsage(UsageType.ANY) }
    }
  }
