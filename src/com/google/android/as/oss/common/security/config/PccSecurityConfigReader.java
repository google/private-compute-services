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

package com.google.android.as.oss.common.security.config;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.common.config.FlagManager.ProtoFlag;
import com.google.android.as.oss.common.security.api.PackageSecurityInfo;
import com.google.android.as.oss.common.security.api.PackageSecurityInfoList;
import java.util.stream.Stream;

/** ConfigReader for {@link PccSecurityConfig}. */
public class PccSecurityConfigReader extends AbstractConfigReader<PccSecurityConfig> {
  private static final String FLAG_PREFIX = "PccSecurity__";

  public static final ProtoFlag<PackageSecurityInfo> ASI_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "asi_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.as")
              .addAllowedReleaseKeys(
                  "3af39ab967aaa5d279e49b5f769cb66e40799838bc8799343ee57ae435d2455b")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> PCS_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "pcs_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.as.oss")
              .addAllowedReleaseKeys(
                  "071f09456bf1a8e8ad2e808ffe6a0ebc13582a7e6f9aba13e47280ad9a85d833")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> PSI_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "psi_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.apps.pixel.psi")
              .addAllowedReleaseKeys(
                  "d439bedff4c060a637ffc07c33ea9fa04a091165c40ee883717c2a89bd5a908f")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> GBOARD_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "gboard_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.inputmethod.latin")
              .addAllowedReleaseKeys(
                  "7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053")
              .addAllowedTestKeys(
                  "d22cc500299fb22873a01a010de1c82fbe4d061119b94814dd301dab50cb7678")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> AGSA_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "agsa_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.googlequicksearchbox")
              .addAllowedReleaseKeys(
                  "f0fd6c5b410f25cb25c3b53346c8972fae30f8ee7411df910480ad6b2d60db83")
              .addAllowedReleaseKeys(
                  "7ce83c1b71f3d572fed04c8d40c5cb10ff75e6d87d9df6fbd53f0468c2905053")
              .addAllowedTestKeys(
                  "1975b2f17177bc89a5dff31f9e64a6cae281a53dc1d1d59b1d147fe1c82afa00")
              .addAllowedTestKeys(
                  "d22cc500299fb22873a01a010de1c82fbe4d061119b94814dd301dab50cb7678")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> AICORE_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "aicore_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.aicore")
              .addAllowedReleaseKeys(
                  "b7971ccc10a03932e14a3557a1b4c2a84be0ecb506777f0c72dd46cf5d7093c6")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> NEXUS_LAUNCHER_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "nexus_launcher_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.apps.nexuslauncher")
              .addAllowedReleaseKeys(
                  "a86bdb059f28f265162d64ce6c8d9772901d227e741581d1f04a5ed132a574d0")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> ODAD_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "PlayProtect_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.PlayProtect")
              .addAllowedReleaseKeys(
                  "cf9bf16382f8aee84040add3489792db844aa6e3cebf267c50fde12531f0853d")
              .build(),
          /* merge= */ false);

  private static final BooleanFlag ENABLE_SECURITY_CHECK =
      BooleanFlag.create(FLAG_PREFIX + "enable_security_check", false);

  private final FlagManager flagManager;

  public static PccSecurityConfigReader create(FlagManager flagManager) {
    PccSecurityConfigReader instance = new PccSecurityConfigReader(flagManager);

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
  protected PccSecurityConfig computeConfig() {
    return PccSecurityConfig.builder()
        .setAsiPackageSecurityInfo(flagManager.get(ASI_PACKAGE_SECURITY_INFO))
        .setPcsPackageSecurityInfo(flagManager.get(PCS_PACKAGE_SECURITY_INFO))
        .setPsiPackageSecurityInfo(flagManager.get(PSI_PACKAGE_SECURITY_INFO))
        .setGboardPackageSecurityInfo(flagManager.get(GBOARD_PACKAGE_SECURITY_INFO))
        .setAgsaPackageSecurityInfo(flagManager.get(AGSA_PACKAGE_SECURITY_INFO))
        .setEnableAllowlistedOnly(flagManager.get(ENABLE_SECURITY_CHECK))
        .setSecurityInfoList(
            PackageSecurityInfoList.newBuilder()
                .addAllPackageSecurityInfos(
                    Stream.of(
                            flagManager.get(ASI_PACKAGE_SECURITY_INFO),
                            flagManager.get(PSI_PACKAGE_SECURITY_INFO),
                            flagManager.get(GBOARD_PACKAGE_SECURITY_INFO),
                            flagManager.get(AGSA_PACKAGE_SECURITY_INFO),
                            flagManager.get(AICORE_PACKAGE_SECURITY_INFO),
                            flagManager.get(NEXUS_LAUNCHER_PACKAGE_SECURITY_INFO),
                            flagManager.get(ODAD_PACKAGE_SECURITY_INFO))
                        .collect(toImmutableList()))
                .build())
        .build();
  }

  private PccSecurityConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
