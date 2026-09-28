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

package com.android.personalcontext.ace.visualizer.embeddedtheme

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.core.content.res.getColorOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.utils.EmbeddedTheme.InlineSuggestion

/**
 * Embedded Theming allows remote UI templates to read client-provided themed values.
 *
 * This composable resolves theme attributes provided by the host client application via
 * [android.service.personalcontext.embedded.InsightSurfaceClientInfo.getThemeResourceId]. It allows
 * remote UI components to adapt their colors and shapes to match the surrounding host client
 * environment.
 *
 * It uses [LocalInsightSurfaceClientInfo] to identify the client package and base theme, then
 * traverses the style hierarchy to extract specific attributes for [EmbeddedTheme].
 *
 * @param content The composable content that will consume the provided themed values.
 * @see android.service.personalcontext.embedded.InsightSurfaceClientInfo.getThemeResourceId
 * @see android.R.attr.embeddedViewTheme
 */
@Composable
fun EmbeddedTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val info = LocalInsightSurfaceClientInfo.current
  val density = LocalDensity.current

  val resolvedTheme =
    remember(context, info.packageName, info.themeResourceId, density) {
      resolveEmbeddedTheme(context, info.packageName, info.themeResourceId, density)
    }

  CompositionLocalProvider(
    InlineSuggestion.LocalEmbeddedColorScheme provides resolvedTheme.inlineSuggestion.colorScheme,
    InlineSuggestion.LocalEmbeddedShapes provides resolvedTheme.inlineSuggestion.shapes,
  ) {
    content()
  }
}

private data class ResolvedTheme(
  val inlineSuggestion: ResolvedInlineSuggestion = ResolvedInlineSuggestion()
)

private data class ResolvedInlineSuggestion(
  val colorScheme: InlineSuggestion.EmbeddedColorScheme = InlineSuggestion.EmbeddedColorScheme(),
  val shapes: InlineSuggestion.EmbeddedShapes = InlineSuggestion.EmbeddedShapes(),
)

private fun resolveEmbeddedTheme(
  context: Context,
  packageName: String,
  themeResourceId: Int,
  density: Density,
): ResolvedTheme {
  if (themeResourceId == 0) return ResolvedTheme()

  val clientContext =
    try {
      // Apply the client's custom theme to allow attribute references (values starting with ?)
      // to be resolved correctly.
      ContextThemeWrapper(context.createPackageContext(packageName, 0), themeResourceId)
    } catch (_: Exception) {
      return ResolvedTheme()
    }

  val embeddedViewThemeResId =
    clientContext.resolveAttribute(themeResourceId, android.R.attr.embeddedViewTheme) {
      getResourceIdOrThrow(0)
    } ?: return ResolvedTheme()

  val inlineSuggestionResId =
    clientContext.resolveAttribute(embeddedViewThemeResId, android.R.attr.inlineSuggestion) {
      getResourceIdOrThrow(0)
    } ?: return ResolvedTheme()

  return ResolvedTheme(
    inlineSuggestion =
      ResolvedInlineSuggestion(
        colorScheme = resolveColorScheme(clientContext, inlineSuggestionResId),
        shapes = resolveShapes(clientContext, inlineSuggestionResId, density),
      )
  )
}

@SuppressLint("ResourceType")
private fun resolveColorScheme(
  context: Context,
  styleResId: Int,
): InlineSuggestion.EmbeddedColorScheme {
  val attrs =
    intArrayOf(
        android.R.attr.strokeColor,
        android.R.attr.textColor,
        android.R.attr.iconColor,
        android.R.attr.suggestionBackgroundColor,
      )
      .sortedArray()

  return context.withStyledAttributes(styleResId, attrs) { typedArray ->
    InlineSuggestion.EmbeddedColorScheme(
      stroke =
        typedArray.getAttribute(attrs, android.R.attr.strokeColor) { Color(getColorOrThrow(it)) },
      text =
        typedArray.getAttribute(attrs, android.R.attr.textColor) { Color(getColorOrThrow(it)) },
      icon =
        typedArray.getAttribute(attrs, android.R.attr.iconColor) { Color(getColorOrThrow(it)) },
      suggestionBackground =
        typedArray.getAttribute(attrs, android.R.attr.suggestionBackgroundColor) {
          Color(getColorOrThrow(it))
        },
    )
  }
}

private fun resolveShapes(
  context: Context,
  styleResId: Int,
  density: Density,
): InlineSuggestion.EmbeddedShapes {
  val attrs = intArrayOf(android.R.attr.suggestionCornerRadius).sortedArray()

  return context.withStyledAttributes(styleResId, attrs) { typedArray ->
    val radiusPx =
      typedArray.getAttribute(attrs, android.R.attr.suggestionCornerRadius) {
        getDimensionOrThrow(it)
      }
    if (radiusPx != null) {
      InlineSuggestion.EmbeddedShapes(
        suggestion = RoundedCornerShape(with(density) { radiusPx.toDp() })
      )
    } else {
      InlineSuggestion.EmbeddedShapes()
    }
  }
}

private inline fun <T> Context.resolveAttribute(
  resId: Int,
  @AttrRes attr: Int,
  block: TypedArray.(Int) -> T,
): T? {
  val attrs = intArrayOf(attr)
  return withStyledAttributes(resId, attrs) { typedArray ->
    runCatching { typedArray.block(0) }.getOrNull()
  }
}

private inline fun <T> Context.withStyledAttributes(
  resId: Int,
  attrs: IntArray,
  block: (TypedArray) -> T,
): T {
  val typedArray = obtainStyledAttributes(resId, attrs)
  try {
    return block(typedArray)
  } finally {
    typedArray.recycle()
  }
}

private inline fun <T> TypedArray.getAttribute(
  attrs: IntArray,
  @AttrRes attrId: Int,
  getResourceOrThrow: TypedArray.(index: Int) -> T,
): T? {
  val index = attrs.indexOf(attrId)
  if (index == -1) return null
  return runCatching { getResourceOrThrow(index) }.getOrNull()
}
