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

import com.google.android.as.oss.supericon.aidl.IConversationContentCallback;

/**
 * Service interface for requesting conversation content.
 */
interface IConversationContentService {
  /**
   * Requests the conversation content.
   *
   * @param conversationContentCallback The callback to receive the conversation content.
   */
  void requestConversationContent(in IConversationContentCallback conversationContentCallback) = 0;

  /**
   * Requests the conversation content (V2, asynchronous screenshot capture flow).
   *
   * @param conversationContentCallback The callback to receive the conversation content and asynchronous screenshot.
   * @param packageName The package name of the app that has the conversation content.
   * @param requestScreenshot True to trigger asynchronous screenshot capture alongside conversation messages.
   * @return True if requestConversationContentV2 is supported by the server, False otherwise (allowing synchronous fallback).
   */
  boolean requestConversationContentV2(in IConversationContentCallback conversationContentCallback, in String packageName, boolean requestScreenshot) = 1;
};
