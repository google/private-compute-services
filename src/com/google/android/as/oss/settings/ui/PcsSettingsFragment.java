/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.settings.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.as.oss.settings.licenses.LicenseMenuActivity;
import dagger.hilt.android.AndroidEntryPoint;

/**
 * Simple settings fragment for PCS.
 *
 * @see PcsSettingsActivity
 */
@AndroidEntryPoint(PreferenceFragmentCompat.class)
public class PcsSettingsFragment extends Hilt_PcsSettingsFragment {

  @Override
  public void onCreatePreferences(@Nullable Bundle bundle, @Nullable String rootKey) {
    setPreferencesFromResource(R.xml.pcs_settings, rootKey);
  }

  @Override
  public boolean onPreferenceTreeClick(@NonNull Preference preference) {
    if (preference.getKey().equals(getString(R.string.pcs_settings_pref_key_licenses))) {
      Intent licensesActivity = new Intent(requireContext(), LicenseMenuActivity.class);
      requireContext().startActivity(licensesActivity);
      return true;
    }
    return super.onPreferenceTreeClick(preference);
  }
}
