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

/*
 * Copyright 2020 Google LLC.
 *
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt
 *
 * Code distributed by Google as part of this project is also subject to an additional IP rights
 * grant found at
 * http://polymer.github.io/PATENTS.txt
 */

package com.google.android.`as`.oss.policies.api

import com.google.android.`as`.oss.policies.api.annotation.Annotation
import com.google.android.`as`.oss.policies.api.capabilities.Capabilities
import com.google.android.`as`.oss.policies.api.capabilities.Capability
import com.google.android.`as`.oss.policies.api.contextrules.AllowAllContextsRule
import com.google.android.`as`.oss.policies.api.contextrules.PolicyContextRule

/** The name of a field within an entity. */
typealias FieldName = String

/** Defines a data usage policy. See [PolicyProto] for the canonical definition of a policy. */
// TODO: Serialize allowedContext in Chronicle ledger
// TODO: Handle serialization for FedEx Policy encoding
data class Policy(
  val name: String,
  val egressType: String,
  val description: String = "",
  val targets: List<PolicyTarget> = emptyList(),
  val configs: Map<String, PolicyConfig> = emptyMap(),
  val annotations: List<Annotation> = emptyList(),
  val allowedContext: PolicyContextRule = AllowAllContextsRule
) {
  /** All fields mentioned the policy (includes nested fields). */
  val allFields: List<PolicyField> = collectAllFields()

  /** The set of all redaction labels mentioned in the policy. */
  val allRedactionLabels: Set<String> = allFields.flatMap { it.redactedUsages.keys }.toSet()

  private fun collectAllFields(): List<PolicyField> {
    fun getAllFields(field: PolicyField): List<PolicyField> {
      return listOf(field) + field.subfields.flatMap { getAllFields(it) }
    }
    return targets.flatMap { target -> target.fields.flatMap { getAllFields(it) } }
  }
}

/** Target schema governed by a policy, see [PolicyTargetProto]. */
data class PolicyTarget(
  val schemaName: String,
  val maxAgeMs: Long = 0,
  val retentions: List<PolicyRetention> = emptyList(),
  val fields: List<PolicyField> = emptyList(),
  val annotations: List<Annotation> = emptyList()
) {

  fun toCapabilities(): List<Capabilities> {
    return retentions.map {
      val ranges = mutableListOf<Capability>()
      ranges.add(
        when (it.medium) {
          StorageMedium.DISK -> Capability.Persistence.ON_DISK
          StorageMedium.RAM -> Capability.Persistence.IN_MEMORY
        }
      )
      if (it.encryptionRequired) {
        ranges.add(Capability.Encryption(true))
      }
      ranges.add(Capability.Ttl.Minutes((maxAgeMs / Capability.Ttl.MILLIS_IN_MIN).toInt()))
      Capabilities(ranges)
    }
  }
}

/** Allowed usages for fields in a schema, see [PolicyFieldProto]. */
data class PolicyField(
  /** List of field names leading from the [PolicyTarget] to this nested field. */
  val fieldPath: List<FieldName>,
  /** Valid usages of this field without redaction. */
  val rawUsages: Set<UsageType> = emptySet(),
  /** Valid usages of this field with redaction first. Maps from redaction label to usages. */
  val redactedUsages: Map<String, Set<UsageType>> = emptyMap(),
  val subfields: List<PolicyField> = emptyList(),
  val annotations: List<Annotation> = emptyList()
) {
  init {
    subfields.forEach { subfield ->
      require(
        fieldPath.size < subfield.fieldPath.size &&
          subfield.fieldPath.subList(0, fieldPath.size) == fieldPath
      ) {
        "Subfield's field path must be nested inside parent's field path, " +
          "but got parent: '$fieldPath', child: '${subfield.fieldPath}'."
      }
    }
  }
}

/** Retention options for storing data, see [PolicyRetentionProto]. */
data class PolicyRetention(val medium: StorageMedium, val encryptionRequired: Boolean = false)

/**
 * Config options specified by a policy, see [PolicyConfigProto]. These are arbitrary string
 * key-value pairs set by the policy author. They have no direct affect on the policy itself.
 */
typealias PolicyConfig = Map<String, String>

/** Type of usage permitted of a field, see [PolicyFieldProto.UsageType]. */
enum class UsageType {
  ANY,
  EGRESS,
  JOIN,
  SANDBOX;

  val canEgress
    get() = this == ANY || this == EGRESS
}

/** Convenience method for checking if any usage in a set allows egress. */
fun Set<UsageType>.canEgress(): Boolean = any { it.canEgress }

/** Target schema governed by a policy, see [PolicyRetentionProto.Medium]. */
enum class StorageMedium {
  RAM,
  DISK,
}
