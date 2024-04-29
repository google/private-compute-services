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
val PecanContextPolicy_FederatedCompute =
  flavoredPolicies(
    name = "PecanContextPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      Track screen context (e.g., recent conversation messages, recent search query), to predict
      the next user action.

      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics, federated learning.
    """
        .trimIndent()

    flavors(Flavor.ASI_PROD) { minRoundSize(minRoundSize = 1000, minSecAggRoundSize = 0) }
    consentRequiredForCollectionOrStorage(Consent.UsageAndDiagnosticsCheckbox)
    presubmitReviewRequired(OwnersApprovalOnly)
    checkpointMaxTtlDays(720)

    target(PECAN_CONVERSATION_SESSION_EVENT_GENERATED_DTD, Duration.ofDays(1)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "sessionId" { rawUsage(UsageType.JOIN) }
      "packageName" { rawUsage(UsageType.ANY) }
      "enteredTimestamp" { rawUsage(UsageType.ANY) }
      "exitedTimestamp" { rawUsage(UsageType.ANY) }
      "messages" {
        "signature" { rawUsage(UsageType.JOIN) }
        "text" { rawUsage(UsageType.ANY) }
        "isSent" { rawUsage(UsageType.ANY) }
        "sentOrReceivedTimestamp" { rawUsage(UsageType.ANY) }
        "lastSeenTimestamp" { rawUsage(UsageType.ANY) }
        "entities" {
          "entityText" { rawUsage(UsageType.ANY) }
          "entityClass" { rawUsage(UsageType.ANY) }
          "score" { rawUsage(UsageType.ANY) }
          "mid" { rawUsage(UsageType.ANY) }
          "collections" { rawUsage(UsageType.ANY) }
        }
      }
    }

    target(PECAN_SEARCH_QUERY_EVENT_GENERATED_DTD, Duration.ofDays(1)) {
      retention(StorageMedium.RAM)
      retention(StorageMedium.DISK)

      "packageName" { rawUsage(UsageType.ANY) }
      "timestamp" { rawUsage(UsageType.ANY) }
      "sourceTag" { rawUsage(UsageType.ANY) }
      "searchQuery" { rawUsage(UsageType.ANY) }
      "entities" {
        "entityText" { rawUsage(UsageType.ANY) }
        "entityClass" { rawUsage(UsageType.ANY) }
        "score" { rawUsage(UsageType.ANY) }
        "mid" { rawUsage(UsageType.ANY) }
        "collections" { rawUsage(UsageType.ANY) }
      }
    }
  }
