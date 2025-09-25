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

package com.google.android.`as`.oss.feedback

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData

@Composable
fun EntityFeedbackDataCollectionContent(
  selectedEntityContents: List<String>,
  feedbackDonationDataResult: Result<FeedbackDonationData>?,
  quartzFeedbackDonationDataResult: Result<QuartzFeedbackDonationData>? = null,
  onBackPressed: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  BackHandler(enabled = true, onBack = onBackPressed)

  Column {
    Spacer(modifier = Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      combinedFeedbackDataResults(feedbackDonationDataResult, quartzFeedbackDonationDataResult)
        .fold(
          onNull = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
          onSuccess = { donationData ->
            ViewFeedbackDataContent(donationData, selectedEntityContents)
          },
          onFailure = { FeedbackDataFailureContent(onDismissRequest = onDismissRequest) },
        )
    }
    Spacer(modifier = Modifier.height(32.dp))
  }
}

@Composable
private fun ViewFeedbackDataContent(
  viewFeedbackData: ViewFeedbackData,
  selectedEntityContents: List<String>,
) {
  val focusManager = LocalFocusManager.current
  LaunchedEffect(Unit) { focusManager.clearFocus() }

  Column {
    // Header
    if (viewFeedbackData.viewFeedbackHeader != null) {
      Text(
        modifier = Modifier.align(Alignment.CenterHorizontally),
        text = viewFeedbackData.viewFeedbackHeader!!,
        style = MaterialTheme.typography.bodySmall,
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    // Data collected text
    val viewDataPrefix =
      if (selectedEntityContents.any { it.isNotBlank() }) {
        "Selected Entity Content:\n -" + selectedEntityContents.joinToString("\n -") + "\n"
      } else {
        ""
      }
    Text(
      modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
      text = viewDataPrefix + viewFeedbackData.viewFeedbackBody,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

/**
 * Custom layout that accepts a mapping from [Key] to Composable contents. This layout measures all
 * contents at the size of [baseKey], and only shows [activeKey].
 *
 * If [activeKey] cannot be found, this will show [baseKey] instead.
 */
@Composable
fun <Key> SameSizeLayout(
  modifier: Modifier = Modifier,
  contentAlignment: Alignment = Alignment.TopStart,
  baseKey: Key,
  activeKey: Key,
  vararg contents: Pair<Key, @Composable BoxScope.() -> Unit>,
) {
  val baseContent = checkNotNull(contents[baseKey])
  val activeContent = if (activeKey != baseKey) contents[activeKey] else null

  Layout(
    modifier = modifier,
    content = { // Always compose contentA, even if it might not be placed.
      Box(modifier = Modifier.wrapContentSize(), contentAlignment = contentAlignment) {
        baseContent.invoke(this)
      }
      Box(modifier = Modifier.wrapContentSize(), contentAlignment = contentAlignment) {
        activeContent?.invoke(this)
      }
    },
  ) { measurables, constraints ->
    // Always measure baseContent
    val baseContentMeasurable = measurables[0]
    val baseContentPlaceable =
      baseContentMeasurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
    val baseContentWidth = baseContentPlaceable.width
    val baseContentHeight = baseContentPlaceable.height

    val activeContentPlaceable =
      if (activeContent != null) {
        // Measure activeContent if it's different
        val activeContentMeasurable = measurables[1]
        activeContentMeasurable.measure(
          constraints.copy(
            minWidth = baseContentWidth,
            maxWidth = baseContentWidth,
            minHeight = baseContentHeight,
            maxHeight = baseContentHeight,
          )
        )
      } else {
        // Else use baseContent
        baseContentPlaceable
      }

    layout(baseContentWidth, baseContentHeight) { activeContentPlaceable.placeRelative(0, 0) }
  }
}

/** Returns the Value of the first Pair whose Key matches the given [key]. */
private operator fun <Key, Value> Array<out Pair<Key, Value>>.get(key: Key): Value? {
  return firstOrNull { it.first == key }?.second
}

@Composable
fun FeedbackDataFailureContent(onDismissRequest: () -> Unit) {
  Column(
    modifier = Modifier.wrapContentSize().padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = "Can't retrieve data",
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(16.dp))
    Button(onClick = { onDismissRequest() }) { Text(text = "Try again later") }
  }
}

private data class CombinedFeedbackData(
  val first: ViewFeedbackData?,
  val second: ViewFeedbackData?,
) : ViewFeedbackData {

  override val viewFeedbackHeader: String? = first?.viewFeedbackHeader ?: second?.viewFeedbackHeader

  override val viewFeedbackBody: String =
    listOfNotNull(first?.viewFeedbackBody, second?.viewFeedbackBody).joinToString("\n")
}

private fun <T : ViewFeedbackData> combinedFeedbackDataResults(
  first: Result<T>?,
  second: Result<T>?,
): Result<ViewFeedbackData> {
  return if (first?.isSuccess == true || second?.isSuccess == true) {
    Result.success(CombinedFeedbackData(first?.getOrNull(), second?.getOrNull()))
  } else {
    Result.failure(
      first?.exceptionOrNull() ?: second?.exceptionOrNull() ?: Exception("No exception")
    )
  }
}
