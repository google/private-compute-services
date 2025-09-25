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

package com.google.android.`as`.oss.delegatedui.service.templates

import android.content.Context
import android.view.View
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.data.DelegatedUiDataResponses
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.map
import kotlinx.coroutines.asCoroutineDispatcher

/** Responsible for constructing the remote template UI for a particular template type. */
interface TemplateRenderer {

  /**
   * Inflates a view from the given [response] template data, or returns null if a view cannot be
   * created from the given [response] data.
   *
   * @param context The context to use to create the view.
   * @param response The data to bind to the view.
   */
  fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View?
}

/**
 * Inflates a view from the given [responses] template data, or returns null if a view cannot be
 * created from the given [responses] data.
 */
fun TemplateRenderer.render(
  lifecycle: DelegatedUiLifecycle,
  context: Context,
  responses: DelegatedUiDataResponses,
  logUsageData: suspend (DelegatedUiUsageData) -> Unit,
  onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
  onSessionClose: () -> Unit,
): View? {
  return TemplateRendererScope(
      context = context,
      lifecycle = lifecycle,
      mainCoroutineContext = context.mainExecutor.asCoroutineDispatcher(),
      additionalData = responses.additionalData?.map(lifecycle.streamScope) { it.additionalData },
      logUsageData = logUsageData,
      onDataEgress = onDataEgress,
      onSessionClose = onSessionClose,
    )
    .onCreateTemplateView(
      context = context,
      response = responses.templateData.map { it.templateData },
    )
}
