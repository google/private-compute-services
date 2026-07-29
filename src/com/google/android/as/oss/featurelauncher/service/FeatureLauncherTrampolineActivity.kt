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

package com.google.android.`as`.oss.featurelauncher.service

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.drawable.toDrawable
import com.google.common.flogger.GoogleLogger

/**
 * A trampoline activity that launches a target intent and returns the result to the
 * FeatureLauncherServiceImpl.
 */
class FeatureLauncherTrampolineActivity : ComponentActivity() {

  private val launcher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      val requestId = intent.getStringExtra(EXTRA_REQUEST_ID)
      logger.atInfo().log("Received activity result for request: %s", requestId)
      if (requestId != null) {
        val deferred = FeatureLauncherServiceImpl.pendingRequests.remove(requestId)
        if (deferred != null) {
          deferred.complete(result)
        } else {
          logger.atWarning().log("No pending request found for ID: %s", requestId)
        }
      }
      finish()
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setupInvisibleWindow()

    val targetIntent = getTargetIntent()
    if (targetIntent != null) {
      launcher.launch(targetIntent)
    } else {
      logger.atSevere().log("Missing target intent in TrampolineActivity")
      finish()
    }
  }

  private fun setupInvisibleWindow() {
    // Set the background to transparent and remove the behind dim effect to make the activity
    // invisible.
    window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window.setLayout(1, 1)
  }

  private fun getTargetIntent(): Intent? {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
      intent.getParcelableExtra(EXTRA_TARGET_INTENT, Intent::class.java)
    } else {
      @Suppress("DEPRECATION") intent.getParcelableExtra(EXTRA_TARGET_INTENT)
    }
  }

  companion object {
    private val logger = GoogleLogger.forEnclosingClass()
    const val EXTRA_TARGET_INTENT = "extra_target_intent"
    const val EXTRA_REQUEST_ID = "extra_request_id"
  }
}
