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

/** ViewHolder for {@link DateTimeDividerItemWrapper} items in the RecyclerView. */
public class DateTimeDividerViewHolder extends LogItemViewHolder {

  private final TextView groupDateTv;

  static DateTimeDividerViewHolder create(ViewGroup parent) {
    View dateView =
        LayoutInflater.from(parent.getContext()).inflate(R.layout.date_divider, parent, false);
    return new DateTimeDividerViewHolder(dateView);
  }

  @Override
  public void bind(LogItemWrapper item) {
    String formattedDateTime = ((DateTimeDividerItemWrapper) item).getFormattedDateTime();
    groupDateTv.setText(formattedDateTime);
  }

  private DateTimeDividerViewHolder(View itemView) {
    super(itemView);
    groupDateTv = itemView.findViewById(R.id.date_time_divider);
  }
}
