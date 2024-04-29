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

package com.google.android.as.oss.fl.federatedcompute.statsd;

import com.google.fcp.client.ExampleStoreService.QueryCallback;
import com.google.intelligence.fcp.client.SelectorContext;

/**
 * Interface for exampleStore connectors that can handle queries from example store service in
 * special cases.
 */
public interface ExampleStoreConnector {
  void startQuery(
      String collection,
      byte[] criteria,
      byte[] resumptionToken,
      QueryCallback callback,
      SelectorContext selectorContext);
}
