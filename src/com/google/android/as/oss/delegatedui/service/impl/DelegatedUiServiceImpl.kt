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

package com.google.android.`as`.oss.delegatedui.service.impl

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.view.View
import android.window.InputTransferToken
import androidx.compose.runtime.withRunningRecomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.compositionContext
import androidx.compose.ui.unit.IntSize
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.ViewCompat.SCROLL_AXIS_HORIZONTAL
import androidx.core.view.ViewCompat.SCROLL_AXIS_VERTICAL
import androidx.core.view.doOnPreDraw
import com.google.android.`as`.oss.common.ExecutorAnnotations.GeneralExecutorQualifier
import com.google.android.`as`.oss.common.ExecutorAnnotations.IoExecutorQualifier
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiHint
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollDelta
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollStartEvent
import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiNestedScrollEvent.NestedScrollStopEvent
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiCreateRequest
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiInvalidateRequest
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiInvalidateResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiPrepareRequest
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiPrepareResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiRequest
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceGrpcKt
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceParcelableKeys.CONFIGURATION_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceParcelableKeys.INPUT_TRANSFER_TOKEN_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiServiceParcelableKeys.SURFACE_PACKAGE_KEY
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.DelegatedUiUpdateRequest
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiCreateResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiDataEgressResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiHintsResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiInvalidateResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiNestedScrollResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiPrepareResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiSizeChangeResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.delegatedUiUpdateResponse
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.nestedScrollDelta
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.nestedScrollStart
import com.google.android.`as`.oss.delegatedui.api.infra.uiservice.nestedScrollStop
import com.google.android.`as`.oss.delegatedui.api.integration.egress.DelegatedUiEgressData
import com.google.android.`as`.oss.delegatedui.service.common.ConnectLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiDataSpec
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions.InvalidDataProviderServiceError
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions.InvalidTemplateRendererError
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiExceptions.NullTemplateRenderedError
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiRenderSpec
import com.google.android.`as`.oss.delegatedui.service.common.PrepareLifecycle
import com.google.android.`as`.oss.delegatedui.service.common.isExactly
import com.google.android.`as`.oss.delegatedui.service.common.size
import com.google.android.`as`.oss.delegatedui.service.impl.ActiveSessionRequest.ConnectRequest
import com.google.android.`as`.oss.delegatedui.service.impl.ActiveSessionRequest.ExternalRequest
import com.google.android.`as`.oss.delegatedui.service.renderer.DelegatedUiRenderer
import com.google.android.`as`.oss.delegatedui.service.renderer.DelegatedUiViewRoot
import com.google.android.`as`.oss.delegatedui.service.renderer.SkippedRender
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverRpcUtils
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import com.google.common.flogger.android.AndroidLogTag
import dagger.hilt.android.qualifiers.ApplicationContext
import io.grpc.Status
import io.grpc.StatusException
import java.time.InstantSource
import java.util.concurrent.Executor
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

