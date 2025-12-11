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

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryData
import com.google.android.`as`.oss.feedback.domain.FeedbackUiState
import com.google.android.`as`.oss.feedback.domain.ViewFeedbackData
import com.google.android.`as`.oss.feedback.domain.fold
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.android.`as`.oss.feedback.ui.CombinedFeedbackData.Companion.combine

@Composable
fun ViewFeedbackDataContent(
  modifier: Modifier = Modifier,
  uiState: FeedbackUiState,
  selectedEntityContents: List<String>,
  feedbackDonationDataResult: Result<FeedbackDonationData>?,
  quartzFeedbackDonationDataResult: Result<QuartzFeedbackDonationData>? = null,
  onViewDataScreenDisplayed: () -> Unit,
  onViewDataSectionCheckedChange: (DataCollectionCategory, Boolean) -> Unit,
  onViewDataSectionExpanded: () -> Unit,
  onBackPressed: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  LaunchedEffect(Unit) { onViewDataScreenDisplayed() }

  BackHandler(enabled = true, onBack = onBackPressed)

  Box(
    modifier = modifier.animateContentSize().fillMaxSize().padding(top = 8.dp, bottom = 32.dp),
    contentAlignment = Alignment.Center,
  ) {
    combinedFeedbackDataResults(feedbackDonationDataResult, quartzFeedbackDonationDataResult)
      .fold(
        onNull = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
        onSuccess = { donationData ->
          ViewFeedbackDataContent(
            uiState = uiState,
            viewFeedbackData = donationData,
            selectedEntityHeader =
              feedbackDonationDataResult
                ?.getOrNull()
                ?.feedbackUiRenderingData
                ?.feedbackViewDataCategoryTitles
                ?.selectedEntityContentTitle,
            selectedEntityContents = selectedEntityContents,
            onCheckedChange = onViewDataSectionCheckedChange,
            onSectionExpanded = onViewDataSectionExpanded,
          )
        },
        onFailure = { FeedbackDataFailureContent(onDismissRequest = onDismissRequest) },
      )
  }
}

@Composable
private fun ViewFeedbackDataContent(
  uiState: FeedbackUiState,
  viewFeedbackData: ViewFeedbackData,
  selectedEntityHeader: String?,
  selectedEntityContents: List<String>,
  onCheckedChange: (DataCollectionCategory, Boolean) -> Unit,
  onSectionExpanded: () -> Unit,
) {
  val focusManager = LocalFocusManager.current
  LaunchedEffect(Unit) { focusManager.clearFocus() }

  var parentBounds: Rect? by remember { mutableStateOf(null) }
  val scrollState = rememberScrollState()
  Column(
    modifier =
      Modifier.fillMaxSize()
        .onGloballyPositioned { parentBounds = it.boundsInWindow() }
        .verticalScroll(scrollState)
  ) {
    val categories =
      selectedEntityCategory(selectedEntityHeader, selectedEntityContents) +
        viewFeedbackData.dataCollectionCategories

    for ((category, data) in categories) {
      DataCollectionSection(
        viewFeedbackData = viewFeedbackData,
        data = data,
        scrollState = scrollState,
        parentBounds = parentBounds,
        checked = uiState.optInChecked[category] ?: false,
        onCheckedChange = { onCheckedChange(category, it) },
        onSectionExpanded = onSectionExpanded,
      )
    }
  }
}

