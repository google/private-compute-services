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

package com.google.android.as.oss.ai.config.impl;

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.ai.config.PcsAiConfig;
import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.common.config.FlagManager.LongFlag;

/** ConfigReader for {@link PcsAiConfig}. */
final class PcsAiConfigReader extends AbstractConfigReader<PcsAiConfig> {
  private static final String FLAG_PREFIX = "PcsAi__";

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  static final BooleanFlag GENAI_INFERENCE_SERVICE_ENABLED =
      BooleanFlag.create("PcsAi__enable_genai_inference_service", false);

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  static final LongFlag GENAI_SERVICE_CONNECTION_TIMEOUT_MS =
      LongFlag.create("PcsAi__genai_service_connection_timeout_ms", 2000L);

  private final FlagManager flagManager;

  static PcsAiConfigReader create(FlagManager flagManager) {
    PcsAiConfigReader instance = new PcsAiConfigReader(flagManager);

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
  protected PcsAiConfig computeConfig() {
    return PcsAiConfig.builder()
        .setGenAiInferenceServiceEnabled(flagManager.get(GENAI_INFERENCE_SERVICE_ENABLED))
        .setGenAiServiceConnectionTimeoutMs(flagManager.get(GENAI_SERVICE_CONNECTION_TIMEOUT_MS))
        .build();
  }

  private PcsAiConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
