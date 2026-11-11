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

package com.android.personalcontext.ace.common

import android.service.personalcontext.insight.ContextInsight

/**
 * Represents an insight grid item.
 *
 * @property insight The [android.service.personalcontext.insight.ContextInsight] to display.
 * @property span The number of columns this item should span in the grid.
 */
data class InsightGridItem(val insight: ContextInsight, val span: Span) {

  enum class Span(val value: Int) {
    /** A small span, the smallest possible unit. */
    SMALL(2),

    /** A half span, typically taking up half of the row. */
    HALF(3),

    /** A medium span. */
    MEDIUM(4),

    /** A large span, typically taking up the entire row */
    LARGE(6);

    operator fun plus(other: Int): Int = this.value + other

    operator fun times(other: Int): Int = this.value * other

    operator fun div(other: Int): Int = this.value / other

    operator fun minus(other: Int): Int = this.value - other

    val floatValue: Float = value.toFloat()

    companion object {

      /** Converts from an [Int] to a [Span]. Used during deserialization only. */
      fun deserializeFromInt(value: Int): Span? = entries.find { it.value == value }

      operator fun Int.plus(other: Span): Int = this + other.value

      operator fun Int.times(other: Span): Int = this * other.value

      operator fun Int.div(other: Span): Int = this / other.value

      operator fun Int.minus(other: Span): Int = this - other.value
    }
  }
}
