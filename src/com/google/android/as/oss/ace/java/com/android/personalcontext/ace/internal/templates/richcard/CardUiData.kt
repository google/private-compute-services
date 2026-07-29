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

package com.android.personalcontext.ace.internal.templates.richcard

import android.app.RemoteAction
import android.graphics.drawable.Icon
import android.service.personalcontext.insight.ActionableInsight
import android.service.personalcontext.insight.ContextInsight
import android.service.personalcontext.insight.InsightActionDetails
import android.service.personalcontext.insight.InsightDisplayDetails
import com.android.personalcontext.ace.client.prototype.serversideclose.ServerSideCloseInsight
import com.android.personalcontext.ace.common.DisplayableInsight
import java.util.UUID

/**
 * A data-driven UI template representing a card.
 *
 * This class is designed as a generic container to support various content types while maintaining
 * a consistent outer structure (attribution, insight, actions).
 *
 * @param id The id of the card
 * @param C The type of the context/content data (e.g., Flight info, Calendar events).
 * @property cardActionDetails The action details of the card click event.
 * @property cardTitle The state of the card title/insight.
 * @property icon The optional icon to be displayed with the card title.
 * @property titleInsight The original title insight used for logging.
 * @property dismissInsight The dismiss button insight for the data. If null, do not render the
 *   dismiss button.
 * @property attribution Source information for the data.
 * @property cardContext Card content info in grid. If null, omit the context section.
 * @property actions Optional list of actions the user can take.
 */
data class CardUiData<out C : DeprecatedUiCardContext>(
  val id: String = UUID.randomUUID().toString(),
  val cardActionDetails: InsightActionDetails? = null,
  val cardTitle: CardTitle? = null,
  val icon: Icon? = null,
  val titleInsight: DisplayableInsight? = null,
  val dismissInsight: ServerSideCloseInsight? = null,
  val attribution: Attribution? = null,
  val cardContext: C? = null,
  val cardContextAction: CardContextAction? = null,
  val actions: List<CardAction>? = null,
)

/** Represents the state of the card title/insight. */
sealed interface CardTitle {
  /** The title is still loading. */
  object Loading : CardTitle

  /** The title is present and available. */
  data class Present(val text: String) : CardTitle
}

/** Source information for the data showing as a title. */
data class Attribution(val sourceAppIcons: List<Icon>, val title: String)

@Deprecated(
  level = DeprecationLevel.WARNING,
  message = "CardContext should be removed once all templates are fully refactored.",
)
/** Card Content Info defined by respective card types. */
interface DeprecatedUiCardContext {
  /** The representation of the card type. */
  val cardType: CardType
  /** Whether the card needs live info from server. */
  val needsLiveInfo: Boolean
    get() = false
}

/**
 * Action for the user to take.
 *
 * @property displayDetails The display details of the action.
 * @property insight The insight of the action. Used for pingback and egressing.
 */
sealed interface CardAction {
  /** The display details of the action. */
  val displayDetails: InsightDisplayDetails
  /** The insight of the action. */
  val insight: ContextInsight?
}

/**
 * Actionable action for the user to send. When user clicks the action, the remote action will be
 * sent.
 *
 * It is converted from [ActionableInsight].
 *
 * @param remoteAction The remote action to be sent.
 */
data class ActionableCardAction(
  override val displayDetails: InsightDisplayDetails,
  override val insight: ActionableInsight?,
  val remoteAction: RemoteAction,
) : CardAction

/**
 * Egressable action for the user to take. When user clicks the action, the insight will be
 * egressed.
 */
data class EgressableCardAction(
  override val displayDetails: InsightDisplayDetails,
  override val insight: ContextInsight?,
) : CardAction

/**
 * Action to be executed when the body of the card is tapped.
 *
 * @property insight The insight used to report the tap event.
 * @property remoteAction The remote action to be executed.
 */
data class CardContextAction(val insight: ContextInsight, val remoteAction: RemoteAction)
