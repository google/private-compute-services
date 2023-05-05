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

import com.google.fcp.client.ExampleStoreIterator;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A wrapper for {@link ExampleStoreIterator.Callback} to forward example from ASI to the federated
 * compute callback. {@link
 * com.google.android.apps.miphone.aiai.common.brella.service.AiAiFederatedDataService} will call
 * our methods here over IPC, which we then forward to {@link ExampleStoreIterator.Callback}.
 */
class ExampleStoreIteratorCallback extends IExampleStoreIteratorCallback.Stub {
  private final ExampleStoreIterator.Callback callback;

  public ExampleStoreIteratorCallback(ExampleStoreIterator.Callback callback) {
    this.callback = callback;
  }

  @Override
  public void onIteratorNextSuccess(
      byte @Nullable [] resultBytes, byte @Nullable [] resumptionToken) {
    callback.onIteratorNextSuccess(resultBytes, false, resumptionToken);
  }

  @Override
  public void onIteratorNextFailure(int statusCode, @Nullable String errorMessage) {
    callback.onIteratorNextFailure(statusCode, errorMessage);
  }
}
