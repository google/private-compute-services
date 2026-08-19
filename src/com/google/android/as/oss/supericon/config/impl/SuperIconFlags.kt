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

import com.google.android.`as`.oss.common.config.FlagManager.BooleanFlag
import com.google.android.`as`.oss.common.config.FlagManager.IntegerFlag
import com.google.android.`as`.oss.common.config.FlagManager.StringFlag

/** Flag configurations. */
object SuperIconFlags {
  const val PREFIX = "PcsSuperIcon__"
  val ENABLE = BooleanFlag.create("${PREFIX}enable", false)
  val MAX_CONSENT_PROMPTS = IntegerFlag.create("${PREFIX}max_consent_prompts", 3)
  val REPROMPT_DURATION_DAYS = IntegerFlag.create("${PREFIX}reprompt_duration_days", 7)
  val ENABLE_SCREENSHOT = BooleanFlag.create("${PREFIX}enable_screenshot", false)
  val SCREENSHOT_TIMEOUT_MS = IntegerFlag.create("${PREFIX}screenshot_timeout_ms", 3000)
  val LEARN_MORE_URL = StringFlag.create("${PREFIX}learn_more_url", "")
}
