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
import com.google.fcp.client.common.api.CommonStatusCodes;
import com.google.fcp.client.ExampleStoreIterator;
import com.google.common.flogger.GoogleLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A wrapper for {@link ExampleStoreIterator}. {@link
 * com.google.android.as.oss.fl.brella.service.AstreaExampleStoreService} will call the methods here
 * over IPC, which is then forward to {@link ExampleStoreIterator} implementation in ASI.
 */
public class ExampleStoreIteratorImpl extends IExampleStoreIterator.Stub {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final ExampleStoreIterator iterator;

  public ExampleStoreIteratorImpl(ExampleStoreIterator iterator) {
    this.iterator = iterator;
  }

  @Override
  public void next(IExampleStoreIteratorCallback callback) {
    try {
      iterator.next(new IteratorCallback(callback));
    } catch (RuntimeException e) {
      // If iterator.next() throws an unexpected exception in ASI, report the failure to PCS so
      // that the training in PCS doesn't hang until timeout.
      // Re-throw the exception in ASI so that we have visibility in ASI crash listnr for
      // unexpected exceptions.
      try {
        callback.onIteratorNextFailure(CommonStatusCodes.ERROR, "iterator.next() failed.");
      } catch (RemoteException remoteException) {
        logger.atWarning().withCause(remoteException).log(
            "RemoteException thrown while reporting callback.onIteratorNextFailure.");
      }
      throw e;
    }
  }

  private static class IteratorCallback implements ExampleStoreIterator.Callback {
    private final IExampleStoreIteratorCallback iExampleStoreIteratorCallback;

    IteratorCallback(IExampleStoreIteratorCallback iExampleStoreIteratorCallback) {
      this.iExampleStoreIteratorCallback = iExampleStoreIteratorCallback;
    }

    @Override
    public boolean onIteratorNextSuccess(
        byte @Nullable [] resultBytes, boolean isTfExample, byte @Nullable [] resumptionToken) {
      try {
        iExampleStoreIteratorCallback.onIteratorNextSuccess(resultBytes, resumptionToken);
      } catch (RemoteException e) {
        // We don't expect any RemoteExceptions. If it does happen for some reason then all we can
        // do now is log the exception and swallow it.
        logger.atWarning().withCause(e).log("RemoteException thrown in onIteratorNextSuccess.");
      }
      return true;
    }

    @Override
    public void onIteratorNextFailure(int statusCode, @Nullable String errorMessage) {
      try {
        iExampleStoreIteratorCallback.onIteratorNextFailure(statusCode, errorMessage);
      } catch (RemoteException e) {
        // We don't expect any RemoteExceptions. If it does happen for some reason then all we can
        // do now is log the exception and swallow it.
        logger.atWarning().withCause(e).log("RemoteException thrown in onIteratorNextFailure.");
      }
    }
  }
}
