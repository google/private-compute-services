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

package com.android.personalcontext.ace.internal.templates.richcard.stackcard

import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.internal.templates.richcard.CardType
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext

/**
 * Represents the structured UI data for a Stack Card template.
 *
 * @property header Optional structured header data for the leading column.
 * @property items The list of items to display in the main stack.
 * @property action The action to trigger when the body is tapped.
 */
data class StackCardUiData(
  val header: HeaderData?,
  val items: List<StackItem>,
  val action: CardContextAction? = null,
) : DeprecatedUiCardContext {
  override val cardType: CardType = CardType.RICH_CARD_STACK
}

/** Represents the header data in the leading column. */
data class HeaderData(
  val title: String,
  val subtitle: String? = null,
  val contentDescription: String? = null,
)

/** Represents an item in the stack. */
data class StackItem(val title: String, val subtitle: String?, val style: Style = Style.STANDARD)

/** Styles for items in the stack. */
enum class Style {
  STANDARD,
  VARIANT,
}
