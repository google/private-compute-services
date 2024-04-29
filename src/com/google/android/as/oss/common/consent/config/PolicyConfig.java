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

package com.google.android.as.oss.common.consent.config;

import com.google.auto.value.AutoValue;

/** Config that contains flags related to pcc policies. */
@AutoValue
public abstract class PolicyConfig {

  public static PolicyConfig.Builder builder() {
    return new AutoValue_PolicyConfig.Builder().setEnableConsentCheckInPcs(false);
  }

  public abstract boolean enableConsentCheckInPcs();

  /** Builder for {@link PolicyConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract PolicyConfig.Builder setEnableConsentCheckInPcs(boolean value);

    public abstract PolicyConfig build();
  }
}
