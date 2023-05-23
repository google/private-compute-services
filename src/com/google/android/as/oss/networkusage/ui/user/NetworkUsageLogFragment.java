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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.user.EntityListViewModel.EntityListViewModelFactory;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.AndroidEntryPoint;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;

/** The fragment containing the list of downloads/uploads in the Network Usage Log. */
@AndroidEntryPoint(Fragment.class)
public class NetworkUsageLogFragment extends Hilt_NetworkUsageLogFragment
    implements SwipeRefreshLayout.OnRefreshListener {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private final NoItemInspectedCallback callback = new NoItemInspectedCallback();

  @Inject NetworkUsageLogRepository repository;
  @Inject NetworkUsageLogContentMap contentMap;
  @Inject ImmutableList<EntityListProcessor> entityListProcessors;

  private NetworkUsageLogAdapter adapter;
  private EntityListViewModel viewModel;
  private SwipeRefreshLayout swipeRefreshContainer;

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    adapter = new NetworkUsageLogAdapter(contentMap);
    viewModel =
        new ViewModelProvider(this, new EntityListViewModelFactory(repository))
            .get(EntityListViewModel.class);

    View rootView = layoutInflater.inflate(R.layout.network_usage_log_fragment, viewGroup, false);

    swipeRefreshContainer = rootView.findViewById(R.id.log_swipe_refresh_container);
    swipeRefreshContainer.setOnRefreshListener(this);

    RecyclerView recyclerView = rootView.findViewById(R.id.log_recycler_view);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

    viewModel.getEntityListLiveData().observe(this, this::initRecyclerViewIfRequired);

    return rootView;
  }

  @Override
  public void onRefresh() {
    if (viewModel.getEntityListLiveData().getValue() == null) {
      return;
    }
    logger.atFine().log("Refreshing entity list");
    reloadList(viewModel.getEntityListLiveData().getValue());
  }

  @Override
  public void onDestroy() {
    if (callback.getItemInspectionCounter() == 0) {
      logger.atInfo().log("No log item inspected.");
    }
    super.onDestroy();
  }

  private void initRecyclerViewIfRequired(List<NetworkUsageEntity> entityList) {
    if (!adapter.getCurrentList().isEmpty() || entityList == null) {
      return;
    }
    logger.atFine().log("Initializing entity list");
    reloadList(entityList);
    // The initial submitList() runs on the main thread, so setRefreshing(false) is required here
    swipeRefreshContainer.setRefreshing(false);
  }

  private void reloadList(@Nullable List<NetworkUsageEntity> entityList) {
    if (entityList == null) {
      return;
    }
    swipeRefreshContainer.setRefreshing(true);
    ImmutableList<LogItemWrapper> items =
        NetworkUsageItemUtils.processEntityList(
            ImmutableList.copyOf(entityList), entityListProcessors, callback);
    adapter.submitList(items, () -> swipeRefreshContainer.setRefreshing(false));
  }

  static class NoItemInspectedCallback implements NetworkUsageItemOnClickCallback {
    private final AtomicInteger itemInspectionCounter = new AtomicInteger(0);

    @Override
    public void onItemInspectionCall() {
      itemInspectionCounter.incrementAndGet();
    }

    public int getItemInspectionCounter() {
      return itemInspectionCounter.get();
    }
  }
}
