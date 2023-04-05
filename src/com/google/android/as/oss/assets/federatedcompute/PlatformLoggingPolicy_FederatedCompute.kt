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

/** Federated compute policy for platform logs logged via statsd. */
val PlatformLoggingPolicy_FederatedCompute =
  flavoredPolicies(
    name = "PlatformLoggingPolicy_FederatedCompute",
    policyType = MonitorOrImproveUserExperienceWithFederatedCompute,
  ) {
    description =
      """
      To enable querying of Android Platform logs in a privacy-preserving way, using federated analytics.
    
      ALLOWED EGRESSES: FederatedCompute.
      ALLOWED USAGES: Federated analytics.
    """
        .trimIndent()
    flavors(Flavor.PCS_RELEASE) { minRoundSize(minRoundSize = 250, minSecAggRoundSize = 250) }
    // No targets because no data is stored in PCC for this feature.
  }
