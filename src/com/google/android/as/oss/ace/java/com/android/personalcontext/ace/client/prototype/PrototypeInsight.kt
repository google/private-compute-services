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

@file:Suppress("FlaggedApi", "NewApi")

package com.android.personalcontext.ace.client.prototype

import android.os.Bundle
import android.service.personalcontext.Token
import android.service.personalcontext.hint.PublishedContextHint
import android.service.personalcontext.insight.ContextInsight
import androidx.annotation.IntRange
import com.android.personalcontext.ace.common.LabeledContextInsight

/**
 * The [PrototypeInsight] pattern enables the creation of custom [ContextInsight] types for
 * prototyping without modifying the Android framework.
 *
 * These custom types are wrapped within a standard [ContextInsight] for transport via ACE, while
 * ensuring that both their parcelable data and child insights are safely preserved. This approach
 * allows new insight types to be built and tested before they are officially integrated into the
 * Android platform as native [ContextInsight] subclasses.
 *
 * To implement this pattern, every prototype insight must inherit from one of two primary subtypes
 * based on its structural needs:
 *
 * * [PrototypeContextInsight]: Designed for standalone, non-collection insights. It manages its own
 *   [originHints] and does not support nested child insights.
 * * [PrototypeInsightCollection]: Designed to act like an [InsightCollection]. It supports nested
 *   insight hierarchies and automatically derives its [originHints] from its children.
 *
 * @property id The unique identifier representing the specific prototype insight type.
 * @property creator The factory responsible for recreating this insight from serialized data.
 */
sealed class PrototypeInsight(val id: PrototypeInsightId, val creator: Creator) {

  /** @see [ContextInsight.getOriginHints] */
  abstract val originHints: Collection<PublishedContextHint>

  /** @see [ContextInsight.getTokens] */
  open val tokens: List<Token> = emptyList()

  /** @see [ContextInsight.getChildren] */
  abstract val children: List<LabeledContextInsight>

  /**
   * Exports the primitive data for this insight into the given [Bundle].
   *
   * [originHints] and [tokens] will be persisted for you automatically.
   *
   * @param bundle The destination [Bundle] to store primitive insight data.
   */
  abstract fun exportDataToBundle(bundle: Bundle)

  /** Final toString implementation to prevent subclass implementations that might leak PII. */
  final override fun toString(): String {
    val details = buildList {
      if (originHints.isNotEmpty()) {
        val count = originHints.size
        val itemText = if (count == 1) "item" else "items"
        add("originHints=[$count $itemText]")
      }
      if (tokens.isNotEmpty()) {
        add("tokens=$tokens")
      }
    }
    return "${this.javaClass.simpleName}(${details.joinToString(", ")})"
  }

  /**
   * Interface for creating instances of [PrototypeInsight] from their serialized state.
   *
   * Implementations of this interface act as factories that reconstruct concrete [PrototypeInsight]
   * subclasses using data previously exported via [exportDataToBundle] and
   * [PrototypeInsightCollection.exportInsightsToList].
   *
   * This mechanism allows the framework to instantiate specific prototype insights without having
   * compile-time knowledge of their concrete classes.
   */
  interface Creator {

    /**
     * Creates a new instance of a concrete [PrototypeInsight] subclass.
     *
     * @param bundle The [Bundle] containing the primitive data for this insight, as populated by
     *   [PrototypeInsight.exportDataToBundle].
     * @param insights The list of child [ContextInsight]s, as populated by
     *   [PrototypeInsightCollection.exportInsightsToList].
     * @param originHints The set of origin hints associated with this insight. These are
     *   automatically preserved by the base class and passed back here for reconstruction.
     * @return A fully initialized instance of the specific [PrototypeInsight] subclass.
     */
    fun create(
      bundle: Bundle,
      insights: List<ContextInsight?>,
      originHints: Set<PublishedContextHint>,
    ): PrototypeInsight
  }
}

/**
 * A [PrototypeInsight] that represents a standalone, non-collection insight. It manages its own
 * [originHints] and does not support nested child insights.
 *
 * @param id The unique identifier for this insight.
 * @param creator The creator used to reconstruct this specific context insight.
 */
