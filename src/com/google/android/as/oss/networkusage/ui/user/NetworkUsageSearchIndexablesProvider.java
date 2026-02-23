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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.Settings;
import com.google.android.settings.search.SearchIndexablesContract;
import com.google.android.settings.search.SearchIndexablesProvider;

/** Provider for Settings search indexing for Pcs network usage log. */
public class NetworkUsageSearchIndexablesProvider extends SearchIndexablesProvider {

  public static final int SEARCH_RANK = 1;

  @Override
  public boolean onCreate() {
    return true;
  }

  @Override
  public Cursor queryXmlResources(String[] projection) {
    return new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
  }

  @Override
  public Cursor queryRawData(String[] projection) {
    var cursor = new MatrixCursor(SearchIndexablesContract.INDEXABLES_RAW_COLUMNS);
    Context context = getContext();
    if (context == null || !isDeveloperOptionsEnabled(context)) {
      return cursor;
    }

    cursor.addRow(createIndexableRow(context));
    return cursor;
  }

  private Object[] createIndexableRow(Context context) {
    Object[] row = new Object[SearchIndexablesContract.INDEXABLES_RAW_COLUMNS.length];
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_RANK] = SEARCH_RANK;
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_KEY] =
        context.getString(R.string.pref_network_usage_log_settings_key);
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_TITLE] =
        context.getString(R.string.pcs_network_usage_log_title);
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_SUMMARY_ON] =
        context.getString(R.string.pcs_network_usage_log_summary);
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_KEYWORDS] =
        context.getString(R.string.pcs_network_usage_log_keywords);
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_ACTION] = Intent.ACTION_VIEW;
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = context.getPackageName();
    row[SearchIndexablesContract.COLUMN_INDEX_RAW_INTENT_TARGET_CLASS] =
        NetworkUsageLogActivity.class.getName();
    return row;
  }

  private boolean isDeveloperOptionsEnabled(Context context) {
    return Settings.Global.getInt(
            context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        == 1;
  }

  @Override
  public Cursor queryNonIndexableKeys(String[] projection) {
    return new MatrixCursor(SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS);
  }
}
