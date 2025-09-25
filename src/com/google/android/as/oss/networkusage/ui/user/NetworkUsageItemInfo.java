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

import androidx.annotation.Nullable;

/**
 * Describes an item in the {@link NetworkUsageItemDetailsActivity} for an item in the
 * NetworkUsageLog.
 */
class NetworkUsageItemInfo extends LogItemWrapper {

  private final String title;
  private final String body;

  NetworkUsageItemInfo(String title, String body) {
    this.title = title;
    this.body = body;
  }

  @Override
  boolean isSameItemAs(@Nullable LogItemWrapper other) {
    return hasSameContentAs(other);
  }

  @Override
  boolean hasSameContentAs(@Nullable LogItemWrapper other) {
    if (!(other instanceof NetworkUsageItemInfo)) {
      return false;
    }
    NetworkUsageItemInfo otherInfo = (NetworkUsageItemInfo) other;
    return title.equals(otherInfo.title) && body.equals(otherInfo.body);
  }

  @Override
  LogItemViewHolderFactory getViewHolderFactory() {
    return LogItemViewHolderFactory.USAGE_ITEM_INFO_VIEW_HOLDER_FACTORY;
  }

  String getTitle() {
    return title;
  }

  String getBody() {
    return body;
  }
}
