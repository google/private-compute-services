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

package com.google.android.as.oss.settings.licenses;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/** An Activity listing third party libraries with notice licenses. */
public final class LicenseMenuActivity extends AppCompatActivity
    implements LicenseMenuFragment.LicenseSelectionListener {

  static final String ARGS_LICENSE = "license";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.pcs_license_menu_activity);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    FragmentManager fm = getSupportFragmentManager();
    Fragment licenseMenuFragment = fm.findFragmentById(R.id.license_menu_fragment_container);
    if (!(licenseMenuFragment instanceof LicenseMenuFragment)) {
      licenseMenuFragment = new LicenseMenuFragment();
      fm.beginTransaction()
          .add(R.id.license_menu_fragment_container, licenseMenuFragment)
          .commitNow();
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      // Go back one place in the history stack, if the app icon is clicked.
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onLicenseSelected(License license) {
    Intent licenseIntent = new Intent(this, LicenseActivity.class);
    licenseIntent.putExtra(ARGS_LICENSE, license);
    startActivity(licenseIntent);
  }
}
