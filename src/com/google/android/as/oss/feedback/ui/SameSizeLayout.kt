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

package com.google.android.`as`.oss.feedback.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

/**
 * A layout that measures a `base` composable and constrains an `alternative` composable to be the
 * exact same size if [matchSize] is `true`.
 *
 * It displays the `alternative` content corresponding to the provided [selector] key. If no
 * `alternative` with a matching key is found, the `base` content is displayed instead. This is
 * useful for creating transitions between different states of a UI element without causing the
 * layout to reflow.
 *
 * Example usage:
 * ```
 * var selector by remember { mutableStateOf("icon") }
 * SameSizeLayout(selector) {
 *   base {
 *    Text("This is the base content that defines the size")
 *  }
 *  alternative(key = "icon") {
 *    Icon(Icons.Filled.Check, contentDescription = null)
 *  }
 * }
 * ```
 *
 * @param T The type of the key used to select the alternative content.
 * @param selector The key that determines which `alternative` content to display. If there are no
 *   matching alternatives, then the `base` content will be displayed.
 * @param modifier The [Modifier] to be applied to the layout.
 * @param matchSize Whether to force the `alternative` content to be the same size as the `base`
 *   content. If `false`, the layout will take the size of the currently selected content.
 * @param content A block which describes the content. Inside this block you can use methods like
 *   [SameSizeLayoutScope.base] and [SameSizeLayoutScope.alternative] to define the `base` and
 *   `alternative` composables, respectively.
 */
@Composable
internal fun <T> SameSizeLayout(
  selector: T,
  modifier: Modifier = Modifier,
  matchSize: Boolean = true,
  content: SameSizeLayoutScope<T>.() -> Unit,
) {
  val scope = SameSizeLayoutScopeImpl<T>().apply { content() }

  val baseContent = checkNotNull(scope.base) { "SameSizeLayout requires a base()" }
  val alternatives = scope.alternatives

  Layout(
    modifier = modifier,
    content = {
      Box { baseContent() }
      alternatives[selector]?.let { Box { it() } }
    },
    measurePolicy = if (matchSize) SameSizeMeasurePolicy else IntrinsicSizeMeasurePolicy,
  )
}

/**
 * A [MeasurePolicy] for a [Layout] that measures a `base` composable and constrains an
 * `alternative` composable to be the exact same size.
 *
 * The final layout will place the `alternative` if it is present; otherwise, it will place the
 * `base`. The overall size of the layout will always be that of the `base`.
 */
private object SameSizeMeasurePolicy : MeasurePolicy {
  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints,
  ): MeasureResult {
    val base = measurables[0].measure(constraints)
    val alternative =
      measurables.getOrNull(1)?.run { measure(Constraints.fixed(base.width, base.height)) }
    return layout(base.width, base.height) {
      // Place alternative, if one matches [key]. Else place base.
      alternative?.placeRelative(0, 0) ?: base.placeRelative(0, 0)
    }
  }
}

/**
 * A [MeasurePolicy] for a [Layout] that measures and displays either a `base` composable or an
 * `alternative` composable, allowing the chosen composable to determine the layout's size.
 *
 * If the `alternative` is present, it will be measured with the incoming constraints, and its size
 * will define the layout's bounds. If no `alternative` exists, the `base` will be measured and used
 * instead.
 */
private object IntrinsicSizeMeasurePolicy : MeasurePolicy {
  override fun MeasureScope.measure(
    measurables: List<Measurable>,
    constraints: Constraints,
  ): MeasureResult {
    val base = measurables[0]
    val alternative = measurables.getOrNull(1)

    val placeable = (alternative ?: base).measure(constraints)
    return layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
  }
}

/**
 * A scope for defining content within [SameSizeLayout]. This provides a DSL to declare a mandatory
 * `base` composable and any number of `alternative` composables.
 *
 * @param T The type of the key used for alternatives.
 */
@LayoutScopeMarker
internal interface SameSizeLayoutScope<T> {

  /**
   * Defines the `base` content of the [SameSizeLayout]. Its measured size will be used as the size
   * for all [alternative]s. This content is displayed if no `alternative` matches the `selector`
   * passed to [SameSizeLayout].
   *
   * @param content The composable content that acts as the size reference.
   */
  fun base(content: @Composable () -> Unit)

  /**
   * Defines a piece of alternative content that can be shown in place of the `base` content.
   *
   * @param key The key to associate with this content. If the `selector` in [SameSizeLayout]
   *   matches this key, this content will be displayed instead of the `base`.
   * @param content The composable content for this alternative.
   */
  fun alternative(key: T, content: @Composable () -> Unit)
}

private class SameSizeLayoutScopeImpl<T> : SameSizeLayoutScope<T> {

  var base: (@Composable () -> Unit)? = null
    private set

  private val _alternatives = mutableMapOf<T, @Composable () -> Unit>()
  val alternatives: Map<T, @Composable () -> Unit> = _alternatives

  override fun base(content: @Composable () -> Unit) {
    base = content
  }

  override fun alternative(key: T, content: @Composable () -> Unit) {
    _alternatives[key] = content
  }
}
