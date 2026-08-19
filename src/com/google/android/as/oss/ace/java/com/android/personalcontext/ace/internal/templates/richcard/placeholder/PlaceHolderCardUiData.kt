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

package com.android.personalcontext.ace.internal.templates.richcard.placeholder

import com.android.personalcontext.ace.internal.templates.richcard.CardContextAction
import com.android.personalcontext.ace.internal.templates.richcard.CardType
import com.android.personalcontext.ace.internal.templates.richcard.DeprecatedUiCardContext

/**
 * Represents the structured UI data for a PlaceHolder card.
 *
 * @property message The message to display.
 * @property action Optional tap action for the card.
 */
data class PlaceHolderCardUiData(
  val message: String? = null,
  val action: CardContextAction? = null,
) : DeprecatedUiCardContext {
  override val cardType: CardType = CardType.PLACEHOLDER
}
