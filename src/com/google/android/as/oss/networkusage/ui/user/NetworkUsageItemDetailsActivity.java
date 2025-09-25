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

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.common.base.Preconditions;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/** The details page for an item in the network usage log. */
@AndroidEntryPoint(CollapsingToolbarBaseActivity.class)
public class NetworkUsageItemDetailsActivity extends Hilt_NetworkUsageItemDetailsActivity {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  static final String NETWORK_USAGE_ITEM_EXTRA_KEY = "NETWORK_USAGE_ITEM_EXTRA_KEY";

  @Inject NetworkUsageLogContentMap contentMap;
  @Inject PcsStatsLog pcsStatsLog;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);

    pcsStatsLog.logIntelligenceCountReported(
        // Network usage log item inspected.
        IntelligenceCountReported.newBuilder()
            .setCountMetricId(CountMetricId.PCS_NETWORK_USAGE_LOG_ITEM_INSPECTED)
            .build());
    logger.atInfo().log("Network usage log item inspected");

    NetworkUsageItemWrapper networkUsageItem =
        Preconditions.checkNotNull(getIntent().getExtras())
            .getParcelable(NETWORK_USAGE_ITEM_EXTRA_KEY);
    Preconditions.checkNotNull(networkUsageItem);

    setContentView(R.layout.network_usage_item_details_activity);
    setTitle(
        contentMap
            .getFeatureName(networkUsageItem.connectionDetails())
            .orElseThrow(
                () ->
                    MissingResourcesForEntityException.missingTitleFor(
                        networkUsageItem.connectionDetails())));

    NetworkUsageLogAdapter adapter = new NetworkUsageLogAdapter(contentMap);
    RecyclerView recyclerView = findViewById(R.id.network_usage_item_details_list);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    adapter.submitList(
        NetworkUsageItemUtils.createNetworkUsageItemInfo(
            this, contentMap, networkUsageItem, pcsStatsLog));
  }
}
