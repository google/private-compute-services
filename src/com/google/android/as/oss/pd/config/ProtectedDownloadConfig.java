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

package com.google.android.as.oss.pd.config;

import com.google.auto.value.AutoValue;

/** A configuration for controlling the behavior of protected downloads. */
@AutoValue
public abstract class ProtectedDownloadConfig {

  public static ProtectedDownloadConfig create(
      boolean enabled,
      boolean enableProtectedDownloadAttestation,
      boolean enableProtectedDownloadVirtualMachines) {
    return new AutoValue_ProtectedDownloadConfig(
        enabled, enableProtectedDownloadAttestation, enableProtectedDownloadVirtualMachines);
  }

  /** Determines if support for protected downloads is enabled in PCS. */
  public abstract boolean enabled();

  /** Determines if Key Attestation should be enabled. */
  public abstract boolean enableProtectedDownloadAttestation();

  /** Determines if virtual machines should be enabled. */
  public abstract boolean enableProtectedDownloadVirtualMachines();
}
