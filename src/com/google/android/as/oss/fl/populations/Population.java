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

package com.google.android.as.oss.fl.populations;

import static com.google.common.collect.AndroidAccessToCollectors.toImmutableMap;
import static java.util.Arrays.stream;

import androidx.annotation.Nullable;
import com.google.common.collect.ImmutableMap;

/**
 * This enum defines all of the Federated Analytics Populations that are approved to run inside of
 * PCC. This is to ensure FA populations in PCC are properly documented and to ensure we are using
 * data for the expected and approved usages.
 */
public enum Population {

  /** Empty population to use as a placeholder when needed. */
  UNKNOWN_POPULATION(""),

  /** FA populations to collect platform logs from Android. */
  PLATFORM_LOGGING("pcs/prod/platform_logging"),

  private final String populationName;
  private static final ImmutableMap<String, Population> POPULATION_MAP =
      stream(values()).collect(toImmutableMap(pop -> pop.populationName, pop -> pop));

  @Nullable
  public static Population getPopulation(String populationName) {
    return POPULATION_MAP.get(populationName);
  }

  Population(String populationName) {
    this.populationName = populationName;
  }

  public String populationName() {
    return populationName;
  }
}
