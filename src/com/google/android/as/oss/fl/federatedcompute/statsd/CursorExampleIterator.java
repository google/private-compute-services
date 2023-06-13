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

package com.google.android.as.oss.fl.federatedcompute.statsd;

import android.database.Cursor;
import androidx.annotation.NonNull;
import com.google.android.as.oss.fl.federatedcompute.statsd.examplegenerator.DefaultExamplesGenerator;
import com.google.android.as.oss.fl.federatedcompute.statsd.examplegenerator.ExamplesGenerator;
import com.google.fcp.client.ExampleStoreIterator;

/** Wraps an example iterator over a generic cursor type. */
public class CursorExampleIterator implements ExampleStoreIterator {
  private final ExamplesGenerator generator;
  private final Cursor cursor;

  public CursorExampleIterator(Cursor cursor) {
    this.cursor = cursor;
    this.generator = new DefaultExamplesGenerator(cursor);
  }

  @Override
  public void next(@NonNull Callback callback) {
    try {
      if (generator.hasNext()) {
        callback.onIteratorNextSuccess(generator.next().toByteArray(), true, null);
      } else {
        callback.onIteratorNextSuccess(null, false, null);
      }
    } catch (RuntimeException e) {
      cursor.close();
      callback.onIteratorNextFailure(
          1, "Failed to generate next example from cursor: " + e.getLocalizedMessage());
    }
  }

  @Override
  public void request(int numExamples) {}

  @Override
  public void close() {
    this.cursor.close();
  }
}
