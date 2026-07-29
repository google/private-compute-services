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

package com.android.personalcontext.ace.internal.templates.richcard.locationpreview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.android.personalcontext.ace.internal.templates.richcard.Attribution
import com.android.personalcontext.ace.internal.templates.richcard.CardUiData
import com.android.personalcontext.ace.internal.templates.richcard.common.CardAppContextBlock
import com.android.personalcontext.ace.internal.templates.richcard.common.CardTemplateLayout
import com.android.personalcontext.ace.internal.templates.richcard.common.LoadingBox
import com.android.personalcontext.ace.internal.templates.richcard.renderer.CardRenderer
import com.android.personalcontext.ace.visualizer.compat.EnergyEffectsAnimationCompat
import javax.inject.Inject

/** [CardRenderer] for Location Preview cards. */
class LocationPreviewCardRenderer
@Inject
internal constructor(private val energyEffectsAnimationCompat: EnergyEffectsAnimationCompat) :
  CardRenderer<DeprecatedUiLocationPreviewCardContext> {

  @Composable
  override fun Render(
    cardUiData: CardUiData<DeprecatedUiLocationPreviewCardContext>,
    modifier: Modifier,
  ) {
    CardTemplateLayout(
      cardUiData = cardUiData,
      energyEffectsAnimationCompat = energyEffectsAnimationCompat,
      modifier = modifier,
    ) {
      val cardContext = cardUiData.cardContext
      val attribution = cardUiData.attribution
      if (attribution?.isValid == true && cardContext != null) {
        CardAppContextBlock(attribution) {
          Column(
            modifier =
              Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceBright, RoundedCornerShape(4.dp))
                .padding(bottom = 16.dp)
          ) {
            // Image Grid section
            val images = cardContext.galleryImages
            if (images == null) {
              LoadingImageGrid()
            } else if (images.isNotEmpty()) {
              ImageGrid(images = images)
            }

            // Text Details section
            Column(
              modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp)
            ) {
              if (cardContext.locationName != null) {
                if (cardContext.locationName.isNotEmpty()) {
                  Text(
                    text = cardContext.locationName,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 4.dp),
                  )
                }
              } else {
                LoadingBox(
                  modifier = Modifier.fillMaxWidth(0.7f).height(20.dp).padding(bottom = 4.dp)
                )
              }

              val rating = cardContext.rating
              val category = cardContext.category
              val hasRating = rating != null && rating.score.isNotEmpty()
              val hasCategory = category != null && category.isNotEmpty()
              val isRatingLoading = rating == null
              val isCategoryLoading = category == null

              if (hasRating || hasCategory || isRatingLoading || isCategoryLoading) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.padding(bottom = 4.dp),
                ) {
                  if (isRatingLoading && isCategoryLoading) {
                    LoadingBox(modifier = Modifier.size(width = 110.dp, height = 16.dp))
                  } else {
                    if (hasRating) {
                      val context = LocalContext.current
                      val ratingIconBitmap =
                        remember(rating.icon) {
                          rating.icon.loadDrawable(context)?.toBitmap()?.asImageBitmap()
                        }

                      if (ratingIconBitmap != null) {
                        Image(
                          bitmap = ratingIconBitmap,
                          contentDescription = null,
                          modifier = Modifier.size(16.dp),
                        )
                      } else {
                        Icon(
                          imageVector = Icons.Default.Star,
                          contentDescription = null,
                          tint = Color(0xFFFABB05),
                          modifier = Modifier.size(16.dp),
                        )
                      }
                      Text(
                        text = rating.score,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp),
                      )
                    } else if (isRatingLoading) {
                      LoadingBox(modifier = Modifier.size(width = 40.dp, height = 16.dp))
                    }

                    val showRatingPart = hasRating || isRatingLoading
                    val showCategoryPart = hasCategory || isCategoryLoading
                    if (showRatingPart && showCategoryPart) {
                      Text(
                        text = BULLET_SEPARATOR,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                      )
                    }

                    if (hasCategory) {
                      Text(
                        text = category,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                      )
                    } else if (isCategoryLoading) {
                      LoadingBox(
                        modifier =
                          Modifier.size(width = 60.dp, height = 16.dp).padding(start = 4.dp)
                      )
                    }
                  }
                }
              }

              if (cardContext.locationAddress != null) {
                if (cardContext.locationAddress.isNotEmpty()) {
                  Text(
                    text = cardContext.locationAddress,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                  )
                }
              } else {
                LoadingBox(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun ImageGrid(
    images: List<DeprecatedUiLocationPreviewCardContext.GalleryImage>,
    modifier: Modifier = Modifier,
  ) {
    Row(
      modifier =
        modifier
          .fillMaxWidth()
          .height(200.dp)
          .padding(start = 16.dp, end = 16.dp, top = 16.dp)
          .clip(RoundedCornerShape(12.dp)) // Clip the entire grid for rounded outer corners
    ) {
      // Large image (Left)
      Box(
        modifier =
          Modifier.weight(0.6f).fillMaxHeight().background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        GalleryImage(bitmap = images[0].bitmap, contentDescription = images[0].contentDescription)
      }

      // Small images (Right)
      if (images.size > 1) {
        val rightImages = images.drop(1).take(4) // Take up to 4 for a 2x2 grid
        Column(modifier = Modifier.weight(0.4f).fillMaxHeight().padding(start = 4.dp)) {
          // Row 1: Top (up to 2 images)
          val topImages = rightImages.take(2)
          if (topImages.isNotEmpty()) {
            Row(
              modifier = Modifier.weight(1f).fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              for (image in topImages) {
                Box(
                  modifier =
                    Modifier.weight(1f)
                      .fillMaxHeight()
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                  GalleryImage(bitmap = image.bitmap, contentDescription = image.contentDescription)
                }
              }
            }
          }

          // Row 2: Bottom (up to 2 images)
          val bottomImages = rightImages.drop(2).take(2)
          if (bottomImages.isNotEmpty()) {
            Row(
              modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              for (image in bottomImages) {
                Box(
                  modifier =
                    Modifier.weight(1f)
                      .fillMaxHeight()
                      .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                  GalleryImage(bitmap = image.bitmap, contentDescription = image.contentDescription)
                }
              }
              // Fill remaining space if less than 2
              for (i in bottomImages.size until 2) {
                Spacer(modifier = Modifier.weight(1f).fillMaxHeight())
              }
            }
          }
        }
      }
    }
  }

  @Composable
  private fun LoadingImageGrid(modifier: Modifier = Modifier) {
    Row(
      modifier =
        modifier
          .fillMaxWidth()
          .height(200.dp)
          .padding(start = 16.dp, end = 16.dp, top = 16.dp)
          .clip(RoundedCornerShape(12.dp))
    ) {
      // Large image (Left)
      Box(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
        LoadingBox(modifier = Modifier.fillMaxSize())
      }

      // Small images (Right)
      Column(modifier = Modifier.weight(0.4f).fillMaxHeight().padding(start = 4.dp)) {
        // Row 1: Top
        Row(
          modifier = Modifier.weight(1f).fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          LoadingBox(modifier = Modifier.weight(1f).fillMaxHeight())
          LoadingBox(modifier = Modifier.weight(1f).fillMaxHeight())
        }
      }
    }
  }

  @Composable
  private fun GalleryImage(
    bitmap: Bitmap?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
  ) {
    if (bitmap != null) {
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
    } else {
      Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant))
    }
  }

  private companion object {
    const val BULLET_SEPARATOR = " • "
  }
}

private val Attribution.isValid: Boolean
  get() = sourceAppIcons.isNotEmpty() && title.isNotEmpty()
