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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.domain.GroundTruthData
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A dropdown menu composable that allows the user to select from a list of ground truth options.
 *
 * This composable displays a title and a list of options. When an option is selected, a checkmark
 * is displayed next to it. The menu will dismiss after an option is selected with a short delay to
 * show the ripple effect.
 *
 * @param expanded Whether the dropdown menu is currently expanded.
 * @param title The title to be displayed at the top of the dropdown menu.
 * @param options A list of `GroundTruthData` objects to be displayed as options in the menu.
 * @param selectedOption The currently selected `GroundTruthData` option, or null if no option is
 *   selected.
 * @param onOptionSelected A callback that is invoked when an option is selected by the user.
 * @param onDismissRequest A callback that is invoked when the user requests to dismiss the menu.
 */
@Composable
fun GroundTruthSelector(
  expanded: Boolean,
  title: String,
  options: List<GroundTruthData>,
  selectedOption: GroundTruthData?,
  onOptionSelected: (GroundTruthData) -> Unit,
  onDismissRequest: () -> Unit,
) {
  DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
    Text(
      modifier = Modifier.padding(12.dp),
      text = title,
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )

    HorizontalDivider()

    for (option in options) {
      val isSelected = option == selectedOption

      val contentColor =
        if (isSelected) {
          MaterialTheme.colorScheme.onSecondaryContainer
        } else {
          MaterialTheme.colorScheme.onSurface
        }

      val scope = rememberCoroutineScope()
      DropdownMenuItem(
        modifier =
          Modifier.then(
            if (isSelected) {
              Modifier.background(MaterialTheme.colorScheme.secondaryContainer)
            } else {
              Modifier
            }
          ),
        text = { Text(option.label) },
        onClick = {
          onOptionSelected(option)

          scope.launch {
            delay(150.milliseconds) // Show the ripple to confirm user selection.
            onDismissRequest()
          }
        },
        trailingIcon = {
          if (isSelected) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null)
          }
        },
        colors =
          MenuDefaults.itemColors(
            textColor = contentColor,
            leadingIconColor = contentColor,
            trailingIconColor = contentColor,
          ),
      )
    }
  }
}
