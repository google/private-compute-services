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

import android.app.personalcontext.hint.ContextHint as AppContextHint
import android.app.personalcontext.insight.ContextInsight as AppContextInsight
import android.service.personalcontext.hint.ContextHint as ServiceContextHint
import android.service.personalcontext.hint.PublishedContextHint
import android.service.personalcontext.insight.ContextInsight as ServiceContextInsight
import android.service.personalcontext.insight.InsightCollection
import com.android.personalcontext.ace.common.RenderTokenUtils.hasRendererToken
import com.android.personalcontext.ace.common.wrappers.IPublishedContextHint
import com.android.personalcontext.ace.common.wrappers.wrap

/** Pretty print utilities for ACE classes. */
object PrettyPrintUtils {

  /**
   * Pretty prints the type(s) of the given hints in a human-readable string.
   *
   * Prefix with "!" if the hint contains
   * [android.service.personalcontext.hint.PublishedContextHint.getRenderTokens].
   *
   * Postfix with "*" if the hint is [transform]ed.
   */
  fun Collection<PublishedContextHint>.toPrettyPrint(
    transform: (ServiceContextHint) -> String? = { null }
  ): String = map { it.wrap() }.toPrettyPrint(transform)

  /**
   * Pretty prints the type(s) of the given hints in a human-readable string.
   *
   * Prefix with "!" if the hint contains
   * [android.service.personalcontext.hint.PublishedContextHint.getRenderTokens].
   *
   * Postfix with "*" if the hint is [transform]ed.
   */
  @JvmName("toPrettyPrintIPublishedContextHint")
  fun Collection<IPublishedContextHint>.toPrettyPrint(
    transform: (ServiceContextHint) -> String? = { null }
  ): String {
    return joinToString(", ") {
      val hint = it.contextHint
      val transformed = transform(hint)

      val name = (transformed ?: hint.hintTypeName).substringAfterLast('.')
      val prefix = if (it.hasRendererToken()) "!" else ""
      val postfix = if (transformed != null) "*" else ""

      "$prefix$name$postfix"
    }
  }

  /**
   * Pretty prints the type(s) of the given hints in a human-readable string.
   *
   * Postfix with "*" if the hint is [transform]ed.
   */
  @JvmName("toPrettyPrintServiceContextHint")
  fun Collection<ServiceContextHint>.toPrettyPrint(
    transform: (ServiceContextHint) -> String? = { null }
  ): String {
    return joinToString(", ") { hint ->
      val transformed = transform(hint)

      val name = (transformed ?: hint.hintTypeName).substringAfterLast('.')
      val prefix = ""
      val postfix = if (transformed != null) "*" else ""

      "$prefix$name$postfix"
    }
  }

  /**
   * Pretty prints the type(s) of the given hints in a human-readable string.
   *
   * Postfix with "*" if the hint is [transform]ed.
   */
  @JvmName("toPrettyPrintAppContextHint")
  fun Collection<AppContextHint>.toPrettyPrint(
    transform: (AppContextHint) -> String? = { null }
  ): String {
    return joinToString(", ") { hint ->
      val transformed = transform(hint)

      val name = (transformed ?: hint.hintTypeName).substringAfterLast('.')
      val prefix = ""
      val postfix = if (transformed != null) "*" else ""

      "$prefix$name$postfix"
    }
  }

  /**
   * Pretty prints the type(s) of the given insight in a human-readable string.
   *
   * Postfix with "*" if the insight is [transform]ed.
   *
   * @param maxDepth The number of nested collection levels to expand before truncating.
   * @param formatting If true, formats the output with newlines and indentation.
   */
  fun ServiceContextInsight.toPrettyPrint(
    maxDepth: Int = 1,
    formatting: Boolean = false,
    transform: (ServiceContextInsight) -> String? = { null },
    children: (ServiceContextInsight) -> List<LabeledContextInsight>? = {
      (it as? InsightCollection)?.insights?.withIndexLabels()
    },
  ): String {
    return buildString {
      toPrettyPrintInternal(
        depth = 0,
        maxDepth = maxDepth,
        formatting = formatting,
        transform = transform,
        children = children,
        builder = this,
      )
    }
  }

  private fun ServiceContextInsight.toPrettyPrintInternal(
    depth: Int,
    maxDepth: Int,
    formatting: Boolean,
    transform: (ServiceContextInsight) -> String?,
    children: (ServiceContextInsight) -> List<LabeledContextInsight>?,
    builder: StringBuilder,
  ) {
    val insight = this
    val transformed = transform(insight)

    val name = (transformed ?: insight.insightTypeName).substringAfterLast('.')
    val postfix = if (transformed != null) "*" else ""

    builder.append(name)
    builder.append(postfix)

    val transformedChildren =
      children(insight)
        ?: (insight as? InsightCollection)?.insights?.withIndexLabels()
        ?: emptyList()
    if (transformedChildren.isNotEmpty()) {
      builder.append("[")
      if (maxDepth <= 0) {
        builder.append("...")
      } else {
        if (formatting) builder.append("\n")

        for ((index, child) in transformedChildren.withIndex()) {
          if (formatting) {
            builder.append("  ".repeat(depth + 1))
          }
          builder.append("${child.label}: ")

          val childInsight = child.insight
          if (childInsight == null) {
            builder.append("<null>")
          } else {
            childInsight.toPrettyPrintInternal(
              depth = depth + 1,
              maxDepth = maxDepth - 1,
              formatting = formatting,
              transform = transform,
              children = children,
              builder = builder,
            )
          }

          if (index < transformedChildren.size - 1) {
            builder.append(",")
            if (formatting) builder.append("\n") else builder.append(" ")
          } else {
            if (formatting) builder.append("\n")
          }
        }

        if (formatting) {
          builder.append("  ".repeat(depth))
        }
      }
      builder.append("]")
    }
  }

  /**
   * Pretty prints the type(s) of the given insight in a human-readable string.
   *
   * Postfix with "*" if the insight is [transform]ed.
   */
  fun AppContextInsight.toPrettyPrint(
    transform: (AppContextInsight) -> String? = { null }
  ): String {
    val insight = this
    val transformed = transform(insight)

    val name =
      (transformed ?: insight.javaClass.canonicalName ?: insight.toString()).substringAfterLast('.')
    val prefix = ""
    val postfix = if (transformed != null) "*" else ""

    return "$prefix$name$postfix"
  }
}
