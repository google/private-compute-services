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

package com.google.android.as.oss.fl.federatedcompute.config.impl;

import android.content.ComponentCallbacks2;
import android.provider.DeviceConfig;
import android.util.Base64;
import androidx.core.util.Supplier;
import com.google.android.as.oss.common.config.FlagNamespace;
import com.google.android.as.oss.fl.federatedcompute.config.PcsFcFlags;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.ExtensionRegistryLite;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;

/** Implementation of {@link PcsFcFlags} which reads flag values from DeviceConfig. */
public final class PcsFcDeviceConfigFlags extends PcsFcFlags {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final String FLAG_PREFIX = "Fc__";
  private static final String FLAG_NAMESPACE =
      FlagNamespace.DEVICE_PERSONALIZATION_SERVICES.toString();

  static PcsFcDeviceConfigFlags create() {
    return new PcsFcDeviceConfigFlags();
  }

  @Override
  public boolean enableDeviceConfigOverrides() {
    return DeviceConfig.getBoolean(
        FLAG_NAMESPACE, FLAG_PREFIX + "enable_device_config_overrides", true);
  }

  @Override
  public int inappTrainingOnTrimMemoryInterruptLevel() {
    return DeviceConfig.getInt(
        FLAG_NAMESPACE,
        FLAG_PREFIX + "inapp_training_on_trim_memory_interrupt_level",
        ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW);
  }

  @Override
  public int logSamplingPercentage() {
    return DeviceConfig.getInt(FLAG_NAMESPACE, FLAG_PREFIX + "log_sampling_percentage", 5);
  }

  @Override
  public int maxSerializedAtomSize() {
    return DeviceConfig.getInt(FLAG_NAMESPACE, FLAG_PREFIX + "max_serialized_atom_size", 2048);
  }

  @Override
  public boolean allowLoggingErrorMessage() {
    return DeviceConfig.getBoolean(
        FLAG_NAMESPACE, FLAG_PREFIX + "allow_logging_error_message", true);
  }

  @Override
  public int logCounterSamplingPercentage() {
    return DeviceConfig.getInt(FLAG_NAMESPACE, FLAG_PREFIX + "log_counter_sampling_percentage", 0);
  }

  @Override
  public boolean allowLoggingSecAggClientEvent() {
    return DeviceConfig.getBoolean(
        FLAG_NAMESPACE, FLAG_PREFIX + "allow_logging_sec_agg_client_event", true);
  }

  private PcsFcDeviceConfigFlags() {}
}
