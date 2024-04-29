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

package com.google.android.as.oss.networkusage.ui.user;

import android.content.Context;
import androidx.annotation.Nullable;

/**
 * A wrapper for objects that make up the items list in the RecyclerView adapter. Items can be
 * either a NetworkUsageEntity, or an Instant representing a date.
 */
abstract class LogItemWrapper {

  /** Returns true if {@code other} represents the same item in the RecyclerView. */
  abstract boolean isSameItemAs(@Nullable LogItemWrapper other);

  /** Returns true if {@code other} contains the same content as shown in the RecyclerView. */
  abstract boolean hasSameContentAs(@Nullable LogItemWrapper other);

  /**
   * Returns the LogItemViewHolderFactory which binds an item in the RecyclerView to the
   * corresponding ViewHolder.
   */
  abstract LogItemViewHolderFactory getViewHolderFactory();

  /** Called when the bound View for this item is clicked. */
  void onItemClick(Context context) {}
}
