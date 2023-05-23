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

import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;

/** Adapter for the list items in the RecyclerViews of the NetworkUsageLog. */
public class NetworkUsageLogAdapter extends ListAdapter<LogItemWrapper, LogItemViewHolder> {

  private final NetworkUsageLogContentMap contentMap;

  public NetworkUsageLogAdapter(NetworkUsageLogContentMap contentMap) {
    super(new ItemWrapperDiffCallback());
    this.contentMap = contentMap;
  }

  @Override
  public int getItemViewType(int position) {
    return getItem(position).getViewHolderFactory().getViewType();
  }

  @Override
  public LogItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LogItemViewHolder viewHolder =
        LogItemViewHolderFactory.ofViewType(viewType).createViewHolder(parent, contentMap);
    viewHolder.itemView.setOnClickListener(
        v -> {
          if (viewHolder.getBindingAdapterPosition() != RecyclerView.NO_POSITION) {
            getItem(viewHolder.getBindingAdapterPosition()).onItemClick(parent.getContext());
          }
        });
    return viewHolder;
  }

  @Override
  public void onBindViewHolder(LogItemViewHolder viewHolder, int position) {
    viewHolder.bind(getItem(position));
  }

  /**
   * ItemCallback that checks whether an item's content has changed. This is used whenever we call
   * submitList(), in order to update item views when necessary.
   */
  static class ItemWrapperDiffCallback extends DiffUtil.ItemCallback<LogItemWrapper> {
    // Checks whether the two objects represent the same object.
    @Override
    public boolean areItemsTheSame(
        @NonNull LogItemWrapper oldItem, @NonNull LogItemWrapper newItem) {
      return newItem.isSameItemAs(oldItem);
    }

    // Checks whether the two objects contain the same data; i.e. display the same content.
    // Only called when areItemsTheSame() returns true.
    @Override
    public boolean areContentsTheSame(
        @NonNull LogItemWrapper oldItem, @NonNull LogItemWrapper newItem) {
      return newItem.hasSameContentAs(oldItem);
    }
  }
}
