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

package com.google.android.as.oss.pd.common;

import com.google.android.as.oss.common.config.FlagNamespace;
import com.google.auto.value.AutoValue;
import java.util.Optional;

/** A Client details corresponded to Client enum. */
@AutoValue
public abstract class ClientConfig {
  /** The client id string representation. */
  public abstract String clientId();

  /** Optional build id flag if client supports versioned downloads. */
  public abstract Optional<BuildIdFlag> buildIdFlag();

  public static ClientConfig create(String clientId) {
    return builder().setClientId(clientId).build();
  }

  public static Builder builder() {
    return new AutoValue_ClientConfig.Builder();
  }

  /** Builder for creating new instances. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setClientId(String clientId);

    public abstract Builder setBuildIdFlag(BuildIdFlag buildIdFlag);

    public abstract ClientConfig build();
  }

  /** BuildId flag specification. */
  @AutoValue
  public abstract static class BuildIdFlag {
    public abstract FlagNamespace flagNamespace();

    public abstract String flagName();

    public static BuildIdFlag create(FlagNamespace flagNamespace, String flagName) {
      return new AutoValue_ClientConfig_BuildIdFlag(flagNamespace, flagName);
    }
  }
}
