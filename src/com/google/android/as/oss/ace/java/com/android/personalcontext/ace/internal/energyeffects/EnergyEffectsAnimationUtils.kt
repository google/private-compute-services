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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.internal.energyeffects

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.libraries.material.compose.effect.ExperimentalMaterial3EffectApi
import com.google.android.libraries.material.compose.energy.getEnergyColors
import com.google.android.libraries.material.gm3.color.tokens.R
import com.google.android.shaderlib.energyeffects.BorderLightConfig
import com.google.android.shaderlib.energyeffects.DotsConfig
import com.google.android.shaderlib.energyeffects.EffectState
import com.google.android.shaderlib.energyeffects.EnergyShaderConfig
import com.google.android.shaderlib.energyeffects.LayoutConfig
import com.google.android.shaderlib.energyeffects.animation.KeyframeSequence
import com.google.android.shaderlib.energyeffects.animation.buildTransitions
import com.google.android.shaderlib.energyeffects.builder.CardAnimationDirection
import com.google.android.shaderlib.energyeffects.builder.DefaultCardConfig
import com.google.android.shaderlib.energyeffects.builder.DefaultChipConfig
import com.google.android.shaderlib.energyeffects.builder.copy
import com.google.android.shaderlib.energyeffects.colors.EnergyColors
import com.google.android.shaderlib.energyeffects.compose.energyEffects
import com.google.android.shaderlib.energyeffects.shader.ShaderUniforms
import com.google.android.shaderlib.energyeffects.utils.CornerRadii
import com.google.android.shaderlib.energyeffects.utils.Margins
import com.google.android.shaderlib.energyeffects.view.EnergyShaderDrawable
import com.google.ux.material.libmonet.energy.EnergyColors as MonetEnergyColors
import com.google.ux.material.libmonet.energy.EnergyColors.BaseColorRole

/** Helper utilities for rendering and controlling the energy shader effects animations. */
object EnergyEffectsAnimationUtils {
  private const val DEFAULT_CARD_CORNER_RADIUS_DP = 20f

  /**
   * Applies the energy effects animation to the composable [Modifier].
   *
   * @param geminiAnimationSpec The specific spec of the animation to render.
   * @param timeSupplierMs A provider for system time, useful to freeze animations in tests.
   */
  @Composable
  fun Modifier.applyEnergyEffectsAnimation(geminiAnimationSpec: GeminiAnimationSpec): Modifier {
    var currentAppState by remember { mutableStateOf(EffectState.ENTRY) }

    return this.energyEffects(
      initialConfig = geminiAnimationSpec.config,
      state = currentAppState,
      stateMap = geminiAnimationSpec.stateMap,
      onStateAnimationFinished = { state, _ ->
        if (state == EffectState.ENTRY) {
          currentAppState = EffectState.LOOP
        }
      },
      mainExecutor = ContextCompat.getMainExecutor(LocalContext.current),
      timeSupplierMs = geminiAnimationSpec.timeSupplierMs,
      timeOffsetMs = geminiAnimationSpec.timeOffsetMs,
    )
  }

  /**
   * Creates a themed [GeminiAnimationSpec] for sage cards.
   *
   * @param colorScheme The active [ColorScheme].
   * @param backgroundColor The base background color for the sage card layout.
   */
  @Composable
  fun createSageCardSpec(
    colorScheme: ColorScheme,
    backgroundColor: Color,
    timeSupplierMs: () -> Long = { System.currentTimeMillis() },
    timeOffsetMs: Long? = null,
  ): GeminiAnimationSpec {
    return remember(colorScheme, backgroundColor, timeSupplierMs, timeOffsetMs) {
      val baseColor = backgroundColor.toArgb()
      val primaryColor = colorScheme.primary.toArgb()
      val secondaryColor = colorScheme.secondary.toArgb()
      val tertiaryColor = colorScheme.tertiary.toArgb()
      val surfaceColor = colorScheme.surface.toArgb()

      val colors =
        MonetEnergyColors.withAccents(
          baseColor,
          primaryColor,
          secondaryColor,
          tertiaryColor,
          surfaceColor,
          false,
          BaseColorRole.SURFACE,
        )

      val energyColors =
        intArrayOf(colors.getOrElse(0) { baseColor }, colors.getOrElse(1) { baseColor })

      val cardConfig =
        DefaultCardConfig(
          surfaceColor = baseColor,
          energyColors = energyColors,
          animationDirection = CardAnimationDirection.SLIDE_UP,
        )

      GeminiAnimationSpec(
        config = cardConfig.initialConfig(),
        stateMap = cardConfig.createEffectsBuilder().buildKeyframeSequences(),
        timeSupplierMs = timeSupplierMs,
        timeOffsetMs = timeOffsetMs,
      )
    }
  }

