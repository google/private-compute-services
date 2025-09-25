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

package com.google.android.as.oss.networkusage.ui.user;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/** ViewHolder for displaying the summary banner in the RecyclerView. */
public class SummaryViewHolder extends LogItemViewHolder {

  private final TextView totalUpdatesTv;
  private final TextView totalUploadSizeTv;
  private final TextView totalDownloadSizeTv;

  static SummaryViewHolder create(ViewGroup parent) {
    View summaryHeaderView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.summary_banner, parent, false);
    return new SummaryViewHolder(summaryHeaderView);
  }

  @Override
  public void bind(LogItemWrapper item) {
    NetworkUsageSummary networkUsageSummary = ((SummaryWrapper) item).getNetworkUsageSummary();
    totalUpdatesTv.setText(
        itemView
            .getContext()
            .getResources()
            .getQuantityString(
                R.plurals.summary_banner_updates,
                networkUsageSummary.updatesCount(),
                networkUsageSummary.updatesCount()));
    totalUploadSizeTv.setText(
        Formatter.formatShortFileSize(itemView.getContext(), networkUsageSummary.totalUpload()));
    totalDownloadSizeTv.setText(
        Formatter.formatShortFileSize(itemView.getContext(), networkUsageSummary.totalDownload()));
  }

  private SummaryViewHolder(View itemView) {
    super(itemView);
    totalUpdatesTv = itemView.findViewById(R.id.total_updates);
    totalUploadSizeTv = itemView.findViewById(R.id.total_upload_size);
    totalDownloadSizeTv = itemView.findViewById(R.id.total_download_size);
  }
}
