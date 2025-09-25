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

import com.google.android.as.oss.common.security.api.PackageSecurityInfo;
import com.google.android.as.oss.common.security.api.PackageSecurityInfoList;
import com.google.auto.value.AutoValue;

/** Config that contains flags related to pcc security. */
@AutoValue
public abstract class PccSecurityConfig {

  public static PccSecurityConfig.Builder builder() {
    return new AutoValue_PccSecurityConfig.Builder();
  }

  public abstract PackageSecurityInfo asiPackageSecurityInfo();

  public abstract PackageSecurityInfo pcsPackageSecurityInfo();

  public abstract PackageSecurityInfo psiPackageSecurityInfo();

  public abstract boolean enableAllowlistedOnly();

  public abstract PackageSecurityInfoList securityInfoList();

  /** Builder for {@link PccSecurityConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {
    public abstract PccSecurityConfig.Builder setAsiPackageSecurityInfo(PackageSecurityInfo value);

    public abstract PccSecurityConfig.Builder setPcsPackageSecurityInfo(PackageSecurityInfo value);

    public abstract PccSecurityConfig.Builder setPsiPackageSecurityInfo(PackageSecurityInfo value);

    public abstract PccSecurityConfig.Builder setEnableAllowlistedOnly(boolean value);

    public abstract PccSecurityConfig.Builder setSecurityInfoList(PackageSecurityInfoList value);

    public abstract PccSecurityConfig build();
  }
}
