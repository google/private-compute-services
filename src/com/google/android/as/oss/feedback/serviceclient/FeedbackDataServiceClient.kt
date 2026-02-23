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

package com.google.android.`as`.oss.feedback.serviceclient

import com.google.android.`as`.oss.feedback.api.dataservice.FeedbackUiRenderingData
import com.google.android.`as`.oss.feedback.api.dataservice.feedbackUiRenderingData
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryData
import com.google.android.`as`.oss.feedback.domain.ViewFeedbackData

/** Service to provide feedback donation data. */
interface FeedbackDataServiceClient {
  /** Gets the feedback donation data from the feedback data service. */
  suspend fun getFeedbackDonationData(
    clientSessionId: String,
    uiElementType: Int,
    uiElementIndex: Int? = null,
    quartzCuj: QuartzCUJ? = null,
  ): Result<FeedbackDonationData>
}

data class FeedbackDonationData(
  val triggeringMessages: List<String> = emptyList(),
  val intentQueries: List<String> = emptyList(),
  val modelOutputs: List<String> = emptyList(),
  val memoryEntities: List<MemoryEntity> = emptyList(),
  val appId: String = "",
  val interactionId: String = "",
  val runtimeConfig: RuntimeConfig = RuntimeConfig(),
  val feedbackUiRenderingData: FeedbackUiRenderingData = feedbackUiRenderingData {},
  val cuj: SpoonCUJ = SpoonCUJ.SPOON_CUJ_UNKNOWN,
) : ViewFeedbackData {
  override val viewFeedbackHeader: String? = feedbackUiRenderingData.feedbackDialogViewDataHeader
  override val viewFeedbackBody: String = toString()

  override val dataCollectionCategories: Map<DataCollectionCategory, DataCollectionCategoryData>
    get() {
      return mapOf(
        DataCollectionCategory.TriggeringMessages to
          DataCollectionCategoryData(
            header = feedbackUiRenderingData.feedbackViewDataCategoryTitles.triggeringMessagesTitle,
            body = triggeringMessages.joinToString("\n"),
          ),
        DataCollectionCategory.IntentQueries to
          DataCollectionCategoryData(
            header = feedbackUiRenderingData.feedbackViewDataCategoryTitles.intentQueriesTitle,
            body = intentQueries.joinToString("\n"),
          ),
        DataCollectionCategory.ModelOutputs to
          DataCollectionCategoryData(
            header = feedbackUiRenderingData.feedbackViewDataCategoryTitles.modelOutputsTitle,
            body = modelOutputs.joinToString("\n"),
          ),
        DataCollectionCategory.MemoryEntities to
          DataCollectionCategoryData(
            header = feedbackUiRenderingData.feedbackViewDataCategoryTitles.memoryEntitiesTitle,
            body =
              memoryEntities
                .map { entity -> "modelVersion: ${entity.modelVersion}\n${entity.entityData}" }
                .joinToString("\n" + "-".repeat(50) + "\n"),
          ),
      )
    }

  override val dataCollectionCategoryExpandContentDescription: String =
    feedbackUiRenderingData.feedbackViewDataCategoryTitles.expandCategoryButtonSemanticsDescription

  override val dataCollectionCategoryCollapseContentDescription: String =
    feedbackUiRenderingData.feedbackViewDataCategoryTitles
      .collapseCategoryButtonSemanticsDescription

  override fun toString(): String {
    return buildString {
      if (triggeringMessages.isNotEmpty()) {
        appendLine("triggeringMessages {")
        for (message in triggeringMessages) {
          appendLine("  $message")
        }
        appendLine("}")
      }

      if (intentQueries.isNotEmpty()) {
        appendLine("intentQueries {")
        for (query in intentQueries) {
          appendLine("  $query")
        }
        appendLine("}")
      }

      if (modelOutputs.isNotEmpty()) {
        appendLine("modelOutputs {")
        for (output in modelOutputs) {
          appendLine("  $output")
        }
        appendLine("}")
      }

      if (memoryEntities.isNotEmpty()) {
        appendLine("memoryEntities {")
        for (entity in memoryEntities) {
          appendLine("  memoryEntity {")
          appendLine("    entityData: ${entity.entityData}")
          appendLine("    modelVersion: ${entity.modelVersion}")
          appendLine("  }")
        }
        appendLine("}")
      }

      append("appId: $appId")
      appendLine("interactionId: $interactionId")
      appendLine("runtimeConfig {")
      appendLine("  appBuildType: ${runtimeConfig.appBuildType}")
      appendLine("  appVersion: ${runtimeConfig.appVersion}")
      appendLine("  modelMetadata: ${runtimeConfig.modelMetadata}")
      appendLine("  modelId: ${runtimeConfig.modelId}")
      appendLine("}")
      if (cuj != SpoonCUJ.SPOON_CUJ_OVERALL_FEEDBACK) appendLine("cuj: $cuj")
    }
  }
}

data class MemoryEntity(val entityData: String, val modelVersion: String)

data class RuntimeConfig(
  val appBuildType: String = "",
  val appVersion: String = "",
  val modelMetadata: String = "",
  val modelId: String = "",
)
