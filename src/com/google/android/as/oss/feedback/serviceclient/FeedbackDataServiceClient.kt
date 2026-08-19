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
import com.google.android.`as`.oss.feedback.api.gateway.BlueflaxCUJ
import com.google.android.`as`.oss.feedback.api.gateway.QuartzCUJ
import com.google.android.`as`.oss.feedback.api.gateway.SpoonCUJ
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategory
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryData
import com.google.android.`as`.oss.feedback.domain.DataCollectionCategoryDataLegacy
import com.google.android.`as`.oss.feedback.domain.FeedbackBodyItem
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
  val sourceDocuments: List<SourceDocument> = emptyList(),
  val failureReason: String = "",
  val appId: String = "",
  val interactionId: String = "",
  val runtimeConfig: RuntimeConfig = RuntimeConfig(),
  val feedbackUiRenderingData: FeedbackUiRenderingData = feedbackUiRenderingData {},
  val cuj: SpoonCUJ = SpoonCUJ.SPOON_CUJ_UNKNOWN,
  val blueflaxCuj: BlueflaxCUJ? = null,
  val defaultDonationOptInL1Enabled: Boolean = false,
  val defaultDonationOptInL0Enabled: Boolean = false,
) : ViewFeedbackData {
  override val viewFeedbackHeader: String? = feedbackUiRenderingData.feedbackDialogViewDataHeader

  override val viewFeedbackBody: String = toString()

  override val dataCollectionCategoriesLegacy:
    Map<DataCollectionCategory, DataCollectionCategoryDataLegacy>
    get() {
      val titles = feedbackUiRenderingData.feedbackViewDataCategoryTitles
      return mapOf(
        DataCollectionCategory.TriggeringMessages to
          DataCollectionCategoryDataLegacy(
            header = titles.triggeringMessagesTitle,
            body = triggeringMessages.joinToString("\n"),
          ),
        DataCollectionCategory.IntentQueries to
          DataCollectionCategoryDataLegacy(
            header = titles.intentQueriesTitle,
            body = intentQueries.joinToString("\n"),
          ),
        DataCollectionCategory.ModelOutputs to
          DataCollectionCategoryDataLegacy(
            header = titles.modelOutputsTitle,
            body = modelOutputs.joinToString("\n"),
          ),
        DataCollectionCategory.MemoryEntities to
          DataCollectionCategoryDataLegacy(
            header = titles.memoryEntitiesTitle,
            body =
              memoryEntities
                .map { entity -> "modelVersion: ${entity.modelVersion}\n${entity.entityData}" }
                .joinToString("\n" + "-".repeat(50) + "\n"),
          ),
      )
    }

  override val dataCollectionCategories: Map<DataCollectionCategory, DataCollectionCategoryData>
    get() {
      val titles = feedbackUiRenderingData.feedbackViewDataCategoryTitles
      return buildMap {
        if (triggeringMessages.isNotEmpty()) {
          put(
            DataCollectionCategory.TriggeringMessages,
            DataCollectionCategoryData(
              header = titles.triggeringMessagesTitle,
              items =
                listOf(
                  FeedbackBodyItem.SimpleText(
                    id = TRIGGERING_MESSAGES_ID,
                    text = triggeringMessages.joinToString("\n"),
                  )
                ),
            ),
          )
        }
        if (intentQueries.isNotEmpty()) {
          put(
            DataCollectionCategory.IntentQueries,
            DataCollectionCategoryData(
              header = titles.intentQueriesTitle,
              items =
                listOf(
                  FeedbackBodyItem.SimpleText(
                    id = INTENT_QUERIES_ID,
                    text = intentQueries.joinToString("\n"),
                  )
                ),
            ),
          )
        }
        if (modelOutputs.isNotEmpty()) {
          put(
            DataCollectionCategory.ModelOutputs,
            DataCollectionCategoryData(
              header = titles.modelOutputsTitle,
              items =
                listOf(
                  FeedbackBodyItem.SimpleText(
                    id = MODEL_OUTPUTS_ID,
                    text = modelOutputs.joinToString("\n"),
                  )
                ),
            ),
          )
        }
        if (failureReason.isNotEmpty()) {
          put(
            DataCollectionCategory.FailureReason,
            DataCollectionCategoryData(
              header = titles.failureReasonTitle,
              items =
                listOf(FeedbackBodyItem.SimpleText(id = "failure_reason", text = failureReason)),
            ),
          )
        }

        // MemoryEntities Category
        val memoryEntityItems =
          if (sourceDocuments.isNotEmpty()) {
            sourceDocuments.mapIndexed { index, doc ->
              val docId = "${DOC_ID_PREFIX}$index"
              FeedbackBodyItem.CheckableListItem(
                id = docId,
                title = doc.l0Title,
                defaultChecked = defaultDonationOptInL1Enabled || defaultDonationOptInL0Enabled,
                children =
                  listOf(
                    FeedbackBodyItem.CheckableListItem(
                      id = "${docId}${ENTITY_ID_SUFFIX}",
                      title = feedbackUiRenderingData.feedbackSourceL1TileTitle,
                      defaultChecked = defaultDonationOptInL1Enabled,
                      children =
                        listOf(
                          FeedbackBodyItem.ChildText(
                            id = "${docId}${ENTITY_TEXT_ID_SUFFIX}",
                            text = doc.memoryEntity.entityData,
                          )
                        ),
                    ),
                    FeedbackBodyItem.CheckableListItem(
                      id = "${docId}${L0_ID_SUFFIX}",
                      title = feedbackUiRenderingData.feedbackSourceDocumentL0TileTitle,
                      defaultChecked = defaultDonationOptInL0Enabled,
                      children =
                        listOf(
                          FeedbackBodyItem.ChildText(
                            id = "${docId}${L0_TEXT_ID_SUFFIX}",
                            text = doc.l0Content,
                          )
                        ),
                    ),
                  ),
              )
            }
          } else if (memoryEntities.isNotEmpty()) {
            listOf(
              FeedbackBodyItem.SimpleText(
                id = MEMORY_ENTITIES_LEGACY_ID,
                text =
                  memoryEntities
                    .map { entity -> "modelVersion: ${entity.modelVersion}\n${entity.entityData}" }
                    .joinToString("\n" + "-".repeat(50) + "\n"),
              )
            )
          } else {
            emptyList()
          }
        if (memoryEntityItems.isNotEmpty()) {
          put(
            DataCollectionCategory.MemoryEntities,
            DataCollectionCategoryData(
              header = titles.memoryEntitiesTitle,
              items = memoryEntityItems,
            ),
          )
        }
      }
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

      if (sourceDocuments.isNotEmpty()) {
        appendLine("sourceDocuments {")
        for (doc in sourceDocuments) {
          appendLine("  sourceDocument {")
          appendLine("    l0Title: ${doc.l0Title}")
          appendLine("    l0Content: ${doc.l0Content}")
          appendLine("    memoryEntity {")
          appendLine("      entityData: ${doc.memoryEntity.entityData}")
          appendLine("      modelVersion: ${doc.memoryEntity.modelVersion}")
          appendLine("    }")
          appendLine("  }")
        }
        appendLine("}")
      }

      if (failureReason.isNotEmpty()) {
        appendLine("failureReason: $failureReason")
      }

      append("appId: $appId")
      appendLine("interactionId: $interactionId")
      appendLine("runtimeConfig {")
      appendLine("  appBuildType: ${runtimeConfig.appBuildType}")
      appendLine("  appVersion: ${runtimeConfig.appVersion}")
      appendLine("  modelMetadata: ${runtimeConfig.modelMetadata}")
      appendLine("  modelId: ${runtimeConfig.modelId}")
      appendLine("}")
      if (cuj != SpoonCUJ.SPOON_CUJ_OVERALL_FEEDBACK && cuj != SpoonCUJ.SPOON_CUJ_UNKNOWN) {
        appendLine("cuj: $cuj")
      }
      if (blueflaxCuj != null && blueflaxCuj != BlueflaxCUJ.BLUEFLAX_CUJ_UNSPECIFIED) {
        appendLine("blueflaxCuj: $blueflaxCuj")
      }
    }
  }

  companion object {
    const val DOC_ID_PREFIX = "doc_"
    const val ENTITY_ID_SUFFIX = "_entity"
    const val L0_ID_SUFFIX = "_l0"
    private const val TRIGGERING_MESSAGES_ID = "triggering_messages"
    private const val INTENT_QUERIES_ID = "intent_queries"
    private const val MODEL_OUTPUTS_ID = "model_outputs"
    private const val MEMORY_ENTITIES_LEGACY_ID = "memory_entities_legacy"
    private const val ENTITY_TEXT_ID_SUFFIX = "_entity_text"
    private const val L0_TEXT_ID_SUFFIX = "_l0_text"
  }
}

data class MemoryEntity(val entityData: String, val modelVersion: String)

data class SourceDocument(
  val l0Title: String,
  val l0Content: String,
  val memoryEntity: MemoryEntity,
)

data class RuntimeConfig(
  val appBuildType: String = "",
  val appVersion: String = "",
  val modelMetadata: String = "",
  val modelId: String = "",
)
