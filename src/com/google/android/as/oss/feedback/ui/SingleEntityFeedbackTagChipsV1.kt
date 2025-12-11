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

package com.google.android.`as`.oss.feedback.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.api.FeedbackTagData

@Composable
fun SingleEntityFeedbackTagChipsV1(
  modifier: Modifier,
  tags: List<FeedbackTagData>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
) {
  FlowRow(
    modifier = modifier,
    horizontalArrangement =
      Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally),
  ) {
    LaunchedEffect(Unit) { onTagsShown(tags) }

    for (tag in tags) {
      val selected = tagsSelection[tag] == true
      FilterChip(
        selected = selected,
        onClick = { onTagSelectionChanged(tag, !selected) },
        leadingIcon = {
          if (selected) {
            Icon(
              imageVector = Icons.Filled.Check,
              contentDescription = null,
              modifier = Modifier.size(FilterChipDefaults.IconSize),
            )
          }
        },
        label = { Text(text = tag.label) },
      )
    }
  }
}
