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

  public static final ProtoFlag<PackageSecurityInfo> GBOARD_CANARY_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "gboard_canary_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.inputmethod.latin.canary")
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

  public static final ProtoFlag<PackageSecurityInfo> PLAYPROTECT_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "PlayProtect_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.PlayProtect")
              .addAllowedReleaseKeys(
                  "cf9bf16382f8aee84040add3489792db844aa6e3cebf267c50fde12531f0853d")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> SAFETYCORE_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "safetycore_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.safetycore")
              .addAllowedReleaseKeys(
                  "133e1925a9b992d327fa1f6c4ba8d57c3b2ebd1f1da4de6b0bfa0bd0411a7ca4")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> BLUEFLAX_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "blueflax_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.apps.pixel.blueflax")
              .addAllowedReleaseKeys(
                  "2815dfa37aedbba232e7113bbfa0b8168d7b71401074378535da4a6fbca15520")
              .addAllowedTestKeys(
                  "103938ee4537e59e8ee792f654504fb8346fc6b346d0bbc4415fc339fcfc8ec1")
              .addAllowedTestKeys(
                  "b4443a5892c6cb3febd3fb3cfdef015e5b1722dd62f85b424a0dbc8950722afc")
              .build(),
          /* merge= */ false);

  public static final ProtoFlag<PackageSecurityInfo> GLASSES_CORE_PACKAGE_SECURITY_INFO =
      ProtoFlag.create(
          FLAG_PREFIX + "glasses_core_package_security_info",
          PackageSecurityInfo.newBuilder()
              .setPackageName("com.google.android.glasses.core")
              .addAllowedReleaseKeys(
                  "3257d599a49d2c961a471ca9843f59d341a405884583fc087df4237b733bbd6d")
              .addAllowedTestKeys(
                  "ae95c5718b550cad726dc268702df2bacbc2aa292924782b4384e2b990022825")
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
        .setBlueflaxPackageSecurityInfo(flagManager.get(BLUEFLAX_PACKAGE_SECURITY_INFO))
        .setGlassesCorePackageSecurityInfo(flagManager.get(GLASSES_CORE_PACKAGE_SECURITY_INFO))
        .setEnableAllowlistedOnly(flagManager.get(ENABLE_SECURITY_CHECK))
        .setSecurityInfoList(
            PackageSecurityInfoList.newBuilder()
                .addAllPackageSecurityInfos(
                    Stream.of(
                            flagManager.get(ASI_PACKAGE_SECURITY_INFO),
                            flagManager.get(PSI_PACKAGE_SECURITY_INFO),
                            flagManager.get(GBOARD_PACKAGE_SECURITY_INFO),
                            flagManager.get(GBOARD_CANARY_PACKAGE_SECURITY_INFO),
                            flagManager.get(AGSA_PACKAGE_SECURITY_INFO),
                            flagManager.get(AICORE_PACKAGE_SECURITY_INFO),
                            flagManager.get(NEXUS_LAUNCHER_PACKAGE_SECURITY_INFO),
                            flagManager.get(PLAYPROTECT_PACKAGE_SECURITY_INFO),
                            flagManager.get(SAFETYCORE_PACKAGE_SECURITY_INFO),
                            flagManager.get(BLUEFLAX_PACKAGE_SECURITY_INFO),
                            flagManager.get(GLASSES_CORE_PACKAGE_SECURITY_INFO))
                        .collect(toImmutableList()))
                .build())
        .build();
  }

  private PccSecurityConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
