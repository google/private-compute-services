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

package com.google.android.`as`.oss.feedback.domain

/** Interface for feedback data to be displayed in the view feedback data dialog. */
interface ViewFeedbackData {
  val viewFeedbackHeader: String?
  val viewFeedbackBody: String
  val dataCollectionCategories: Map<DataCollectionCategory, DataCollectionCategoryData>
  val dataCollectionCategoryExpandContentDescription: String
  val dataCollectionCategoryCollapseContentDescription: String
}

/** Categories of data collection that the user can choose to independently enable or disable. */
enum class DataCollectionCategory {
  /** Legacy V1 ui will use this in lieu of any actual categories. */
  LegacyV1,
  TriggeringMessages,
  IntentQueries,
  ModelOutputs,
  MemoryEntities,
  SelectedEntityContent,
}

data class DataCollectionCategoryData(val header: String, val body: String)