@Composable
private fun DataCollectionSection(
  modifier: Modifier = Modifier,
  viewFeedbackData: ViewFeedbackData,
  data: DataCollectionCategoryData,
  scrollState: ScrollState,
  parentBounds: Rect?,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  onSectionExpanded: () -> Unit,
) {
  var shouldKeepInView by remember { mutableStateOf(false) }
  var childTop: Float? by remember { mutableStateOf(null) }
  var childBottom: Float? by remember { mutableStateOf(null) }

  LaunchedEffect(shouldKeepInView, childTop, childBottom, parentBounds) {
    if (shouldKeepInView) {
      scrollState.scrollChildIntoView(
        childTop,
        childBottom,
        parentBounds?.top,
        parentBounds?.bottom,
      )
    }
  }

  Column(
    modifier =
      modifier
        .onGloballyPositioned {
          val position = it.positionInWindow()
          childTop = position.y
          childBottom = position.y + it.size.height
        }
        .animateContentSize(finishedListener = { _, _ -> shouldKeepInView = false })
  ) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(checked = checked, onCheckedChange = onCheckedChange)

      Row(
        modifier =
          Modifier.semantics {
              this.role = Role.Button
              this.contentDescription =
                if (expanded) {
                  viewFeedbackData.dataCollectionCategoryCollapseContentDescription
                } else {
                  viewFeedbackData.dataCollectionCategoryExpandContentDescription
                }
            }
            .clickable {
              expanded = !expanded
              if (expanded) {
                shouldKeepInView = true
                onSectionExpanded()
              }
            }
            .padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          modifier = Modifier.weight(1f),
          text = data.header,
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
          modifier = Modifier.padding(16.dp),
          painter =
            painterResource(
              if (expanded) R.drawable.keyboard_arrow_up_24 else R.drawable.keyboard_arrow_down_24
            ),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          contentDescription = null,
        )
      }
    }

    if (expanded) {
      Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text = data.body,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
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

// Add the selected entity content to the data collection categories if it exists.
private fun selectedEntityCategory(
  selectedEntityHeader: String?,
  selectedEntityContents: List<String>,
): Map<DataCollectionCategory, DataCollectionCategoryData> {
  return if (selectedEntityContents.isNotEmpty() && selectedEntityHeader != null) {
    mapOf(
      DataCollectionCategory.SelectedEntityContent to
        DataCollectionCategoryData(
          header = selectedEntityHeader,
          body = selectedEntityContents.joinToString("\n"),
        )
    )
  } else {
    emptyMap()
  }
}

private data class CombinedFeedbackData(val first: ViewFeedbackData, val second: ViewFeedbackData) :
  ViewFeedbackData {

  override val viewFeedbackHeader: String? = first.viewFeedbackHeader ?: second.viewFeedbackHeader

  override val viewFeedbackBody: String =
    listOf(first.viewFeedbackBody, second.viewFeedbackBody).joinToString("\n")

  override val dataCollectionCategories: Map<DataCollectionCategory, DataCollectionCategoryData> =
    first.dataCollectionCategories.mergeWith(second.dataCollectionCategories) { data1, data2 ->
      DataCollectionCategoryData(
        header = data1.header,
        body = listOf(data1.body, data2.body).joinToString("\n"),
      )
    }

  override val dataCollectionCategoryExpandContentDescription: String =
    first.dataCollectionCategoryExpandContentDescription

  override val dataCollectionCategoryCollapseContentDescription: String =
    first.dataCollectionCategoryCollapseContentDescription

  companion object {
    fun combine(first: ViewFeedbackData?, second: ViewFeedbackData?): ViewFeedbackData? {
      if (first == null) return second
      if (second == null) return first
      return CombinedFeedbackData(first, second)
    }
  }
}

private fun <T : ViewFeedbackData> combinedFeedbackDataResults(
  first: Result<T>?,
  second: Result<T>?,
): Result<ViewFeedbackData> {
  return combine(first?.getOrNull(), second?.getOrNull())?.let { Result.success(it) }
    ?: Result.failure(
      first?.exceptionOrNull() ?: second?.exceptionOrNull() ?: Exception("No exception")
    )
}

/**
 * Merges this map with another map, resolving key collisions using the provided `combine` lambda
 * function.
 *
 * This function is immutable and returns a new map.
 */
private fun <K, V : Any> Map<K, V>.mergeWith(
  other: Map<K, V>,
  combine: (v1: V, v2: V) -> V,
): Map<K, V> {
  val result = toMutableMap()
  for ((key, value) in other) {
    result.merge(key, value, combine)
  }
  return result
}
