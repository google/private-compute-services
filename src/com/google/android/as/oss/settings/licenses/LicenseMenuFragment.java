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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.loader.app.LoaderManager.LoaderCallbacks;
import androidx.loader.content.Loader;
import java.util.ArrayList;
import java.util.List;

/** A Fragment listing third party libraries with notice licenses. */
public final class LicenseMenuFragment extends Fragment implements LoaderCallbacks<List<License>> {

  private static final int LOADER_ID = 54321;
  private ArrayAdapter<License> listAdapter;
  private LicenseSelectionListener licenseSelectionListener;

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    // Check if the parent fragment implements the LicenseSelectionListener interface.
    Fragment parentFragment = getParentFragment();
    if (parentFragment instanceof LicenseSelectionListener) {
      licenseSelectionListener = (LicenseSelectionListener) parentFragment;
    } else {
      // Attempt to fallback onto the parent activity.
      FragmentActivity parentActivity = getActivity();
      if (parentActivity instanceof LicenseSelectionListener) {
        licenseSelectionListener = (LicenseSelectionListener) parentActivity;
      }
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    licenseSelectionListener = null;
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.pcs_license_menu_fragment, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    FragmentActivity parentActivity = getActivity();
    listAdapter =
        new ArrayAdapter<>(
            parentActivity, R.layout.pcs_licenses_license, R.id.license, new ArrayList<>());
    parentActivity.getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    ListView listView = (ListView) view.findViewById(R.id.license_list);
    listView.setAdapter(listAdapter);
    listView.setOnItemClickListener(
        (parent, view1, position, id) -> {
          License license = (License) parent.getItemAtPosition(position);
          if (licenseSelectionListener != null) {
            licenseSelectionListener.onLicenseSelected(license);
          }
        });
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    getActivity().getSupportLoaderManager().destroyLoader(LOADER_ID);
  }

  @Override
  public Loader<List<License>> onCreateLoader(int id, Bundle args) {
    return new LicenseLoader(getActivity());
  }

  @Override
  public void onLoadFinished(Loader<List<License>> loader, List<License> licenses) {
    listAdapter.clear();
    listAdapter.addAll(licenses);
    listAdapter.notifyDataSetChanged();
  }

  @Override
  public void onLoaderReset(Loader<List<License>> loader) {
    listAdapter.clear();
    listAdapter.notifyDataSetChanged();
  }

  /** Notifies the listener when a license is selected. */
  public interface LicenseSelectionListener {
    void onLicenseSelected(License license);
  }
}
