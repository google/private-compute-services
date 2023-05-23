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

package com.google.android.as.oss.networkusage.ui.user;

import android.view.ViewGroup;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.common.collect.ImmutableList;

/** Maps items in the RecyclerView with the correct ViewHolder. */
abstract class LogItemViewHolderFactory {
  private static final int USAGE_ITEM_VIEW_TYPE = 1;
  private static final int DATE_DIVIDER_VIEW_TYPE = 2;
  private static final int SUMMARY_VIEW_TYPE = 3;
  private static final int USAGE_ITEM_INFO_VIEW_TYPE = 4;

  static final LogItemViewHolderFactory USAGE_ITEM_VIEW_HOLDER_FACTORY =
      new LogItemViewHolderFactory() {
        @Override
        LogItemViewHolder createViewHolder(ViewGroup parent, NetworkUsageLogContentMap contentMap) {
          return NetworkUsageItemViewHolder.create(parent, contentMap);
        }

        @Override
        int getViewType() {
          return USAGE_ITEM_VIEW_TYPE;
        }
      };
  static final LogItemViewHolderFactory DATE_DIVIDER_VIEW_HOLDER_FACTORY =
      new LogItemViewHolderFactory() {
        @Override
        LogItemViewHolder createViewHolder(ViewGroup parent, NetworkUsageLogContentMap unused) {
          return DateTimeDividerViewHolder.create(parent);
        }

        @Override
        int getViewType() {
          return DATE_DIVIDER_VIEW_TYPE;
        }
      };
  static final LogItemViewHolderFactory SUMMARY_VIEW_HOLDER_FACTORY =
      new LogItemViewHolderFactory() {
        @Override
        LogItemViewHolder createViewHolder(ViewGroup parent, NetworkUsageLogContentMap unused) {
          return SummaryViewHolder.create(parent);
        }

        @Override
        int getViewType() {
          return SUMMARY_VIEW_TYPE;
        }
      };
  static final LogItemViewHolderFactory USAGE_ITEM_INFO_VIEW_HOLDER_FACTORY =
      new LogItemViewHolderFactory() {
        @Override
        LogItemViewHolder createViewHolder(ViewGroup parent, NetworkUsageLogContentMap unused) {
          return NetworkUsageItemInfoViewHolder.create(parent);
        }

        @Override
        int getViewType() {
          return USAGE_ITEM_INFO_VIEW_TYPE;
        }
      };
  static final ImmutableList<LogItemViewHolderFactory> viewHolderFactories =
      ImmutableList.of(
          USAGE_ITEM_VIEW_HOLDER_FACTORY,
          DATE_DIVIDER_VIEW_HOLDER_FACTORY,
          SUMMARY_VIEW_HOLDER_FACTORY,
          USAGE_ITEM_INFO_VIEW_HOLDER_FACTORY);

  static LogItemViewHolderFactory ofViewType(int viewTypeId) {
    for (LogItemViewHolderFactory viewHolderFactory : viewHolderFactories) {
      if (viewHolderFactory.getViewType() == viewTypeId) {
        return viewHolderFactory;
      }
    }
    throw new AssertionError(String.format("Unknown view type %d", viewTypeId));
  }

  /** Returns an instance of the ViewHolder of given viewType. */
  abstract LogItemViewHolder createViewHolder(
      ViewGroup parent, NetworkUsageLogContentMap contentMap);

  /** Returns a unique identifier for the view type. */
  abstract int getViewType();
}
