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

package com.android.personalcontext.ace.common.wrappers

import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.service.personalcontext.embedded.InsightSurfaceClientInfo
import android.service.personalcontext.insight.ContextInsight
import androidx.annotation.RequiresApi
import androidx.annotation.VisibleForTesting
import java.util.UUID

/** Wrapper interface for [InsightSurfaceClientInfo]. */
sealed interface IInsightSurfaceClientInfo {

  /**
   * Returns the unwrapped [InsightSurfaceClientInfo]. May return null if originally wrapped from a
   * unit test, where constructing an instance of [InsightSurfaceClientInfo] is not possible.
   */
  fun unwrap(): InsightSurfaceClientInfo?

  /** @see InsightSurfaceClientInfo.id */
  val id: UUID

  /** @see InsightSurfaceClientInfo.displayId */
  val displayId: Int

  /** @see InsightSurfaceClientInfo.measureSpecWidth */
  val measureSpecWidth: Int

  /** @see InsightSurfaceClientInfo.measureSpecHeight */
  val measureSpecHeight: Int

  /** @see InsightSurfaceClientInfo.backgroundColor */
  val backgroundColor: Color

  /** @see InsightSurfaceClientInfo.nestedScrollAxes */
  val nestedScrollAxes: Int

  /** @see InsightSurfaceClientInfo.nestedScrollAxisLocked */
  val nestedScrollAxisLocked: Boolean

  /** @see InsightSurfaceClientInfo.shouldBlur */
  fun shouldBlur(): Boolean

  /** @see InsightSurfaceClientInfo.themeResourceId */
  val themeResourceId: Int

  /** @see InsightSurfaceClientInfo.packageName */
  val packageName: String

  /** @see InsightSurfaceClientInfo.configuration */
  val configuration: Configuration

  /** @see InsightSurfaceClientInfo.onReceiveInsight */
  fun onReceiveInsight(insight: ContextInsight)

  /** @see InsightSurfaceClientInfo.extras */
  @get:RequiresApi(Build.VERSION_CODES_FULL.CINNAMON_BUN_2) val extras: Bundle
}

/** Creates an [IInsightSurfaceClientInfo] from a [InsightSurfaceClientInfo]. */
fun InsightSurfaceClientInfo.wrap(): IInsightSurfaceClientInfo =
  InsightSurfaceClientInfoWrapper(this)

private class InsightSurfaceClientInfoWrapper(private val original: InsightSurfaceClientInfo) :
  IInsightSurfaceClientInfo {
  override fun unwrap() = original

  override val id: UUID
    get() = original.id

  override val displayId: Int
    get() = original.displayId

  override val measureSpecWidth: Int
    get() = original.measureSpecWidth

  override val measureSpecHeight: Int
    get() = original.measureSpecHeight

  override val backgroundColor: Color
    get() = original.backgroundColor

  override val nestedScrollAxes: Int
    get() = original.nestedScrollAxes

  override val nestedScrollAxisLocked: Boolean
    get() = original.nestedScrollAxisLocked

  override fun shouldBlur(): Boolean = original.shouldBlur()

  override val themeResourceId: Int
    get() = original.themeResourceId

  override val packageName: String
    get() = original.packageName

  override val configuration: Configuration
    get() = original.configuration

  override val extras: Bundle
    get() = original.extras

  override fun onReceiveInsight(insight: ContextInsight) = original.onReceiveInsight(insight)
}

@VisibleForTesting
class InsightSurfaceClientInfoForTesting(
  override val id: UUID = UUID.randomUUID(),
  override val displayId: Int = 0,
  override val measureSpecWidth: Int = 0,
  override val measureSpecHeight: Int = 0,
  override val backgroundColor: Color = Color.valueOf(0),
  override val nestedScrollAxes: Int = 0,
  override val nestedScrollAxisLocked: Boolean = false,
  private val shouldBlur: Boolean = false,
  override val themeResourceId: Int = 0,
  override val packageName: String = "",
  override val configuration: Configuration = Configuration(),
  override val extras: Bundle = Bundle.EMPTY,
  private val onReceiveInsight: (ContextInsight) -> Unit = {},
) : IInsightSurfaceClientInfo {
  override fun unwrap() = null

  override fun shouldBlur(): Boolean = shouldBlur

  override fun onReceiveInsight(insight: ContextInsight) = onReceiveInsight.invoke(insight)
}
