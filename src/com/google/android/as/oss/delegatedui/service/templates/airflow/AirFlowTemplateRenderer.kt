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

package com.google.android.`as`.oss.delegatedui.service.templates.airflow

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.AirflowEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.ErrorEvent
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.airflowEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.dataReadyEvent
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.errorEvent
import com.google.android.`as`.oss.delegatedui.api.integration.egress.airflow.preparingDataEvent
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.airflow.data.AirflowDataServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.service.templates.airflow.data.Annotations.AirflowDataService
import com.google.android.`as`.oss.delegatedui.service.templates.airflow.data.getDataRequest
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.common.flogger.GoogleLogger
import io.grpc.Status
import io.grpc.StatusRuntimeException
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A [TemplateRenderer] for the AirFlow template. This is the entry point for rendering the
 * template.
 */
class AirFlowTemplateRenderer
@Inject
internal constructor(
  @AirflowDataService
  private val airflowDataServiceStub: AirflowDataServiceGrpcKt.AirflowDataServiceCoroutineStub
) : TemplateRenderer {

  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val data = response.data.airflowTemplateData
    return ComposeView(context).apply {
      setContent {
        MainTheme {
          LaunchedEffect(Unit) { doOnImpression(data.uiIdToken) { logUsage() } }

          CtaPill(
            label = data.label,
            icon = response.image.valueOrNull,
            modifier =
              Modifier.doOnClick(
                data.uiIdToken,
                onClick = {
                  logger.atInfo().log("AirFlowTemplateRenderer: onClick")

                  launch { logUsage() } // Will log the click for analytics.
                  launch {
                    sendEgressData {
                      airflowEgressData = airflowEgressData {
                        preparingDataEvent = preparingDataEvent {}
                      }
                    }
                    logger.atInfo().log("AirFlowTemplateRenderer: sent preparingDataEvent")

                    val egressData = makeEgressData()
                    sendEgressData { airflowEgressData = egressData }
                    logger.atInfo().log("AirFlowTemplateRenderer: sent egress event")
                  }
                },
              ),
          )
        }
      }
    }
  }

  private suspend fun makeEgressData(): AirflowEgressData = airflowEgressData {
    try {
      val response = airflowDataServiceStub.getData(getDataRequest {})
      dataReadyEvent = dataReadyEvent { this.data = response.data }
    } catch (e: StatusRuntimeException) {
      logger.atWarning().withCause(e).log("Failed to get data from AirflowDataService")
      errorEvent = errorEvent {
        this.statusCode =
          when (e.status.code) {
            Status.Code.DEADLINE_EXCEEDED -> ErrorEvent.StatusCode.DEADLINE_EXCEEDED
            else -> ErrorEvent.StatusCode.INTERNAL
          }
      }
    } catch (e: Exception) {
      logger.atWarning().withCause(e).log("Failed to get data from AirflowDataService")
      errorEvent = errorEvent { this.statusCode = ErrorEvent.StatusCode.INTERNAL }
    }
  }

  private companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}

/** A call to action UI in the form of a pill. */
@Composable
fun CtaPill(label: String, icon: Bitmap?, modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .heightIn(min = CTA_MIN_HEIGHT)
        .background(
          color = MaterialTheme.colorScheme.surface,
          shape = RoundedCornerShape(CORNER_RADIUS),
        )
        .clip(RoundedCornerShape(CORNER_RADIUS))
        .focusable()
        .padding(horizontal = HORIZONTAL_PADDING, vertical = VERTICAL_PADDING),
    contentAlignment = Alignment.CenterStart,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      icon?.let {
        Icon(
          it.asImageBitmap(),
          contentDescription = null,
          modifier = Modifier.size(ICON_SIZE),
          tint = MaterialTheme.colorScheme.onSurface,
        )
      }
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Start,
      )
      Spacer(modifier = Modifier.width(4.dp))
    }
  }
}

private val ICON_SIZE = 20.dp
private val CORNER_RADIUS = 16.dp
private val HORIZONTAL_PADDING = 12.dp
private val VERTICAL_PADDING = 8.dp
private val CTA_MIN_HEIGHT = 48.dp

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

  MaterialTheme(colorScheme = colorScheme, typography = Typography()) { content() }
}
