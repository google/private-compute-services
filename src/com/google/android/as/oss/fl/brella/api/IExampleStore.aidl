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

import com.google.android.as.oss.fl.brella.api.IStartQueryCallback;

 /**
  * The interface that the AiAiFederatedDataService returns in onBind().
  * This defines the IPC between PCS and ASI process to get training examples.
  *
  */
interface IExampleStore {

  /**
   * Version : 0
   * Delegates the {@link com.google.fcp.client.IExampleStore#startQuery}
   * call to AiAiFederatedDataService.
   *
   * NOTE: Oneway so that if ASI throws a RuntimeException we don't crash PCS.
   */
  oneway void startQuery(
    in String collection,
    in byte[] criteria,
    in byte[] resumptionToken,
    IStartQueryCallback startQueryCallback);

  /**
   * Version : 1
   * Fetches the current version supported by the IExampleStore in AiAiFederatedDataService.
   * NOTE: Default value  is false for case where this method does not exist in older versions.
   */
  boolean supportsSelectorContext();

  /**
   * Version : 1
   * Delegates the {@link com.google.fcp.client.IExampleStore#startQuery}
   * call to AiAiFederatedDataService's startQueryWithSelectorContext where it is supported.
   *
   * NOTE: Oneway so that if ASI throws a RuntimeException we don't crash PCS.
   */
  oneway void startQueryWithSelectorContext(
    in String collection,
    in byte[] criteria,
    in byte[] resumptionToken,
    IStartQueryCallback startQueryCallback,
    in byte[] selectorContext);
}
