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

package com.google.android.`as`.oss.dataattribution

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.`as`.oss.common.CoroutineQualifiers.IoDispatcher
import com.google.android.`as`.oss.delegatedui.api.integration.templates.uiIdToken
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.EnabledState
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.InteractionType
import com.google.android.`as`.oss.logging.uiusage.api.LogUsageDataRequest.SemanticsType
import com.google.android.`as`.oss.logging.uiusage.api.UsageDataServiceGrpcKt
import com.google.android.`as`.oss.logging.uiusage.api.logUsageDataRequest
import com.google.common.flogger.GoogleLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** View model for [DataAttributionDialog]. */
@HiltViewModel
internal class DataAttributionDialogViewModel
@Inject
constructor(
  @ApplicationContext private val context: Context,
  @IoDispatcher private val ioCoroutineDispatcher: CoroutineDispatcher,
  private var usageDataService: UsageDataServiceGrpcKt.UsageDataServiceCoroutineStub,
) : ViewModel() {
  private val _uiState = MutableStateFlow(DataAttributionUiState())
  val uiState = _uiState.asStateFlow()

  init {
    viewModelScope.launch { resolveSettingsActivity() }
  }

  private suspend fun resolveSettingsActivity() {
    val intent = createUnresolvedSettingsIntent()
    val resolved =
      withContext(ioCoroutineDispatcher) {
        intent.resolveActivityInfo(context.packageManager, 0) != null
      }
    _uiState.update { it.copy(settingsIntent = if (resolved) intent else null) }
  }

  private fun createUnresolvedSettingsIntent(): Intent =
    Intent().apply {
      setClassName(
        "com.google.android.apps.pixel.psi",
        "com.google.android.apps.pixel.psi.app.settings.SettingsActivity",
      )
    }

  fun logUiEvent(
    uiElementType: Int,
    uiElementIndex: Int = 0,
    clientSessionId: String,
    enabledState: EnabledState = EnabledState.ENABLED_STATE_UNSPECIFIED,
    interactionType: InteractionType = InteractionType.INTERACTION_TYPE_CLICK,
    semanticsType: SemanticsType = SemanticsType.SEMANTICS_TYPE_LOG_USAGE,
  ) {
    try {
      // Check for server app version for backwards compatibility.
      val serverAppVersion =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
          context.packageManager.getPackageInfo(PACKAGE_NAME, 0).longVersionCode
        } else {
          context.packageManager
            .getPackageInfo(PACKAGE_NAME, PackageManager.PackageInfoFlags.of(0))
            .longVersionCode
        }

      if (serverAppVersion > VERSION_THRESHOLD) {
        viewModelScope.launch {
          val unused =
            usageDataService.logUsageData(
              logUsageDataRequest {
                this.clientSessionUuid = clientSessionId
                this.uiIdToken = uiIdToken {
                  this.elementType = uiElementType
                  this.index = uiElementIndex
                }
                this.semantics = semanticsType
                this.interaction = interactionType
              }
            )
        }
      } else {
        logger.atWarning().log("Server app version is too old: %d", serverAppVersion)
      }
    } catch (e: Exception) {
      logger.atWarning().withCause(e).log("Failed to log UI event")
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
    private const val PACKAGE_NAME = "com.google.android.apps.pixel.psi"
    private const val VERSION_THRESHOLD =
      1923 // The minimum server app version that supports the new logging API.
  }
}

/**
 * Ui state for [DataAttributionDialog].
 *
 * @property settingsIntent The intent to launch the settings activity. This may be null if the
 *   activity fails to resolve.
 */
data class DataAttributionUiState(val settingsIntent: Intent? = null)
