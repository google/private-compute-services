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

package com.google.android.`as`.oss.dataattribution

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.google.android.`as`.oss.dataattribution.proto.AttributionChipData
import com.google.android.`as`.oss.dataattribution.proto.AttributionDialogData

/** The public API for showing the DataAttributionActivity. */
object DataAttributionApi {

  /** Key for the AttributionDialogData proto extra in the Intent bundle. */
  internal const val EXTRA_ATTRIBUTION_DIALOG_DATA_PROTO = "attribution_dialog_data_proto"
  /** Key for the AttributionChipData proto extra in the Intent bundle. */
  internal const val EXTRA_ATTRIBUTION_CHIP_DATA_PROTO = "attribution_chip_data_proto"
  /** Key for the source deep links in the Intent bundle. */
  internal const val EXTRA_ATTRIBUTION_SOURCE_DEEP_LINKS = "attribution_source_deep_links"

  private const val PCS_PKG_NAME: String = "com.google.android.as.oss"

  private const val DATA_ATTRIBUTION_ACTIVITY_NAME: String =
    "com.google.android.as.oss.dataattribution.DataAttributionActivity"

  /**
   * Creates an [Intent] that starts [DataAttributionActivity] with the correct parameters.
   *
   * @param attributionDialogData The [AttributionDialogData] used to render the attribution dialog.
   * @param attributionChipData An optional [AttributionChipData] that, if provided, will be used to
   *   render a chip that represents the data that will be egressed on click. Providing this field
   *   also enables the feedback entry-point from attributions.
   * @param sourceDeepLinks An optional array of deep links for each AttributionCardData in
   *   [attributionDialogData]. If provided, each non-null deep link will be used to make the
   *   [AttributionCard] clickable.
   */
  fun createDataAttributionIntent(
    context: Context,
    attributionDialogData: AttributionDialogData,
    attributionChipData: AttributionChipData?,
    sourceDeepLinks: Array<PendingIntent?>?,
  ): Intent {
    require(
      sourceDeepLinks == null || sourceDeepLinks.size == attributionDialogData.attributionsCount
    ) {
      "You must provide an array whose size equals [attributionDialogData.attributionsCount]."
    }

    val intent =
      Intent(Intent.ACTION_MAIN).apply {
        setComponent(ComponentName(PCS_PKG_NAME, DATA_ATTRIBUTION_ACTIVITY_NAME))
        // FLAG_ACTIVITY_NEW_TASK: Needed to start an Activity outside of an Activity context.
        // FLAG_ACTIVITY_CLEAR_TASK: Fix for old Activity being reused.
        // FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS: Ensure the Activity launched doesn't show up as a
        // separate task when a user opens recents.
        setFlags(
          FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK or FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        putExtra(EXTRA_ATTRIBUTION_DIALOG_DATA_PROTO, attributionDialogData.toByteArray())
        attributionChipData?.let { putExtra(EXTRA_ATTRIBUTION_CHIP_DATA_PROTO, it.toByteArray()) }
        sourceDeepLinks?.let { putExtra(EXTRA_ATTRIBUTION_SOURCE_DEEP_LINKS, it) }
      }

    return intent
  }
}