  /**
   * Creates and starts a themed [Drawable] that applies the effects animation.
   *
   * @param context The [Context] to resolve theme colors and resources.
   * @param geminiAnimationSpec The spec of the animation.
   */
  fun getAndStartEffectsDrawable(
    context: Context,
    geminiAnimationSpec: GeminiAnimationSpec,
  ): Drawable? {
    val drawable = getEffectsDrawable(context, geminiAnimationSpec) ?: return null
    startAnimation(drawable)
    return drawable
  }

  private fun getEffectsDrawable(
    context: Context,
    geminiAnimationSpec: GeminiAnimationSpec,
  ): Drawable? {
    var drawableRef: EnergyShaderDrawable? = null
    val drawable =
      EnergyShaderDrawable(
        context = context,
        initialConfig = geminiAnimationSpec.config,
        keyframesForStates = geminiAnimationSpec.stateMap,
        initialState = EffectState.HIDDEN,
        onStateUpdateFinishedCallback = { finishedState ->
          if (finishedState == EffectState.ENTRY) {
            drawableRef?.updateState(EffectState.LOOP)
          }
        },
      )
    drawableRef = drawable
    return drawable
  }

  private fun startAnimation(drawable: Drawable) {
    (drawable as? EnergyShaderDrawable)?.updateState(EffectState.ENTRY)
  }

  /**
   * A data class that holds the configuration and keyframe sequences for a Gemini animation style.
   *
   * @param config The [EnergyShaderConfig] that defines the visual appearance of the animation.
   * @param stateMap A map of [EffectState] to [KeyframeSequence] that defines the animation
   *   sequence.
   */
  data class GeminiAnimationSpec(
    val config: EnergyShaderConfig,
    val stateMap: Map<EffectState, KeyframeSequence>,
    val timeSupplierMs: () -> Long = { System.currentTimeMillis() },
    val timeOffsetMs: Long? = null,
  )

  /**
   * Creates a themed [GeminiAnimationSpec] for chips.
   *
   * @param cornerRadius The corner radius of the chip boundary. If null, the default pill shape
   *   corner radii is inherited.
   * @param density The screen density.
   * @param colorScheme The active [ColorScheme].
   * @param context The [Context] to resolve system theme colors.
   */
  @Composable
  fun createChipSpec(
    cornerRadius: CornerRadius?,
    density: Float,
    colorScheme: ColorScheme,
    context: Context,
    strokeColor: Color,
    backgroundColor: Color,
  ): GeminiAnimationSpec {
    val isDark = isSystemInDarkTheme()
    return remember(
      cornerRadius,
      density,
      colorScheme,
      context,
      strokeColor,
      backgroundColor,
      isDark,
    ) {
      val resId =
        if (isDark) {
          R.color.gm3_sys_color_dynamic_dark_surface_container
        } else {
          R.color.gm3_sys_color_dynamic_light_surface_container
        }
      val colors = EnergyColors.from(resId, context)

      val energyColor1 = colors[0] // middle
      val energyColor2 = colors[1] // end
      val cornerRadii = cornerRadius?.let { CornerRadii(it.x / density) }
      val chipConfig =
        MessageInlineChipConfig(
          surfaceColor = colorScheme.surfaceContainer.toArgb(),
          energyColors = intArrayOf(energyColor1, energyColor2),
          cornerRadii = cornerRadii,
          strokeColor = strokeColor.toArgb(),
          backgroundColor = backgroundColor.toArgb(),
          surfaceColorCrystallized = colorScheme.surface.toArgb(),
        )
      val builder = chipConfig.createEffectsBuilder(null)
      GeminiAnimationSpec(chipConfig.chipEntryConfig(), builder.buildKeyframeSequences())
    }
  }

