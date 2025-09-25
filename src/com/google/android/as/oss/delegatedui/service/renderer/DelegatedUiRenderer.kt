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

package com.google.android.`as`.oss.delegatedui.service.renderer

import android.content.Context
import android.view.View
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateType
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions.InvalidTemplateRendererError
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiRenderSpec
import com.google.android.`as`.oss.delegatedui.service.data.DelegatedUiDataRepository
import com.google.android.`as`.oss.delegatedui.service.data.DelegatedUiUsageDataLogger
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.render
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.android.AndroidLogTag
import javax.inject.Inject

/** Responsible for delegating the construction of the remote UI to the right template renderer. */
interface DelegatedUiRenderer {

  /**
   * Creates a view from the given [spec] input data. If this returns null, then it signals that
   * there is no available remote content for the given input data.
   */
  suspend fun render(
    lifecycle: DelegatedUiLifecycle,
    context: Context,
    spec: DelegatedUiRenderSpec,
    onDataEgress: suspend (DelegatedUiEgressData) -> Unit = {},
    onSessionClose: () -> Unit = {},
  ): RenderedView?
}

/** The remote UI rendered as a View. */
data class RenderedView(val view: View, val accessibilityPaneTitle: String)

class DelegatedUiRendererImpl
@Inject
internal constructor(
  private val dataRepository: DelegatedUiDataRepository,
  private val usageDataLogger: DelegatedUiUsageDataLogger,
  private val templateRenderers:
    Map<DelegatedUiTemplateType, @JvmSuppressWildcards TemplateRenderer>,
) : DelegatedUiRenderer {

  override suspend fun render(
    lifecycle: DelegatedUiLifecycle,
    context: Context,
    spec: DelegatedUiRenderSpec,
    onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
    onSessionClose: () -> Unit,
  ): RenderedView? {
    logger.atFiner().log("Fetching data for %s", spec)
    val responses = dataRepository.getData(lifecycle, spec)
    val templateType = responses.templateData.value.templateType
    val templateRenderer =
      templateRenderers[templateType] ?: throw InvalidTemplateRendererError(templateType)

    val view =
      templateRenderer.render(
        lifecycle = lifecycle,
        context = context,
        responses = responses,
        logUsageData = { usageData -> logUsageData(spec, usageData) },
        onDataEgress = onDataEgress,
        onSessionClose = onSessionClose,
      )

    if (view == null) {
      logger
        .atWarning()
        .log(
          "**********\n[DelegatedUILifecycle] DUI-Service template renderer %s rendered null from:\n%s\n**********",
          templateRenderer::class.simpleName,
          responses.templateData.value.templateData,
        )
    } else {
      logger
        .atInfo()
        .log(
          "**********\n[DelegatedUILifecycle] DUI-Service template renderer %s rendered non-null %s from:\n%s\n**********",
          templateRenderer::class.simpleName,
          view,
          responses.templateData.value.templateData,
        )
    }

    return view?.let {
      RenderedView(view, responses.templateData.value.commonData.accessibilityPaneTitle)
    }
  }

  private suspend fun logUsageData(spec: DelegatedUiRenderSpec, usageData: DelegatedUiUsageData) {
    usageDataLogger.logUsageData(
      spec.sessionUuid,
      spec.clientId,
      spec.dataProviderInfo.dataProvider,
      usageData,
    )
  }

  companion object {
    @AndroidLogTag("DelegatedUiRenderer") private val logger = GoogleLogger.forEnclosingClass()
  }
}
