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

package com.google.android.as.oss.conversationid.service.aidl;

// The service to update conversation id. It's used by Android intelligence platform.
// Android intelligence platform will provide the hash salt as well.
// Pcs will use the hash salt to generate the anonymous id.
interface IConversationIdUpdateService  {
  void enterConversation(in String conversationId, in String hashSalt) = 0;
  void exitConversation() = 1;
};
