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

package com.google.android.`as`.oss.delegatedui.service.templates.bugle

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.node.Ref

/**
 * An [AnimatedVisibility] for a list of items, with the ability to animate changes to the list.
 *
 * This method maintains an internal state of the previous composition and then diffs the new state
 * against the previous one. It then uses that diff to animate items in or out using
 * [AnimatedVisibilityAfterInitialComposition].
 *
 * Note that diffing the list to check for removed items is a heavy operation. This composable
 * should be reserved for places where the list is relatively small.
 */
@SuppressLint("NewApi")
@Composable
internal fun <T> BugleAnimatedListItemVisibility(
  values: List<T>?,
  itemEnter: (index: Int) -> EnterTransition = { fadeIn() + scaleIn() },
  itemExit: (index: Int) -> ExitTransition = { fadeOut() + scaleOut() },
  label: String = "AnimatedListItemVisibilityAfterInitialComposition",
  itemContent: @Composable (itemValue: T) -> Unit,
) {
  val ref = remember { Ref<List<T>>() }
  val previouslyDrawnList = ref.value
  val currentList = values ?: emptyList()

  val listToDraw: List<T?> =
    if (previouslyDrawnList == null || currentList.size >= previouslyDrawnList.size) {
      // If the new list is bigger than the old list, assume we've just added elements so we can
      // just draw them all and they will animate in as expected.
      currentList
    } else if (currentList.isEmpty()) {
      // Shortcut from the list diffing if we've removed all elements.
      List(previouslyDrawnList.size) { null }
    } else {
      // If the new list is smaller than the old list, we've removed elements and need to pass a
      // null into the AnimatedVisibilityAfterInitialComposition, otherwise the element which was
      // removed won't animate out. The algorithm below replaces removed items with null by
      // finding
      // the diff between the two lists.
      val result: MutableList<T?> = previouslyDrawnList.toMutableList()
      val diff = (previouslyDrawnList - currentList.toSet()).toSet()
      result.apply { replaceAll { value -> if (diff.contains(value)) null else value } }
    }
  for ((index, value) in listToDraw.withIndex()) {
    AnimatedVisibilityAfterInitialComposition(
      value,
      itemEnter.invoke(index),
      itemExit.invoke(index),
      label,
      itemContent,
    )
  }

  ref.value = values
}

/**
 * An [AnimatedVisibility] which takes into account whether or not the element should actually be
 * animated based on the current state of parent compositions. For example, if an element is loaded
 * fast enough that it is included in the initial composition of the message list, then there is no
 * reason to animate it, but if it comes after the initial composition and would otherwise pop into
 * existence, then it should be animated.
 *
 * This also differs from [AnimatedVisibility] by immediately kicking off the animation if the
 * element needs animation. Specifically, this works well for UX components which tend to be
 * completely excluded from the composition (by passing in null UiData to a parent composable) and
 * are only drawn if the UiData is not null. This composable masks the logic for kicking off that
 * transition.
 *
 * @param value the nullable value to be used for whether or not the content should be displayed. If
 *   the value is non-null, then content will be invoked with it and displayed. If it is null, then
 *   previously displayed content will animate out according to the exit transition before being
 *   removed from the composition.
 * @see [AnimatedVisibility]
 */
@Composable
private fun <T> AnimatedVisibilityAfterInitialComposition(
  value: T?,
  enter: EnterTransition,
  exit: ExitTransition,
  label: String = "AnimatedVisibilityAfterInitialComposition",
  content: @Composable (value: T) -> Unit,
) {
  val ref = remember { Ref<T>() }

  ref.value = value ?: ref.value

  val visibleState = remember { MutableTransitionState(false) }

  LaunchedEffect(Unit) { visibleState.targetState = true }

  AnimatedVisibility(visibleState = visibleState, enter = enter, exit = exit, label = label) {
    ref.value?.let { value -> content(value) }
  }
}
