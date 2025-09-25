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

package com.google.android.as.oss.fl.fc.api;

import com.google.android.as.oss.fl.fc.api.IExampleStoreIterator;

/**
 * Called by AiAiFederatedDataService to return {@link IExampleStoreIterator}
 * implementation to PCS.
 *
 * NOTE: Not oneway since we control the implementation running in PCS,
 * and we can therefore ensure that it won't block any threads.
 */
interface IStartQueryCallback {
  /**
   * Method called to pass IExampleStoreIterator from ASI to PCS.
   */
  void onSuccess(in IExampleStoreIterator exampleStoreIterator);

  /**
   * Method called when query validation failed in ASI.
   */
  void onFailure();

   /**
    * Method called when query validation failed in ASI.
    */
    void onFailureWithError(in int statusCode);
}
