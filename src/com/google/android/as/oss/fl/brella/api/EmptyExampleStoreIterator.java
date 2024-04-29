/*
 * Copyright 2024 Google LLC
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

/**
 * Empty iterator to support code paths where no data should be returned (e.g. if the feature is
 * toggled off via a flag).
 */
public final class EmptyExampleStoreIterator implements ExampleStoreIterator {
  public static EmptyExampleStoreIterator create() {
    return new EmptyExampleStoreIterator();
  }

  @Override
  public void next(Callback callback) {
    callback.onIteratorNextSuccess(null, true, null);
  }

  @Override
  public void request(int numExamples) {}

  @Override
  public void close() {}

  private EmptyExampleStoreIterator() {}
}
