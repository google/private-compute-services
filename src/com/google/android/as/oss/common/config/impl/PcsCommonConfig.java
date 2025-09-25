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

package com.google.android.as.oss.common.config.impl;

import com.google.auto.value.AutoValue;

/** Config that contains flags to enable heartbeat job that rescheduled PCS components. */
@AutoValue
public abstract class PcsCommonConfig {

  public static PcsCommonConfig.Builder builder() {
    return new AutoValue_PcsCommonConfig.Builder().setEnableHeartBeatJob(false);
  }

  public abstract boolean enableHeartBeatJob();

  /** Builder for {@link PcsCommonConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract PcsCommonConfig.Builder setEnableHeartBeatJob(boolean value);

    public abstract PcsCommonConfig build();
  }
}
