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

package com.google.android.as.oss.fl.brella.service.util;

import static com.google.android.as.oss.fl.brella.service.util.PolicyConstants.FEDERATED_COMPUTE_CONFIG_KEY;
import static com.google.android.as.oss.fl.brella.service.util.PolicyConstants.REQUIRED_USER_CONSENT_CONFIG_KEY;

import arcs.core.data.proto.PolicyProto;
import com.google.android.as.oss.policies.api.Policy;
import com.google.android.as.oss.policies.api.proto.PolicyProtoKt;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.flogger.GoogleLogger;
import java.util.Map;
import java.util.Map.Entry;

/** Utility class holding Policy compatibility checking helper methods. */
public class PolicyFinder {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private PolicyFinder() {}

  /**
   * Returns a policy compatible with the config and egressType requested in the query. If no such
   * policy exists, returns absent. It's possible that multiple policies match, but in production
   * there is only ever one policy per name. For internal builds, multiple policies can be present.
   * A matching policy only is selected if it has the correct values for config sections we validate
   * locally like the `federatedCompute` config.
   */
  // Java 8 compatibility is not universal yet so we still use Guava's Optional.
  @SuppressWarnings("Guava")
  public static Optional<Policy> findCompatiblePolicy(
      PolicyProto queryProto, Multimap<String, PolicyProto> installedPolicies) {
    String policyName = queryProto.getName();
    if (!installedPolicies.containsKey(policyName)) {
      logger.atWarning().log("Policy name=%s in the query is not installed.", policyName);
      return Optional.absent();
    }

    Policy queryPolicy = PolicyProtoKt.decode(queryProto);
    for (PolicyProto installedPolicyProto : installedPolicies.get(policyName)) {
      if (installedPolicyProto == null) {
        logger.atWarning().log("Installed policy name=%s is null, rejecting query.", policyName);
        continue;
      }

      Policy installedPolicy = PolicyProtoKt.decode(installedPolicyProto);
      if (!isPolicyCompatible(installedPolicy, queryPolicy)) {
        continue;
      }

      return Optional.of(installedPolicy);
    }

    logger.atWarning().log(
        "Installed policy name=%s isn't compatible with the policy pushed in the query.",
        policyName);
    return Optional.absent();
  }

  /**
   * Checks whether a query policy is compatible with an installed policy. The policy is compatible
   * if all the required policy configs are compatible, and it uses the same egress type.
   */
  public static boolean isPolicyCompatible(Policy installedPolicy, Policy queryPolicy) {
    String policyName = installedPolicy.getName();
    if (!installedPolicy.getEgressType().equals(queryPolicy.getEgressType())) {
      logger.atWarning().log(
          "Installed policy name=%s egress type doesn't match query egress type.", policyName);
      return false;
    }
    if (!installedConfigMatchesQueryConfig(
        FEDERATED_COMPUTE_CONFIG_KEY, installedPolicy, queryPolicy)) {
      return false;
    }
    if (!installedConfigMatchesQueryConfig(
        REQUIRED_USER_CONSENT_CONFIG_KEY, installedPolicy, queryPolicy)) {
      return false;
    }
    return true;
  }

  /**
   * Checks the installed config matches query policy, ignoring keys not in installed version so
   * config additions do not cause a failure.
   */
  private static boolean installedConfigMatchesQueryConfig(
      String section, Policy installedPolicy, Policy queryPolicy) {
    Map<String, String> installedConfig =
        installedPolicy.getConfigs().getOrDefault(section, ImmutableMap.of());
    Map<String, String> queryConfig =
        queryPolicy.getConfigs().getOrDefault(section, ImmutableMap.of());
    for (Entry<String, String> installedEntry : installedConfig.entrySet()) {
      String key = installedEntry.getKey();
      if (!installedEntry.getValue().equals(queryConfig.get(key))) {
        logger.atWarning().log(
            "Installed policy mismatch section=%s key=%s installedVal=%s pushedVal=%s",
            section, key, installedConfig.get(key), queryConfig.get(key));
        return false;
      }
    }
    return true;
  }
}
