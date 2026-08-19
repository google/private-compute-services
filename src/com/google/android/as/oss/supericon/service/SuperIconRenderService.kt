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

package com.google.android.`as`.oss.supericon.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.RemoteException
import android.util.LruCache
import android.util.Size
import android.util.TypedValue
import android.view.Display
import android.view.Gravity
import android.view.InflateException
import android.view.LayoutInflater
import android.view.SurfaceControlViewHost
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.window.InputTransferToken
import androidx.annotation.VisibleForTesting
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.createBitmap
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.google.android.`as`.oss.common.Executors.PIR_EXECUTOR
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.supericon.aidl.ConversationData
import com.google.android.`as`.oss.supericon.aidl.ISuperIconRenderCallback
import com.google.android.`as`.oss.supericon.aidl.ISuperIconRenderService
import com.google.android.`as`.oss.supericon.aidl.ISuperIconSurfacePackageResultCallback
import com.google.android.`as`.oss.supericon.aidl.ISuperIconUi
import com.google.android.`as`.oss.supericon.aidl.RenderOptions
import com.google.android.`as`.oss.supericon.aidl.RenderOptions.Companion.DEFAULT_BACKGROUND_SIZE
import com.google.android.`as`.oss.supericon.config.SuperIconConfig
import com.google.android.`as`.oss.supericon.utils.ConsentEventConstants
import com.google.android.`as`.oss.supericon.utils.SuperIconErrorCodes
import com.google.android.`as`.oss.supericon.utils.SuperIconUiType
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.MaterialColors
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.common.flogger.android.AndroidFluentLogger
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** A service that renders a view based on the given view spec. */
@SuppressLint("NewApi")
@AndroidEntryPoint(Service::class)
class SuperIconRenderService : Hilt_SuperIconRenderService() {
  internal var backgroundDispatcher: CoroutineDispatcher = PIR_EXECUTOR.asCoroutineDispatcher()
    @VisibleForTesting
    set(value) {
      field = value
      backgroundScope = CoroutineScope(value)
    }

  internal var backgroundScope = CoroutineScope(backgroundDispatcher)

  private lateinit var mainDispatcher: CoroutineDispatcher

  private lateinit var mainScope: CoroutineScope

  @Inject lateinit var configReader: ConfigReader<SuperIconConfig>
  @Inject internal lateinit var consentManager: SuperIconConsentManager
  @Inject internal lateinit var callbackHelper: ConversationContentCallbackHelper
  @Inject internal lateinit var surfaceControlViewHostFactory: SurfaceControlViewHostFactory

  private lateinit var renderService: SuperIconRenderServiceBinderStub

  override fun onCreate() {
    logger.atInfo().log("onCreate")
    super.onCreate()
    mainDispatcher = mainExecutor.asCoroutineDispatcher()
    mainScope = CoroutineScope(mainDispatcher)
  }

  override fun onDestroy() {
    super.onDestroy()
    backgroundScope.cancel()
    mainScope.cancel()
  }

  override fun onBind(intent: Intent): IBinder {
    logger.atInfo().log("onBind")
    renderService =
      SuperIconRenderServiceBinderStub(
        this,
        backgroundScope,
        backgroundDispatcher,
        mainScope,
        mainDispatcher,
      )
    return renderService
  }

  override fun onUnbind(intent: Intent): Boolean {
    logger.atInfo().log("onUnbind")
    renderService.cancelRender()
    logger
      .atFine()
      .log(
        "size of activeSuperIconUis: %s when onUnbind()",
        renderService.activeSuperIconUis.size(),
      )
    return super.onUnbind(intent)
  }

  internal fun createForceDarkImmuneDrawable(original: Drawable): Drawable {
    val width = original.intrinsicWidth.takeIf { it > 0 } ?: 100
    val height = original.intrinsicHeight.takeIf { it > 0 } ?: 100

    // 1. Bake the tint into a raw Bitmap using the CPU
    val tintedBitmap = createBitmap(width, height)
    val canvas = Canvas(tintedBitmap)
    original.setBounds(0, 0, width, height)
    original.draw(canvas)

    // 2. Wrap it in a Shader geometry so the GPU doesn't recognize it as an image
    return object : Drawable() {
      private val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          shader = BitmapShader(tintedBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
      private val matrix = Matrix()

      override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        // Scale the texture to match the ImageView's bounds
        val scaleX = bounds.width().toFloat() / tintedBitmap.width
        val scaleY = bounds.height().toFloat() / tintedBitmap.height
        matrix.setScale(scaleX, scaleY)
        paint.shader.setLocalMatrix(matrix)
      }

      override fun draw(canvas: Canvas) {
        canvas.drawRect(bounds, paint)
      }

      override fun getIntrinsicWidth(): Int = tintedBitmap.width

      override fun getIntrinsicHeight(): Int = tintedBitmap.height

      override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
      }

      override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}

