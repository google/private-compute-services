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

package com.android.personalcontext.ace.visualizer.connector

import android.content.Context
import android.service.personalcontext.embedded.InsightSurfaceClientInfo
import android.service.personalcontext.hint.PublishedContextHint
import android.service.personalcontext.insight.HintInvalidationInsight
import android.util.Log
import android.view.View
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.LocalSaveableStateRegistry
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.personalcontext.ace.common.MetaTags.ACE_EMBEDDED_TAG
import com.android.personalcontext.ace.common.PrettyPrintUtils.toPrettyPrint
import com.android.personalcontext.ace.common.wrappers.IInsightSurfaceClientInfo
import com.android.personalcontext.ace.common.wrappers.IPublishedContextInsight
import com.android.personalcontext.ace.common.wrappers.IRenderToken
import com.android.personalcontext.ace.visualizer.compat.ClientSignalCompat
import com.android.personalcontext.ace.visualizer.compat.EmbeddedScrollCompat
import com.android.personalcontext.ace.visualizer.compat.EmptyRenderCompat
import com.android.personalcontext.ace.visualizer.compat.InsightEventReporterFactoryCompat
import com.android.personalcontext.ace.visualizer.compat.PrototypeTransformCompat
import com.android.personalcontext.ace.visualizer.compose.ComposeViewFactory
import com.android.personalcontext.ace.visualizer.embeddedscroll.embeddedScroll
import com.android.personalcontext.ace.visualizer.embeddedtheme.EmbeddedTheme
import com.android.personalcontext.ace.visualizer.session.VisualizerSession
import com.android.personalcontext.ace.visualizer.session.VisualizerSessionFactory
import com.android.personalcontext.ace.visualizer.templates.LocalInsightEventReporter
import com.android.personalcontext.ace.visualizer.templates.LocalInsightSurfaceClientInfo
import com.android.personalcontext.ace.visualizer.templates.LocalPublishedContextInsight
import com.android.personalcontext.ace.visualizer.templates.LocalRenderToken
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of [VisualizerServiceConnector].
 *
 * This class is responsible for handling the insights and constructing a View by rendering from a
 * suitable template.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressWarnings("FlaggedApi", "NewApi")