abstract class PrototypeContextInsight(
  id: PrototypeInsightId,
  creator: PrototypeContextInsightCreator,
) : PrototypeInsight(id, creator) {

  final override val children: List<LabeledContextInsight>
    get() = emptyList()

  /** A specialized [Creator] for [PrototypeContextInsight]. */
  abstract class PrototypeContextInsightCreator : Creator {

    final override fun create(
      bundle: Bundle,
      insights: List<ContextInsight?>,
      originHints: Set<PublishedContextHint>,
    ): PrototypeInsight {
      return create(bundle, originHints)
    }

    /**
     * Creates a new instance of a concrete [PrototypeContextInsight] subclass.
     *
     * @param bundle The [Bundle] containing the primitive data for this insight.
     * @param originHints The set of origin hints associated with this insight.
     * @return A fully initialized instance of the specific [PrototypeContextInsight] subclass.
     */
    abstract fun create(
      bundle: Bundle,
      originHints: Set<PublishedContextHint>,
    ): PrototypeContextInsight
  }
}

/**
 * A [PrototypeInsight] that acts like an [InsightCollection]. It supports nested insight
 * hierarchies and automatically derives its [originHints] from its children.
 *
 * @param id The unique identifier for this insight collection.
 * @param creator The creator used to reconstruct this insight collection.
 */
abstract class PrototypeInsightCollection(
  id: PrototypeInsightId,
  creator: PrototypeInsightCollectionCreator,
) : PrototypeInsight(id, creator) {

  /** Insight collections origin hints are derived from its child elements. */
  final override val originHints: Collection<PublishedContextHint>
    get() = exportInsightsToList().flatMap { it.insight?.originHints ?: emptySet() }.toSet()

  final override val children: List<LabeledContextInsight>
    get() = exportInsightsToList()

  /**
   * Exports the child [ContextInsight]s for this collection into a list.
   *
   * @return A list of child insights contained within this collection.
   */
  abstract fun exportInsightsToList(): List<LabeledContextInsight>

  /** A specialized [Creator] for [PrototypeInsightCollection]. */
  abstract class PrototypeInsightCollectionCreator : Creator {

    final override fun create(
      bundle: Bundle,
      insights: List<ContextInsight?>,
      originHints: Set<PublishedContextHint>,
    ): PrototypeInsight {
      return create(bundle, insights)
    }

    /**
     * Creates a new instance of a concrete [PrototypeInsightCollection] subclass.
     *
     * @param bundle The [Bundle] containing the primitive data for this collection.
     * @param insights The list of child [ContextInsight]s belonging to this collection.
     * @return A fully initialized instance of the specific [PrototypeInsightCollection] subclass.
     */
    abstract fun create(bundle: Bundle, insights: List<ContextInsight?>): PrototypeInsightCollection
  }
}

/**
 * A unique identifier for each type of [PrototypeInsight].
 *
 * @property uid A unique positive value for each entry, which should not change after creation to
 *   ensure backwards-compatibility.
 * @property typeName The class name of the prototype, may be used by OSI for identification.
 */
// Next ID: 13
enum class PrototypeInsightId(@field:IntRange(from = 1) val uid: Int, val typeName: String) {
  ExampleEmbeddedInsightId(1, "ExampleEmbeddedInsight"),
  EmbeddedScrollInsightId(2, "EmbeddedScrollInsight"),
  ClientActionInsightId(3, "ClientActionInsight"),
  WeatherInsightId(4, "WeatherInsight"),
  EmptyRenderInsightId(5, "EmptyRenderInsight"),
  CardInsightId(7, "CardInsight"),
  InsightGridId(8, "InsightGrid"),
  ServerSideCloseInsightId(9, "ServerSideCloseInsight"),
  RenderTokenInsightId(10, "RenderTokenInsight"),
  ClientSignalInsightId(11, "ClientSignalInsight"),
  LoadingInsightId(12, "LoadingInsight"),
}
