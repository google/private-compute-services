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

package com.google.android.`as`.oss.policies.api

/**
 * Verifies that a set of [Policies][Policy] all conform to requirements of [Chronicle] which may be
 * more restrictive than what is imposed directly by Arcs [Policy].
 */
interface PolicyConformanceCheck {
  /**
   * Applies conformance rules to the set of [policies] and throws a [MalformedPolicy]
   * [com.google.android.libraries.pcc.chronicle.api.error.MalformedPolicySet] error if any do not
   * follow the rules.
   */
  fun checkPoliciesConform(policies: Set<Policy>)
}