class VisualizerServiceConnectorImpl
@Inject
constructor(
  private val templates: Set<@JvmSuppressWildcards VisualizerTemplate>,
  private val sessionFactory: VisualizerSessionFactory,
  private val composeViewFactory: ComposeViewFactory,
  private val embeddedScrollCompat: EmbeddedScrollCompat,
  private val emptyRenderCompat: EmptyRenderCompat,
  private val insightEventReporterFactoryCompat: InsightEventReporterFactoryCompat,
  private val prototypeTransformationCompat: PrototypeTransformCompat,
  private val clientSignalCompat: ClientSignalCompat,
) : VisualizerServiceConnector {

  private val sessions = mutableMapOf<UUID, VisualizerSession>()
  private val embeddedViews = mutableMapOf<UUID, View>()
  private val originHints = mutableMapOf<UUID, Set<PublishedContextHint>>()
  private val clientInfoStates = mutableMapOf<UUID, MutableState<IInsightSurfaceClientInfo>>()
  private val savedComposeStates = mutableMapOf<UUID, Map<String, List<Any?>>>()

  override fun onClientConnected(info: InsightSurfaceClientInfo) {
    Log.i(TAG, "Visualizer: onClientConnected(${info.id}) [$ACE_EMBEDDED_TAG]")

    sessions.computeIfAbsent(info.id) {
      val view =
        checkNotNull(embeddedViews[info.id]) {
          "onClientConnected() called before onCreateEmbeddedView() returned a valid ComposeView for ${info.id}"
        }
      sessionFactory.createSession(view)
    }
  }

  override fun onCreateEmbeddedView(
    context: Context,
    publishedInsight: IPublishedContextInsight,
    renderToken: IRenderToken?,
    info: IInsightSurfaceClientInfo,
  ): View? {
    Log.i(TAG, "Visualizer: onCreateEmbeddedView(${info.id}) [$ACE_EMBEDDED_TAG]")

    val clientInfoState = mutableStateOf(info)
    clientInfoStates[info.id] = clientInfoState

    val result = createEmbeddedView(renderToken, publishedInsight, clientInfoState, context)

    return when (result) {
      is VisualizerResult.NoView -> null
      is VisualizerResult.SameView -> embeddedViews[info.id]
      is VisualizerResult.NewView -> {
        result.view.also {
          embeddedViews[info.id] = it
          originHints[info.id] = publishedInsight.insight.originHints
        }
      }
    }
  }

  private fun createEmbeddedView(
    renderToken: IRenderToken?,
    publishedInsight: IPublishedContextInsight,
    clientInfoState: MutableState<IInsightSurfaceClientInfo>,
    context: Context,
  ): VisualizerResult {
    if (renderToken == null) {
      Log.e(TAG, "Visualizer: RenderToken must never be null [$ACE_EMBEDDED_TAG]")
      return VisualizerResult.NoView
    }

    val insight = publishedInsight.insight

    val hintTypes =
      insight.originHints.toPrettyPrint { prototypeTransformationCompat.transform(it) }
    val insightTypes =
      insight.toPrettyPrint(
        transform = { prototypeTransformationCompat.transform(it) },
        children = { prototypeTransformationCompat.transformChildren(it) },
      )

    Log.i(TAG, "Visualizer: Received ($hintTypes) -> ($insightTypes) [$ACE_EMBEDDED_TAG]")

    if (emptyRenderCompat.isEmpty(insight)) {
      Log.w(TAG, "Visualizer: Empty insight, returning null view [$ACE_EMBEDDED_TAG]")
      return VisualizerResult.NoView
    }

    if (insight is HintInvalidationInsight) {
      val isCurrentViewInvalidated =
        originHints[clientInfoState.value.id]?.any { insight.isHintInvalidated(it) } == true
      return if (isCurrentViewInvalidated) {
        VisualizerResult.NewView(View(context))
      } else {
        VisualizerResult.SameView
      }
    }

    with(clientSignalCompat) {
      if (insight.containsPiiHint()) clientInfoState.value.sendPiiClientSignal()
    }

    val contents = templates.mapNotNull { template ->
      val result = runCatching { template.handleInsight(publishedInsight) }

      result.onFailure { e ->
        Log.e(
          TAG,
          "Visualizer: → ${template.javaClass.simpleName} ERROR: ${e.stackTraceToString()} [$ACE_EMBEDDED_TAG]",
        )
      }
      result.onSuccess { content ->
        if (content == null) {
          Log.v(TAG, "Visualizer: → ${template.javaClass.simpleName} SKIPPED [$ACE_EMBEDDED_TAG]")
        } else {
          Log.i(TAG, "Visualizer: → ${template.javaClass.simpleName} HANDLED [$ACE_EMBEDDED_TAG]")
        }
      }

      result.getOrNull()
    }

    if (contents.isEmpty()) {
      Log.e(
        TAG,
        "Visualizer: No templates found to render insight types ($insightTypes), returning null view [$ACE_EMBEDDED_TAG]",
      )
      return VisualizerResult.NoView
    }

    if (contents.size > 1) {
      Log.w(
        TAG,
        "Visualizer: Multiple templates want to render insight types ($insightTypes), making an arbitrary choice [$ACE_EMBEDDED_TAG]",
      )
    }

    val content = contents.first()

    return VisualizerResult.NewView(
      composeViewFactory.createComposeView(context) {
        setContent {
          val currentInfo by clientInfoState

          val saveableStateRegistry =
            remember(id) {
              SaveableStateRegistry(
                restoredValues = savedComposeStates[currentInfo.id],
                canBeSaved = { true },
              )
            }

          val blurRadius by
            animateDpAsState(
              targetValue = if (currentInfo.shouldBlur()) 5.dp else 0.dp,
              animationSpec =
                if (currentInfo.shouldBlur()) {
                  MaterialTheme.motionScheme.slowEffectsSpec()
                } else {
                  MaterialTheme.motionScheme.defaultEffectsSpec()
                },
              label = "BlurAnimation",
            )

          val sessionScope = sessions[currentInfo.id]?.sessionScope
          val insightEventReporter =
            remember(sessionScope) { insightEventReporterFactoryCompat.create(sessionScope) }

          val context = LocalContext.current
          val configuration = currentInfo.configuration
          val clientContext =
            remember(context, configuration) { context.createConfigurationContext(configuration) }

          CompositionLocalProvider(
            LocalContext provides clientContext,
            LocalConfiguration provides clientContext.resources.configuration,
            LocalSaveableStateRegistry provides saveableStateRegistry,
            LocalInsightSurfaceClientInfo provides currentInfo,
            LocalRenderToken provides renderToken,
            LocalPublishedContextInsight provides publishedInsight,
            LocalInsightEventReporter provides insightEventReporter,
          ) {
            EmbeddedTheme {
              Box(
                modifier =
                  Modifier.embeddedScroll { event ->
                      with(embeddedScrollCompat) { currentInfo.sendEmbeddedScrollEvent(event) }
                    }
                    .background(Color(currentInfo.backgroundColor.toArgb()))
                    .blur(radius = blurRadius, edgeTreatment = BlurredEdgeTreatment.Unbounded)
              ) {
                content()
              }

              DisposableEffect(currentInfo.id) {
                onDispose {
                  savedComposeStates[currentInfo.id] = saveableStateRegistry.performSave()
                }
              }
            }
          }
        }
      }
    )
  }

  override fun onClientUpdated(
    oldClientInfo: IInsightSurfaceClientInfo,
    newClientInfo: IInsightSurfaceClientInfo,
  ): Boolean {
    Log.i(
      TAG,
      "Visualizer: onClientUpdated(${newClientInfo.id}): ${diff(oldClientInfo,newClientInfo)} [$ACE_EMBEDDED_TAG]",
    )
    val state = clientInfoStates[newClientInfo.id]
    if (state != null) {
      state.value = newClientInfo
      return true
    }
    return false
  }

  override fun onClientDisconnected(info: IInsightSurfaceClientInfo) {
    Log.i(TAG, "Visualizer: onClientDisconnected(${info.id}) [$ACE_EMBEDDED_TAG]")

    sessions.remove(info.id)?.destroy()
    embeddedViews.remove(info.id)
    originHints.remove(info.id)
    clientInfoStates.remove(info.id)
    savedComposeStates.remove(info.id)
  }

  private fun diff(old: IInsightSurfaceClientInfo, new: IInsightSurfaceClientInfo): String {
    val changes = buildList {
      fun <T> addIfChanged(oldVal: T, newVal: T, label: String) {
        if (oldVal != newVal) add(label)
      }

      addIfChanged(old.id, new.id, "id=${new.id}")
      addIfChanged(old.displayId, new.displayId, "displayId=${new.displayId}")
      addIfChanged(
        old.measureSpecWidth,
        new.measureSpecWidth,
        "measureSpecWidth=${new.measureSpecWidth}",
      )
      addIfChanged(
        old.measureSpecHeight,
        new.measureSpecHeight,
        "measureSpecHeight=${new.measureSpecHeight}",
      )
      addIfChanged(
        old.backgroundColor,
        new.backgroundColor,
        "backgroundColor=${new.backgroundColor}",
      )
      addIfChanged(
        old.nestedScrollAxes,
        new.nestedScrollAxes,
        "nestedScrollAxes=${new.nestedScrollAxes}",
      )
      addIfChanged(
        old.nestedScrollAxisLocked,
        new.nestedScrollAxisLocked,
        "nestedScrollAxisLocked=${new.nestedScrollAxisLocked}",
      )
      addIfChanged(old.shouldBlur(), new.shouldBlur(), "shouldBlur=${new.shouldBlur()}")
      addIfChanged(
        old.themeResourceId,
        new.themeResourceId,
        "themeResourceId=${new.themeResourceId}",
      )
      addIfChanged(old.packageName, new.packageName, "packageName=${new.packageName}")
      addIfChanged(old.configuration, new.configuration, "configuration changed")
    }

    return changes.joinToString(separator = ", ", prefix = "{", postfix = "}").ifEmpty {
      "no changes"
    }
  }

  companion object {
    private const val TAG = "VisualizerService"
  }
}

/** The outcome of an attempt to create an embedded View. */
private sealed interface VisualizerResult {

  /** Indicates that the visualizer was unable to, or chose not to, create a View. */
  object NoView : VisualizerResult

  /** Indicates that the currently displayed View should continue to be displayed. */
  object SameView : VisualizerResult

  /** Indicates that a new View was successfully created and is ready to be displayed. */
  data class NewView(val view: View) : VisualizerResult
}