      @Deprecated("Deprecated") override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
    }
  }

  internal inner class SuperIconRenderServiceBinderStub(
    private val context: Context,
    private val backgroundScope: CoroutineScope,
    private val backgroundDispatcher: CoroutineDispatcher,
    private val mainScope: CoroutineScope,
    private val mainDispatcher: CoroutineDispatcher,
  ) : ISuperIconRenderService.Stub() {
    private val currentRenderRequests = ConcurrentHashMap<Int, RenderRequestParams>()
    internal val activeSuperIconUis =
      object : LruCache<SuperIconUi, Boolean>(MAXIMUM_ACTIVE_UI_COUNT) {
        override fun entryRemoved(
          evicted: Boolean,
          superIconUi: SuperIconUi,
          oldValue: Boolean,
          newValue: Boolean?,
        ) {
          // Final safety net to release the SurfaceControlViewHost resources in case there is a
          // leak somehow.
          logger.atFine().log("releases surfaceControlViewHost on eviction %s", superIconUi)
          superIconUi.releaseOnEviction()
        }
      }
    private var consentDialogUi: SuperIconUi? = null

    // Currently only allow Gboard to connect to [SuperIconRenderService].
    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
      if (!configReader.config.enableSuperIcon) {
        throw RemoteException("super icon feature is disabled.")
      }
      return super.onTransact(code, data, reply, flags)
    }

    override fun render(
      icon: Icon,
      iconWidth: Int,
      iconHeight: Int,
      background: Icon,
      width: Int,
      height: Int,
      minWidth: Int,
      minHeight: Int,
      maxWidth: Int,
      maxHeight: Int,
      displayId: Int,
      configuration: Configuration,
      hostInputToken: InputTransferToken,
      callback: ISuperIconRenderCallback,
    ) {
      renderWithOptions(
        RenderOptions(
          width = width,
          height = height,
          minWidth = minWidth,
          minHeight = minHeight,
          maxWidth = maxWidth,
          maxHeight = maxHeight,
          uiType = SuperIconUiType.SUPER_ICON,
          icon = icon,
          iconWidth = iconWidth,
          iconHeight = iconHeight,
          iconScaleX = 1.0f,
          iconScaleY = 1.0f,
          background = background,
        ),
        displayId = displayId,
        configuration = configuration,
        hostInputToken = hostInputToken,
        callback = callback,
      )
    }

    override fun renderWithOptions(
      renderOptions: RenderOptions,
      displayId: Int,
      configuration: Configuration,
      hostInputToken: InputTransferToken,
      callback: ISuperIconRenderCallback,
    ) {
      val params = Params(renderOptions, displayId, configuration, hostInputToken, callback)
      if (!params.isValidRenderOptions()) {
        callback.onError(SuperIconErrorCodes.INVALID_PARAMETER, INVALID_PARAMETER_ERROR_MESSAGE)
        logger.atSevere().log("$INVALID_PARAMETER_ERROR_MESSAGE: %s", renderOptions)
        return
      }
      val uiType = renderOptions.uiType
      if (params == currentRenderRequests[uiType]?.params) {
        logger.atFine().log("ignore render request with exact same options")
        return
      }
      currentRenderRequests[uiType]?.renderJob?.cancel()
      currentRenderRequests[uiType]?.contentJob?.cancel()
      currentRenderRequests[uiType] =
        RenderRequestParams(params, renderJob = null, contentJob = null).apply {
          renderJob =
            mainScope.safeLaunch(
              onError = { e ->
                when (e) {
                  is InflateException ->
                    callback.onError(
                      SuperIconErrorCodes.RENDER_FAILED,
                      e.message ?: "Inflation failed",
                    )
                  is IllegalArgumentException ->
                    callback.onError(
                      SuperIconErrorCodes.INVALID_PARAMETER,
                      e.message ?: INVALID_PARAMETER_ERROR_MESSAGE,
                    )
                  else ->
                    callback.onError(
                      SuperIconErrorCodes.UNKNOWN,
                      e.message ?: "Unknown render error",
                    )
                }
              }
            ) {
              val (renderContext, display) = getRenderContextAndDisplay(configuration, displayId)

              val view =
                if (uiType == SuperIconUiType.CONSENT_TOGGLE) {
                  createConsentToggleView(
                    renderContext,
                    display,
                    params.hostInputToken,
                    params,
                    callback,
                  )
                } else {
                  val chipView = createChip(renderContext, params, display)
                  chipView.focusable = View.NOT_FOCUSABLE
                  chipView
                }

              val measuredSize =
                measureSize(
                  view,
                  Size(renderOptions.width, renderOptions.height),
                  Size(renderOptions.minWidth, renderOptions.minHeight),
                  Size(renderOptions.maxWidth, renderOptions.maxHeight),
                )
              logger.atFine().log("#render, measuredSize: %s, view: %s", measuredSize, view)

              renderHost(
                view,
                params,
                measuredSize,
                renderContext,
                display,
                hostInputToken,
                renderOptions.windowToken,
                uiType,
                callback,
              )
            }
        }
    }

    @SuppressLint("InflateParams")
    @androidx.annotation.MainThread
    private suspend fun createConsentToggleView(
      renderContext: Context,
      display: Display,
      hostInputToken: InputTransferToken,
      params: Params,
      callback: ISuperIconRenderCallback,
    ): View {
      val themedContext = ContextThemeWrapper(renderContext, R.style.Theme_Material3_DayNight)
      val monetContext = DynamicColors.wrapContextIfAvailable(themedContext)
      val view =
        LayoutInflater.from(monetContext)
          .inflate(R.layout.super_icon_consent_toggle, /* root= */ null)

      logger
        .atFine()
        .log("uiType: %s renderOptions: %s", params.renderOptions.uiType, params.renderOptions)
      val renderOptions = params.renderOptions
      val labelTextView = view.findViewById<TextView>(R.id.consent_toggle_text)
      val iconView = view.findViewById<ImageView>(R.id.consent_toggle_icon)

      loadDrawableFromIcon(renderOptions.background, renderContext = renderContext) { background ->
        if (background != null) {
          applyCustomRippleColor(background, renderOptions)
          view.background = background
        }
      }

      // If a label is provided, configure the label text and make the label and icon container
      // visible.
      renderOptions.label
        ?.takeIf { it.isNotEmpty() }
        ?.let { label ->
          ViewCompat.setAccessibilityPaneTitle(view, renderOptions.accessibilityPaneTitle ?: label)
          labelTextView.text = label
          labelTextView.visibility = View.VISIBLE
          iconView.visibility = View.VISIBLE

          // Apply typography options to ensure visual parity with Gboard
          if (renderOptions.labelColor != RenderOptions.DEFAULT_LABEL_COLOR) {
            labelTextView.setTextColor(renderOptions.labelColor)
          }
          val targetFontFamily = renderOptions.fontFamily ?: RenderOptions.DEFAULT_FONT_FAMILY
          val currentTypeface = labelTextView.typeface
          // Use custom weight if specified, otherwise fallback to TextView's current weight or
          // default.
          val targetWeight =
            if (renderOptions.textWeight != RenderOptions.INVALID_TEXT_WEIGHT) {
              renderOptions.textWeight
            } else {
              currentTypeface?.weight ?: DEFAULT_TYPEFACE_WEIGHT
            }
          val currentItalic = currentTypeface?.isItalic ?: false
          labelTextView.typeface =
            Typeface.create(
              Typeface.create(targetFontFamily, Typeface.NORMAL),
              targetWeight,
              currentItalic,
            )
          if (renderOptions.textSizeInPixels > 0) {
            labelTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, renderOptions.textSizeInPixels)
          }
          labelTextView.textScaleX = renderOptions.textScaleX
          // Force Gboard parity: Material 3 defaults to 0.00714286, but Gboard uses 0.0.
          labelTextView.letterSpacing = DEFAULT_LETTER_SPACING
          // Apply line spacing from render options to match Gboard's layout spec.
          labelTextView.setLineSpacing(
            renderOptions.lineSpacingExtra,
            renderOptions.lineSpacingMultiplier,
          )
          // Load the icon drawable and apply force-dark immunity to ensure consistent rendering.
          loadDrawableFromIcon(renderOptions.icon, renderContext = renderContext) { drawable ->
            if (drawable != null) {
              val immuneDrawable = createForceDarkImmuneDrawable(drawable)
              iconView.setImageDrawable(immuneDrawable)
            } else {
              iconView.setImageDrawable(null)
            }
          }
        }

      val switchView = view.findViewById<MaterialSwitch>(R.id.consent_toggle_switch)

      val initialState =
        consentManager.hasGrantedConsent(params.renderOptions.consentVersion.toLong())
      switchView.isChecked = initialState

      view.addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
          private var observerJob: Job? = null

          override fun onViewAttachedToWindow(v: View) {
            observerJob = mainScope.launch {
              consentManager.consentStateFlow.collect { _ ->
                switchView.isChecked =
                  consentManager.hasGrantedConsent(params.renderOptions.consentVersion.toLong())
              }
            }
            mainScope.launch {
              v.performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
            }
          }

          override fun onViewDetachedFromWindow(v: View) {
            observerJob?.cancel()
            observerJob = null
            currentRenderRequests[SuperIconUiType.CONSENT_TOGGLE]?.contentJob?.cancel()
            currentRenderRequests[SuperIconUiType.CONSENT_TOGGLE]?.contentJob = null
          }
        }
      )

      view.setOnClickListener {
        switchView.toggle()
        val isChecked = switchView.isChecked

        if (isChecked) {
          currentRenderRequests[SuperIconUiType.CONSENT_TOGGLE]?.contentJob =
            backgroundScope.safeLaunch(errorLogMessage = "Failed granting consent") {
              consentManager.recordConsentState(
                ConsentState.GRANTED,
                renderOptions.consentVersion.toLong(),
              )
              callback.onConsentGranted(
                awaitCallback(context, callback, renderOptions.consentVersion.toLong())
              )
            }
        } else {
          currentRenderRequests[SuperIconUiType.CONSENT_TOGGLE]?.contentJob =
            backgroundScope.safeLaunch(errorLogMessage = "Failed revoking context") {
              consentManager.recordConsentState(
                ConsentState.REVOKED,
                renderOptions.consentVersion.toLong(),
              )
              callback.onConsentDenied()
            }
        }
      }

      // Configure accessibility so the root layout announces itself as a Switch and
      // reports the checked state of the hidden MaterialSwitch to TalkBack.
      ViewCompat.setAccessibilityDelegate(
        view,
        object : AccessibilityDelegateCompat() {
          override fun onInitializeAccessibilityNodeInfo(
            host: View,
            info: AccessibilityNodeInfoCompat,
          ) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            info.className = Switch::class.java.name
            info.isCheckable = true
            info.checked =
              if (switchView.isChecked) {
                AccessibilityNodeInfoCompat.CHECKED_STATE_TRUE
              } else {
                AccessibilityNodeInfoCompat.CHECKED_STATE_FALSE
              }
          }
        },
      )

      return view
    }

    @SuppressLint("InflateParams")
    private fun createConsentView(
      renderContext: Context,
      callback: ISuperIconRenderCallback,
      totalDisplayCount: Int,
      icon: Icon?,
      consentVersion: Long,
    ): Pair<View, View.OnAttachStateChangeListener> {
      val themedContext = ContextThemeWrapper(renderContext, R.style.Theme_Material3_DayNight)
      val monetContext = DynamicColors.wrapContextIfAvailable(themedContext)
      val consentView =
        LayoutInflater.from(monetContext).inflate(R.layout.super_icon_consent_dialog, null, false)

      val imageView: ImageView? = consentView.findViewById(R.id.super_icon_image)
      if (imageView == null) {
        logger
          .atSevere()
          .log("R.id.super_icon_image not found in R.layout.super_icon_consent_dialog")
      } else {
        loadDrawableFromIcon(icon, renderContext = monetContext) { drawable ->
          if (drawable != null) {
            val colorPrimary = MaterialColors.getColor(consentView, R.attr.colorPrimary)
            drawable.mutate().setTint(colorPrimary)
          }
          imageView.setImageDrawable(drawable)
        }
      }

      when {
        consentVersion == 0L -> {
          consentView
            .findViewById<TextView>(R.id.super_icon_consent_title)
            ?.setText(R.string.super_icon_consent_title)
          consentView
            .findViewById<TextView>(R.id.super_icon_consent_body_1)
            ?.setText(R.string.super_icon_consent_body_1)
          consentView
            .findViewById<TextView>(R.id.super_icon_consent_body_2)
            ?.setText(R.string.super_icon_consent_body_2)
          consentView.findViewById<TextView>(R.id.super_icon_consent_body_3)?.visibility = View.GONE
          consentView.findViewById<TextView>(R.id.super_icon_consent_body_4)?.visibility = View.GONE
          consentView.findViewById<TextView>(R.id.super_icon_consent_body_5)?.visibility = View.GONE
        }
        consentVersion >= 1L -> {
          if (consentVersion > 1L) {
            logger
              .atWarning()
              .log("Unknown consentVersion: %d. Falling back to V1 strings.", consentVersion)
          }
          // The layout XML uses V1 strings and visibility by default.
        }
      }

      // Tracks whether the user actively clicked the Yes, No, or Learn more buttons.
      // This allows us to distinguish between explicit button presses and implicit dismissals
      // (e.g., tapping outside the dialog) when the view is eventually detached.
      var userMadeExplicitChoice = false
      fun executeConsentAction(
        remoteErrorMsg: String,
        genericErrorMsg: String,
        ioErrorMsg: String = "Failed to record consent state",
        action: suspend () -> Unit,
      ): Job {
        return backgroundScope.safeLaunch(
          errorLogMessage = genericErrorMsg,
          onError = { e ->
            if (e is IOException) {
              callback.onError(SuperIconErrorCodes.UNKNOWN, e.message ?: ioErrorMsg)
            } else {
              callback.onError(SuperIconErrorCodes.UNKNOWN, e.message ?: genericErrorMsg)
            }
          },
        ) {
          action()
        }
      }

      val grantAction = View.OnClickListener {
        userMadeExplicitChoice = true
        currentRenderRequests[SuperIconUiType.CONSENT_DIALOG]?.contentJob =
          executeConsentAction(
            remoteErrorMsg = "Failed to report consent granted",
            genericErrorMsg = "Failed executing grant action",
          ) {
            consentManager.recordConsentState(ConsentState.GRANTED, consentVersion)
            callback.onConsentMetricsLogged(ConsentEventConstants.GRANTED, totalDisplayCount)
            callback.onConsentGranted(awaitCallback(context, callback, consentVersion))
          }
      }
      val denyAction = View.OnClickListener {
        userMadeExplicitChoice = true
        currentRenderRequests[SuperIconUiType.CONSENT_DIALOG]?.contentJob =
          executeConsentAction(
            remoteErrorMsg = "Failed to report consent denied",
            genericErrorMsg = "Failed executing deny action",
          ) {
            consentManager.recordConsentState(ConsentState.DENIED, consentVersion)
            callback.onConsentMetricsLogged(ConsentEventConstants.DENIED, totalDisplayCount)
            callback.onConsentDenied()
          }
      }
      consentView.findViewById<View>(R.id.btn_yes).setOnClickListener(grantAction)
      consentView.findViewById<View>(R.id.btn_no).setOnClickListener(denyAction)

      // Parse the <annotation url="..."> tag from the strings.xml resource and convert it
      // into a clickable link that opens the learn more URL via IPC and dismisses the dialog.
      val bodyFooterLearnMore = consentView.findViewById<TextView>(R.id.super_icon_consent_body_5)
      if (bodyFooterLearnMore != null) {
        val text = android.text.SpannableString(bodyFooterLearnMore.text)
        val annotations = text.getSpans(0, text.length, android.text.Annotation::class.java)
        for (annotation in annotations) {
          if (annotation.key == "url") {
            val start = text.getSpanStart(annotation)
            val end = text.getSpanEnd(annotation)
            val flags = text.getSpanFlags(annotation)
            val url =
              configReader.config.learnMoreUrl.takeIf { it.isNotEmpty() } ?: annotation.value
            text.setSpan(
              object : android.text.style.ClickableSpan() {
                override fun onClick(widget: View) {
                  userMadeExplicitChoice = true
                  callback.onLinkClicked(url)
                }
              },
              start,
              end,
              flags,
            )
          }
        }
        bodyFooterLearnMore.text = text
        bodyFooterLearnMore.movementMethod = android.text.method.LinkMovementMethod.getInstance()
      }

      val attachListener =
        object : View.OnAttachStateChangeListener {
          private var focusJob: Job? = null

          override fun onViewAttachedToWindow(v: View) {
            val titleView = v.findViewById<TextView>(R.id.super_icon_consent_title)
            // TalkBack struggles to establish initial focus and linear swiping over the
            // SurfaceControlViewHost process boundary without an explicit "kick".
            focusJob = mainScope.launch {
              @SuppressWarnings("AndroidLint") // Required for TalkBack in SurfaceControlViewHost
              titleView?.performAccessibilityAction(
                android.view.accessibility.AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS,
                null,
              )
            }
          }

          override fun onViewDetachedFromWindow(v: View) {
            focusJob?.cancel()
            focusJob = null
            // If the view is detached because Gboard is requesting the surface package (which
            // requires recreating the host to bridge Accessibility tokens), Gboard will temporarily
            // remove the listener to avoid triggering this block.
            if (!userMadeExplicitChoice) {
              currentRenderRequests[SuperIconUiType.CONSENT_DIALOG]?.contentJob =
                executeConsentAction(
                  remoteErrorMsg = "Failed to report implicit consent denied",
                  ioErrorMsg = "Failed to record implicit consent state",
                  genericErrorMsg = "Failed executing implicit deny action",
                ) {
                  // User did not explicitly grant or deny consent, so we treat it as a denial.
                  consentManager.recordConsentState(ConsentState.DENIED, consentVersion)
                  callback.onConsentMetricsLogged(ConsentEventConstants.DENIED, totalDisplayCount)
                  callback.onConsentDenied()
                }
            }
          }
        }
      consentView.addOnAttachStateChangeListener(attachListener)

      return Pair(consentView, attachListener)
    }

    @SuppressLint("InflateParams")
    private fun createChip(renderContext: Context, params: Params, display: Display): View {
      val chipLayoutId =
        when (params.renderOptions.uiType) {
          SuperIconUiType.SPELL_CHECKER_CHIP -> R.layout.spell_checker_chip
          SuperIconUiType.SUPER_ICON_IN_PANEL -> R.layout.super_icon_chip_in_panel
          else -> R.layout.super_icon_chip
        }
      val view =
        LayoutInflater.from(renderContext).inflate(chipLayoutId, null).apply {
          params.renderOptions.accessibilityPaneTitle?.let {
            // Set accessibility pane title so the accessibility service announces the correct
            // window title when focused.
            ViewCompat.setAccessibilityPaneTitle(this, it)
          }
          contentDescription = params.renderOptions.contentDescription
          ViewCompat.setAccessibilityDelegate(
            this,
            object : AccessibilityDelegateCompat() {
              override fun onInitializeAccessibilityNodeInfo(
                view: View,
                info: AccessibilityNodeInfoCompat,
              ) {
                super.onInitializeAccessibilityNodeInfo(view, info)
                // Only set explicit role and class name if requested. For some embedded views
                // (like Writing Tools), explicit roles can cause TalkBack to segment speech
                // and trigger IPC fragility in SurfaceControlViewHost, causing focus to disappear.
                if (params.renderOptions.roleDescription != null) {
                  info.roleDescription = params.renderOptions.roleDescription
                  info.className = Button::class.java.name
                }
              }

              var lastFocusTime = 0L

              override fun performAccessibilityAction(
                host: View,
                action: Int,
                args: Bundle?,
              ): Boolean {
                logger
                  .atFine()
                  .log(
                    "performAccessibilityAction action=%d (64=FOCUS, 128=CLEAR), isA11yFocused=%b",
                    action,
                    host.isAccessibilityFocused,
                  )

                if (action == AccessibilityNodeInfoCompat.ACTION_ACCESSIBILITY_FOCUS) {
                  lastFocusTime = System.currentTimeMillis()
                }

                if (action == AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                  val timeSinceFocus = System.currentTimeMillis() - lastFocusTime
                  // Workaround for SurfaceControlViewHost fragility: sometimes a spurious
                  // CLEAR_ACCESSIBILITY_FOCUS is dispatched immediately after FOCUS.
                  // Swallow it if it arrives within the debounce window.
                  if (timeSinceFocus < 150) { // Spurious clear window
                    logger.atInfo().log("Swallowing spurious CLEAR action")
                    return true // Swallow the spurious clear
                  }
                }

                return super.performAccessibilityAction(host, action, args)
              }
            },
          )
        }
      logger
        .atFine()
        .log("uiType: %s renderOptions: %s", params.renderOptions.uiType, params.renderOptions)

      with(params.renderOptions) {
        if (
          (uiType == SuperIconUiType.SPELL_CHECKER_CHIP ||
            uiType == SuperIconUiType.SUPER_ICON_IN_PANEL) && !label.isNullOrEmpty()
        ) {
          view.findViewById<TextView>(R.id.text).apply {
            text = label
            if (labelColor != RenderOptions.DEFAULT_LABEL_COLOR && labelColor != 0) {
              setTextColor(labelColor)
            }
            if (
              params.renderOptions.uiType == SuperIconUiType.SUPER_ICON_IN_PANEL && !isUnderTest
            ) {
              // Makes the TextView selected to make the marquee to start scrolling.
              isSelected = true
              // Sets the TextView IMPORTANT_FOR_ACCESSIBILITY_NO to avoid it's focused by
              // accessibility service.
              importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            }
            fontFamily
              ?.takeIf { it != RenderOptions.DEFAULT_FONT_FAMILY }
              ?.let { typeface = Typeface.create(it, Typeface.NORMAL) }
            if (textSizeInPixels > 0) {
              setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeInPixels)
            }
            textScaleX = this@with.textScaleX
          }
        }
      }

      // --- Set the Drawable on the Chip ---
      setChipIcon(view, renderContext, params.renderOptions)

      loadDrawableFromIcon(params.renderOptions.background, renderContext = renderContext) {
        background ->
        if (background != null) {
          applyCustomRippleColor(background, params.renderOptions)
          // For SUPER_ICON, we support custom background sizes. We apply the background
          // to a dedicated background view if present in the layout, allowing custom sizing
          // without affecting the overall chip dimensions or other UI types.
          if (params.renderOptions.uiType == SuperIconUiType.SUPER_ICON) {
            val backgroundView = view.findViewById<View>(R.id.background)
            if (backgroundView != null) {
              backgroundView.background = background
              if (
                params.renderOptions.backgroundWidth != DEFAULT_BACKGROUND_SIZE ||
                  params.renderOptions.backgroundHeight != DEFAULT_BACKGROUND_SIZE
              ) {
                backgroundView.layoutParams =
                  backgroundView.layoutParams.apply {
                    width = params.renderOptions.backgroundWidth
                    height = params.renderOptions.backgroundHeight
                  }
              }
            } else {
              view.background = background
            }
          } else {
            view.background = background
          }
        }
      }

      view.setOnClickListener {
        currentRenderRequests[params.renderOptions.uiType]?.contentJob =
          backgroundScope.safeLaunch(
            errorLogMessage = "Failed executing click action",
            onError = { e ->
              params.callback.onError(
                SuperIconErrorCodes.UNKNOWN,
                e.message ?: "Unknown click error",
              )
            },
          ) {
            // If the user hasn't made a choice yet (UNSET), show the consent form.
            if (consentManager.isConsentUnset()) {
              showConsentForm(renderContext, params, display)
              return@safeLaunch
            }

            val hasGranted =
              consentManager.hasGrantedConsent(params.renderOptions.consentVersion.toLong())
            val conversationData =
              awaitCallback(context, params.callback, params.renderOptions.consentVersion.toLong())
            if (hasGranted) {
              logger.atFine().log("onClick with consent granted")
              params.callback.onClick(conversationData)
            } else {
              if (conversationData.messages.isEmpty()) {
                logger.atFine().log("onClick with empty conversation data")
                params.callback.onClick(ConversationData(emptyList(), packageName = ""))
              } else if (
                consentManager.shouldShowConsentForm(params.renderOptions.consentVersion.toLong())
              ) {
                showConsentForm(renderContext, params, display)
              } else {
                logger.atFine().log("onClick with consent denied")
                params.callback.onClick(ConversationData(emptyList(), packageName = ""))
              }
            }
          }
      }
      return view
    }

    private fun setChipIcon(view: View, renderContext: Context, renderOptions: RenderOptions) {
      val imageView: ImageView = view.findViewById(R.id.icon) ?: return
      loadDrawableFromIcon(renderOptions.icon, renderContext = renderContext) { drawable ->
        if (drawable != null) {
          // Apply the bypass force dark theme
          val immuneDrawable = createForceDarkImmuneDrawable(drawable)
          imageView.setImageDrawable(immuneDrawable)
        } else {
          imageView.setImageDrawable(null)
        }

        imageView.scaleX = renderOptions.iconScaleX
        imageView.scaleY = renderOptions.iconScaleY
      }
      imageView.layoutParams =
        imageView.layoutParams.apply {
          width = renderOptions.iconWidth
          height = renderOptions.iconHeight
        }

      if (renderOptions.subIcon != null) {
        val subImageView: ImageView = view.findViewById(R.id.expand_icon) ?: return
        loadDrawableFromIcon(renderOptions.subIcon, renderContext = renderContext) { drawable ->
          if (drawable != null) {
            // Apply the bypass force dark theme
            val immuneDrawable = createForceDarkImmuneDrawable(drawable)
            subImageView.setImageDrawable(immuneDrawable)
          } else {
            subImageView.setImageDrawable(null)
          }

          subImageView.scaleX = renderOptions.subIconScaleX
          subImageView.scaleY = renderOptions.subIconScaleY
        }
        subImageView.layoutParams =
          subImageView.layoutParams.apply {
            width = renderOptions.subIconWidth
            height = renderOptions.subIconHeight
          }
      }
    }

    private suspend fun showConsentForm(renderContext: Context, params: Params, display: Display) {
      withContext(mainDispatcher) {
        consentManager.recordConsentFormShown()
        val totalDisplayCount = consentManager.getConsentFormShownTimes()
        currentRenderRequests[SuperIconUiType.CONSENT_DIALOG] =
          RenderRequestParams(params, null, null)
        val (consentView, attachListener) =
          createConsentView(
            renderContext,
            params.callback,
            totalDisplayCount,
            params.renderOptions.icon,
            params.renderOptions.consentVersion.toLong(),
          )

        consentView.focusable = View.NOT_FOCUSABLE
        val displayMetrics = renderContext.resources.displayMetrics
        val desiredWidth =
          TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              CONSENT_DIALOG_MAX_WIDTH_DP,
              displayMetrics,
            )
            .toInt()
        val horizontalMarginPx =
          TypedValue.applyDimension(
              TypedValue.COMPLEX_UNIT_DIP,
              CONSENT_DIALOG_HORIZONTAL_MARGIN_DP,
              displayMetrics,
            )
            .toInt()
        // Material Design 3 guidelines specify a maximum dialog width (typically 280dp-560dp,
        // using 360dp here) to prevent overly long, unreadable text lines on tablets and foldables.
        // However, if the user maximizes the Android "Display size" (accessibility scaling),
        // the screen's logical dp width shrinks. We use minOf() to dynamically scale the dialog
        // down to fit the physical screen with proper margins in those edge cases.
        val dialogWidth = minOf(desiredWidth, displayMetrics.widthPixels - horizontalMarginPx)

        // Pcs renders the consent dialog via SurfaceControlViewHost, which lacks an
        // AppCompat context. Therefore, standard auto-wrapping widgets like ButtonBarLayout
        // or ConstraintLayout crash the rendering pipeline. We must dynamically measure
        // the standard LinearLayout buttons here and manually stack them vertically if
        // they exceed the available dialog width (e.g., for large font/display scales).
        val btnYes = consentView.findViewById<Button>(R.id.btn_yes)
        val btnNo = consentView.findViewById<Button>(R.id.btn_no)
        val buttonBar = consentView.findViewById<LinearLayout>(R.id.button_container)
        if (btnYes != null && btnNo != null && buttonBar != null) {
          btnYes.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
          btnNo.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

          val cardPaddingPx =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                CONSENT_DIALOG_CARD_PADDING_DP,
                displayMetrics,
              )
              .toInt()
          val marginBetweenPx =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                CONSENT_DIALOG_BUTTON_MARGIN_DP,
                displayMetrics,
              )
              .toInt()

          val maxAvailableWidth = dialogWidth - cardPaddingPx
          val requiredWidth = btnYes.measuredWidth + btnNo.measuredWidth + marginBetweenPx

          if (requiredWidth > maxAvailableWidth) {
            buttonBar.orientation = LinearLayout.VERTICAL

            // Remove and re-add in reverse order to place 'OK' on top
            buttonBar.removeAllViews()
            buttonBar.addView(btnYes)
            buttonBar.addView(btnNo)

            val yesParams = btnYes.layoutParams as LinearLayout.LayoutParams
            yesParams.topMargin = 0
            yesParams.marginStart = 0
            yesParams.marginEnd = 0
            btnYes.layoutParams = yesParams

            val noParams = btnNo.layoutParams as LinearLayout.LayoutParams
            noParams.topMargin = marginBetweenPx
            noParams.marginStart = 0
            noParams.marginEnd = 0
            btnNo.layoutParams = noParams
          }
        }

        val measuredSize =
          measureSize(
            consentView,
            Size(dialogWidth, ViewGroup.LayoutParams.WRAP_CONTENT),
            Size(dialogWidth, 0),
            Size(dialogWidth, (displayMetrics.heightPixels * 0.85).toInt()),
          )
        logger.atFine().log("Displaying consent form with size: %s", measuredSize)
        params.callback.onConsentMetricsLogged(ConsentEventConstants.SHOWN, totalDisplayCount)

        renderHost(
          consentView,
          params,
          measuredSize,
          renderContext,
          display,
          params.hostInputToken,
          params.renderOptions.windowToken,
          SuperIconUiType.CONSENT_DIALOG,
          params.callback,
          attachListener,
        )
      }
    }

    private suspend fun renderHost(
      view: View,
      params: Params,
      measuredSize: Size,
      renderContext: Context,
      display: Display,
      hostInputToken: InputTransferToken,
      windowToken: IBinder?,
      @SuperIconUiType uiType: Int,
      callback: ISuperIconRenderCallback,
      attachListener: View.OnAttachStateChangeListener? = null,
    ) {
      logger.atFine().log("windowToken: %s", windowToken)
      val host = createSurfaceControlViewHost(display, windowToken, hostInputToken)
      val superIconUi =
        SuperIconUi(host, params, view, measuredSize, renderContext, display, attachListener)
      var success = false
      try {
        (view.parent as? ViewGroup)?.removeView(view)
        val viewToSet = FrameLayout(renderContext)
        viewToSet.addView(view)
        propagateAccessibilityPaneTitle(viewToSet, view, params.renderOptions)

        host.setView(viewToSet, measuredSize.width, measuredSize.height)
        logger.atFine().log("create a host %s", host)
        // Trigger the initial callback to update the consent toggle status box in the client (e.g.,
        // Gboard) when the popup menu is first shown.
        // We return empty ConversationData here to avoid the expensive awaitCallback IPC call
        // during initial rendering. Actual toggle value changes (handled in
        // createConsentToggleView) will fetch and return the valid conversation data.
        // Without this initial callback, the status box will show the default XML text until the
        // user interacts with the toggle.
        if (uiType == SuperIconUiType.CONSENT_TOGGLE) {
          backgroundScope.launch {
            val state =
              consentManager.hasGrantedConsent(params.renderOptions.consentVersion.toLong())
            if (state) {
              // This is for rendering the toggle, not for turning on the toggle. Return empty
              // ConversationData immediately to optimize rendering latency. Gboard's controller
              // will ignore this initial empty data to avoid overwriting its cached context data.
              callback.onConsentGranted(ConversationData(emptyList(), packageName = ""))
            } else {
              callback.onConsentDenied()
            }
          }
        }
        if (uiType == SuperIconUiType.CONSENT_DIALOG) {
          // Only store CONSENT_DIALOG in consentDialogUi to avoid it being evicted by the LRU
          // cache, as it is a critical modal UI. Other UIs go to activeSuperIconUis cache.
          this@SuperIconRenderServiceBinderStub.consentDialogUi?.releaseOnEviction()
          this@SuperIconRenderServiceBinderStub.consentDialogUi = superIconUi
        } else {
          this@SuperIconRenderServiceBinderStub.activeSuperIconUis.put(superIconUi, true)
          logger
            .atFine()
            .log("add activeSuperIconUis.count: %s %s", activeSuperIconUis.size(), superIconUi)
        }
        // We post the callback invocation to the end of the main thread handler queue, to
        // make sure the callback happens after the views are drawn. This is needed because
        // calling {@link SurfaceControlViewHost#setView()} will post a task to the main
        // thread to draw the view asynchronously.
        withContext(mainDispatcher) {
          callback.onRendered(
            SuperIconUiWrapper(WeakReference<SuperIconUi>(superIconUi)),
            host.surfacePackage,
            measuredSize.width,
            measuredSize.height,
            uiType,
          )
        }
        success = true
      } finally {
        if (!success) {
          superIconUi.releaseSurfaceControlViewHost(uiType = uiType)
          logger
            .atFine()
            .log("released host due to cancellation or failure: %s %s", host, superIconUi)
        }
      }
    }

    private suspend fun getRenderContextAndDisplay(
      configuration: Configuration,
      displayId: Int,
    ): Pair<Context, Display> =
      withContext(backgroundDispatcher) {
        val renderContext = context.createConfigurationContext(configuration)
        val display =
          with(DisplayManagerCompat.getInstance(renderContext)) {
            getDisplay(displayId) ?: displays[0]
          }
        Pair(renderContext, display)
      }

    private fun createSurfaceControlViewHost(
      display: Display,
      windowToken: IBinder?,
      hostInputToken: InputTransferToken,
    ): SurfaceControlViewHost =
      if (windowToken != null) {
        surfaceControlViewHostFactory.create(context, display, windowToken)
      } else {
        surfaceControlViewHostFactory.create(context, display, hostInputToken)
      }

    private fun propagateAccessibilityPaneTitle(
      viewToSet: View,
      view: View,
      renderOptions: RenderOptions,
    ) {
      val paneTitle =
        ViewCompat.getAccessibilityPaneTitle(view)
          ?: renderOptions.accessibilityPaneTitle
          ?: renderOptions.label
      paneTitle
        ?.takeIf { it.isNotEmpty() }
        ?.let { title ->
          ViewCompat.setAccessibilityPaneTitle(viewToSet, title)
          // Clear it from the inner view to prevent TalkBack from announcing nested panes
          ViewCompat.setAccessibilityPaneTitle(view, null)
        }
    }

    internal inner class SuperIconUiWrapper(internal val weakRef: WeakReference<SuperIconUi>) :
      ISuperIconUi.Stub() {
      override fun getSurfacePackage(callback: ISuperIconSurfacePackageResultCallback) {
        val superIconUi = weakRef.get()
        logger.atFine().log("getSurfacePackage, superIconUi: %s", superIconUi)
        superIconUi?.getSurfacePackage(callback)
      }

      override fun releaseSurfaceControlViewHost(uiType: Int) {
        val superIconUi = weakRef.get()
        logger.atFine().log("releaseSurfaceControlViewHost, superIconUi: %s", superIconUi)
        superIconUi?.releaseSurfaceControlViewHost(uiType)
      }
    }

    internal inner class SuperIconUi(
      internal var viewHost: SurfaceControlViewHost?,
      val params: Params,
      val view: View,
      val measuredSize: Size,
      val renderContext: Context,
      val display: Display,
      val attachListener: View.OnAttachStateChangeListener? = null,
    ) {
      fun releaseSurfaceControlViewHost(uiType: Int) {
        mainScope.launch {
          releaseResourcesInternal()
          if (uiType == SuperIconUiType.CONSENT_DIALOG) {
            if (this@SuperIconRenderServiceBinderStub.consentDialogUi === this@SuperIconUi) {
              this@SuperIconRenderServiceBinderStub.consentDialogUi = null
            }
          } else {
            activeSuperIconUis.remove(this@SuperIconUi)
            logger
              .atFine()
              .log("remove activeSuperIconUis.count: %s %s", activeSuperIconUis.size(), this)
          }
        }
      }

      fun releaseOnEviction() {
        mainScope.launch { releaseResourcesInternal() }
      }

      private fun releaseResourcesInternal() {
        logger.atFine().log("release host %s %s", viewHost, this)
        viewHost?.release()
        viewHost = null
        val uiType = params.renderOptions.uiType
        currentRenderRequests[uiType]?.contentJob?.cancel()
        currentRenderRequests[uiType]?.contentJob = null
      }

      fun getSurfacePackage(surfacePackageResultCallback: ISuperIconSurfacePackageResultCallback) {
        mainScope.launch {
          logger.atFine().log("recreate surfaceControlViewHost")
          try {
            if (attachListener != null) {
              view.removeOnAttachStateChangeListener(attachListener)
            }
            releaseResourcesInternal()
            // The previous SurfaceControlViewHost's root view (the old FrameLayout) was destroyed
            // during release(). We must remove our inner `view` from it to avoid an
            // IllegalStateException ("The specified child already has a parent") and wrap it
            // in a fresh FrameLayout for the new host.
            (view.parent as? ViewGroup)?.removeView(view)
            val viewToSet = FrameLayout(renderContext)
            // Ensure the container FrameLayout matches the measured size and centers the content
            // vertically to align with Gboard's UI expectations.
            val layoutParams = FrameLayout.LayoutParams(measuredSize.width, measuredSize.height)
            layoutParams.gravity = Gravity.CENTER_VERTICAL
            viewToSet.layoutParams = layoutParams
            viewToSet.addView(view)
            propagateAccessibilityPaneTitle(viewToSet, view, params.renderOptions)
            if (attachListener != null) {
              // Safe to re-attach the listener now that the recreation detachments are done.
              view.addOnAttachStateChangeListener(attachListener)
            }
            // Recreate the SurfaceControlViewHost using the window token if available.
            // This is crucial for Accessibility (e.g. TalkBack) to bridge the focus
            // between the client process (Gboard) and the host process (Pcs).
            val host =
              createSurfaceControlViewHost(
                display,
                params.renderOptions.windowToken,
                params.hostInputToken,
              )
            host.setView(viewToSet, measuredSize.width, measuredSize.height)
            val surfacePackage = host.surfacePackage
            if (surfacePackage == null) {
              logger.atSevere().log("Failed to get surface package during recreation (null)")
              handleRecreationFailure()
              return@launch
            }
            viewHost = host
            backgroundScope.launch {
              try {
                surfacePackageResultCallback.onResult(surfacePackage)
              } catch (e: RemoteException) {
                logger.atSevere().withCause(e).log("RemoteException calling onSurfacePackage")
              }
            }
          } catch (e: Exception) {
            logger.atSevere().withCause(e).log("Failed to recreate surface package")
            handleRecreationFailure()
          }
        }
      }

      private fun handleRecreationFailure() {
        if (params.renderOptions.uiType == SuperIconUiType.CONSENT_DIALOG) {
          backgroundScope.launch {
            try {
              // If we fail to recreate the surface package, the consent dialog cannot be
              // rendered on Gboard's side. Since the user cannot grant consent, we must
              // implicitly deny it so that Gboard can dismiss its popup container and
              // reset its UI state instead of hanging indefinitely.
              params.callback.onConsentDenied()
            } catch (e: RemoteException) {
              logger
                .atSevere()
                .withCause(e)
                .log("Failed to report consent denied after recreation failure")
            }
          }
        }
      }
    }

    fun cancelRender() {
      for (request in currentRenderRequests.values) {
        request.renderJob?.cancel()
        request.contentJob?.cancel()
      }
    }

    suspend fun awaitCallback(
      context: Context,
      clientCallback: ISuperIconRenderCallback,
      consentVersion: Long,
    ): ConversationData =
      callbackHelper.awaitCallback(context, backgroundScope, clientCallback, consentVersion)

    private fun CoroutineScope.safeLaunch(
      errorLogMessage: String = "Unhandled exception",
      onError: ((Throwable) -> Unit)? = null,
      block: suspend CoroutineScope.() -> Unit,
    ): Job {
      return launch {
        try {
          block()
        } catch (e: CancellationException) {
          throw e
        } catch (e: Exception) {
          logger.atSevere().withCause(e).log("%s", errorLogMessage)
          try {
            onError?.invoke(e)
          } catch (re: RemoteException) {
            logger.atSevere().withCause(re).log("Failed to notify caller via AIDL")
          }
        }
      }
    }
  }

  internal data class Params(
    val renderOptions: RenderOptions,
    val displayId: Int,
    val configuration: Configuration,
    val hostInputToken: InputTransferToken,
    val callback: ISuperIconRenderCallback,
  ) {
    fun isValidRenderOptions(): Boolean {
      if (
        renderOptions.width <= 0 ||
          renderOptions.height <= 0 ||
          renderOptions.minWidth <= 0 ||
          renderOptions.minHeight <= 0 ||
          renderOptions.maxWidth <= 0 ||
          renderOptions.maxHeight <= 0 ||
          renderOptions.minWidth > renderOptions.width ||
          renderOptions.minHeight > renderOptions.height ||
          renderOptions.width > renderOptions.maxWidth ||
          renderOptions.height > renderOptions.maxHeight
      ) {
        return false
      }
      if (
        renderOptions.textWeight != RenderOptions.INVALID_TEXT_WEIGHT &&
          (renderOptions.textWeight <= 0 || renderOptions.textWeight > 1000)
      ) {
        return false
      }
      if (
        renderOptions.uiType == SuperIconUiType.SUPER_ICON ||
          renderOptions.uiType == SuperIconUiType.SPELL_CHECKER_CHIP ||
          renderOptions.uiType == SuperIconUiType.SUPER_ICON_IN_PANEL
      ) {
        return renderOptions.icon != null &&
          renderOptions.background != null &&
          !(renderOptions.iconWidth <= 0 || renderOptions.iconHeight <= 0)
      }
      return true
    }
  }

  private data class RenderRequestParams(
    val params: Params,
    var renderJob: Job?,
    var contentJob: Job?,
  )

  private companion object {
    val logger: AndroidFluentLogger = AndroidFluentLogger.create("PcsSuperIcon")
    val isUnderTest: Boolean by lazy {
      try {
        Class.forName("androidx.test.platform.app.InstrumentationRegistry")
        true
      } catch (e: ClassNotFoundException) {
        false
      }
    }
    const val INVALID_PARAMETER_ERROR_MESSAGE: String = "invalid parameter"

    const val MAXIMUM_ACTIVE_UI_COUNT = 10
    const val DEFAULT_TYPEFACE_WEIGHT = 500
    const val DEFAULT_LETTER_SPACING = 0.0f

    const val CONSENT_DIALOG_MAX_WIDTH_DP = 360f
    const val CONSENT_DIALOG_HORIZONTAL_MARGIN_DP = 48f
    const val CONSENT_DIALOG_CARD_PADDING_DP = 48f
    const val CONSENT_DIALOG_BUTTON_MARGIN_DP = 8f

    private fun applyCustomRippleColor(background: Drawable?, renderOptions: RenderOptions) {
      if (
        background is RippleDrawable &&
          renderOptions.backgroundRippleColor != RenderOptions.DEFAULT_BACKGROUND_RIPPLE_COLOR
      ) {
        background.mutate()
        background.setColor(ColorStateList.valueOf(renderOptions.backgroundRippleColor))
      }
    }

    fun loadDrawableFromIcon(
      icon: Icon?,
      renderContext: Context,
      action: (drawable: Drawable?) -> Unit,
    ) {
      val nonNullIcon: Icon = icon ?: return action(null)
      val iconDrawable: Drawable? =
        try {
          nonNullIcon.loadDrawable(renderContext)
        } catch (e: Exception) {
          logger.atSevere().withCause(e).log("Failed to load Drawable from Icon")
          null
        }
      if (iconDrawable != null) {
        logger.atFine().log("icon is replaced with Drawable loaded from Icon.")
        action(iconDrawable)
      } else {
        logger.atSevere().log("IconDrawable is null. Clearing the icon.")
        action(null)
      }
    }

    fun measureSize(view: View, viewSize: Size, minSize: Size, maxSize: Size): Size {
      if (
        viewSize.width != ViewGroup.LayoutParams.WRAP_CONTENT &&
          viewSize.height != ViewGroup.LayoutParams.WRAP_CONTENT
      ) {
        return Size(viewSize.width, viewSize.height)
      }
      val heightMeasureSpec =
        if (viewSize.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
          View.MeasureSpec.makeMeasureSpec(maxSize.height, View.MeasureSpec.AT_MOST)
        } else {
          View.MeasureSpec.makeMeasureSpec(viewSize.height, View.MeasureSpec.EXACTLY)
        }
      val widthMeasureSpec =
        if (viewSize.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
          View.MeasureSpec.makeMeasureSpec(maxSize.width, View.MeasureSpec.AT_MOST)
        } else {
          View.MeasureSpec.makeMeasureSpec(viewSize.width, View.MeasureSpec.EXACTLY)
        }
      view.measure(widthMeasureSpec, heightMeasureSpec)
      return Size(
        view.measuredWidth.coerceAtLeast(minSize.width),
        view.measuredHeight.coerceAtLeast(minSize.height),
      )
    }
  }
}
