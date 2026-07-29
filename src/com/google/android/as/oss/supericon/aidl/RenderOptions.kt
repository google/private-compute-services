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

package com.google.android.`as`.oss.supericon.aidl

import android.graphics.Color.BLACK
import android.graphics.drawable.Icon
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.google.android.`as`.oss.supericon.utils.SuperIconUiType
import com.google.fcp.client.common.internal.safeparcel.AbstractSafeParcelable
import com.google.fcp.client.common.internal.safeparcel.SafeParcelable
import java.util.Objects

/**
 * Options for rendering a view. The [icon] and [background] can be null based on the [uiType] to be
 * rendered.
 *
 * @property width The total width for the rendering area.
 * @property height The total height for the rendering area.
 * @property minWidth The minimum width for the rendering area.
 * @property minHeight The minimum height for the rendering area.
 * @property maxWidth The maximum width for the rendering area.
 * @property maxHeight The maximum height for the rendering area.
 * @property uiType The type of UI to render.
 * @property icon The icon to be rendered.
 * @property iconWidth The width of the icon.
 * @property iconHeight The height of the icon.
 * @property iconScaleX The horizontal scale factor for the icon.
 * @property iconScaleY The vertical scale factor for the icon.
 * @property background The background icon.
 * @property label The label text to be rendered.
 * @property labelColor The color of the label text.
 * @property fontFamily The font family of the label text.
 * @property textSizeInPixels The text size in pixels of the label text.
 * @property textScaleX The horizontal scale factor for the label text.
 * @property windowToken The window token.
 * @property accessibilityPaneTitle The accessibility pane title.
 */
