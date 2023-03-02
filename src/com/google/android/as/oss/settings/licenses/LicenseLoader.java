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

import android.content.Context;
import androidx.loader.content.AsyncTaskLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/** {@link AsyncTaskLoader} to load the list of licenses for the license menu activity. */
final class LicenseLoader extends AsyncTaskLoader<List<License>> {

  private List<License> licenses;

  LicenseLoader(Context context) {
    // This must only pass the application context to avoid leaking a pointer to the Activity.
    super(context.getApplicationContext());
  }

  @Override
  public List<License> loadInBackground() {
    // De-dupe with a set
    TreeSet<License> licenses = new TreeSet<>();
    licenses.addAll(Licenses.getLicenses(getContext()));
    return Collections.unmodifiableList(new ArrayList<>(licenses));
  }

  @Override
  public void deliverResult(List<License> licenses) {
    this.licenses = licenses;
    super.deliverResult(licenses);
  }

  @Override
  protected void onStartLoading() {
    if (licenses != null) {
      deliverResult(licenses);
    } else {
      forceLoad();
    }
  }

  @Override
  protected void onStopLoading() {
    cancelLoad();
  }
}
