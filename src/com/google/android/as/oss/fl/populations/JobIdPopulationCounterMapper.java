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

package com.google.android.as.oss.fl.populations;

import androidx.annotation.Nullable;
import com.google.android.as.oss.logging.PcsStatsEnums.ValueMetricId;
import com.google.common.collect.ImmutableMap;

/**
 * JobId to Population feature mapper. Every job scheduled in pcs either has a persistent hash as
 * generated from farmHashFingerprint64() of the population name or supplied explicitly using {@link
 * FederationConfig#trainerJobId()}
 */
public final class JobIdPopulationCounterMapper {

  /**
   * Static map contains mapping for PlayProtect related jobs as they are not present in the common
   * {@link com.google.android.as.oss.fl.populations.Population} enum.
   */
  private static final ImmutableMap<Integer, ValueMetricId> JOB_ID_METRIC_MAP =
      ImmutableMap.of(
          369941447, ValueMetricId.PLAY_PROTECTION_POPULATION_SCHEDULED_COUNT); //  - clean up after

  // moving the

  // PlayProtect population to common Population enum file

  /**
   * Returns the {@link ValueMetricId} corresponding to the given {@code jobId}.
   *
   * <p>If the {@code jobId} is not found in the static {@link #JOB_ID_METRIC_MAP} then it will try
   * to find the population name from the hash fingerprint.
   */
  @Nullable
  public static ValueMetricId getValueMetricId(int jobId) {
    // JobId is converted to positive to allow comparison with the hash fingerprint which is also
    // stored in the in-memory map as always positive.
    jobId = Math.abs(jobId);

    if (JOB_ID_METRIC_MAP.containsKey(jobId)) {
      return JOB_ID_METRIC_MAP.get(jobId);
    }

    Population population = Population.getPopulationByHashFingerprint(jobId);
    if (population != null) {
      return getFeatureValueMetricIdByPopulationName(population.populationName());
    }

    return null;
  }

  private static ValueMetricId getFeatureValueMetricIdByPopulationName(String populationName) {
    if (populationName.startsWith("nowplaying")) {
      return ValueMetricId.NOW_PLAYING_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("smartselect")) {
      return ValueMetricId.SMARTSELECT_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("safecomms")) {
      return ValueMetricId.SAFE_COMMS_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("nextconversation")) {
      return ValueMetricId.NEXT_CONVERSATION_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("autofill")) {
      return ValueMetricId.AUTOFILL_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("contentcapture")) {
      return ValueMetricId.CONTENT_CAPTURE_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("echo")) {
      return ValueMetricId.ECHO_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("overview")) {
      return ValueMetricId.OVERVIEW_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("pecan")) {
      return ValueMetricId.PECAN_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("livetranslate")) {
      return ValueMetricId.LIVE_TRANSLATE_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("search")) {
      return ValueMetricId.TOAST_SEARCH_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("toastquery")) {
      return ValueMetricId.TOAST_QUERY_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("ambientcontext")) {
      return ValueMetricId.AMBIENT_CONTEXT_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("PlayProtect")) {
      return ValueMetricId.PLAY_PROTECTION_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.contains("platform_logging")) {
      return ValueMetricId.PLATFORM_LOGGING_POPULATION_SCHEDULED_COUNT;
    } else if (populationName.startsWith("smartnotification")) {
      return ValueMetricId.SMART_NOTIFICATION_POPULATION_SCHEDULED_COUNT;
    }

    return ValueMetricId.UNKNOWN_POPULATION_SCHEDULED_COUNT;
  }

  private JobIdPopulationCounterMapper() {}
}
