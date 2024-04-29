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

package com.google.android.as.oss.networkusage.ui.user;

import androidx.annotation.Nullable;

/** {@link LogItemWrapper} for the content of the summary banner. */
class SummaryWrapper extends LogItemWrapper {
  private final NetworkUsageSummary networkUsageSummary;

  protected SummaryWrapper(NetworkUsageSummary networkUsageSummary) {
    this.networkUsageSummary = networkUsageSummary;
  }

  NetworkUsageSummary getNetworkUsageSummary() {
    return networkUsageSummary;
  }

  @Override
  LogItemViewHolderFactory getViewHolderFactory() {
    return LogItemViewHolderFactory.SUMMARY_VIEW_HOLDER_FACTORY;
  }

  @Override
  boolean isSameItemAs(@Nullable LogItemWrapper other) {
    return equals(other);
  }

  @Override
  boolean hasSameContentAs(@Nullable LogItemWrapper other) {
    return equals(other);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (!(obj instanceof SummaryWrapper)) {
      return false;
    }
    SummaryWrapper otherSummaryWrapper = (SummaryWrapper) obj;
    return networkUsageSummary.equals(otherSummaryWrapper.networkUsageSummary);
  }

  @Override
  public int hashCode() {
    return networkUsageSummary.hashCode();
  }
}
