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

package com.android.personalcontext.ace.internal.templates.richcard.grid

import com.android.personalcontext.ace.common.InsightGridItem.Span
import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.internal.templates.richcard.CardType
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext

/** Represents the non-semantic UI data for a grid card. */
data class GridCardUiData(
  val title: TitleData? = null,
  val gridItems: List<GridCardItem>? = null,
  val action: CardContextAction? = null,
) : DeprecatedUiCardContext {
  override val cardType: CardType = CardType.RICH_CARD_GRID
}

/** Title data for the grid card. At least one field must be non-null for it to show. */
data class TitleData(
  val mainTitle: String? = null,
  val mainTitleContentDescription: String? = null,
  val mainSubtitle: String? = null,
  val accessory: AccessoryData? = null,
) {
  init {
    require(mainTitle != null || mainSubtitle != null || accessory != null) {
      "At least one field must not be null"
    }
  }
}

/** Accessory data displayed alongside the title. */
data class AccessoryData(val text: String, val isVariant: Boolean = false)

sealed interface GridCardItem {
  val span: Span

  data class Loaded(val title: String?, val subtitle: String?, override val span: Span) :
    GridCardItem

  data class Loading(override val span: Span) : GridCardItem
}
