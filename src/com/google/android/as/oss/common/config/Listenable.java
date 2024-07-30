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

package com.google.android.as.oss.common.config;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Implemented by classes that can be listened to.
 *
 * <p>There is no explicit contract on which thread the implementor should call the listener
 * callbacks.
 *
 * @param <L> type of the listener.
 */
public interface Listenable<L> {
  /**
   * Adds a listener to the listener list. Note this does not check that the listener already does
   * not already exist in the list so calling it twice with the same listener will add two
   * references of it, and it will get notified twice.
   *
   * @return true if the listener was successfully added.
   */
  @CanIgnoreReturnValue
  boolean addListener(L listener);

  /**
   * Variant of {@link #addListener} that adds a listener to the weakListener list. This listener
   * will be garbage collected if it is no longer referenced by any other code.
   *
   * @return true if the listener was successfully added.
   */
  @CanIgnoreReturnValue
  default boolean addWeakListener(L listener) {
    return false;
  }

  /**
   * Removes a listener from the listener list, if exists in the list.
   *
   * @return true if the listener was removed, false if it wasn't, because it was not in the list.
   */
  @CanIgnoreReturnValue
  boolean removeListener(L listener);
}
