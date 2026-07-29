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

package com.android.personalcontext.ace.internal.templates.richcard.imagegallery

import android.graphics.drawable.Icon
import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.internal.templates.richcard.CardType
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext

/** Represents the structured UI data for an Image Gallery card. */
sealed interface ImageGalleryCardUiData : DeprecatedUiCardContext {

  /** The action to perform when the card is clicked. */
  val action: CardContextAction?

  /** State when the entire Image Gallery card is loading. */
  data object LoadingData : ImageGalleryCardUiData {
    override val cardType: CardType = CardType.RICH_CARD_IMAGE_GALLERY
    override val action: CardContextAction? = null
  }

  /**
   * State when the Image Gallery card has data.
   *
   * @property header The header of the card.
   * @property subtitle The subtitle of the card.
   * @property subtitleIcon The icon to display next to the subtitle.
   * @property subtitleSuffix The suffix to append to the subtitle.
   * @property tertiaryText The tertiary text of the card.
   * @property subtitleContentDescription The content description for the subtitle.
   * @property images The list of images to display in the collage.
   * @property action The action to perform when the card is clicked.
   */
  data class LoadedData(
    val header: String? = null,
    val subtitle: String? = null,
    val subtitleIcon: Icon? = null,
    val subtitleSuffix: String? = null,
    val tertiaryText: String? = null,
    val subtitleContentDescription: String? = null,
    val images: List<Icon> = emptyList(),
    override val action: CardContextAction? = null,
  ) : ImageGalleryCardUiData {
    override val cardType: CardType = CardType.RICH_CARD_IMAGE_GALLERY
  }
}
