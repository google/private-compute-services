/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.networkusage.config.impl;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.networkusage.config.NetworkUsageLogConfig;

/** ConfigReader for {@link NetworkUsageLogConfig}. */
class NetworkUsageLogConfigReader extends AbstractConfigReader<NetworkUsageLogConfig> {
  private static final String FLAG_PREFIX = "AstreaNetworkUsageLog__";

  static final BooleanFlag ENABLE_NETWORK_USAGE_LOG =
      BooleanFlag.create("AstreaNetworkUsageLog__enable_network_usage_log", false);

  static final BooleanFlag REJECT_UNKNOWN_REQUESTS =
      BooleanFlag.create("AstreaNetworkUsageLog__reject_unknown_requests", false);

  private final FlagManager flagManager;

  static NetworkUsageLogConfigReader create(FlagManager flagManager) {
    NetworkUsageLogConfigReader instance = new NetworkUsageLogConfigReader(flagManager);

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
  protected NetworkUsageLogConfig computeConfig() {
    return NetworkUsageLogConfig.builder()
        .setNetworkUsageLogEnabled(flagManager.get(ENABLE_NETWORK_USAGE_LOG))
        .setRejectUnknownRequests(flagManager.get(REJECT_UNKNOWN_REQUESTS))
        .build();
  }

  private NetworkUsageLogConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
