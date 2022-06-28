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

package com.google.android.`as`.oss.policies.api.capabilities

import com.google.android.`as`.oss.policies.api.annotation.Annotation

/**
 * Store capabilities containing a combination of individual [Capability]s (e.g. Persistence and/or
 * Ttl and/or Queryable etc). If a certain capability does not appear in the combination, it is not
 * restricted.
 */
class Capabilities(capabilities: List<Capability> = emptyList()) {
  val ranges: List<Capability.Range>

  constructor(capability: Capability) : this(listOf(capability))

  init {
    ranges = capabilities.map { it -> it.toRange() }
    require(ranges.distinctBy { it.min.tag }.size == capabilities.size) {
      "Capabilities must be unique $capabilities."
    }
  }

  val persistence: Capability.Persistence?
    get() = getCapability<Capability.Persistence>()

  val ttl: Capability.Ttl?
    get() = getCapability<Capability.Ttl>()

  val isEncrypted: Boolean?
    get() = getCapability<Capability.Encryption>()?.let { it.value }

  val isQueryable: Boolean?
    get() = getCapability<Capability.Queryable>()?.let { it.value }

  val isShareable: Boolean?
    get() = getCapability<Capability.Shareable>()?.let { it.value }

  val isEmpty = ranges.isEmpty()

  /**
   * Returns true, if the given [Capability] is within the corresponding [Capability.Range] of same
   * type of this. For example, [Capabilities] with Ttl range of 1-5 days `contains` a Ttl of 3
   * days.
   */
  fun contains(capability: Capability): Boolean {
    return ranges.find { it.isCompatible(capability) }?.contains(capability) ?: false
  }

  /** Returns true if all ranges in the given [Capabilities] are contained in this. */
  fun containsAll(other: Capabilities): Boolean {
    return other.ranges.all { otherRange -> contains(otherRange) }
  }

  fun isEquivalent(other: Capabilities): Boolean {
    return ranges.size == other.ranges.size && other.ranges.all { hasEquivalent(it) }
  }

  fun hasEquivalent(capability: Capability): Boolean {
    return ranges.any { it.isCompatible(capability) && it.isEquivalent(capability) }
  }

  override fun toString(): String = "Capabilities($ranges)"

  private inline fun <reified T : Capability> getCapability(): T? {
    return ranges
      .find { it.min is T }
      ?.let {
        require(it.min.isEquivalent(it.max)) { "Cannot get capability for a range" }
        it.min as T
      }
  }

  companion object {
    fun fromAnnotations(annotations: List<Annotation>): Capabilities {
      val ranges = mutableListOf<Capability.Range>()
      Capability.Persistence.fromAnnotations(annotations)?.let { ranges.add(it.toRange()) }
      Capability.Encryption.fromAnnotations(annotations)?.let { ranges.add(it.toRange()) }
      Capability.Ttl.fromAnnotations(annotations)?.let { ranges.add(it.toRange()) }
      Capability.Queryable.fromAnnotations(annotations)?.let { ranges.add(it.toRange()) }
      Capability.Shareable.fromAnnotations(annotations)?.let { ranges.add(it.toRange()) }
      return Capabilities(ranges)
    }

    fun fromAnnotation(annotation: Annotation) = fromAnnotations(listOf(annotation))
  }
}
