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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.client.prototype

import android.app.personalcontext.hint.ContextHint as AppContextHint
import android.service.personalcontext.hint.BundleHint
import android.service.personalcontext.hint.ContextHint
import com.android.personalcontext.ace.common.builders.HintFilterKt
import com.android.personalcontext.ace.common.builders.bundleHint

private const val PROTOTYPE_HINT_V1_ID_KEY = "prototype_hint_id_key"

object PrototypeHintUtils {

  /** Converts the [PrototypeHint] into a [ContextHint]. */
  fun <T : PrototypeHint> T.toContextHintV1(): ContextHint {
    val prototype = this

    return bundleHint(prototype.id.typeName).apply {
      prototype.exportDataToBundle(dataBundle)
      dataBundle.putInt(PROTOTYPE_HINT_V1_ID_KEY, prototype.id.uid)
    }
  }

  /** Converts a [ContextHint] back into a [PrototypeHint], if possible. */
  fun ContextHint.toPrototypeHintV1(): PrototypeHint? {
    if (this !is BundleHint) return null

    val uid = dataBundle.getInt(PROTOTYPE_HINT_V1_ID_KEY)
    val id = PrototypeHintId.entries.find { it.uid == uid } ?: return null

    val creator = PrototypeHintRegistry.getHintCreator(id)
    return creator?.create(dataBundle)
  }

  /** Converts a [ContextHint] back into a [PrototypeHint] as [T], if possible. */
  @JvmSynthetic
  @JvmName("toTypedPrototypeHintV1")
  inline fun <reified T : PrototypeHint> ContextHint.toPrototypeHintV1(): T? {
    return toPrototypeHintV1() as? T
  }

  /** Returns whether a [ContextHint] is a [PrototypeHint] of type [T]. */
  @JvmSynthetic
  inline fun <reified T : PrototypeHint> ContextHint.isPrototypeHintV1(): Boolean {
    return toPrototypeHintV1() is T
  }

  /**
   * Returns whether a [android.app.personalcontext.hint.ContextHint] is a [PrototypeHint] of type
   * [id].
   */
  fun AppContextHint.isPrototypeHintV1(id: PrototypeHintId): Boolean {
    return hintTypeName == id.typeName
  }

  /** Adds a filter that matches a specific [PrototypeHint] using its [PrototypeHintId]. */
  fun HintFilterKt.FilterScope.prototypeHintV1(id: PrototypeHintId) {
    bundleHint(id.typeName)
  }

  /** Converts the [PrototypeHint] into a [ContextHint]. */
  @Deprecated(
    message = "Use toContextHintV1() instead",
    replaceWith = ReplaceWith("toContextHintV1()"),
  )
  fun <T : PrototypeHint> T.toContextHint(): ContextHint = toContextHintV1()

  /** Converts a [ContextHint] back into a [PrototypeHint], if possible. */
  @Deprecated(
    message = "Use toPrototypeHintV1() instead",
    replaceWith = ReplaceWith("toPrototypeHintV1()"),
  )
  fun ContextHint.toPrototypeHint(): PrototypeHint? = toPrototypeHintV1()

  /** Converts a [ContextHint] back into a [PrototypeHint] as [T], if possible. */
  @Deprecated(
    message = "Use toPrototypeHintV1() instead",
    replaceWith = ReplaceWith("toPrototypeHintV1<T>()"),
  )
  @JvmSynthetic
  @JvmName("toTypedPrototypeHint")
  inline fun <reified T : PrototypeHint> ContextHint.toPrototypeHint(): T? {
    return toPrototypeHintV1<T>()
  }

  /** Returns whether a [ContextHint] is a [PrototypeHint] of type [T]. */
  @Deprecated(
    message = "Use isPrototypeHintV1() instead",
    replaceWith = ReplaceWith("isPrototypeHintV1<T>()"),
  )
  @JvmSynthetic
  inline fun <reified T : PrototypeHint> ContextHint.isPrototypeHint(): Boolean {
    return isPrototypeHintV1<T>()
  }

  /**
   * Returns whether a [android.app.personalcontext.hint.ContextHint] is a [PrototypeHint] of type
   * [id].
   */
  @Deprecated(
    message = "Use isPrototypeHintV1(id) instead",
    replaceWith = ReplaceWith("isPrototypeHintV1(id)"),
  )
  fun AppContextHint.isPrototypeHint(id: PrototypeHintId): Boolean {
    return isPrototypeHintV1(id)
  }

  /** Adds a filter that matches a specific [PrototypeHint] using its [PrototypeHintId]. */
  @Deprecated(
    message = "Use prototypeHintV1(id) instead",
    replaceWith = ReplaceWith("prototypeHintV1(id)"),
  )
  fun HintFilterKt.FilterScope.prototypeHint(id: PrototypeHintId) {
    prototypeHintV1(id)
  }
}
