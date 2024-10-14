/*
 * Copyright 2024 Google LLC
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

package com.google.android.as.oss.survey.config.impl;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.BooleanFlag;
import com.google.android.as.oss.survey.config.PcsSurveyConfig;

/** ConfigReader for {@link PcsSurveyConfig}. */
class PcsSurveyConfigReader extends AbstractConfigReader<PcsSurveyConfig> {
  private static final String FLAG_PREFIX = "PcsSurvey__";

  static final BooleanFlag ENABLE_SURVEY = BooleanFlag.create("PcsSurvey__enable_survey", false);

  private final FlagManager flagManager;

  static PcsSurveyConfigReader create(FlagManager flagManager) {
    PcsSurveyConfigReader instance = new PcsSurveyConfigReader(flagManager);

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
  protected PcsSurveyConfig computeConfig() {
    return PcsSurveyConfig.builder().setEnableSurvey(flagManager.get(ENABLE_SURVEY)).build();
  }

  private PcsSurveyConfigReader(FlagManager flagManager) {
    this.flagManager = flagManager;
  }
}
