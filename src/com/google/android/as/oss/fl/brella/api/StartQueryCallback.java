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

import android.os.RemoteException;
import com.google.android.as.oss.fl.brella.api.proto.TrainingError;
import com.google.fcp.client.ExampleStoreIterator;
import com.google.fcp.client.ExampleStoreService.QueryCallback;
import com.google.common.flogger.GoogleLogger;

/**
 * A wrapper for {@link QueryCallback} to forward example from ASI to the federated compute
 * callback. {@link
 * com.google.android.apps.miphone.aiai.common.brella.service.AiAiFederatedDataService} will call
 * our methods here over IPC, which we then forward to {@link QueryCallback}.
 */
public class StartQueryCallback extends IStartQueryCallback.Stub {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final QueryCallback queryCallback;

  public StartQueryCallback(QueryCallback queryCallback) {
    this.queryCallback = queryCallback;
  }

  @Override
  public void onSuccess(IExampleStoreIterator exampleStoreIterator) {
    queryCallback.onStartQuerySuccess(new ExampleStoreIteratorImpl(exampleStoreIterator));
  }

  @Override
  public void onFailure() {
    queryCallback.onStartQueryFailure(
        TrainingError.TRAINING_ERROR_START_QUERY_FAILED_VALUE, "StartQuery failure reported");
  }

  @Override
  public void onFailureWithError(int statusCode) {
    queryCallback.onStartQueryFailure(statusCode, "StartQuery failure reported");
  }

  private static class ExampleStoreIteratorImpl implements ExampleStoreIterator {
    private final IExampleStoreIterator exampleStoreIterator;

    private ExampleStoreIteratorImpl(IExampleStoreIterator exampleStoreIterator) {
      this.exampleStoreIterator = exampleStoreIterator;
    }

    @Override
    public void next(Callback callback) {
      try {
        exampleStoreIterator.next(new ExampleStoreIteratorCallback(callback));
      } catch (RemoteException e) {
        // We don't expect any RemoteExceptions. If it does happen for some reason then all we can
        // do now is log the exception and swallow it.
        logger.atWarning().withCause(e).log("RemoteException thrown in passing iterator callback.");
      }
    }

    // TODO: Handle ExampleStoreIterator's request() and close() API in PCS.
    @Override
    public void request(int numExamples) {}

    // TODO: Handle ExampleStoreIterator's request() and close() API in PCS.
    @Override
    public void close() {}
  }
}
