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

package com.google.android.as.oss.networkusage.config;

import com.google.auto.value.AutoValue;

/** Config that contains Network Usage Log flags. */
@AutoValue
public abstract class NetworkUsageLogConfig {

  public static Builder builder() {
    return new AutoValue_NetworkUsageLogConfig.Builder().setNetworkUsageLogEnabled(false);
  }

  public abstract boolean networkUsageLogEnabled();

  public abstract boolean rejectUnknownRequests();

  /** Builder for {@link NetworkUsageLogConfig} */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setNetworkUsageLogEnabled(boolean value);

    public abstract Builder setRejectUnknownRequests(boolean value);

    public abstract NetworkUsageLogConfig build();
  }
}
