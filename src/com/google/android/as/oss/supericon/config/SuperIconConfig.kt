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

package com.google.android.`as`.oss.supericon.config

/** Configuration for the supericon feature. */
data class SuperIconConfig(
  /** True if the SuperIcon feature is enabled. */
  val enableSuperIcon: Boolean,
  /** The maximum number of times to show the consent prompt before permanently disabling it. */
  val maxConsentPrompts: Int,
  /** The duration to wait before reprompting the user for consent after a denial. */
  val repromptDuration: kotlin.time.Duration,
  /** True if screenshot capture is enabled. */
  val enableScreenshot: Boolean,
  /** The timeout for asynchronous screenshot capture in milliseconds. */
  val screenshotTimeoutMs: Long = 3000L,
  /** The URL to open when the "Learn more" link is clicked. */
  val learnMoreUrl: String = "",
)
