/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.fl.brella.api;

import com.google.fcp.client.common.api.Status;
import com.google.fcp.client.ResultHandlingService.ResultHandlingCallback;

/**
 * A wrapper for {@link com.google.fcp.client.ResultHandlingService.ResultHandlingCallback} to
 * forward status from client to the federated compute callback. Client will call our methods here
 * over IPC, which we then forward to {@link
 * com.google.fcp.client.ResultHandlingService.ResultHandlingCallback}.
 */
public class StatusCallback extends IStatusCallback.Stub {
  private final ResultHandlingCallback resultHandlingCallback;

  public StatusCallback(ResultHandlingCallback resultHandlingCallback) {
    this.resultHandlingCallback = resultHandlingCallback;
  }

  @Override
  public void onResult(Status result) {
    resultHandlingCallback.onResult(result);
  }
}
