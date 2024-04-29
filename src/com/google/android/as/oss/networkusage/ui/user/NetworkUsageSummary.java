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

import com.google.auto.value.AutoValue;

/** Contains the contents of the summary banner in the network usage log. */
@AutoValue
abstract class NetworkUsageSummary {
  abstract int updatesCount();

  abstract long totalUpload();

  abstract long totalDownload();

  public static Builder builder() {
    return new AutoValue_NetworkUsageSummary.Builder();
  }

  /** Builder for creating new instances of {@link NetworkUsageSummary}. */
  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setUpdatesCount(int updatesCount);

    abstract Builder setTotalUpload(long totalUpload);

    abstract Builder setTotalDownload(long totalDownload);

    abstract NetworkUsageSummary build();
  }
}
