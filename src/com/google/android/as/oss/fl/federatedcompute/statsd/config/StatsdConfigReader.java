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

package com.google.android.as.oss.fl.federatedcompute.statsd.config;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;

/** ConfigReader for {@link StatsdConfig}. */
public class StatsdConfigReader extends AbstractConfigReader<StatsdConfig> {
  private static final String FLAG_PREFIX = "PlatformLogging__";

  public static final BooleanFlag ENABLE_PLATFORM_LOGGING =
      BooleanFlag.create(FLAG_PREFIX + "enable_platform_logging", true);
  public static final BooleanFlag ENABLE_METRIC_WISE_POPULATIONS =
      BooleanFlag.create(FLAG_PREFIX + "enable_metric_wise_populations", false);

  private final FlagManager flagManager;

  public static StatsdConfigReader create(FlagManager flagManager) {
    StatsdConfigReader instance = new StatsdConfigReader(flagManager);

    instance
        .flagManager
        .listenable()
        .addListener(
            (flagNames) -> {
              if (FlagListener.anyHasPrefix(flagNames, FLAG_PREFIX)) {
                instance.refreshConfig();
              }
            });

    return instance;
  }

  @Override
  protected StatsdConfig computeConfig() {
    return StatsdConfig.builder()
        .setEnablePlatformLogging(flagManager.get(ENABLE_PLATFORM_LOGGING))
        .setEnableMetricWisePopulations(flagManager.get(ENABLE_METRIC_WISE_POPULATIONS))
        .build();
  }

  private StatsdConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
