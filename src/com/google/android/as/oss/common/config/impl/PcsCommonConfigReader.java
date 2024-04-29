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

package com.google.android.as.oss.common.config.impl;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;

/** ConfigReader for {@link PcsCommonConfig}. */
public class PcsCommonConfigReader extends AbstractConfigReader<PcsCommonConfig> {
  private static final String FLAG_PREFIX = "PcsCommon__";

  public static final BooleanFlag ENABLE_HEARTBEAT_JOB =
      BooleanFlag.create(FLAG_PREFIX + "enable_heartbeat_job", false);

  private final FlagManager flagManager;

  /**
   * Creates a {@link PcsCommonConfigReader} instance.
   *
   * <p>The created instance will refresh the config whenever any of the flags with the prefix
   * "PcsCommon__" is changed.
   */
  public static PcsCommonConfigReader create(FlagManager flagManager) {
    PcsCommonConfigReader instance = new PcsCommonConfigReader(flagManager);

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
  protected PcsCommonConfig computeConfig() {
    return PcsCommonConfig.builder()
        .setEnableHeartBeatJob(flagManager.get(ENABLE_HEARTBEAT_JOB))
        .build();
  }

  private PcsCommonConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
