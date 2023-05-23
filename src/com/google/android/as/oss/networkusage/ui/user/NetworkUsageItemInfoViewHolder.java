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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** ViewHolder for {@link NetworkUsageItemInfo} items in the RecyclerView. */
class NetworkUsageItemInfoViewHolder extends LogItemViewHolder {
  private final TextView titleTv;
  private final TextView bodyTv;

  static NetworkUsageItemInfoViewHolder create(ViewGroup parent) {
    View networkUsageItemInfoView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.network_usage_item_info, parent, false);
    return new NetworkUsageItemInfoViewHolder(networkUsageItemInfoView);
  }

  @Override
  void bind(LogItemWrapper item) {
    NetworkUsageItemInfo info = (NetworkUsageItemInfo) item;
    titleTv.setText(info.getTitle());
    bodyTv.setText(info.getBody());
  }

  public NetworkUsageItemInfoViewHolder(View networkUsageItemInfoView) {
    super(networkUsageItemInfoView);
    this.titleTv = networkUsageItemInfoView.findViewById(R.id.network_usage_item_info_title);
    this.bodyTv = networkUsageItemInfoView.findViewById(R.id.network_usage_item_info_body);
  }
}
