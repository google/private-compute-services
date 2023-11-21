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

package com.google.android.as.oss.attestation.config.impl;

import com.google.android.as.oss.attestation.config.PcsAttestationMeasurementConfig;
import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.common.config.FlagManager.LongFlag;

/** ConfigReader for {@link PcsAttestationMeasurementConfig}. */
class PcsAttestationMeasurementConfigReader
    extends AbstractConfigReader<PcsAttestationMeasurementConfig> {
  private static final String FLAG_PREFIX = "PcsAttestationMeasurement__";

  static final BooleanFlag ENABLE_ATTESTATION_MEASUREMENT =
      BooleanFlag.create("PcsAttestationMeasurement__enable_attestation_measurement", false);

  static final BooleanFlag SCHEDULE_ATTESTATION_JOB =
      BooleanFlag.create("PcsAttestationMeasurement__schedule_attestation_job", false);

  static final BooleanFlag ENABLE_RANDOM_JITTER =
      BooleanFlag.create("PcsAttestationMeasurement__enable_random_jitter", false);

  static final LongFlag MIN_DELAY_SECONDS =
      LongFlag.create("PcsAttestationMeasurement__minimum_jitter_delay_s", 0L);

  static final LongFlag MAX_DELAY_SECONDS =
      LongFlag.create("PcsAttestationMeasurement__maximum_jitter_delay_s", 59L);

  private final FlagManager flagManager;

  static PcsAttestationMeasurementConfigReader create(FlagManager flagManager) {
    PcsAttestationMeasurementConfigReader instance =
        new PcsAttestationMeasurementConfigReader(flagManager);

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
  protected PcsAttestationMeasurementConfig computeConfig() {
    return PcsAttestationMeasurementConfig.builder()
        .setEnableAttestationMeasurement(flagManager.get(ENABLE_ATTESTATION_MEASUREMENT))
        .setScheduleAttestationJob(flagManager.get(SCHEDULE_ATTESTATION_JOB))
        .setEnableRandomJitter(flagManager.get(ENABLE_RANDOM_JITTER))
        .setMinDelaySeconds(flagManager.get(MIN_DELAY_SECONDS))
        .setMaxDelaySeconds(flagManager.get(MAX_DELAY_SECONDS))
        .build();
  }

  private PcsAttestationMeasurementConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