  /**
   * Creates a themed [GeminiAnimationSpec] for cards derived from a base color.
   *
   * @param baseColor The base color from which the energy colors are derived.
   * @param colorScheme The active [ColorScheme]. Defaults to [MaterialTheme.colorScheme].
   * @param cornerRadiusDp The corner radius of the card boundary in DP.
   * @param cardPaddingDp The card padding in DP.
   * @param spreadDp The spread of the card boundary in DP. Default is 20 DP.
   */
  @OptIn(ExperimentalMaterial3EffectApi::class)
  @Composable
  fun createCardSpec(
    baseColor: Color,
    colorScheme: ColorScheme = MaterialTheme.colorScheme,
    cornerRadiusDp: Int,
    cardPaddingDp: Int,
    spreadDp: Int = 20,
  ): GeminiAnimationSpec {
    val energyColors = deriveEnergyColors(baseColor, colorScheme)
    val glowColorMidArgb = energyColors[0].toArgb()
    val glowColorEndArgb = energyColors[1].toArgb()
    val cardConfig =
      object :
        DefaultCardConfig(
          surfaceColor = Color.Transparent.toArgb(),
          energyColors = intArrayOf(glowColorMidArgb, glowColorEndArgb),
        ) {
        override fun cardBaseConfig(): EnergyShaderConfig {
          return super.cardBaseConfig().copy {
            layout = layout.copy {
              cornerRadii = CornerRadii(cornerRadiusDp.toFloat())
              spread = spreadDp.toFloat()
              margins = Margins(cardPaddingDp.toFloat())
            }
            panel =
              this.getOrCreatePanel().copy {
                color = Color.Transparent.toArgb()
                alpha = 0f
              }
          }
        }
      }
    val builder = cardConfig.createEffectsBuilder(null)
    return GeminiAnimationSpec(cardConfig.cardEntryConfig(), builder.buildKeyframeSequences())
  }

  /**
   * Creates a themed [GeminiAnimationSpec] for cards with a single glow color. Will be removed
   * after all usages are migrated to the baseColor version.
   *
   * @param glowColor The color of the glow effect.
   * @param cornerRadiusDp The corner radius of the card boundary in DP.
   * @param cardPaddingDp The card padding in DP.
   * @param spreadDp The spread of the card boundary in DP. Default is 20 DP.
   */
  @Deprecated("Use the baseColor version instead.")
  fun createCardSpecLegacy(
    glowColor: Color,
    cornerRadiusDp: Int,
    cardPaddingDp: Int,
    spreadDp: Int = 20,
  ): GeminiAnimationSpec {
    val glowColorArgb = glowColor.toArgb()
    val cardConfig =
      object :
        DefaultCardConfig(
          surfaceColor = Color.Transparent.toArgb(),
          energyColors = intArrayOf(glowColorArgb, glowColorArgb),
        ) {
        override fun cardBaseConfig(): EnergyShaderConfig {
          return super.cardBaseConfig().copy {
            layout = layout.copy {
              cornerRadii = CornerRadii(cornerRadiusDp.toFloat())
              spread = spreadDp.toFloat()
              margins = Margins(cardPaddingDp.toFloat())
            }
            panel =
              this.getOrCreatePanel().copy {
                color = Color.Transparent.toArgb()
                alpha = 0f
              }
          }
        }
      }
    val builder = cardConfig.createEffectsBuilder(null)
    return GeminiAnimationSpec(cardConfig.cardEntryConfig(), builder.buildKeyframeSequences())
  }

  /**
   * Creates a themed [GeminiAnimationSpec] for cards.
   *
   * @param cornerRadius The corner radius of the card boundary. If null, the default configuration
   *   value (20dp) is inherited.
   * @param density The screen density.
   * @param glowColor The color of the glow effect.
   */
  @Deprecated("Use createCardSpec instead.")
  @Composable
  fun createCardSpecLegacy(
    cornerRadius: CornerRadius?,
    density: Float,
    glowColor: Color,
  ): GeminiAnimationSpec {
    return remember(cornerRadius, density, glowColor) {
      val glowColorArgb = glowColor.toArgb()
      val cornerRadiusDp = cornerRadius?.x ?: DEFAULT_CARD_CORNER_RADIUS_DP
      createCardAnimationSpec(cornerRadiusDp, glowColorArgb)
    }
  }

