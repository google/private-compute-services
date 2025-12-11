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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.api.FeedbackTagData
import com.google.android.`as`.oss.feedback.domain.GroundTruthData
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FeedbackTagChips(
  modifier: Modifier = Modifier,
  alignment: Alignment.Horizontal,
  tags: List<FeedbackTagData>,
  tagsSelection: Map<FeedbackTagData, Boolean>,
  groundTruthTitle: String,
  tagsGroupTruthOptions: Map<FeedbackTagData, List<GroundTruthData>?>,
  tagsGroundTruthSelection: Map<FeedbackTagData, GroundTruthData?>,
  onTagsShown: (List<FeedbackTagData>) -> Unit,
  onTagSelectionChanged: (FeedbackTagData, Boolean) -> Unit,
  onTagGroundTruthSelected: (FeedbackTagData, GroundTruthData) -> Unit,
) {
  var expandedTag by remember { mutableStateOf<FeedbackTagData?>(null) }

  LaunchedEffect(Unit) { onTagsShown(tags) }

  FlowRow(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = alignment),
  ) {
    for (tag in tags) {
      val selected = tagsSelection.getOrDefault(tag, false)
      val groundTruthOptions = tagsGroupTruthOptions[tag]
      val groundTruthSelection = tagsGroundTruthSelection[tag]

      val scope = rememberCoroutineScope()
      Box {
        FilterChip(
          selected = selected,
          onClick = {
            if (!selected && groundTruthOptions != null) {
              scope.launch {
                delay(150.milliseconds)
                expandedTag = tag
              }
            }
            onTagSelectionChanged(tag, !selected)
          },
          leadingIcon =
            if (selected) {
              {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
              }
            } else {
              null
            },
          trailingIcon =
            if (!selected && !groundTruthOptions.isNullOrEmpty()) {
              {
                Icon(
                  imageVector = Icons.Filled.ArrowDropDown,
                  contentDescription = groundTruthTitle,
                  modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
              }
            } else {
              null
            },
          label = { Text(text = tag.label) },
        )

        if (expandedTag != null && !groundTruthOptions.isNullOrEmpty()) {
          GroundTruthSelector(
            expanded = expandedTag == tag,
            title = groundTruthTitle,
            options = groundTruthOptions,
            selectedOption = groundTruthSelection,
            onOptionSelected = {
              if (!selected) {
                onTagSelectionChanged(tag, true)
              }
              onTagGroundTruthSelected(tag, it)
            },
            onDismissRequest = { expandedTag = null },
          )
        }
      }
    }
  }
}
