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
import android.support.v4.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity;
import com.google.common.flogger.GoogleLogger;
import dagger.hilt.android.AndroidEntryPoint;
import javax.inject.Inject;
import org.checkerframework.checker.nullness.qual.Nullable;

/** The activity showing PCS's Network Usage Log. */
@AndroidEntryPoint(CollapsingToolbarBaseActivity.class)
public class NetworkUsageLogActivity extends Hilt_NetworkUsageLogActivity {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    logger.atInfo().log("Network usage log is opened");

    setContentView(R.layout.network_usage_log_activity);

    // Only add the fragments if the activity is being newly created
    if (savedInstanceState != null) {
      return;
    }

    FragmentTransaction fragmentTransaction =
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.settings_fragment_container, new NetworkUsageLogPreferenceFragment());

    if (isUserOptedIn()) {
      fragmentTransaction =
          fragmentTransaction.add(R.id.log_fragment_container, new NetworkUsageLogFragment());
    }

    fragmentTransaction.commit();
  }

  private boolean isUserOptedIn() {
    String preferenceKey = getString(R.string.pref_network_usage_log_enabled_key);
    return PreferenceManager.getDefaultSharedPreferences(this)
        .getBoolean(
            preferenceKey,
            getResources().getBoolean(R.bool.pref_network_usage_log_enabled_default));
  }
}
