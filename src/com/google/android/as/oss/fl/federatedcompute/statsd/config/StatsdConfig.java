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

package com.google.android.as.oss.fl.federatedcompute.statsd.config;

import com.google.auto.value.AutoValue;

/** Config that contains flags to enable querying platform logs. */
@AutoValue
public abstract class StatsdConfig {

  public static StatsdConfig.Builder builder() {
    return new AutoValue_StatsdConfig.Builder()
        .setEnablePlatformLogging(false)
        .setEnableMetricWisePopulations(false);
  }

  public abstract boolean enablePlatformLogging();

  public abstract boolean enableMetricWisePopulations();

  /** Builder for {@link StatsdConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract StatsdConfig.Builder setEnablePlatformLogging(boolean value);

    public abstract StatsdConfig.Builder setEnableMetricWisePopulations(boolean value);

    public abstract StatsdConfig build();
  }
}
