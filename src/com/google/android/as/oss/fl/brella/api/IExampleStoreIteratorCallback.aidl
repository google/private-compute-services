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

import com.google.android.as.oss.fl.brella.api.IExampleStoreIteratorCallback;
import javax.annotation.Nullable;

/**
 * Defines the callback to pass a training example from ASI to PCS.
 * NOTE: Not oneway since we control the implementation running in the
 * PCS, and we can therefore ensure that it won't block
 * any threads.
 */
interface IExampleStoreIteratorCallback {
  /**
   * Method called to pass a training example from ASI to PCS.
   */
  void onIteratorNextSuccess(
    @Nullable in byte [] resultBytes,
    @Nullable in byte [] resumptionToken);

  /**
   * Method called when the training example could not be obtained.
   */
  void onIteratorNextFailure(in int statusCode, @Nullable in String errorMessage);
}
