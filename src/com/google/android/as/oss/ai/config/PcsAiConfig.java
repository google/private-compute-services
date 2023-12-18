/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.ai.config;

import com.google.auto.value.AutoValue;

/** Config that contains Pcs Ai flags. */
@AutoValue
public abstract class PcsAiConfig {

  public static Builder builder() {
    return new AutoValue_PcsAiConfig.Builder().setGenAiInferenceServiceEnabled(false);
  }

  public abstract boolean genAiInferenceServiceEnabled();

  public abstract long genAiServiceConnectionTimeoutMs();

  /** Builder for {@link PcsAiConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setGenAiInferenceServiceEnabled(boolean value);

    public abstract Builder setGenAiServiceConnectionTimeoutMs(long value);

    public abstract PcsAiConfig build();
  }
}
