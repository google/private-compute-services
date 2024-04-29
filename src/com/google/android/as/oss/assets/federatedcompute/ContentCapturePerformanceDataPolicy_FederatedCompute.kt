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

/** Content Capture Performance Data FederatedCompute policy. */
val ContentCapturePerformanceDataPolicy_FederatedCompute =
  flavoredPolicies(
    name = "ContentCapturePerformanceDataPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To track system health costs associated with the content capture service to detect problems
      and allow optimization of features based on content capture.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(
      PERSISTED_CONTENT_CAPTURE_PERFORMANCE_ENTITY_GENERATED_DTD,
      maxAge = Duration.ofDays(14),
    ) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "id" { rawUsage(UsageType.JOIN) }
      "timestampMillis" {
        ConditionalUsage.TruncatedToDays.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
      "autofillModelVersion" { rawUsage(UsageType.ANY) }
      "sessionType" { rawUsage(UsageType.ANY) }
      "processingTimeMs" { rawUsage(UsageType.ANY) }
      "cpuTimeMs" { rawUsage(UsageType.ANY) }
      "elapsedTimeMs" { rawUsage(UsageType.ANY) }
      "eventsProcessed" { rawUsage(UsageType.ANY) }
      "activityEventsProcessed" { rawUsage(UsageType.ANY) }
      "annotatorCalls" { rawUsage(UsageType.ANY) }
      "globalAnnotationUpdates" { rawUsage(UsageType.ANY) }
      "taskAnnotationUpdates" { rawUsage(UsageType.ANY) }
      "nodeAnnotationUpdates" { rawUsage(UsageType.ANY) }
      "eventAnnotationUpdates" { rawUsage(UsageType.ANY) }
      "nodesAdded" { rawUsage(UsageType.ANY) }
      "nodesRemoved" { rawUsage(UsageType.ANY) }
      "propertyUpdates" { rawUsage(UsageType.ANY) }
      "stringsAdded" { rawUsage(UsageType.ANY) }
      "charsAdded" { rawUsage(UsageType.ANY) }
      "nativeProcessingCalls" { rawUsage(UsageType.ANY) }
      "nativeProcessingTimeMs" { rawUsage(UsageType.ANY) }
      "activeSessionTimeMs" { rawUsage(UsageType.ANY) }
      "sourcePackageName" {
        ConditionalUsage.Top2000PackageNamesWith2000Wau.whenever(UsageType.ANY)
        rawUsage(UsageType.JOIN)
      }
    }
  }
