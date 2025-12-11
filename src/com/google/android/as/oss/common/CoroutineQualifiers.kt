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

package com.google.android.`as`.oss.common

import javax.inject.Qualifier

/**
 * Declares qualifiers for each [kotlinx.coroutines.CoroutineDispatcher] used in PCS as well as
 * injectable [kotlinx.coroutines.CoroutineScope] instances.
 */
object CoroutineQualifiers {
  /**
   * Annotation to bind a [kotlinx.coroutines.CoroutineScope] associated with the application's
   * lifecycle.
   */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class ApplicationScope

  /** Annotation to bind a [kotlinx.coroutines.CoroutineDispatcher] used in general jobs. */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class GeneralDispatcher

  /** Annotation to bind a [kotlinx.coroutines.CoroutineDispatcher] used in IO jobs. */
  @Qualifier @Retention(AnnotationRetention.RUNTIME) annotation class IoDispatcher
}
