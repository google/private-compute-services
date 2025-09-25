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

package com.google.android.`as`.oss.delegatedui.service.templates.scope.interactions

import android.view.View
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_CLICK
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.InteractionType.INTERACTION_TYPE_LONG_CLICK
import com.google.android.`as`.oss.delegatedui.api.integration.templates.UiIdToken
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionHelper
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionListener

/** An interaction that represents a user click or long-click. */
interface ClickInteraction {

  /** Invokes [onClick] and [onLongClick] on click and long-click, respectively. */
  fun View.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener? = null,
    onClick: InteractionListener,
  )

  /** Invokes [onClick] on click. */
  fun Modifier.doOnClick(uiTokenId: UiIdToken, onClick: InteractionListener): Modifier =
    doOnClick(uiTokenId, onLongClick = null, onClick = onClick)

  /** Invokes [onClick] and [onLongClick] on click and long-click, respectively. */
  fun Modifier.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener?,
    onClick: InteractionListener,
  ): Modifier
}

class ClickInteractionImpl(private val helper: InteractionHelper) : ClickInteraction {

  override fun View.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener?,
    onClick: InteractionListener,
  ) =
    with(helper) {
      setOnClickListener {
        onInteraction(uiTokenId, interaction = INTERACTION_TYPE_CLICK, block = onClick)
      }

      if (onLongClick != null) {
        setOnLongClickListener {
          onInteraction(uiTokenId, interaction = INTERACTION_TYPE_LONG_CLICK, block = onLongClick)
          true
        }
      } else {
        setOnLongClickListener(null)
        isLongClickable = false
      }
    }

  override fun Modifier.doOnClick(
    uiTokenId: UiIdToken,
    onLongClick: InteractionListener?,
    onClick: InteractionListener,
  ): Modifier =
    with(helper) {
      return combinedClickable(
        onClick = {
          onInteraction(uiTokenId, interaction = INTERACTION_TYPE_CLICK, block = onClick)
        },
        onLongClick =
          onLongClick?.let {
            {
              onInteraction(
                uiTokenId,
                interaction = INTERACTION_TYPE_LONG_CLICK,
                block = onLongClick,
              )
            }
          },
      )
    }
}