class DelegatedUiServiceImpl
@Inject
internal constructor(
  @ApplicationContext private val appContext: Context,
  @IoExecutorQualifier ioExecutor: Executor,
  @GeneralExecutorQualifier private val generalExecutor: Executor,
  private val renderer: DelegatedUiRenderer,
  private val parcelableOverRpcUtils: ParcelableOverRpcUtils,
) : DelegatedUiServiceGrpcKt.DelegatedUiServiceCoroutineImplBase() {

  private val uiDispatcher = AndroidUiDispatcher.Main
  private val ioDispatcher = ioExecutor.asCoroutineDispatcher()
  private val generalDispatcher = generalExecutor.asCoroutineDispatcher()

  /**
   * Stateful cache of all active sessions that maps from session UUID to a mutable flow associated
   * with that session. An active session is defined as one started by [connectDelegatedUiSession]
   * and is actively streaming.
   *
   * Any emissions of [ExternalRequest] into that flow will be handled by the corresponding session.
   */
  private val activeSessions = mutableMapOf<String, MutableSharedFlow<ExternalRequest>>()

  override suspend fun prepareDelegatedUiSession(
    request: DelegatedUiPrepareRequest
  ): DelegatedUiPrepareResponse {
    var sessionUuid: String? = null
    try {
      logger
        .atInfo()
        .log(
          "vvvvvvvvvv\n[DelegatedUILifecycle] DUI-Service prepareDelegatedUiSession() request received: %s",
          request,
        )
      val configuration =
        with(parcelableOverRpcUtils) { receiveParcelableFromRequest(CONFIGURATION_KEY) }
      val spec = request.toRenderSpec(configuration)
      sessionUuid = spec.sessionUuid

      val view =
        withContext(uiDispatcher) {
          renderer.render(lifecycle = PrepareLifecycle(), context = spec.context, spec = spec).view
        }

      val isExactSize = spec.measureSpecWidth.isExactly() && spec.measureSpecHeight.isExactly()
      val measuredSize =
        when {
          isExactSize -> IntSize(spec.measureSpecWidth.size, spec.measureSpecHeight.size)
          view is ComposeView -> null
          else ->
            withContext(uiDispatcher) {
              withRunningRecomposer { recomposer ->
                view.compositionContext = recomposer
                view.measure(spec.measureSpecWidth, spec.measureSpecHeight)
                IntSize(view.measuredWidth, view.measuredHeight)
              }
            }
        }

      val response = delegatedUiPrepareResponse {
        this.isContentAvailable = true
        measuredSize?.let { this.desiredWidth = it.width }
        measuredSize?.let { this.desiredHeight = it.height }
        this.sessionUuid = spec.sessionUuid
      }

      logger
        .atInfo()
        .log(
          "[DelegatedUILifecycle] DUI-Service sending prepare response: isContentAvailable: true, %s\n^^^^^^^^^^",
          response,
        )
      return response
    } catch (e: Throwable) {
      when (e) {
        is CancellationException -> {
          logger
            .atInfo()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log("[DelegatedUILifecycle] DUI-Service prepare cancelled.\n^^^^^^^^^^")
        }
        is InvalidTemplateRendererError -> {
          logger
            .atWarning()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log(
              "[DelegatedUILifecycle] DUI-Service prepare could not find template renderer.\n^^^^^^^^^^"
            )
        }
        is NullTemplateRenderedError -> {
          logger
            .atWarning()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log("[DelegatedUILifecycle] DUI-Service prepare rendered null view.\n^^^^^^^^^^")
        }
        is InvalidDataProviderServiceError -> {
          logger
            .atWarning()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log(
              "[DelegatedUILifecycle] DUI-Service prepare could not find data provider service.\n^^^^^^^^^^"
            )
        }
        is StatusException -> {
          logger
            .atWarning()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log(
              "[DelegatedUILifecycle] DUI-Service prepare received error from service.\n^^^^^^^^^^"
            )
        }
        else -> {
          logger
            .atSevere()
            .withCause(e)
            .withStackTrace(StackSize.SMALL)
            .log(
              "[DelegatedUILifecycle] DUI-Service prepare encountered unexpected error.\n^^^^^^^^^^"
            )
        }
      }
      return delegatedUiPrepareResponse {
        this.isContentAvailable = false
        sessionUuid?.let { this.sessionUuid = it }
      }
    }
  }

  override fun connectDelegatedUiSession(
    requests: Flow<DelegatedUiRequest>
  ): Flow<DelegatedUiResponse> = channelFlow {
    logger.atInfo().log("[DelegatedUILifecycle] DUI-Service connectDelegatedUiSession() initiated")
    withContext(uiDispatcher) {
      lateinit var lifecycle: ConnectLifecycle
      lateinit var root: DelegatedUiViewRoot

      try {
        val token =
          with(parcelableOverRpcUtils) { receiveParcelableFromRequest(INPUT_TRANSFER_TOKEN_KEY) }
        val configuration =
          with(parcelableOverRpcUtils) { receiveParcelableFromRequest(CONFIGURATION_KEY) }

        val connectRequests = requests.map { ConnectRequest(it) }
        val externalRequests =
          MutableSharedFlow<ExternalRequest>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
          )

        merge(connectRequests, externalRequests).collect { r ->
          logger.atFiner().log("connectDelegatedUiSession r received: %s", r)
          when {
            r is ConnectRequest && r.request.hasCreateRequest() -> {
              logger
                .atInfo()
                .log(
                  "vvvvvvvvvv\n[DelegatedUILifecycle] DUI-Service create request received: %s",
                  r.request.createRequest,
                )

              val spec = r.request.createRequest.toRenderSpec(configuration)
              activeSessions[spec.sessionUuid] = externalRequests

              lifecycle =
                ConnectLifecycle(
                  CoroutineScope(
                    SupervisorJob(coroutineContext[Job]) +
                      ioDispatcher +
                      CoroutineName("ConnectLifecycle.streamScope(${spec.sessionUuid})")
                  )
                )
              root =
                onCreateRequest(
                  lifecycle = lifecycle,
                  spec = spec,
                  displayId = r.request.createRequest.displayId,
                  token = token,
                  onSizeChange = { measuredWidth, measuredHeight ->
                    sendSizeChange(measuredWidth, measuredHeight)
                  },
                  onNestedScroll = { sendNestedScroll(it) },
                  onDataEgress = { sendEgressData(it) },
                  onSendHints = { sendHints(it) },
                  onSessionClose = { closeSession(it) },
                )

              logger
                .atInfo()
                .log("[DelegatedUILifecycle] DUI-Service completed create request.\n^^^^^^^^^^")
            }
            r is ConnectRequest && r.request.hasUpdateRequest() -> {
              logger
                .atInfo()
                .log(
                  "vvvvvvvvvv\n[DelegatedUILifecycle] DUI-Service update request received: %s",
                  r.request.updateRequest,
                )

              val spec = r.request.updateRequest.toRenderSpec(root.spec)
              onUpdateRequest(
                lifecycle = lifecycle,
                spec = spec,
                root = root,
                onDataEgress = { sendEgressData(it) },
                onSendHints = { sendHints(it) },
                onSessionClose = { closeSession() },
              )

              logger
                .atInfo()
                .log("[DelegatedUILifecycle] DUI-Service completed update request.\n^^^^^^^^^^")
            }
            r is ExternalRequest.InvalidateRequest -> {
              logger
                .atInfo()
                .log(
                  "vvvvvvvvvv\n[DelegatedUILifecycle] DUI-Service invalidate request received: %s",
                  r,
                )

              onInvalidateRequest(
                lifecycle = lifecycle,
                spec = root.spec,
                root = root,
                onDataEgress = { sendEgressData(it) },
                onSessionClose = { closeSession() },
              )

              logger
                .atInfo()
                .log("[DelegatedUILifecycle] DUI-Service completed invalidate request.\n^^^^^^^^^^")
            }
          }
        }
      } catch (e: Throwable) {
        when (e) {
          is CancellationException -> {
            logger
              .atInfo()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log("[DelegatedUILifecycle] DUI-Service connect cancelled.\n^^^^^^^^^^")
          }
          is InvalidTemplateRendererError -> {
            logger
              .atWarning()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log(
                "[DelegatedUILifecycle] DUI-Service connect could not find template renderer.\n^^^^^^^^^^"
              )
          }
          is NullTemplateRenderedError -> {
            logger
              .atWarning()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log("[DelegatedUILifecycle] DUI-Service connect rendered null view.\n^^^^^^^^^^")
          }
          is InvalidDataProviderServiceError -> {
            logger
              .atWarning()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log(
                "[DelegatedUILifecycle] DUI-Service connect could not find data provider service.\n^^^^^^^^^^"
              )
          }
          is StatusException -> {
            logger
              .atWarning()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log(
                "[DelegatedUILifecycle] DUI-Service connect received error from service.\n^^^^^^^^^^"
              )
          }
          else -> {
            logger
              .atSevere()
              .withCause(e)
              .withStackTrace(StackSize.SMALL)
              .log(
                "[DelegatedUILifecycle] DUI-Service connect encountered unexpected error.\n^^^^^^^^^^"
              )
          }
        }
        throw e
      } finally {
        activeSessions.remove(root.spec.sessionUuid)
        root.destroyGracefully(100.milliseconds)
        lifecycle.streamScope.cancel()
      }
    }
  }

  override suspend fun invalidateDelegatedUiSession(
    request: DelegatedUiInvalidateRequest
  ): DelegatedUiInvalidateResponse {
    try {
      logger
        .atInfo()
        .log(
          "vvvvvvvvvv\n[DelegatedUILifecycle] DUI-Service invalidateDelegatedUiSession() request received: %s",
          request,
        )

      val success = activeSessions[request.sessionUuid]?.tryEmit(ExternalRequest.InvalidateRequest)

      val response = delegatedUiInvalidateResponse { this.success = success == true }
      logger
        .atInfo()
        .log(
          "[DelegatedUILifecycle] DUI-Service sending invalidate response: %s\n^^^^^^^^^^",
          response,
        )
      return response
    } catch (e: Throwable) {
      logger
        .atSevere()
        .withCause(e)
        .withStackTrace(StackSize.SMALL)
        .log(
          "[DelegatedUILifecycle] DUI-Service invalidate encountered unexpected error.\n^^^^^^^^^^"
        )
      return delegatedUiInvalidateResponse { this.success = false }
    }
  }

  private fun ProducerScope<DelegatedUiResponse>.sendSizeChange(
    measuredWidth: Int,
    measuredHeight: Int,
  ) {
    launch {
      logger.atFiner().log("Sending size change response: %d, %d", measuredWidth, measuredHeight)
      send(
        delegatedUiResponse {
          this.sizeChangeResponse = delegatedUiSizeChangeResponse {
            this.updatedWidth = measuredWidth
            this.updatedHeight = measuredHeight
          }
        }
      )
    }
  }

  private fun ProducerScope<DelegatedUiResponse>.sendNestedScroll(
    event: DelegatedUiNestedScrollEvent
  ) {
    launch {
      logger.atFinest().log("Sending nested scroll response: %s", event)

      send(
        delegatedUiResponse {
          this.nestedScrollResponse = delegatedUiNestedScrollResponse {
            when (event) {
              is NestedScrollStartEvent -> {
                this.nestedScrollStart = nestedScrollStart { this.axes = event.axes }
              }
              is NestedScrollDelta -> {
                this.nestedScrollDelta = nestedScrollDelta {
                  this.scrollX = event.scrollX
                  this.scrollY = event.scrollY
                }
              }
              is NestedScrollStopEvent -> {
                this.nestedScrollStop = nestedScrollStop {
                  this.flingX = event.flingX
                  this.flingY = event.flingY
                }
              }
            }
          }
        }
      )
    }
  }

  private suspend fun ProducerScope<DelegatedUiResponse>.sendEgressData(
    data: DelegatedUiEgressData
  ) {
    logger.atFiner().log("Sending egress data response: %s", data)
    send(
      delegatedUiResponse {
        this.dataEgressResponse = delegatedUiDataEgressResponse { this.egressData = data }
      }
    )
  }

  private suspend fun ProducerScope<DelegatedUiResponse>.sendHints(hints: Set<DelegatedUiHint>) {
    logger.atFiner().log("Sending hints response: %s", hints)
    send(
      delegatedUiResponse { this.hintsResponse = delegatedUiHintsResponse { this.hints += hints } }
    )
  }

  /**
   * Cancels the DUI session with a server-side close. This raises a [CancellationException] in the
   * receiver's coroutine.
   *
   * If [cause] is provided, this is considered an unexpected close due to a crash. If [cause] is
   * not provided, then this represents an intentional close.
   *
   * In either case, the client should not automatically retry the DUI session.
   */
  private fun ProducerScope<DelegatedUiResponse>.closeSession(cause: Exception? = null) {
    val description =
      when (cause) {
        null -> "Server-side close due to user cancellation."
        else -> "Server-side close due to unexpected error."
      }
    close(StatusException(Status.CANCELLED.withDescription(description).withCause(cause)))
  }

  @SuppressLint("NewApi") // Context#getDisplay and SurfacePackage requires API 30
  private suspend fun SendChannel<DelegatedUiResponse>.onCreateRequest(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
    token: InputTransferToken,
    displayId: Int,
    onSizeChange: (Int, Int) -> Unit,
    onNestedScroll: (DelegatedUiNestedScrollEvent) -> Unit,
    onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
    onSendHints: suspend (Set<DelegatedUiHint>) -> Unit,
    onSessionClose: (Exception?) -> Unit,
  ): DelegatedUiViewRoot {
    val display =
      with(DisplayManagerCompat.getInstance(spec.context)) { getDisplay(displayId) ?: displays[0] }
    val root =
      DelegatedUiViewRoot(
        sessionUuid = spec.sessionUuid,
        context = spec.context,
        display = display,
        token = token,
        onSizeChange = onSizeChange,
        onNestedScroll = onNestedScroll,
        onRenderError = onSessionClose,
      )

    with(parcelableOverRpcUtils) {
      attachParcelableToResponse(SURFACE_PACKAGE_KEY, root.surfacePackage)
    }

    logger.atFiner().log("Rendering view for create request: %s", spec.sessionUuid)
    val renderedView =
      renderer.render(
        lifecycle = lifecycle,
        context = spec.context,
        spec = spec,
        onDataEgress = onDataEgress,
        onSendHints = onSendHints,
        onSessionClose = { onSessionClose(null) },
      )
    root.setContentView(renderedView, spec)

    renderedView.view.awaitNextPreDraw()
    logger.atFiner().log("Sending create response: %s", spec.sessionUuid)
    send(
      delegatedUiResponse {
        this.createResponse = delegatedUiCreateResponse { this.sessionUuid = spec.sessionUuid }
      }
    )

    return root
  }

  private suspend fun SendChannel<DelegatedUiResponse>.onUpdateRequest(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
    root: DelegatedUiViewRoot,
    onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
    onSendHints: suspend (Set<DelegatedUiHint>) -> Unit,
    onSessionClose: () -> Unit,
  ) {
    if (root.spec.dataSpec != spec.dataSpec) {
      logger.atFiner().log("Rendering view for update request: %s", spec.sessionUuid)
      val renderedView =
        renderer.render(
          lifecycle = lifecycle,
          context = spec.context,
          spec = spec,
          onDataEgress = onDataEgress,
          onSendHints = onSendHints,
          onSessionClose = onSessionClose,
        )
      root.setContentView(renderedView, spec)
      logger.atFiner().log("Rendered view %s for update request: %s", renderedView.view, spec)

      renderedView.view.awaitNextPreDraw()
    } else {
      logger.atFiner().log("Updating existing view for update request: %s", spec.sessionUuid)
      root.updateInputSpec(spec.inputSpec)
      root.setContentView(SkippedRender, spec)
      logger.atFiner().log("Updated existing view for update request: %s", spec)
    }

    logger.atFiner().log("Sending update response")
    send(delegatedUiResponse { this.updateResponse = delegatedUiUpdateResponse {} })
  }

  private suspend fun SendChannel<DelegatedUiResponse>.onInvalidateRequest(
    lifecycle: DelegatedUiLifecycle,
    spec: DelegatedUiRenderSpec,
    root: DelegatedUiViewRoot,
    onDataEgress: suspend (DelegatedUiEgressData) -> Unit,
    onSessionClose: () -> Unit,
  ) {
    logger.atFiner().log("Rendering view for invalidate request: %s", spec.sessionUuid)
    val renderedView =
      renderer.render(
        lifecycle = lifecycle,
        context = spec.context,
        spec = spec,
        onDataEgress = onDataEgress,
        onSessionClose = onSessionClose,
      )
    root.setContentView(renderedView, spec)
    logger.atFiner().log("Rendered view %s for invalidate request: %s", renderedView.view, spec)
  }

  private fun DelegatedUiPrepareRequest.toRenderSpec(
    configuration: Configuration
  ): DelegatedUiRenderSpec =
    DelegatedUiRenderSpec(
      configuration = configuration,
      clientId = this.clientId,
      sessionUuid = generateSessionUuid(),
      dataProviderInfo = this.dataProviderInfo,
      measureSpecWidth = this.measureSpecWidth,
      measureSpecHeight = this.measureSpecHeight,
      backgroundColor = Color.TRANSPARENT,
      clientNestedScrollAxes = SCROLL_AXIS_HORIZONTAL or SCROLL_AXIS_VERTICAL,
      clientNestedScrollAxisLock = true,
      inputSpec = DelegatedUiInputSpec(noTouchHint = false),
      dataSpec = DelegatedUiDataSpec(ingressData = this.ingressData),
    )

  private fun DelegatedUiCreateRequest.toRenderSpec(
    configuration: Configuration
  ): DelegatedUiRenderSpec =
    this.clientInputs.run {
      DelegatedUiRenderSpec(
        configuration = configuration,
        clientId = clientId,
        sessionUuid = sessionUuid.ifEmpty { generateSessionUuid() },
        dataProviderInfo = dataProviderInfo,
        measureSpecWidth = measureSpecWidth,
        measureSpecHeight = measureSpecHeight,
        backgroundColor = backgroundColor,
        clientNestedScrollAxes = clientNestedScrollAxes,
        clientNestedScrollAxisLock =
          when (hasClientNestedScrollAxisLock()) {
            true -> clientNestedScrollAxisLock
            else -> true
          },
        inputSpec = DelegatedUiInputSpec(noTouchHint = noTouchHint),
        dataSpec = DelegatedUiDataSpec(ingressData = this.ingressData),
      )
    }

  private fun DelegatedUiUpdateRequest.toRenderSpec(
    spec: DelegatedUiRenderSpec
  ): DelegatedUiRenderSpec =
    this.clientInputs.run {
      spec.copy(
        dataProviderInfo = if (hasDataProviderInfo()) dataProviderInfo else spec.dataProviderInfo,
        measureSpecWidth = if (hasMeasureSpecWidth()) measureSpecWidth else spec.measureSpecWidth,
        measureSpecHeight =
          if (hasMeasureSpecHeight()) measureSpecHeight else spec.measureSpecHeight,
        backgroundColor = if (hasBackgroundColor()) backgroundColor else spec.backgroundColor,
        clientNestedScrollAxes =
          if (hasClientNestedScrollAxes()) clientNestedScrollAxes else spec.clientNestedScrollAxes,
        clientNestedScrollAxisLock =
          when (hasClientNestedScrollAxisLock()) {
            true -> clientNestedScrollAxisLock
            else -> spec.clientNestedScrollAxisLock
          },
        inputSpec =
          DelegatedUiInputSpec(
            noTouchHint = if (hasNoTouchHint()) noTouchHint else spec.inputSpec.noTouchHint
          ),
        dataSpec =
          DelegatedUiDataSpec(
            ingressData = if (hasIngressData()) ingressData else spec.dataSpec.ingressData
          ),
      )
    }

  private fun generateSessionUuid() = InstantSource.system().instant().toString()

  private suspend fun View.awaitNextPreDraw() {
    suspendCancellableCoroutine { cont ->
      val listener = doOnPreDraw { if (cont.isActive) cont.resume(Unit) }

      cont.invokeOnCancellation { listener.removeListener() }
    }
  }

  private fun DelegatedUiViewRoot.destroyGracefully(delayDuration: Duration) {
    CoroutineScope(generalDispatcher).launch(NonCancellable) {
      delay(delayDuration)
      // Switch to the UI dispatcher to call destroy
      withContext(uiDispatcher) { destroy() }
    }
  }

  private val DelegatedUiRenderSpec.context: Context
    get() = appContext.createConfigurationContext(configuration)

  companion object {
    @AndroidLogTag("DelegatedUiService") private val logger = GoogleLogger.forEnclosingClass()
  }
}
