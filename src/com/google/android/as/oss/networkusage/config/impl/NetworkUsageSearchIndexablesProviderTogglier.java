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

package com.google.android.as.oss.networkusage.config.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.initializer.PcsInitializer;
import com.google.android.as.oss.networkusage.config.NetworkUsageLogConfig;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;

/** Enables or disables the NetworkUsageSearchIndexablesProvider based on a flag. */
public final class NetworkUsageSearchIndexablesProviderTogglier implements PcsInitializer {
  private static final String TAG = "NetworkUsageSearchIndexablesProviderTogglier";

  private final Context context;
  private final ConfigReader<NetworkUsageLogConfig> configReader;

  @Inject
  NetworkUsageSearchIndexablesProviderTogglier(
      @ApplicationContext Context context, ConfigReader<NetworkUsageLogConfig> configReader) {
    this.context = context;
    this.configReader = configReader;
  }

  @Override
  public void run() {
    toggle(configReader.getConfig().networkUsageSearchIndexablesProviderEnabled());
    configReader.addListener((config, previous) -> {
      toggle(config.networkUsageSearchIndexablesProviderEnabled());
    });
  }

  private void toggle(boolean enabled) {
    ComponentName componentName =
        new ComponentName(
            context,
            "com.google.android.as.oss.networkusage.ui.user.NetworkUsageSearchIndexablesProvider");
    int newState =
        enabled
            ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

    try {
      context
          .getPackageManager()
          .setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
    } catch (IllegalArgumentException e) {
      Log.w(TAG, "Component not found, skipping toggle: " + componentName, e);
    }
  }
}
