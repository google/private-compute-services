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

package com.google.android.as.oss.common.consent.config;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;

/** ConfigReader for {@link PolicyConfig}. */
public class PolicyConfigReader extends AbstractConfigReader<PolicyConfig> {
  private static final String FLAG_PREFIX = "PcsPolicy__";

  public static final BooleanFlag ENABLE_CONSENT_CHECK_IN_PCS =
      BooleanFlag.create(FLAG_PREFIX + "enable_consent_check_in_pcs", false);

  private final FlagManager flagManager;

  public static PolicyConfigReader create(FlagManager flagManager) {
    PolicyConfigReader instance = new PolicyConfigReader(flagManager);

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
  protected PolicyConfig computeConfig() {
    return PolicyConfig.builder()
        .setEnableConsentCheckInPcs(flagManager.get(ENABLE_CONSENT_CHECK_IN_PCS))
        .build();
  }

  private PolicyConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
