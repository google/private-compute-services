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

package com.google.android.as.oss.asr.config.impl;

import com.google.android.as.oss.asr.config.PcsAsrConfig;
import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.common.config.FlagManager.ProtoFlag;
import com.google.android.as.oss.common.security.api.PackageSecurityInfo;

/** ConfigReader for {@link PcsAsrConfig}. */
class PcsAsrConfigReader extends AbstractConfigReader<PcsAsrConfig> {

  private static final String FLAG_PREFIX = "PcsAsr__";

  static final BooleanFlag ENABLE_ASR = BooleanFlag.create("PcsAsr__enable_asr", false);

  public static final ProtoFlag<PackageSecurityInfo> SRSG_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "srsg_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.tts")
              .addAllowedReleaseKeys(
                  "7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053")
              .addAllowedReleaseKeys(
                  "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83")
              .addAllowedTestKeys(
                  "d22cc500299fb22873a01a010de1c82fbe4d061119b94814dd301dab50cb7678")
              .addAllowedTestKeys(
                  "1975b2f17177bc89a5dff31f9e64a6cae281a53dc1d1d59b1d147fe1c82afa00")
              .build(),
          /* merge= */ false);

  private final FlagManager flagManager;

  static PcsAsrConfigReader create(FlagManager flagManager) {
    PcsAsrConfigReader instance = new PcsAsrConfigReader(flagManager);

    instance
        .flagManager
        .listenable()
        .addListener(flagNames -> {
          if (FlagListener.anyHasPrefix(flagNames, FLAG_PREFIX)) {
            instance.refreshConfig();
          }
        });

    return instance;
  }

  @Override
  protected PcsAsrConfig computeConfig() {
    return PcsAsrConfig.builder()
        .setEnableAsr(flagManager.get(ENABLE_ASR))
        .setSrsgPackageSecurityInfo(flagManager.get(SRSG_PACKAGE_SECURITY_INFO))
        .build();
  }

  private PcsAsrConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
