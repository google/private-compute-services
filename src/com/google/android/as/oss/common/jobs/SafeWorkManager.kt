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

package com.google.android.`as`.oss.common.jobs

import android.annotation.SuppressLint
import android.content.Context
import android.provider.DeviceConfig
import androidx.work.WorkManager
import com.google.android.libraries.directboot.DirectBootUtils
import com.google.common.flogger.GoogleLogger
import java.util.function.Consumer

/**
 * Direct-Boot-safe runner for [WorkManager] operations in Pcs (Private Compute Services) and
 * Android System Intelligence.
 *
 * If the user's credential-encrypted (CE) storage is locked (Direct Boot state) and the feature
 * flag is enabled, execution is deferred until the device is unlocked via
 * [DirectBootUtils.runWhenUnlocked].
 */
object SafeWorkManager {
  private val logger = GoogleLogger.forEnclosingClass()
  private const val NAMESPACE = "device_personalization_services"
  private const val FLAG_ENABLE_SAFE_WORK_MANAGER = "PccAiaiSecurity__enable_safe_work_manager"

  /** Returns whether the SafeWorkManager feature flag is enabled via DeviceConfig. */
  @SuppressLint("NewApi")
  @JvmStatic
  fun isFlagEnabled(context: Context): Boolean {
    return try {
      DeviceConfig.getBoolean(NAMESPACE, FLAG_ENABLE_SAFE_WORK_MANAGER, false)
    } catch (t: Throwable) {
      logger.atFine().withCause(t).log("Failed to read SafeWorkManager flag from DeviceConfig")
      false
    }
  }

  /** Returns true if credential storage is unlocked and WorkManager is safe to access. */
  @JvmStatic fun isUnlocked(context: Context): Boolean = DirectBootUtils.isUserUnlocked(context)

  /**
   * Executes [action] safely. When [FLAG_ENABLE_SAFE_WORK_MANAGER] is enabled and device is locked,
   * defers execution until user unlock via [DirectBootUtils.runWhenUnlocked].
   */
  @JvmSynthetic
  fun runWhenUnlocked(context: Context, action: (WorkManager) -> Unit) {
    if (!isFlagEnabled(context)) {
      action(WorkManager.getInstance(context))
      return
    }

    if (isUnlocked(context)) {
      try {
        action(WorkManager.getInstance(context))
      } catch (t: Throwable) {
        logger.atFine().withCause(t).log("Failed to execute WorkManager action")
      }
    } else {
      logger.atFine().log("Device is in Direct Boot. Deferring WorkManager action until unlocked.")
      DirectBootUtils.runWhenUnlocked(context) {
        try {
          action(WorkManager.getInstance(context))
        } catch (t: Throwable) {
          logger.atFine().withCause(t).log("Failed to execute deferred WorkManager action")
        }
      }
    }
  }

  /** Java-compatible overload accepting a [Consumer]. */
  @JvmStatic
  fun runWhenUnlocked(context: Context, action: Consumer<WorkManager>) {
    runWhenUnlocked(context) { workManager -> action.accept(workManager) }
  }
}
