/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.fl.localcompute.impl;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.fl.brella.api.proto.TrainingError;
import com.google.android.as.oss.fl.localcompute.FileCopyStartQuery;
import com.google.android.as.oss.fl.localcompute.LocalComputeResourceManager;
import com.google.fcp.client.ExampleStoreIterator;
import com.google.fcp.client.ExampleStoreService.QueryCallback;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.intelligence.fcp.client.SelectorContext;
import com.google.protobuf.ByteString;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Example;
import org.tensorflow.example.Feature;
import org.tensorflow.example.Features;

/** A Singleton implementation of handling the filecopy ExampleStore startQuery call */
@Singleton
class FileCopyStartQueryImpl implements FileCopyStartQuery {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @VisibleForTesting
  static final String ABSOLUTE_PATH_TF_EXAMPLE_KEY = "copied_input_resource_path";

  private final Executor executor;
  private final LocalComputeResourceManager resourceManager;

  @Inject
  FileCopyStartQueryImpl(
      @IoExecutorQualifier Executor executor, LocalComputeResourceManager resourceManager) {
    this.executor = executor;
    this.resourceManager = resourceManager;
  }

  /**
   * A dummy startQuery that actually copies the input resource from ASI by providing collection as
   * a string key.
   */
  @Override
  public void startQuery(
      String collection,
      byte[] criteria,
      byte[] resumptionToken,
      QueryCallback callback,
      SelectorContext selectorContext) {
    String sessionName = selectorContext.getComputationProperties().getSessionName();

    Futures.addCallback(
        resourceManager.copyResourceAtTraining(sessionName, collection),
        new FutureCallback<String>() {
          @Override
          public void onSuccess(String absolutePath) {
            if (!absolutePath.isEmpty()) {
              ExampleStoreIterator iterator = createAbsolutePathExampleStoreIterator(absolutePath);
              callback.onStartQuerySuccess(iterator);
            } else {
              logger.atWarning().log("Failed to copy the input resource.");
              callback.onStartQueryFailure(
                  TrainingError.TRAINING_ERROR_PCC_COPY_LOCAL_COMPUTE_RESOURCE_FAILED_VALUE,
                  "Failed to copy the input resource.");
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().log("Failed to copy the input resource.");
            callback.onStartQueryFailure(
                TrainingError.TRAINING_ERROR_PCC_COPY_LOCAL_COMPUTE_RESOURCE_FAILED_VALUE,
                t.getMessage());
          }
        },
        executor);
  }

  private ExampleStoreIterator createAbsolutePathExampleStoreIterator(String absolutePath) {
    return new ExampleStoreIterator() {
      private boolean hasNext = true;

      @Override
      public void next(Callback callback) {
        if (hasNext) {
          callback.onIteratorNextSuccess(
              absolutePathToExample(absolutePath).toByteArray(), true, null);
          hasNext = false;
        } else {
          callback.onIteratorNextSuccess(null, true, null);
        }
      }

      @Override
      public void request(int numExamples) {}

      @Override
      public void close() {}
    };
  }

  @VisibleForTesting
  Example absolutePathToExample(String absolutePath) {
    Feature.Builder pathFeature = Feature.newBuilder();
    pathFeature.setBytesList(
        BytesList.newBuilder().addValue(ByteString.copyFromUtf8(absolutePath)).build());
    Features.Builder features = Features.newBuilder();
    features.putFeature(ABSOLUTE_PATH_TF_EXAMPLE_KEY, pathFeature.build());
    return Example.newBuilder().setFeatures(features).build();
  }
}
