/*
 * Copyright 2024 Google LLC
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

package com.google.android.as.oss.common.consent;

import android.content.Context;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import com.google.common.flogger.GoogleLogger;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of {@link UsageReportingOptedInState} interface. */
@ThreadSafe
public class GlobalUsageReportingOptedInStateImpl implements UsageReportingOptedInState {
  private static final GoogleLogger logcat = GoogleLogger.forEnclosingClass();
  private final Context context;

  public GlobalUsageReportingOptedInStateImpl(Context context) {
    this.context = context;
  }

  @Override
  public boolean isOptedIn() {
    try {
      // Needs to read the value each time as there are no listeners for Settings.Global changes.
      return Settings.Global.getInt(context.getContentResolver(), "multi_cb") == 1;
    } catch (SettingNotFoundException e) {
      logcat.atFine().log("Consent value not found");
      return false;
    }
  }
}