  private fun createCardAnimationSpec(cornerRadiusDp: Float, glowColor: Int): GeminiAnimationSpec {
    val config =
      EnergyShaderConfig(
        layout = LayoutConfig(cornerRadii = CornerRadii(cornerRadiusDp)),
        borderLight =
          BorderLightConfig(
            strokeWidth = 1f,
            color = glowColor,
            alpha = 0f,
            topIntensity = 0.4f,
            bottomIntensity = 0.1f,
            topBlurRadius = 20f,
            bottomBlurRadius = 20f,
            distortionSpeed = 0.45f,
            distortionFrequency = 0.0033f,
            distortionAmplitude = 15.75f,
          ),
        panel = null,
        radialGradient = null,
        rimLight = null,
        spotLight = null,
        rippleLight = null,
        dots =
          DotsConfig(
            alpha = 0f,
            maskStrokeWidth = 30f,
            waveAmplitude = 10f,
            waveFrequency = 0.2f,
            color = glowColor,
          ),
        loader = null,
      )

    val stateMap =
      mapOf(
        EffectState.ENTRY to
          KeyframeSequence(
            buildTransitions {
              transition(duration = 500, easing = LinearEasing) {
                keyframe(ShaderUniforms.borderLightAlpha, 0.5f)
              }
              transition(duration = 500, easing = LinearEasing) {
                keyframe(ShaderUniforms.borderLightAlpha, 0f)
              }
            } +
              buildTransitions {
                transition(duration = 200, easing = LinearEasing) {
                  keyframe(ShaderUniforms.dotsAlpha, 0f)
                }
                transition(duration = 700, easing = LinearEasing) {
                  keyframe(ShaderUniforms.dotsAlpha, 0.5f)
                }
                transition(duration = 700, easing = LinearEasing) {
                  keyframe(ShaderUniforms.dotsAlpha, 0f)
                }
              }
          ),
        EffectState.LOOP to
          KeyframeSequence(
            buildTransitions {
              transition(duration = 2000, easing = LinearEasing) {
                keyframe(ShaderUniforms.borderLightAlpha, 0f)
                keyframe(ShaderUniforms.dotsAlpha, 0f)
              }
            },
            isRepeatable = false,
          ),
      )

    return GeminiAnimationSpec(config, stateMap)
  }

  /**
   * Derives energy colors from a base color, remembering the result across recompositions.
   *
   * @param baseColor The base color from which the energy colors are derived.
   * @param colorScheme The active [ColorScheme].
   * @return An array of derived energy colors.
   */
  @OptIn(ExperimentalMaterial3EffectApi::class)
  @Composable
  fun deriveEnergyColors(baseColor: Color, colorScheme: ColorScheme): Array<Color> {
    return remember(
      baseColor,
      colorScheme.primary,
      colorScheme.secondary,
      colorScheme.tertiary,
      colorScheme.surface,
    ) {
      getEnergyColors(baseColor, colorScheme)
    }
  }
}

class MessageInlineChipConfig(
  surfaceColor: Int? = null,
  energyColors: IntArray? = null,
  private val cornerRadii: CornerRadii? = null,
  private val strokeColor: Int? = null,
  private val backgroundColor: Int? = null,
  surfaceColorCrystallized: Int? = null,
) :
  DefaultChipConfig(
    surfaceColor = surfaceColor,
    energyColors = energyColors,
    surfaceColorCrystallized = surfaceColorCrystallized,
  ) {
  override fun chipBaseConfig(): EnergyShaderConfig {
    return super.chipBaseConfig().copy {
      cornerRadii?.let {
        layout = layout.copy {
          cornerRadii = it
          spread = 4f
          margins = Margins(0f)
        }
      }
    }
  }

  override fun chipCrystallizedConfig(): EnergyShaderConfig {
    return super.chipCrystallizedConfig().copy {
      backgroundColor?.let { bgColor ->
        panel =
          getOrCreatePanel().copy {
            color = bgColor
            alpha = 1f
          }
      }
      strokeColor?.let { sColor ->
        borderLight =
          getOrCreateBorderLight().copy {
            color = sColor
            alpha = 1f
          }
      }
    }
  }
}
