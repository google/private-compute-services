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

package com.google.android.as.oss.pd.config.impl;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.pd.config.ProtectedDownloadConfig;

/** A reader for {@link ProtectedDownloadConfig} from the configuration flags. */
class ProtectedDownloadConfigReader extends AbstractConfigReader<ProtectedDownloadConfig> {
  private static final String FLAG_PREFIX = "ProtectedDownload__";

  static final BooleanFlag ENABLED_FLAG = BooleanFlag.create(FLAG_PREFIX + "enabled", false);

  private final FlagManager flagManager;

  public static ProtectedDownloadConfigReader create(FlagManager flagManager) {
    ProtectedDownloadConfigReader reader = new ProtectedDownloadConfigReader(flagManager);
    reader.initialize();
    return reader;
  }

  @Override
  protected ProtectedDownloadConfig computeConfig() {
    return ProtectedDownloadConfig.create(flagManager.get(ENABLED_FLAG));
  }

  private void initialize() {
    flagManager
        .listenable()
        .addListener(
            flagNames -> {
              if (FlagListener.anyHasPrefix(flagNames, FLAG_PREFIX)) {
                refreshConfig();
              }
            });
  }

  private ProtectedDownloadConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
