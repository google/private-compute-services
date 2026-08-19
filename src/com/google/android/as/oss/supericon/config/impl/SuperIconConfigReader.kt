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

package com.google.android.`as`.oss.supericon.config.impl

import com.google.android.`as`.oss.common.config.AbstractConfigReader
import com.google.android.`as`.oss.common.config.FlagListener
import com.google.android.`as`.oss.common.config.FlagManager
import com.google.android.`as`.oss.supericon.config.SuperIconConfig
import kotlin.time.Duration.Companion.days

/** ConfigReader for [SuperIconConfig]. */
class SuperIconConfigReader(private val flagManager: FlagManager) :
  AbstractConfigReader<SuperIconConfig>() {
  init {
    flagManager
      .listenable()
      .addListener(
        FlagListener {
          if (FlagListener.anyHasPrefix(it, SuperIconFlags.PREFIX)) {
            refreshConfig()
          }
        }
      )
  }

  override fun computeConfig() =
    SuperIconConfig(
      enableSuperIcon = flagManager.get(SuperIconFlags.ENABLE),
      maxConsentPrompts = flagManager.get(SuperIconFlags.MAX_CONSENT_PROMPTS),
      repromptDuration = flagManager.get(SuperIconFlags.REPROMPT_DURATION_DAYS).days,
      enableScreenshot = flagManager.get(SuperIconFlags.ENABLE_SCREENSHOT),
      screenshotTimeoutMs = flagManager.get(SuperIconFlags.SCREENSHOT_TIMEOUT_MS).toLong(),
      learnMoreUrl = flagManager.get(SuperIconFlags.LEARN_MORE_URL),
    )
}
