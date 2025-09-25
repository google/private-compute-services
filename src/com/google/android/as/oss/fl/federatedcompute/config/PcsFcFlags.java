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

package com.google.android.as.oss.fl.federatedcompute.config;

import android.content.ComponentCallbacks2;

/** Interface to add and override PCS-specific flags. */
public abstract class PcsFcFlags {

  /** Whether to read FC flags from DeviceConfig. */
  public abstract boolean enableDeviceConfigOverrides();

  public int inappTrainingOnTrimMemoryInterruptLevel() {
    return ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW;
  }

  /** Percentage of messages to be logged. */
  public int logSamplingPercentage() {
    return 0;
  }

  /** Percentage of counters to be logged. */
  public int logCounterSamplingPercentage() {
    return 0;
  }

  /** Max duration before timing out a binder IPC connection. */
  public int maxBinderDelaySeconds() {
    return 100; // high, to default to simulate no timeout
  }

  /** Maximum size of serialized atoms logged by PCS. */
  public int maxSerializedAtomSize() {
    return 0;
  }

  /** Whether or not PCS should log error message strings from federated compute. */
  public boolean allowLoggingErrorMessage() {
    return false;
  }

  /** Whether or not SecAggClientLogEvents should be logged. */
  public boolean allowLoggingSecAggClientEvent() {
    return false;
  }

  /** Enables the {@link AttestationClient} to be set in the federated compute library. */
  @Override
  public boolean allowAttestationClientOverride() {
    return true;
  }
}
