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

package com.google.android.`as`.oss.delegatedui.service.templates.subzero

import android.content.Context
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow

class SubZeroRemindersTileTemplateRenderer @Inject internal constructor() : TemplateRenderer {
  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val templateData = response.data.subzeroRemindersTileTemplateData
    if (templateData.remindersList.isEmpty()) {
      return null
    }
    val pendingIntentList = response.pendingIntentList.valueOrNull?.toList() ?: emptyList()
    val remoteActionList = response.remoteActionList.valueOrNull?.toList() ?: emptyList()

    return ComposeView(context).apply {
      setContent {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()
        Box(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
          MainTheme { SubZeroRemindersTileCards(templateData, pendingIntentList, remoteActionList) }
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
  customColorScheme: ColorScheme? = null,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    customColorScheme
      ?: when {
        dynamicColor -> {
          val context = LocalContext.current
          if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
      }

  MaterialTheme(colorScheme = colorScheme, typography = Typography()) { content() }
}
