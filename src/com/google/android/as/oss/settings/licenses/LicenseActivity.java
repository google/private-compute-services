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

package com.google.android.as.oss.settings.licenses;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

/** Simple Activity that renders locally stored open source legal info in a text view. */
public final class LicenseActivity extends AppCompatActivity {
  private static final String TAG = "LicenseActivity";
  private static final String STATE_SCROLL_POS = "scroll_pos";

  @Override
  public void onCreate(Bundle bundle) {
    super.onCreate(bundle);
    setContentView(R.layout.pcs_license_activity);

    License license = getIntent().getParcelableExtra(LicenseMenuActivity.ARGS_LICENSE);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(license.getLibraryName());

      // Show 'up' button with no logo.
      getSupportActionBar().setDisplayShowHomeEnabled(true);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setLogo(null);
    }

    TextView textView = (TextView) findViewById(R.id.license_activity_textview);
    String licenseText = Licenses.getLicenseText(this, license);
    if (licenseText == null) {
      finish();
      return;
    }
    textView.setText(licenseText);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    ScrollView scrollView = (ScrollView) findViewById(R.id.license_activity_scrollview);
    TextView textView = (TextView) findViewById(R.id.license_activity_textview);
    Layout layout = textView.getLayout();
    if (layout != null) {
      int firstVisibleLine = layout.getLineForVertical(scrollView.getScrollY());
      int firstVisibleChar = layout.getLineStart(firstVisibleLine);
      outState.putInt(STATE_SCROLL_POS, firstVisibleChar);
    }
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    final ScrollView scrollView = (ScrollView) findViewById(R.id.license_activity_scrollview);
    final int firstVisibleChar = savedInstanceState.getInt(STATE_SCROLL_POS);
    if (firstVisibleChar != 0) {
      scrollView.post(
          () -> {
            TextView textView = (TextView) findViewById(R.id.license_activity_textview);
            Layout layout = textView.getLayout();
            if (layout != null) {
              int firstVisibleLine = layout.getLineForOffset(firstVisibleChar);
              int offset = layout.getLineTop(firstVisibleLine);
              scrollView.scrollTo(0, offset);
            }
          });
    }
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
