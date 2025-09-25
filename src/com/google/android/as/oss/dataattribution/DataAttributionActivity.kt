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
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.ui.platform.ComposeView
import com.google.android.`as`.oss.dataattribution.DataAttributionApi.EXTRA_ATTRIBUTION_CHIP_DATA_PROTO
import com.google.android.`as`.oss.dataattribution.DataAttributionApi.EXTRA_ATTRIBUTION_DIALOG_DATA_PROTO
import com.google.android.`as`.oss.dataattribution.DataAttributionApi.EXTRA_ATTRIBUTION_SOURCE_DEEP_LINKS
import com.google.android.`as`.oss.dataattribution.proto.AttributionChipData
import com.google.android.`as`.oss.dataattribution.proto.AttributionDialogData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.common.flogger.GoogleLogger
import com.google.common.flogger.StackSize
import dagger.hilt.android.AndroidEntryPoint

/** An activity to host UI components when a user long clicks Delegated UI. */
@AndroidEntryPoint(ComponentActivity::class)
class DataAttributionActivity : Hilt_DataAttributionActivity() {

  private val viewModel: DataAttributionActivityViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    try {
      val attributionDialogData =
        intent.getByteArrayExtra(EXTRA_ATTRIBUTION_DIALOG_DATA_PROTO)!!.let {
          AttributionDialogData.parseFrom(it)
        }
      val attributionChipData =
        intent.getByteArrayExtra(EXTRA_ATTRIBUTION_CHIP_DATA_PROTO)?.let {
          AttributionChipData.parseFrom(it)
        }
      val sourceDeepLinks =
        intent.getParcelableArrayExtra<PendingIntent>(
          EXTRA_ATTRIBUTION_SOURCE_DEEP_LINKS,
          PendingIntent::class.java,
        ) as Array<PendingIntent?>?

      val view = createView(attributionDialogData, attributionChipData, sourceDeepLinks)
      showDialog(view)
    } catch (e: Exception) {
      logger
        .atSevere()
        .withCause(e)
        .withStackTrace(StackSize.SMALL)
        .log("DataAttributionActivity.onCreate() failed. Finishing activity.")
      finish()
      return
    }
  }

  private fun createView(
    attributionDialogData: AttributionDialogData,
    attributionChipData: AttributionChipData?,
    sourceDeepLinks: Array<PendingIntent?>?,
  ) =
    ComposeView(this).apply {
      setContent {
        DataAttributionDialog(
          attributionDialogData = attributionDialogData,
          attributionChipData = attributionChipData,
          sourceDeepLinks = sourceDeepLinks,
          onDismissRequest = { finish() },
        )
      }
    }

  private fun showDialog(view: View) {
    val dialogGravity = viewModel.getDialogGravity(window)
    val dialog =
      // Use an AlertDialog to keep the keyboard visible when the activity is launched.
      MaterialAlertDialogBuilder(this, R.style.Theme_DataAttribution_Dialog)
        .apply {
          setView(view)
          setOnDismissListener { finish() }
          if (dialogGravity is DialogGravity.AboveIme) {
            setBackgroundInsetTop(0)
            setBackgroundInsetBottom(dialogGravity.backgroundInsetBottomPx)
          }
        }
        .create()
    val gravity = if (dialogGravity is DialogGravity.AboveIme) Gravity.BOTTOM else Gravity.CENTER
    dialog.window?.setGravity(gravity)
    dialog.show()
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
  }
}
