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

package com.android.personalcontext.ace.internal.templates.richcard.imagegallery

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.common.GoogleSansText
import com.android.personalcontext.ace.internal.templates.richcard.common.LoadingBox
import com.android.personalcontext.ace.internal.templates.richcard.common.cardContextActionClickable
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import javax.inject.Inject

class ImageGalleryCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<ImageGalleryCardUiData> {

  @Composable
  override fun Render(cardUiData: CardUiData<ImageGalleryCardUiData>, modifier: Modifier) {
    val uiContext = cardUiData.cardContext ?: return
    if (cardUiData.attribution == null) return

    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .cardContextActionClickable(uiContext.action)
            .padding(bottom = 12.dp)
      ) {
        CollageContent(uiContext = uiContext)
      }
    }
  }

  /** Renders the full collage card content by composing info text and images. */
  @Composable
  private fun CollageContent(uiContext: ImageGalleryCardUiData, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth()) {
      when (uiContext) {
        is ImageGalleryCardUiData.LoadingData -> {
          LoadingInfoText()
          LoadingImageCollage()
        }
        is ImageGalleryCardUiData.LoadedData -> {
          val subtitleIconBitmap =
            remember(uiContext.subtitleIcon) {
              uiContext.subtitleIcon?.loadDrawable(context)?.toBitmap()
            }
          val bitmaps =
            remember(uiContext.images) {
              uiContext.images.mapNotNull { it.loadDrawable(context)?.toBitmap() }
            }
          InfoText(
            header = uiContext.header,
            subtitle = uiContext.subtitle,
            subtitleIcon = subtitleIconBitmap,
            subtitleSuffix = uiContext.subtitleSuffix,
            tertiaryText = uiContext.tertiaryText,
            subtitleContentDescription = uiContext.subtitleContentDescription,
          )
          ImageCollage(images = bitmaps)
        }
      }
    }
  }

  /** Renders the structured information text section (header, subtitle, tertiary text). */
  @Composable
  private fun InfoText(
    header: String?,
    subtitle: String?,
    subtitleIcon: Bitmap?,
    subtitleSuffix: String?,
    tertiaryText: String?,
    modifier: Modifier = Modifier,
    subtitleContentDescription: String? = null,
  ) {
    Column(modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
      if (header != null) {
        Text(
          text = header,
          style =
            MaterialTheme.typography.titleLarge.copy(
              fontSize = 18.sp,
              lineHeight = 24.sp,
              fontWeight = FontWeight.Medium,
              letterSpacing = 0.sp,
            ),
          color = MaterialTheme.colorScheme.onSurface,
        )
      }
      if (subtitle != null) {
        if (subtitle.isNotEmpty() || subtitleIcon != null || !subtitleSuffix.isNullOrEmpty()) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              if (subtitleContentDescription != null) {
                Modifier.clearAndSetSemantics { contentDescription = subtitleContentDescription }
              } else {
                Modifier
              },
          ) {
            if (subtitle.isNotEmpty()) {
              SubduedText(subtitle)
            }
            SubtitleIcon(subtitleIcon)
            if (!subtitleSuffix.isNullOrEmpty()) {
              SubduedText(subtitleSuffix)
            }
          }
        }
      }
      if (tertiaryText != null && tertiaryText.isNotEmpty()) {
        SubduedText(tertiaryText)
      }
    }
  }

  /** Renders the loading skeleton placeholder bars for the information text section. */
  @Composable
  private fun LoadingInfoText(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)) {
      LoadingBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).padding(bottom = 4.dp))
      LoadingBox(modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).padding(bottom = 4.dp))
      LoadingBox(modifier = Modifier.fillMaxWidth(0.5f).height(20.dp).padding(bottom = 4.dp))
    }
  }

  /** Renders subdued grey text for secondary and tertiary labels. */
  @Composable
  private fun SubduedText(text: String, modifier: Modifier = Modifier) {
    Text(
      text = text,
      style =
        MaterialTheme.typography.bodyMedium.copy(
          fontFamily = GoogleSansText,
          fontSize = 14.sp,
          lineHeight = 20.sp,
          fontWeight = FontWeight.W400,
          letterSpacing = 0.1.sp,
        ),
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = modifier,
    )
  }
}

/** Renders the subtitle icon if present. */
@Composable
private fun SubtitleIcon(iconBitmap: Bitmap?) {
  if (iconBitmap != null) {
    Image(
      bitmap = iconBitmap.asImageBitmap(),
      contentDescription = null,
      modifier = Modifier.padding(start = 2.dp, end = 4.dp).size(12.dp),
    )
  }
}

/** Renders a multi-image collage displaying up to 4 loaded bitmaps. */
@Composable
private fun ImageCollage(images: List<Bitmap>, modifier: Modifier = Modifier) {
  if (images.isEmpty()) return

  val displayImages = images.take(3)

  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        .clip(RoundedCornerShape(12.dp))
  ) {
    when (displayImages.size) {
      1 -> GalleryImage(bitmap = displayImages[0])
      2 ->
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          GalleryImage(bitmap = displayImages[0], modifier = Modifier.weight(1f))
          GalleryImage(bitmap = displayImages[1], modifier = Modifier.weight(1f))
        }
      3 ->
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          GalleryImage(bitmap = displayImages[0], modifier = Modifier.weight(1f))
          Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            GalleryImage(bitmap = displayImages[1], modifier = Modifier.weight(1f))
            GalleryImage(bitmap = displayImages[2], modifier = Modifier.weight(1f))
          }
        }
      4 ->
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            GalleryImage(bitmap = displayImages[0], modifier = Modifier.weight(1f))
            GalleryImage(bitmap = displayImages[1], modifier = Modifier.weight(1f))
          }
          Row(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            GalleryImage(bitmap = displayImages[2], modifier = Modifier.weight(1f))
            GalleryImage(bitmap = displayImages[3], modifier = Modifier.weight(1f))
          }
        }
    }
  }
}

/** Renders the loading skeleton placeholder layout for the image collage. */
@Composable
private fun LoadingImageCollage(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        .clip(RoundedCornerShape(12.dp))
  ) {
    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      LoadingBox(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(4.dp)))
      Column(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        LoadingBox(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(4.dp)))
        LoadingBox(modifier = Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(4.dp)))
      }
    }
  }
}

/** Renders an individual rounded gallery image within a collage grid. */
@Composable
private fun GalleryImage(bitmap: Bitmap, modifier: Modifier = Modifier) {
  Image(
    bitmap = bitmap.asImageBitmap(),
    contentDescription = null,
    modifier = modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)),
    contentScale = ContentScale.Crop,
  )
}
