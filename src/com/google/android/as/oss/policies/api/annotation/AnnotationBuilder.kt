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

/*
 * Copyright 2021 Google LLC.
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
 * Builds an [Annotation] with the supplied [name], using an [AnnotationBuilder] [block].
 *
 * Example:
 * ```kotlin
 * val foo = annotation("ttl") {
 *   param("duration", "15 days")
 * }
 * val bar = annotation("encrypted")
 * ```
 */
fun annotation(name: String, block: AnnotationBuilder.() -> Unit = {}): Annotation =
  AnnotationBuilder(name).apply(block).build()

class AnnotationBuilder(private val name: String) {
  private val params = mutableMapOf<String, AnnotationParam>()

  /**
   * Adds an [AnnotationParam] to the [Annotation] being built.
   *
   * The [value] supplied must be either [String], [Int], or [Boolean]: to match the allowable types
   * of [AnnotationParam].
   */
  fun param(name: String, value: String): AnnotationBuilder {
    val param = AnnotationParam.Str(value)
    params[name] = param
    return this
  }

  /**
   * Adds an [AnnotationParam] to the [Annotation] being built.
   *
   * The [value] supplied must be either [String], [Int], or [Boolean]: to match the allowable types
   * of [AnnotationParam].
   */
  fun param(name: String, value: Int): AnnotationBuilder {
    val param = AnnotationParam.Num(value)
    params[name] = param
    return this
  }

  /**
   * Adds an [AnnotationParam] to the [Annotation] being built.
   *
   * The [value] supplied must be either [String], [Int], or [Boolean]: to match the allowable types
   * of [AnnotationParam].
   */
  fun param(name: String, value: Boolean): AnnotationBuilder {
    val param = AnnotationParam.Bool(value)
    params[name] = param
    return this
  }

  fun build(): Annotation = Annotation(name, params.toMap())
}