@SafeParcelable.Class(creator = "RenderOptionsCreator")
class RenderOptions
constructor(
  @field:SafeParcelable.Field(id = 1, getter = "getWidth") val width: Int,
  @field:SafeParcelable.Field(id = 2, getter = "getHeight") val height: Int,
  @field:SafeParcelable.Field(id = 3, getter = "getMinWidth") val minWidth: Int,
  @field:SafeParcelable.Field(id = 4, getter = "getMinHeight") val minHeight: Int,
  @field:SafeParcelable.Field(id = 5, getter = "getMaxWidth") val maxWidth: Int,
  @field:SafeParcelable.Field(id = 6, getter = "getMaxHeight") val maxHeight: Int,
  @field:SafeParcelable.Field(id = 7, getter = "getUiType")
  @field:SuperIconUiType
  val uiType: Int = SuperIconUiType.SUPER_ICON,
  @field:SafeParcelable.Field(id = 8, getter = "getIcon") val icon: Icon? = null,
  @field:SafeParcelable.Field(id = 9, getter = "getIconWidth") val iconWidth: Int = 0,
  @field:SafeParcelable.Field(id = 10, getter = "getIconHeight") val iconHeight: Int = 0,
  @field:SafeParcelable.Field(id = 11, getter = "getIconScaleX") val iconScaleX: Float = 1.0f,
  @field:SafeParcelable.Field(id = 12, getter = "getIconScaleY") val iconScaleY: Float = 1.0f,
  @field:SafeParcelable.Field(id = 13, getter = "getBackground") val background: Icon? = null,
  @field:SafeParcelable.Field(id = 14, getter = "getLabel") val label: String? = null,
  @field:SafeParcelable.Field(id = 15, getter = "getLabelColor")
  val labelColor: Int = DEFAULT_LABEL_COLOR,
  @field:SafeParcelable.Field(id = 16, getter = "getFontFamily")
  val fontFamily: String? = DEFAULT_FONT_FAMILY,
  @field:SafeParcelable.Field(id = 17, getter = "getTextSizeInPixels")
  val textSizeInPixels: Float = 0.0f, // Default to 0.0f
  @field:SafeParcelable.Field(id = 18, getter = "getTextScaleX") val textScaleX: Float = 1.0f,
  @field:SafeParcelable.Field(id = 19, getter = "getWindowToken") val windowToken: IBinder? = null,
  @field:SafeParcelable.Field(id = 20, getter = "getContentDescription")
  val contentDescription: String? = null,
  @field:SafeParcelable.Field(id = 21, getter = "getRoleDescription")
  val roleDescription: String? = null,
  @field:SafeParcelable.Field(
    id = 22,
    getter = "getAccessibilityPaneTitleBundle",
    type = "android.os.Bundle",
  )
  val accessibilityPaneTitle: CharSequence? = null,
  @field:SafeParcelable.Field(id = 23, getter = "getSubIcon") val subIcon: Icon? = null,
  @field:SafeParcelable.Field(id = 24, getter = "getSubIconWidth") val subIconWidth: Int = 0,
  @field:SafeParcelable.Field(id = 25, getter = "getSubIconHeight") val subIconHeight: Int = 0,
  @field:SafeParcelable.Field(id = 26, getter = "getSubIconScaleX", defaultValue = "1.0f")
  val subIconScaleX: Float = 1.0f,
  @field:SafeParcelable.Field(id = 27, getter = "getSubIconScaleY", defaultValue = "1.0f")
  val subIconScaleY: Float = 1.0f,
) : AbstractSafeParcelable() {

  @SafeParcelable.Constructor
  constructor(
    @SafeParcelable.Param(id = 1) width: Int,
    @SafeParcelable.Param(id = 2) height: Int,
    @SafeParcelable.Param(id = 3) minWidth: Int,
    @SafeParcelable.Param(id = 4) minHeight: Int,
    @SafeParcelable.Param(id = 5) maxWidth: Int,
    @SafeParcelable.Param(id = 6) maxHeight: Int,
    @SafeParcelable.Param(id = 7) @SuperIconUiType uiType: Int,
    @SafeParcelable.Param(id = 8) icon: Icon?,
    @SafeParcelable.Param(id = 9) iconWidth: Int,
    @SafeParcelable.Param(id = 10) iconHeight: Int,
    @SafeParcelable.Param(id = 11) iconScaleX: Float,
    @SafeParcelable.Param(id = 12) iconScaleY: Float,
    @SafeParcelable.Param(id = 13) background: Icon?,
    @SafeParcelable.Param(id = 14) label: String?,
    @SafeParcelable.Param(id = 15) labelColor: Int,
    @SafeParcelable.Param(id = 16) fontFamily: String?,
    @SafeParcelable.Param(id = 17) textSizeInPixels: Float,
    @SafeParcelable.Param(id = 18) textScaleX: Float,
    @SafeParcelable.Param(id = 19) windowToken: IBinder?,
    @SafeParcelable.Param(id = 20) contentDescription: String?,
    @SafeParcelable.Param(id = 21) roleDescription: String?,
    @SafeParcelable.Param(id = 22) accessibilityPaneTitleBundle: Bundle?,
    @SafeParcelable.Param(id = 23) subIcon: Icon?,
    @SafeParcelable.Param(id = 24) subIconWidth: Int,
    @SafeParcelable.Param(id = 25) subIconHeight: Int,
    @SafeParcelable.Param(id = 26) subIconScaleX: Float,
    @SafeParcelable.Param(id = 27) subIconScaleY: Float,
  ) : this(
    width = width,
    height = height,
    minWidth = minWidth,
    minHeight = minHeight,
    maxWidth = maxWidth,
    maxHeight = maxHeight,
    uiType = uiType,
    icon = icon,
    iconWidth = iconWidth,
    iconHeight = iconHeight,
    iconScaleX = iconScaleX,
    iconScaleY = iconScaleY,
    background = background,
    label = label,
    labelColor = labelColor,
    fontFamily = fontFamily,
    textSizeInPixels = textSizeInPixels,
    textScaleX = textScaleX,
    windowToken = windowToken,
    contentDescription = contentDescription,
    roleDescription = roleDescription,
    accessibilityPaneTitle =
      accessibilityPaneTitleBundle?.getCharSequence(KEY_ACCESSIBILITY_PANE_TITLE),
    subIcon = subIcon,
    subIconWidth = subIconWidth,
    subIconHeight = subIconHeight,
    subIconScaleX = subIconScaleX,
    subIconScaleY = subIconScaleY,
  )

  // SafeParcelable requires a Creator and specific writing logic
  override fun writeToParcel(dest: Parcel, flags: Int) {
    RenderOptionsCreator.writeToParcel(this, dest, flags)
  }

  /** Returns [accessibilityPaneTitle] as a String. */
  fun getAccessibilityPaneTitleString(): String? = accessibilityPaneTitle?.toString()

  /** Returns [accessibilityPaneTitle] wrapped in a [Bundle] for safe parceling across processes. */
  fun getAccessibilityPaneTitleBundle(): Bundle? {
    val title = accessibilityPaneTitle ?: return null
    return Bundle().apply { putCharSequence(KEY_ACCESSIBILITY_PANE_TITLE, title) }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || javaClass != other.javaClass) return false
    val otherOptions = other as RenderOptions
    return width == otherOptions.width &&
      height == otherOptions.height &&
      minWidth == otherOptions.minWidth &&
      minHeight == otherOptions.minHeight &&
      maxWidth == otherOptions.maxWidth &&
      maxHeight == otherOptions.maxHeight &&
      uiType == otherOptions.uiType &&
      iconWidth == otherOptions.iconWidth &&
      iconHeight == otherOptions.iconHeight &&
      iconScaleX == otherOptions.iconScaleX &&
      iconScaleY == otherOptions.iconScaleY &&
      icon?.equals(otherOptions.icon) ?: (otherOptions.icon == null) &&
      background?.equals(otherOptions.background) ?: (otherOptions.background == null) &&
      label == otherOptions.label &&
      labelColor == otherOptions.labelColor &&
      fontFamily == otherOptions.fontFamily &&
      textSizeInPixels == otherOptions.textSizeInPixels &&
      textScaleX == otherOptions.textScaleX &&
      windowToken == otherOptions.windowToken &&
      contentDescription == otherOptions.contentDescription &&
      roleDescription == otherOptions.roleDescription &&
      TextUtils.equals(accessibilityPaneTitle, otherOptions.accessibilityPaneTitle) &&
      (subIcon?.equals(otherOptions.subIcon) ?: (otherOptions.subIcon == null)) &&
      subIconWidth == otherOptions.subIconWidth &&
      subIconHeight == otherOptions.subIconHeight &&
      subIconScaleX == otherOptions.subIconScaleX &&
      subIconScaleY == otherOptions.subIconScaleY
  }

  override fun hashCode(): Int {
    return Objects.hash(
      width,
      height,
      minWidth,
      minHeight,
      maxWidth,
      maxHeight,
      uiType,
      icon,
      iconWidth,
      iconHeight,
      iconScaleX,
      iconScaleY,
      background,
      label,
      labelColor,
      fontFamily,
      textSizeInPixels,
      textScaleX,
      windowToken,
      contentDescription,
      roleDescription,
      accessibilityPaneTitle?.toString(),
      subIcon,
      subIconWidth,
      subIconHeight,
      subIconScaleX,
      subIconScaleY,
    )
  }

  override fun toString(): String {
    return "RenderOptions(width=$width, height=$height, minWidth=$minWidth, minHeight=$minHeight, maxWidth=$maxWidth, maxHeight=$maxHeight, uiType=$uiType, icon=$icon, iconWidth=$iconWidth, iconHeight=$iconHeight, iconScaleX=$iconScaleX, iconScaleY=$iconScaleY, background=$background, label=$label, labelColor=$labelColor, fontFamily=$fontFamily, textSizeInPixels=$textSizeInPixels, textScaleX=$textScaleX, windowToken=$windowToken, contentDescription=$contentDescription, roleDescription=$roleDescription, accessibilityPaneTitle=$accessibilityPaneTitle, subIcon=$subIcon, subIconWidth=$subIconWidth, subIconHeight=$subIconHeight, subIconScaleX=$subIconScaleX, subIconScaleY=$subIconScaleY)"
  }

  companion object {
    @JvmField val CREATOR: Parcelable.Creator<RenderOptions> = RenderOptionsCreator()
    const val DEFAULT_LABEL_COLOR = BLACK
    const val DEFAULT_FONT_FAMILY = "google-sans-text-medium"
    const val KEY_ACCESSIBILITY_PANE_TITLE = "accessibility_pane_title"
  }
}
