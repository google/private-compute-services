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

package com.google.android.`as`.oss.policies.api.annotation

/**
 * An Arcs annotations containing additional information on an Arcs manifest element. An Annotation
 * may be attached to a plan, particle, handle, type etc.
 */
data class Annotation(val name: String, val params: Map<String, AnnotationParam> = emptyMap()) {

  fun getParam(name: String): AnnotationParam {
    return requireNotNull(params[name]) { "Annotation '$this.name' missing '$name' parameter" }
  }

  fun getStringParam(paramName: String): String {
    val paramValue = getParam(paramName)
    require(paramValue is AnnotationParam.Str) {
      "Annotation param $paramName must be string, instead got $paramValue"
    }
    return paramValue.value
  }

  fun getOptionalStringParam(paramName: String): String? {
    return if (params.containsKey(paramName)) getStringParam(paramName) else null
  }

  companion object {
    fun createArcId(id: String) = Annotation("arcId", mapOf("id" to AnnotationParam.Str(id)))

    fun createTtl(value: String) = Annotation("ttl", mapOf("value" to AnnotationParam.Str(value)))

    fun createCapability(name: String) = Annotation(name)

    /**
     * Returns an annotation indicating that a particle is an egress particle.
     *
     * @param egressType optional egress type for the particle
     */
    fun createEgress(egressType: String? = null): Annotation {
      val params = mutableMapOf<String, AnnotationParam>()
      if (egressType != null) {
        params["type"] = AnnotationParam.Str(egressType)
      }
      return Annotation("egress", params)
    }

    /** Returns an annotation indicating the name of the policy which governs a recipe. */
    fun createPolicy(policyName: String): Annotation {
      return Annotation("policy", mapOf("name" to AnnotationParam.Str(policyName)))
    }

    /** Annotation indicating that a particle is isolated. */
    val isolated = Annotation("isolated")

    /** Annotation indicating that a particle has ingress. */
    val ingress = Annotation("ingress")
  }
}
