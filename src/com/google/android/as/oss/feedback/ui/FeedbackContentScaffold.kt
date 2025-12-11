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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun FeedbackContentScaffold(
  modifier: Modifier = Modifier,
  headerIcon: ImageVector?,
  headerIconContentDescription: String?,
  headerIconOnClick: () -> Unit,
  headerTitle: String,
  primaryButtonLabel: String,
  primaryButtonLoading: Boolean,
  primaryButtonOnClick: () -> Unit,
  content: @Composable () -> Unit,
) {
  FillContentColumnLayout(
    modifier = modifier.padding(horizontal = 8.dp).padding(bottom = 20.dp),
    spacing = 8.dp,
    header = {
      HeaderRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        headerIcon = headerIcon,
        headerIconOnClick = headerIconOnClick,
        headerIconContentDescription = headerIconContentDescription,
        headerTitle = headerTitle,
      )
    },
    content = content,
    footer = {
      ButtonsRow(
        modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp),
        primaryButtonOnClick = primaryButtonOnClick,
        primaryButtonLoading = primaryButtonLoading,
        primaryButtonLabel = primaryButtonLabel,
      )
    },
  )
}

@Composable
private fun HeaderRow(
  modifier: Modifier,
  headerIcon: ImageVector?,
  headerIconOnClick: () -> Unit,
  headerIconContentDescription: String?,
  headerTitle: String,
) {
  Row(
    modifier = modifier.heightIn(min = 32.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Icon button
    if (headerIcon != null) {
      IconButton(modifier = Modifier.size(32.dp), onClick = headerIconOnClick) {
        Icon(imageVector = headerIcon, contentDescription = headerIconContentDescription)
      }
    } else {
      Spacer(modifier = Modifier.size(32.dp))
    }

    // Title
    Text(text = headerTitle, style = MaterialTheme.typography.headlineSmall)

    Spacer(modifier = Modifier.size(32.dp))
  }
}

@Composable
private fun ButtonsRow(
  modifier: Modifier,
  primaryButtonOnClick: () -> Unit,
  primaryButtonLoading: Boolean,
  primaryButtonLabel: String,
) {
  Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
    Button(onClick = primaryButtonOnClick) {
      SameSizeLayout(selector = primaryButtonLoading) {
        base { Text(text = primaryButtonLabel, style = MaterialTheme.typography.labelLarge) }

        alternative(key = true) {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 3.dp,
            )
          }
        }
      }
    }
  }
}
