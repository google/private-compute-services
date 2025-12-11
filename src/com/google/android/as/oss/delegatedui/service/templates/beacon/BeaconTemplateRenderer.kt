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

package com.google.android.`as`.oss.delegatedui.service.templates.beacon

import android.app.PendingIntent
import android.content.Context
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.config.beacon.BeaconConfig
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

val LocalConfigReader =
  staticCompositionLocalOf<ConfigReader<BeaconConfig>> { error("No ConfigReader provided") }

/**
 * A [TemplateRenderer] for the Beacon template. This is the entry point for rendering the template.
 */
class BeaconTemplateRenderer
@Inject
internal constructor(val configReader: ConfigReader<BeaconConfig>) : TemplateRenderer {
  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    if (!response.data.beaconTemplateData.hasWidget()) {
      return null
    }

    val widget = response.data.beaconTemplateData.widget
    val pendingIntentList: List<PendingIntent> =
      response.pendingIntentList.valueOrNull ?: emptyList()

    return ComposeView(context).apply {
      setContent {
        CompositionLocalProvider(LocalConfigReader provides configReader) {
          val nestedScrollInterop = rememberNestedScrollInteropConnection()
          Box(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
            MainTheme { BeaconWidgetContainer(widget, pendingIntentList) }
          }
        }
      }
    }
  }
}

/** Apply Material 3 Theme to all descendant composables. */
@Composable
fun MainTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography().withFlexFont()) { content() }
}
