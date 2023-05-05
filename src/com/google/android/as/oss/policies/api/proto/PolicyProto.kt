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

package com.google.android.`as`.oss.policies.api.proto

import arcs.core.data.proto.PolicyConfigProto
import arcs.core.data.proto.PolicyFieldProto
import arcs.core.data.proto.PolicyProto
import arcs.core.data.proto.PolicyRetentionProto
import arcs.core.data.proto.PolicyTargetProto
import com.google.android.`as`.oss.policies.api.FieldName
import com.google.android.`as`.oss.policies.api.Policy
import com.google.android.`as`.oss.policies.api.PolicyField
import com.google.android.`as`.oss.policies.api.PolicyRetention
import com.google.android.`as`.oss.policies.api.PolicyTarget
import com.google.android.`as`.oss.policies.api.StorageMedium
import com.google.android.`as`.oss.policies.api.UsageType
import com.google.android.`as`.oss.policies.api.contextrules.All

fun PolicyProto.decode(): Policy {
  require(name.isNotEmpty()) { "Policy name is missing." }
  require(egressType.isNotEmpty()) { "Egress type is missing." }
  return Policy(
    name = name,
    description = description,
    egressType = egressType,
    targets = targetsList.map { it.decode() },
    configs =
      configsList.associateBy(keySelector = { it.name }, valueTransform = { it.metadataMap }),
    annotations = annotationsList.map { it.decode() }
  )
}

fun Policy.encode(): PolicyProto {
  require(allowedContext is All) { "allowedContext must allow all contexts." }
  return PolicyProto.newBuilder()
    .setName(name)
    .setDescription(description)
    .setEgressType(egressType)
    .addAllTargets(targets.map { it.encode() })
    .addAllConfigs(
      configs.map { (name, metadata) ->
        PolicyConfigProto.newBuilder().setName(name).putAllMetadata(metadata).build()
      }
    )
    .addAllAnnotations(annotations.map { it.encode() })
    .build()
}

private fun PolicyTargetProto.decode(): PolicyTarget {
  return PolicyTarget(
    schemaName = schemaType,
    maxAgeMs = maxAgeMs,
    retentions = retentionsList.map { it.decode() },
    fields = fieldsList.map { it.decode() },
    annotations = annotationsList.map { it.decode() }
  )
}

fun PolicyTarget.encode(): PolicyTargetProto {
  return PolicyTargetProto.newBuilder()
    .setSchemaType(schemaName)
    .setMaxAgeMs(maxAgeMs)
    .addAllRetentions(retentions.map { it.encode() })
    .addAllFields(fields.map { it.encode() })
    .addAllAnnotations(annotations.map { it.encode() })
    .build()
}

private fun PolicyFieldProto.decode(parentFieldPath: List<FieldName> = emptyList()): PolicyField {
  val rawUsages = mutableSetOf<UsageType>()
  val redactedUsages = mutableMapOf<String, MutableSet<UsageType>>()
  for (usage in usagesList) {
    if (usage.redactionLabel.isEmpty()) {
      rawUsages.add(usage.usage.decode())
    } else {
      redactedUsages.getOrPut(usage.redactionLabel) { mutableSetOf() }.add(usage.usage.decode())
    }
  }
  val fieldPath = parentFieldPath + name
  return PolicyField(
    fieldPath = fieldPath,
    rawUsages = rawUsages,
    redactedUsages = redactedUsages,
    subfields = subfieldsList.map { it.decode(fieldPath) },
    annotations = annotationsList.map { it.decode() }
  )
}

fun PolicyField.encode(): PolicyFieldProto {
  val rawUsages =
    rawUsages.map { usage ->
      PolicyFieldProto.AllowedUsage.newBuilder().setUsage(usage.encode()).build()
    }
  val redactedUsages =
    redactedUsages.flatMap { (label, usages) ->
      usages.map { usage ->
        PolicyFieldProto.AllowedUsage.newBuilder()
          .setRedactionLabel(label)
          .setUsage(usage.encode())
          .build()
      }
    }
  val allUsages = rawUsages + redactedUsages
  return PolicyFieldProto.newBuilder()
    .setName(fieldPath.last())
    .addAllUsages(allUsages)
    .addAllSubfields(subfields.map { it.encode() })
    .addAllAnnotations(annotations.map { it.encode() })
    .build()
}

private fun PolicyRetentionProto.decode(): PolicyRetention {
  return PolicyRetention(medium = medium.decode(), encryptionRequired = encryptionRequired)
}

private fun PolicyRetention.encode(): PolicyRetentionProto {
  return PolicyRetentionProto.newBuilder()
    .setMedium(medium.encode())
    .setEncryptionRequired(encryptionRequired)
    .build()
}

private fun PolicyFieldProto.UsageType.decode() =
  when (this) {
    PolicyFieldProto.UsageType.ANY -> UsageType.ANY
    PolicyFieldProto.UsageType.EGRESS -> UsageType.EGRESS
    PolicyFieldProto.UsageType.JOIN -> UsageType.JOIN
    PolicyFieldProto.UsageType.USAGE_TYPE_UNSPECIFIED,
    PolicyFieldProto.UsageType.UNRECOGNIZED ->
      throw UnsupportedOperationException("Unknown usage type: $this")
  }

private fun UsageType.encode() =
  when (this) {
    UsageType.ANY -> PolicyFieldProto.UsageType.ANY
    UsageType.EGRESS -> PolicyFieldProto.UsageType.EGRESS
    UsageType.JOIN -> PolicyFieldProto.UsageType.JOIN
    // TODO: Change this once proto is forked and updated.
    UsageType.SANDBOX ->
      throw java.lang.UnsupportedOperationException("SANDBOX isn't supported in the proto")
  }

private fun PolicyRetentionProto.Medium.decode() =
  when (this) {
    PolicyRetentionProto.Medium.RAM -> StorageMedium.RAM
    PolicyRetentionProto.Medium.DISK -> StorageMedium.DISK
    PolicyRetentionProto.Medium.MEDIUM_UNSPECIFIED,
    PolicyRetentionProto.Medium.UNRECOGNIZED ->
      throw UnsupportedOperationException("Unknown retention medium: $this")
  }

private fun StorageMedium.encode() =
  when (this) {
    StorageMedium.RAM -> PolicyRetentionProto.Medium.RAM
    StorageMedium.DISK -> PolicyRetentionProto.Medium.DISK
  }
