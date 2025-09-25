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

package com.google.android.as.oss.common.config;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A simple thread-safe multicast listener helper.
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * interface FruitListener {
 *   void onBananas();
 * }
 * MulticastListenable<FruitListener> listener = MulticastListenable.create();
 * listener.add(() -> logger.atInfo().log("Bananas!"));
 * listener.notify(listener -> listener.onBananas());
 * }</pre>
 */
// Note: "L extends Object" (as opposite to "L extends @Nullable Object") is how we tell the
// nullness checker that L is not nullable.
// See [redacted]
@SuppressWarnings("ExtendsObject")
@ThreadSafe
public class MulticastListenable<L extends Object> implements Listenable<L> {
  private final CopyOnWriteArrayList<L> listeners;
  private final CopyOnWriteArrayList<WeakReference<L>> weakListeners;
  private final Executor notifyExecutor;

  /**
   * Returns an instance of {@link MulticastListenable}.
   *
   * @param notifyExecutor the executor to use for calling the listeners' callbacks during {@link
   *     #notify(Consumer)}.
   */
  // incompatible type argument for type parameter L of MulticastListenable.
  @SuppressWarnings("nullness:type.argument")
  public static <L> MulticastListenable<L> create(Executor notifyExecutor) {
    return new MulticastListenable<>(notifyExecutor);
  }

  /**
   * Returns an instance of {@link MulticastListenable} which calls the listener callbacks using a
   * direct executor.
   */
  // incompatible type argument for type parameter L of MulticastListenable.
  @SuppressWarnings("nullness:type.argument")
  public static <L> MulticastListenable<L> create() {
    return create(MoreExecutors.directExecutor());
  }

  @Override
  @CanIgnoreReturnValue
  public boolean addWeakListener(L listener) {
    return weakListeners.add(new WeakReference<>(listener));
  }

  @Override
  @CanIgnoreReturnValue
  public boolean addListener(L listener) {
    return listeners.add(listener);
  }

  @Override
  @CanIgnoreReturnValue
  public boolean removeListener(L listener) {
    if (listeners.remove(listener)) {
      return true;
    }

    boolean removed = false;
    for (int i = 0; i < weakListeners.size(); i++) {
      if (weakListeners.get(i).get() == listener) {
        weakListeners.remove(i);
        removed = true;
        break;
      }
    }
    return removed;
  }

  /** Removes all listeners that have been garbage collected. */
  private void cleanUp() {
    for (int i = weakListeners.size() - 1; i >= 0; i--) {
      if (weakListeners.get(i).get() == null) {
        weakListeners.remove(i);
      }
    }
  }

  public boolean isEmpty() {
    return listeners.isEmpty() && weakListeners.isEmpty();
  }

  /**
   * Executes the given function with every registered listener. The function can use the listener
   * to call the appropriate notification method in its interface.
   *
   * <p>StrongRef listeners are notified first in the order they were added. WeakRef listeners that
   * are not garbage collected will be notified after the StrongRef listeners.
   */
  @SuppressWarnings("nullness") // wrong nullness warning on ..function.accept(listener.get())
  public void notify(Consumer<L> function) {
    synchronized (listeners) {
      for (L listener : listeners) {
        notifyExecutor.execute(() -> function.accept(listener));
      }
    }
    synchronized (weakListeners) {
      for (WeakReference<L> listener : weakListeners) {
        if (listener.get() != null) {
          notifyExecutor.execute(() -> function.accept(listener.get()));
        }
      }
      cleanUp();
    }
  }

  protected MulticastListenable(Executor notifyExecutor) {
    listeners = new CopyOnWriteArrayList<>();
    weakListeners = new CopyOnWriteArrayList<>();
    this.notifyExecutor = notifyExecutor;
  }
}
