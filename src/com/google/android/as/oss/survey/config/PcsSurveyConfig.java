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

package com.google.android.as.oss.survey.config;

import com.google.auto.value.AutoValue;

/** Config that contains survey feature flags. */
@AutoValue
public abstract class PcsSurveyConfig {

  public static Builder builder() {
    return new AutoValue_PcsSurveyConfig.Builder().setEnableSurvey(false);
  }

  public abstract boolean enableSurvey();

  /** Builder for {@link PcsSurveyConfig} */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setEnableSurvey(boolean value);

    public abstract PcsSurveyConfig build();
  }
}
