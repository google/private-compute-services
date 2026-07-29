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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.domain.GroundTruthData

/**
 * A dropdown menu composable that allows the user to select multiple ground truth options.
 *
 * @param expanded Whether the dropdown menu is currently expanded.
 * @param title The title to be displayed at the top of the dropdown menu.
 * @param options A list of `GroundTruthData` objects to be displayed as options in the menu.
 * @param selectedOptions A set of the currently selected `GroundTruthData` options.
 * @param onOptionToggled A callback that is invoked when an option is toggled by the user.
 * @param onDismissRequest A callback that is invoked when the user requests to dismiss the menu.
 */
@Composable
fun GroundTruthSelector(
  expanded: Boolean,
  title: String,
  options: List<GroundTruthData>,
  selectedOptions: Set<GroundTruthData>,
  singleSelection: Boolean = false,
  onOptionToggled: (GroundTruthData) -> Unit,
  onDismissRequest: () -> Unit,
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    modifier = Modifier.fillMaxWidth(0.8f),
  ) {
    Text(
      modifier = Modifier.padding(12.dp),
      text = title,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )

    HorizontalDivider()

    Column {
      val itemCount = options.size
      options.forEachIndexed { index, option ->
        val isSelected = selectedOptions.contains(option)

        DropdownMenuItem(
          text = { Text(text = option.label) },
          onClick = { onOptionToggled(option) },
          selected = isSelected,
          shapes = MenuDefaults.itemShape(index, itemCount),
          supportingText = {
            if (option.sourceApp.isNotEmpty()) {
              Text(text = option.sourceApp)
            }
          },
          leadingIcon =
            if (singleSelection) {
              null
            } else {
              { Checkbox(checked = isSelected, onCheckedChange = null) }
            },
        )
      }
    }
  }
}
