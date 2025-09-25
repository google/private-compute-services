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

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import java.util.Optional;

/** ViewHolder for a list item in the RecyclerView holding the Network Usage Log. */
public class NetworkUsageItemViewHolder extends LogItemViewHolder {
  private final NetworkUsageLogContentMap contentMap;
  private final TextView mechanismNameTv;
  private final TextView featureNameTv;
  private final TextView descriptionTv;

  public static NetworkUsageItemViewHolder create(
      ViewGroup parent, NetworkUsageLogContentMap contentMap) {
    View itemView =
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.network_usage_item, parent, /* attachToRoot= */ false);
    return new NetworkUsageItemViewHolder(itemView, contentMap);
  }

  @SuppressLint("DefaultLocale")
  @Override
  public void bind(LogItemWrapper item) {
    NetworkUsageItemWrapper networkUsageItem = (NetworkUsageItemWrapper) item;
    ConnectionDetails connectionDetails = networkUsageItem.connectionDetails();
    Optional<String> featureName = contentMap.getFeatureName(connectionDetails);
    Optional<String> description = contentMap.getDescription(connectionDetails);

    featureNameTv.setText(
        featureName.orElseThrow(
            () -> MissingResourcesForEntityException.missingTitleFor(connectionDetails)));
    descriptionTv.setText(
        description.orElseThrow(
            () -> MissingResourcesForEntityException.missingDescriptionFor(connectionDetails)));

    String creationTime =
        NetworkUsageItemUtils.getFormattedTime(networkUsageItem.latestCreationTime());
    String mechanismName = contentMap.getMechanismName(connectionDetails);

    mechanismNameTv.setText(String.format("%s \u2022 %s", creationTime, mechanismName));
  }

  private NetworkUsageItemViewHolder(View itemView, NetworkUsageLogContentMap contentMap) {
    super(itemView);
    this.contentMap = contentMap;
    mechanismNameTv = itemView.findViewById(R.id.usage_item_mechanism_name);
    featureNameTv = itemView.findViewById(R.id.usage_item_feature_name);
    descriptionTv = itemView.findViewById(R.id.usage_item_description);
  }
}
