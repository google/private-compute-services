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
import java.text.DateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

/** {@link LogItemWrapper} for date/time divider items in the RecyclerView. */
class DateTimeDividerItemWrapper extends LogItemWrapper {

  private final String formattedDateTime;

  @SuppressWarnings("AndroidJdkLibsChecker") // We need an oss formatter
  static DateTimeDividerItemWrapper withDateFormat(LocalDate localDate) {
    return new DateTimeDividerItemWrapper(
        localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
  }

  static DateTimeDividerItemWrapper withTimeFormat(Instant instant) {
    return new DateTimeDividerItemWrapper(DateFormat.getTimeInstance().format(Date.from(instant)));
  }

  String getFormattedDateTime() {
    return formattedDateTime;
  }

  @Override
  boolean isSameItemAs(@Nullable LogItemWrapper other) {
    return other instanceof DateTimeDividerItemWrapper
        && formattedDateTime.equals(((DateTimeDividerItemWrapper) other).formattedDateTime);
  }

  @Override
  boolean hasSameContentAs(@Nullable LogItemWrapper other) {
    return isSameItemAs(other);
  }

  @Override
  LogItemViewHolderFactory getViewHolderFactory() {
    return LogItemViewHolderFactory.DATE_DIVIDER_VIEW_HOLDER_FACTORY;
  }

  private DateTimeDividerItemWrapper(String formattedDateTime) {
    this.formattedDateTime = formattedDateTime;
  }
}
