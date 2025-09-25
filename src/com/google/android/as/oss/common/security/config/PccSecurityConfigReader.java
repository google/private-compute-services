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

  private static final BooleanFlag ENABLE_SECURITY_CHECK =
      BooleanFlag.create(FLAG_PREFIX + "enable_security_check", false);

  private static final ProtoFlag<PackageSecurityInfoList> PACKAGE_SECURITY_INFO_LIST =
      ProtoFlag.create(
          FLAG_PREFIX + "package_security_info_list",
          PackageSecurityInfoList.newBuilder()
              .addAllPackageSecurityInfos(
                  Stream.of(
                          "com.google.android.as",
                          "com.google.android.aicore",
                          "com.google.android.apps.pixel.psi",
                          "com.google.android.apps.nexuslauncher",
                          "com.google.android.PlayProtect",
                          "com.android.systemui")
                      .map(
                          pkgName ->
                              PackageSecurityInfo.newBuilder().setPackageName(pkgName).build())
                      .collect(toImmutableList()))
              .build(),
          false);

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
        .setEnableAllowlistedOnly(flagManager.get(ENABLE_SECURITY_CHECK))
        .setSecurityInfoList(flagManager.get(PACKAGE_SECURITY_INFO_LIST))
        .build();
  }

  private PccSecurityConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
