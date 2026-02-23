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

package com.google.android.`as`.oss.delegatedui.service.templates.bugle

import android.app.RemoteAction
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.dataattribution.proto.attributionChipData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_LONG_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.egress.bugle.bugleEgressData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateData
import com.google.android.`as`.oss.delegatedui.api.integration.templates.bugle.BugleAction
import com.google.android.`as`.oss.delegatedui.config.DelegatedUiConfig
import com.google.android.`as`.oss.delegatedui.service.common.DelegatedUiInputSpec
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import com.google.android.`as`.oss.delegatedui.service.templates.bugle.BugleTemplateRenderer.Companion.ANIMATED_MERGED_CHIPS_ROW_DISABLE_SIZE_ANIMATION
import com.google.android.`as`.oss.delegatedui.service.templates.bugle.BugleTemplateRenderer.Companion.ANIMATED_MERGED_CHIPS_ROW_ENABLE_SIZE_ANIMATION
import com.google.android.`as`.oss.delegatedui.service.templates.bugle.BugleTemplateRenderer.Companion.BUGLE_ACTIONS_CHIPS_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.bugle.BugleTemplateRenderer.Companion.BUGLE_SUGGESTIONS_CHIPS_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.bugle.BugleTemplateRenderer.Companion.STANDALONE_CHIPS_ROW_TEST_TAG
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont
import com.google.android.`as`.oss.delegatedui.service.templates.scope.TemplateRendererScope
import com.google.android.`as`.oss.delegatedui.utils.IconOrImage
import com.google.android.`as`.oss.delegatedui.utils.ResponseWithParcelables
import com.google.android.`as`.oss.delegatedui.utils.SerializableBitmap.serializeToByteString
import com.google.android.`as`.oss.delegatedui.utils.TintableIcon
import com.google.android.`as`.oss.delegatedui.utils.asTintableIcon
import com.google.common.flogger.GoogleLogger
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

val LocalConfigReader =
  staticCompositionLocalOf<ConfigReader<DelegatedUiConfig>> { error("No ConfigReader provided") }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
