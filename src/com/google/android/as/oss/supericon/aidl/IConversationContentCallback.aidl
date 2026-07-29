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

package com.google.android.as.oss.supericon.aidl;

import android.graphics.Bitmap;
import com.google.android.as.oss.supericon.aidl.ConversationData;
import com.google.android.as.oss.supericon.utils.SuperIconErrorCodes;

/**
 * Callback interface for receiving conversation content.
 */
interface IConversationContentCallback {
  /**
   * Called when the conversation content is available.
   *
   * @param conversationData The conversation data.
   */
  void onResponse(in ConversationData conversationData) = 0;

  /**
   * Called when an error occurs while fetching conversation content.
   */
  void onError(@SuperIconErrorCodes int errorCode, String errorMessage) = 1;

  /**
   * Called when the screenshot is available.
   *
   * @param screenshot The screenshot bitmap.
   */
  void onScreenshotResponse(in Bitmap screenshot) = 2;
}
