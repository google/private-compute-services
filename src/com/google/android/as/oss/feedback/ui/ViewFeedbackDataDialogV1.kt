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
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryData
import com.google.android.`as`.oss.feedback.domain.ViewFeedbackData
import com.google.android.`as`.oss.feedback.domain.fold
import com.google.android.`as`.oss.feedback.quartz.serviceclient.QuartzFeedbackDonationData
import com.google.android.`as`.oss.feedback.serviceclient.FeedbackDonationData
import com.google.android.`as`.oss.feedback.ui.CombinedFeedbackDataV1.Companion.combine

@Composable
fun EntityFeedbackDataCollectionContentV1(
  modifier: Modifier = Modifier,
  selectedEntityContents: List<String>,
  feedbackDonationDataResult: Result<FeedbackDonationData>?,
  quartzFeedbackDonationDataResult: Result<QuartzFeedbackDonationData>? = null,
  onViewDataDisplayed: () -> Unit,
  onBackPressed: () -> Unit,
  onDismissRequest: () -> Unit,
) {
  LaunchedEffect(Unit) { onViewDataDisplayed() }

  BackHandler(enabled = true, onBack = onBackPressed)

  Column(modifier) {
    Spacer(modifier = Modifier.height(8.dp))
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      combinedFeedbackDataResults(feedbackDonationDataResult, quartzFeedbackDonationDataResult)
        .fold(
          onNull = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
          onSuccess = { donationData ->
            ViewFeedbackDataContent(donationData, selectedEntityContents)
          },
          onFailure = { FeedbackDataFailureContentV1(onDismissRequest = onDismissRequest) },
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
        "relatedSuggestions :" +
          selectedEntityContents.joinToString(separator = "\n -", prefix = "\n -", postfix = "\n")
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

@Composable
fun FeedbackDataFailureContentV1(onDismissRequest: () -> Unit) {
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

private data class CombinedFeedbackDataV1(
  val first: ViewFeedbackData,
  val second: ViewFeedbackData,
) : ViewFeedbackData {

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
      return CombinedFeedbackDataV1(first, second)
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
  other.forEach { (key, value) -> result.merge(key, value, combine) }
  return result
}
