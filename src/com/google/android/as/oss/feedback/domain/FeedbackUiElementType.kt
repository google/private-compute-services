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

package com.google.android.`as`.oss.feedback.domain

/** Element type for UI elements that are logged in PSI. */
enum class FeedbackUiElementType(val id: Int) {
  FEEDBACK_SCREEN(33),
  FEEDBACK_THUMBS_UP_BUTTON(34),
  FEEDBACK_THUMBS_DOWN_BUTTON(35),
  FEEDBACK_CONSENT_CHECKBOX(36),
  FEEDBACK_REASON_CHIP(37),
  FEEDBACK_SUBMIT_BUTTON(38),
  FEEDBACK_VIEW_ALL_DATA_BUTTON(39),
  FEEDBACK_VIEW_ALL_DATA_SCREEN(40),
}