class BugleTemplateRenderer
@Inject
internal constructor(val configReader: ConfigReader<DelegatedUiConfig>) : TemplateRenderer {

  override fun TemplateRendererScope.onCreateTemplateView(
    context: Context,
    inputSpecFlow: StateFlow<DelegatedUiInputSpec>,
    response: ResponseWithParcelables<DelegatedUiTemplateData>,
  ): View? {
    val data = response.data.bugleTemplateData

    val suggestionChipRowData =
      if (data.bugleSuggestionsList.isNotEmpty()) {
        SuggestionChipRowData(
          logoIcon = response.image.valueOrNull,
          bugleSuggestionModelList =
            getBugleSuggestionModelList(data, response.pendingIntentList.valueOrNull),
        )
      } else {
        null
      }

    // get the action chip row data
    val remoteActionsList = response.remoteActionList.valueOrNull
    val actionChipRowData =
      if (data.bugleActionsList.isNotEmpty()) {
        if (data.bugleActionsList.size == remoteActionsList?.size) {
          ActionChipRowData(data.bugleActionsList, remoteActionsList)
        } else {
          logger
            .atWarning()
            .log("Bugle actions list size is not equal to remote actions list size.")
          null
        }
      } else {
        null
      }

    // return null if suggestion chip row data and action chip row data are both null.
    if (suggestionChipRowData == null && actionChipRowData == null) {
      // Skip bugle chips
      logger.atInfo().log("No data or remoteActionList doesn't match action chips. Skipping.")
      return null
    }

    return ComposeView(context).apply {
      setContent {
        val inputSpec by inputSpecFlow.collectAsState()
        val shouldBlur = inputSpec.noTouchHint

        CompositionLocalProvider(LocalConfigReader provides configReader) {
          MainTheme {
            MaterialTheme(
              colorScheme =
                MaterialTheme.colorScheme.copy(
                  outlineVariant =
                    if (data.hasStrokeColor() && data.strokeColor != 0) {
                      Color(data.strokeColor)
                    } else {
                      MaterialTheme.colorScheme.outlineVariant
                    },
                  onSurface =
                    if (data.hasFontColor() && data.fontColor != 0) {
                      Color(data.fontColor)
                    } else {
                      MaterialTheme.colorScheme.onSurface
                    },
                  primary =
                    if (data.hasIconColor() && data.iconColor != 0) {
                      Color(data.iconColor)
                    } else {
                      MaterialTheme.colorScheme.primary
                    },
                )
            ) {
              val nestedScrollInterop = rememberNestedScrollInteropConnection()
              LaunchedEffect(Unit) {
                doOnInterop(
                  uiTokenId = data.bugleViewUiIdToken,
                  interactionType =
                    DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_UNSPECIFIED,
                ) {
                  sendHints(data.hintsList.toSet())
                }
              }

              val blurRadius by
                animateDpAsState(
                  targetValue = if (shouldBlur) 5.dp else 0.dp,
                  animationSpec =
                    if (shouldBlur) {
                      MaterialTheme.motionScheme.slowEffectsSpec()
                    } else {
                      MaterialTheme.motionScheme.defaultEffectsSpec()
                    },
                  label = "BlurAnimation",
                )

              Column(
                Modifier.fillMaxWidth()
                  .then(
                    if (blurRadius > 0.dp) {
                      Modifier.blur(
                        radius = blurRadius,
                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                      )
                    } else {
                      Modifier
                    }
                  )
              ) {
                Box(modifier = Modifier.nestedScroll(nestedScrollInterop)) {
                  BugleChipsRow(
                    isStandaloneRowEnabled = data.isStandaloneRowEnabled,
                    enableSizeAnimation =
                      data.enableSizeAnimation || !data.hasEnableSizeAnimation(),
                    suggestionChipRowData = suggestionChipRowData,
                    actionChipRowData = actionChipRowData,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  internal companion object {
    val logger = GoogleLogger.forEnclosingClass()
    const val BUGLE_SUGGESTIONS_CHIPS_TEST_TAG = "BUGLE_SUGGESTIONS_CHIPS_TEST_TAG"
    const val BUGLE_ACTIONS_CHIPS_TEST_TAG = "BUGLE_ACTIONS_CHIPS_TEST_TAG"
    const val STANDALONE_CHIPS_ROW_TEST_TAG = "STANDALONE_CHIPS_ROW_TEST_TAG"
    const val ANIMATED_MERGED_CHIPS_ROW_ENABLE_SIZE_ANIMATION =
      "ANIMATED_MERGED_CHIPS_ROW_ENABLE_SIZE_ANIMATION"
    const val ANIMATED_MERGED_CHIPS_ROW_DISABLE_SIZE_ANIMATION =
      "ANIMATED_MERGED_CHIPS_ROW_DISABLE_SIZE_ANIMATION"
  }
}

internal data class SuggestionChipRowData(
  val logoIcon: Bitmap?,
  val bugleSuggestionModelList: List<BugleSuggestionModel>,
)

data class ActionChipRowData(
  val bugleActionsList: List<BugleAction>,
  val remoteActionsList: List<RemoteAction>,
)

private fun boostChroma(color: Color): Color {
  val hctColor = FloatArray(3)
  ColorUtils.colorToM3HCT(color.toArgb(), hctColor)
  val chroma = hctColor[1]
  return if (chroma < 5) {
    color
  } else {
    Color(ColorUtils.M3HCTToColor(hctColor[0], 70f, hctColor[2]))
  }
}

@Composable
internal fun TemplateRendererScope.BugleChipsRow(
  isStandaloneRowEnabled: Boolean,
  enableSizeAnimation: Boolean,
  suggestionChipRowData: SuggestionChipRowData?,
  actionChipRowData: ActionChipRowData?,
) {
  if (isStandaloneRowEnabled) {
    StandaloneChipsRow(suggestionChipRowData, actionChipRowData)
  } else {
    AnimatedMergedChipsRow(enableSizeAnimation, suggestionChipRowData, actionChipRowData)
  }
}

@Composable
private fun TemplateRendererScope.StandaloneChipsRow(
  suggestionChipRowData: SuggestionChipRowData?,
  actionChipRowData: ActionChipRowData?,
) {
  LazyRow(
    modifier =
      Modifier.fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
        .testTag(STANDALONE_CHIPS_ROW_TEST_TAG),
    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
    verticalAlignment = Alignment.Bottom,
  ) {
    if (suggestionChipRowData != null) {
      itemsIndexed(suggestionChipRowData.bugleSuggestionModelList) { index, suggestion ->
        BugleSuggestionChip(suggestionChipRowData.logoIcon, suggestion)
      }
    }
    if (actionChipRowData != null) {
      itemsIndexed(actionChipRowData.bugleActionsList) { index, action ->
        BugleActionChip(action, remoteAction = actionChipRowData.remoteActionsList[index])
      }
    }
  }
}

@Composable
private fun TemplateRendererScope.AnimatedMergedChipsRow(
  enableSizeAnimation: Boolean,
  suggestionChipRowData: SuggestionChipRowData?,
  actionChipRowData: ActionChipRowData?,
) {
  if (enableSizeAnimation) {
    var showContent by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    val localDensity = LocalDensity.current

    AnimatedVisibility(
      visible = showContent,
      enter =
        expandIn(
          expandFrom = Alignment.BottomEnd,
          initialSize = { with(localDensity) { IntSize(0, 48.dp.roundToPx()) } },
          animationSpec = spring(dampingRatio = 0.6f, stiffness = 800f),
        ),
    ) {
      MergedChipsRow(
        Modifier.testTag(ANIMATED_MERGED_CHIPS_ROW_ENABLE_SIZE_ANIMATION),
        suggestionChipRowData,
        actionChipRowData,
      )
    }
  } else {
    MergedChipsRow(
      Modifier.testTag(ANIMATED_MERGED_CHIPS_ROW_DISABLE_SIZE_ANIMATION),
      suggestionChipRowData,
      actionChipRowData,
    )
  }
}

@Composable
private fun TemplateRendererScope.MergedChipsRow(
  modifier: Modifier,
  suggestionChipRowData: SuggestionChipRowData?,
  actionChipRowData: ActionChipRowData?,
) {
  Row(
    modifier = modifier.wrapContentWidth().heightIn(48.dp).padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.End),
    verticalAlignment = Alignment.Bottom,
  ) {
    val suggestionEnterEasing = CubicBezierEasing(0f, 0f, 0f, 1f)
    if (suggestionChipRowData != null) {
      BugleAnimatedListItemVisibility(
        values = suggestionChipRowData.bugleSuggestionModelList,
        itemEnter = { _ ->
          scaleIn(
            animationSpec =
              tween(
                durationMillis = Constants.ANIMATION_REVEAL_DURATION_MILLIS,
                delayMillis = Constants.ANIMATION_REVEAL_DELAY_MILLIS,
                easing = suggestionEnterEasing,
              )
          )
        },
      ) { suggestion ->
        BugleSuggestionChip(suggestionChipRowData.logoIcon, suggestion)
      }
    }
    if (actionChipRowData != null) {
      BugleAnimatedListItemVisibility(
        values = actionChipRowData.bugleActionsList.zip(actionChipRowData.remoteActionsList),
        itemEnter = { _ ->
          scaleIn(
            animationSpec =
              tween(
                durationMillis = Constants.ANIMATION_REVEAL_DURATION_MILLIS,
                delayMillis = Constants.ANIMATION_REVEAL_DELAY_MILLIS,
                easing = suggestionEnterEasing,
              )
          )
        },
      ) { (magicAction, remoteAction) ->
        BugleActionChip(bugleAction = magicAction, remoteAction = remoteAction)
      }
    }
  }
}

private fun onlyIf(condition: Boolean, action: () -> Unit): (() -> Unit)? =
  if (condition) action else null

@Composable
internal fun TemplateRendererScope.BugleSuggestionChip(
  logoIcon: Bitmap?,
  suggestionModel: BugleSuggestionModel,
) {
  val suggestion = suggestionModel.suggestion
  val scope = rememberCoroutineScope()
  BugleOutlinedButton(
    modifier = Modifier.testTag(BUGLE_SUGGESTIONS_CHIPS_TEST_TAG),
    chipOnClick = {
      scope.launch {
        doOnInterop(suggestion.uiIdToken, interactionType = INTERACTION_TYPE_CLICK) {
          sendEgressData {
            this.bugleEgressData = bugleEgressData { this.suggestionText = suggestion.text }
          }
        }
      }
    },
    chipOnLongClick =
      onlyIf(suggestion.hasAttributionDialogData()) {
        scope.launch {
          doOnInterop(suggestion.uiIdToken, interactionType = INTERACTION_TYPE_LONG_CLICK) {
            showDataAttribution(
              attributionDialogData = suggestion.attributionDialogData,
              attributionChipData =
                attributionChipData {
                  logoIcon?.serializeToByteString()?.let { this.chipIcon = it }
                  this.chipLabel = suggestion.text
                },
              sourceDeepLinks = suggestionModel.pendingIntentList.toTypedArray(),
            )
          }
        }
      },
  ) {
    LaunchedEffect(Unit) { doOnImpression(suggestion.uiIdToken) { logUsage() } }

    BugleRowContent(
      icon = logoIcon?.asTintableIcon(tintable = true),
      text = suggestion.text,
      attribution = suggestion.attribution,
      description = suggestion.contentDescription,
    )
  }
}

@Composable
fun TemplateRendererScope.BugleActionChip(bugleAction: BugleAction, remoteAction: RemoteAction) {
  val iconBitmap = remoteAction.iconBitmap

  val scope = rememberCoroutineScope()
  BugleOutlinedButton(
    modifier = Modifier.testTag(BUGLE_ACTIONS_CHIPS_TEST_TAG),
    chipOnClick = {
      scope.launch {
        doOnInterop(bugleAction.actionData.uiIdToken, interactionType = INTERACTION_TYPE_CLICK) {
          // bugleAction.containsQuery definition from [ParsedAction]: True if the action uses a
          // query instead of an intent.
          if (bugleAction.containsQuery) {
            sendEgressData {
              bugleEgressData = bugleEgressData { photoSearchQuery = bugleAction.actionData.query }
            }
          } else {
            executeAction { remoteAction.toAction() }
          }
        }
      }
    },
    chipOnLongClick =
      onlyIf(bugleAction.actionData.hasAttributionDialogData()) {
        scope.launch {
          doOnInterop(
            bugleAction.actionData.uiIdToken,
            interactionType = INTERACTION_TYPE_LONG_CLICK,
          ) {
            showDataAttribution(
              attributionDialogData = bugleAction.actionData.attributionDialogData,
              attributionChipData = bugleAction.actionData.attributionChipData,
              sourceDeepLinks = null,
            )
          }
        }
      },
  ) {
    LaunchedEffect(Unit) { doOnImpression(bugleAction.actionData.uiIdToken) { logUsage() } }
    BugleActionChipContents(bugleAction, iconBitmap = iconBitmap)
  }
}

@Composable
fun BugleActionChipContents(data: BugleAction, iconBitmap: Bitmap?) {
  BugleRowContent(
    icon = iconBitmap?.asTintableIcon(tintable = false),
    text = data.actionData.title,
    description = data.actionData.description,
  )
}

private val RemoteAction.iconBitmap: Bitmap?
  @Composable
  get() {
    return if (shouldShowIcon()) {
      val context = LocalContext.current
      remember(icon) {
        try {
          icon.loadDrawable(context)?.toBitmap()
        } catch (e: Exception) {
          e.printStackTrace()
          null
        }
      }
    } else {
      null
    }
  }

@Composable
fun MainTheme(content: @Composable () -> Unit) {
  val context = LocalContext.current
  val colorScheme =
    if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

  MaterialTheme(colorScheme = colorScheme, typography = Typography()) { content() }
}

@Composable
private fun BugleOutlinedButton(
  modifier: Modifier,
  chipOnClick: () -> Unit,
  chipOnLongClick: (() -> Unit)? = null,
  chipContents: @Composable () -> Unit,
) {
  val enableBugleOutlineAnimationV2 = LocalConfigReader.current.config.enableBugleOutlineAnimationV2
  val shape = RoundedCornerShape(Constants.CornerRadius)
  val interactionSource = remember { MutableInteractionSource() }
  Box(
    modifier =
      modifier
        .clip(shape)
        .widthIn(min = 30.dp, max = 320.dp)
        .heightIn(min = 40.dp)
        .background(color = Color.Transparent, shape = shape)
        .combinedClickable(
          onClick = chipOnClick,
          onLongClick = chipOnLongClick,
          interactionSource = interactionSource,
          indication = ripple(color = MaterialTheme.colorScheme.onSurface),
        )
        .animatedActionBorder(Constants.BorderStrokeWidth, true)
        .semantics { role = Role.Button },
    contentAlignment = Alignment.Center,
  ) {
    if (enableBugleOutlineAnimationV2) {
      Box(
        modifier =
          Modifier.matchParentSize()
            .blur(2.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            .animatedActionBorder(strokeWidth = Constants.InnerBorderStrokeWidth, false)
      )
    }
    chipContents()
  }
}

@Composable
private fun BugleRowContent(
  icon: TintableIcon?,
  text: String,
  attribution: String? = null,
  description: String? = null,
) {
  val modifier =
    description?.let { Modifier.clearAndSetSemantics { contentDescription = it } } ?: Modifier
  val contentPadding =
    PaddingValues(
      start = Constants.ButtonHorizontalPadding,
      top = Constants.ButtonVerticalPadding,
      end = Constants.ButtonHorizontalPadding,
      bottom = Constants.ButtonVerticalPadding,
    )
  Row(
    modifier = modifier.padding(contentPadding),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Icon
    icon?.let {
      IconOrImage(
        icon = icon,
        modifier = Modifier.size(18.dp).align(Alignment.CenterVertically),
        tint = MaterialTheme.colorScheme.primary,
      )
    }

    // Text
    if (attribution == null) {
      SuggestionText(text, maxLines = 2, modifier = Modifier.align(Alignment.CenterVertically))
    } else {
      Column {
        SuggestionText(text, maxLines = 1)
        Text(
          text = attribution,
          style = MaterialTheme.typography.bodyMedium.withFlexFont(weight = 550, round = 0f),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun SuggestionText(text: String, maxLines: Int, modifier: Modifier = Modifier) {
  Text(
    text = text,
    modifier = modifier,
    style = MaterialTheme.typography.labelLarge.withFlexFont(weight = 500, round = 0f),
    color = MaterialTheme.colorScheme.onSurface,
    overflow = TextOverflow.Ellipsis,
    maxLines = maxLines,
  )
}

@Composable
fun Modifier.animatedActionBorder(strokeWidth: Dp, withSolidColor: Boolean): Modifier {
  val rotationAngle = remember { Animatable(Constants.INITIAL_ROTATION_DEGREES) }
  val fadeProgress = remember { Animatable(0f) } // 0f = full gradient, 1f = full solid

  val strokeWidthPx = with(LocalDensity.current) { strokeWidth.toPx() }
  val halfStroke = strokeWidthPx / 2f
  val topLeft = Offset(halfStroke, halfStroke)
  val solidColor = MaterialTheme.colorScheme.outlineVariant
  val strokeAnimStartColor: Color = boostChroma(MaterialTheme.colorScheme.tertiaryContainer)
  val strokeAnimMiddleColor: Color = boostChroma(MaterialTheme.colorScheme.primaryFixedDim)
  val strokeAnimEndColor: Color = boostChroma(MaterialTheme.colorScheme.primary)

  // Trigger animations when the composable enters the composition
  LaunchedEffect(Unit) {
    launch {
      rotationAngle.animateTo(
        targetValue = Constants.INITIAL_ROTATION_DEGREES + 360f,
        animationSpec =
          tween(durationMillis = Constants.ROTATION_DURATION_MILLIS, easing = LinearEasing),
      )
    }

    launch {
      fadeProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = Constants.FADE_DURATION_MILLIS,
            delayMillis = Constants.FADE_DELAY_MILLIS,
            easing = LinearEasing,
          ),
      )
    }
  }

  return drawBehind {
    val currentRotationRad = Math.toRadians(rotationAngle.value.toDouble()).toFloat()
    val solidOutlineFadeIn = fadeProgress.value

    val gradientOutlineFadeOut = (1f - solidOutlineFadeIn)
    val gradientRadius = sqrt(size.width * size.width + size.height * size.height) / 2f

    val center = size.center
    val strokeStyle = Stroke(width = strokeWidthPx)

    // Gradient
    val cosTheta = cos(currentRotationRad)
    val sinTheta = sin(currentRotationRad)

    val startOffset =
      Offset(x = center.x - gradientRadius * cosTheta, y = center.y - gradientRadius * sinTheta)
    val endOffset =
      Offset(x = center.x + gradientRadius * cosTheta, y = center.y + gradientRadius * sinTheta)

    val gradientBrush =
      Brush.linearGradient(
        Constants.GRADIENT_START_FRACTION to strokeAnimStartColor,
        Constants.GRADIENT_MIDDLE_FRACTION to strokeAnimMiddleColor,
        Constants.GRADIENT_END_FRACTION to strokeAnimEndColor,
        start = startOffset,
        end = endOffset,
        tileMode = TileMode.Clamp,
      )

    val innerGradientBrush =
      Brush.linearGradient(
        Constants.GRADIENT_START_FRACTION to strokeAnimStartColor.copy(alpha = 0.2f),
        Constants.GRADIENT_MIDDLE_FRACTION to strokeAnimMiddleColor.copy(alpha = 0.2f),
        Constants.GRADIENT_END_FRACTION to strokeAnimEndColor.copy(alpha = 0.2f),
        start = startOffset,
        end = endOffset,
        tileMode = TileMode.Clamp,
      )

    drawRoundRect(
      brush =
        if (withSolidColor) {
          gradientBrush
        } else {
          innerGradientBrush
        },
      topLeft = topLeft,
      size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
      cornerRadius = CornerRadius(Constants.CornerRadius.toPx()),
      alpha = gradientOutlineFadeOut,
      style = strokeStyle,
    )

    if (withSolidColor) {
      drawRoundRect(
        color = solidColor,
        topLeft = topLeft,
        size = Size(size.width - strokeWidthPx, size.height - strokeWidthPx),
        cornerRadius = CornerRadius(Constants.CornerRadius.toPx()),
        alpha = solidOutlineFadeIn,
        style = strokeStyle,
      )
    }
  }
}
