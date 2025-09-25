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

import static com.google.common.base.Preconditions.checkNotNull;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.widget.CompoundButton;
import androidx.preference.PreferenceFragmentCompat;
import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FutureCallback;
import dagger.hilt.android.AndroidEntryPoint;
import java.time.Instant;
import javax.inject.Inject;

/** PreferenceFragment for network usage log settings. */
@AndroidEntryPoint(PreferenceFragmentCompat.class)
public class NetworkUsageLogPreferenceFragment extends Hilt_NetworkUsageLogPreferenceFragment
    implements OnMainSwitchChangeListener {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private MainSwitchPreference mainSwitchPreference;

  @Inject NetworkUsageLogRepository repository;
  @Inject PcsStatsLog pcsStatsLogger;

  // Incompatible parameter type for savedInstanceState.
  // Incompatible parameter type for rootKey.
  @SuppressWarnings("nullness:override.param.invalid")
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.network_usage_log_settings, rootKey);
    mainSwitchPreference =
        checkNotNull(findPreference(getString(R.string.pref_network_usage_log_enabled_key)));
    mainSwitchPreference.addOnSwitchChangeListener(this);
  }

  @Override
  public void onSwitchChanged(CompoundButton switchView, boolean isChecked) {
    if (isChecked) {
      onSwitchedOn();
    } else {
      showAlertDialog();
    }
  }

  private void showAlertDialog() {
    new AlertDialog.Builder(checkNotNull(getActivity()))
        .setTitle(R.string.disable_network_usage_log_alert_title)
        .setMessage(R.string.disable_network_usage_log_alert_message)
        .setPositiveButton(
            R.string.disable_network_usage_log_alert_button_turn_off,
            (unused, which) -> onSwitchedOff())
        .setNegativeButton(
            R.string.disable_network_usage_log_alert_button_cancel,
            (unused, which) -> onDialogCanceled())
        .setOnCancelListener(dialog -> onDialogCanceled())
        .create()
        .show();
  }

  private void onDialogCanceled() {
    mainSwitchPreference.setChecked(true);
  }

  private void onSwitchedOn() {
    pcsStatsLogger.logIntelligenceCountReported(
        // Network usage log opt-in.
        IntelligenceCountReported.newBuilder()
            .setCountMetricId(CountMetricId.PCS_NETWORK_USAGE_LOG_OPTED_IN)
            .build());
    FragmentManager fragmentManager = getParentFragmentManager();
    Fragment logFragment = fragmentManager.findFragmentById(R.id.log_fragment_container);
    if (logFragment != null) {
      return;
    }
    getParentFragmentManager()
        .beginTransaction()
        .replace(R.id.log_fragment_container, new NetworkUsageLogFragment())
        .commit();
  }

  private void onSwitchedOff() {
    pcsStatsLogger.logIntelligenceCountReported(
        // Network usage log opt-out.
        IntelligenceCountReported.newBuilder()
            .setCountMetricId(CountMetricId.PCS_NETWORK_USAGE_LOG_OPTED_OUT)
            .build());
    logger.atInfo().log("NetworkUsageLog switched off by user.");
    mainSwitchPreference.setChecked(false);
    removeNetworkUsageLogFragment();
    clearDatabase();
  }

  private void removeNetworkUsageLogFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    Fragment logFragment = fragmentManager.findFragmentById(R.id.log_fragment_container);
    if (logFragment != null) {
      fragmentManager.beginTransaction().remove(logFragment).commit();
    }
  }

  private void clearDatabase() {
    repository.deleteAllBefore(
        Instant.now(),
        new FutureCallback<>() {
          @Override
          public void onSuccess(Integer result) {
            if (result != -1) {
              logger.atFine().log("Successfully removed %d entities", result);
            } else {
              logger.atWarning().log("Failed to delete old entities");
            }
          }

          @Override
          public void onFailure(Throwable t) {
            logger.atWarning().withCause(t).log("Failed to delete entities.");
          }
        });
  }
}
