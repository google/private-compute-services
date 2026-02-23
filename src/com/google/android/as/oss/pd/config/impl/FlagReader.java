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

package com.google.android.as.oss.pd.config.impl;

import com.google.android.as.oss.common.config.AbstractConfigReader;
import com.google.android.as.oss.common.config.FlagManager;
import com.google.android.as.oss.common.config.FlagManager.LongFlag;
import com.google.android.as.oss.common.config.FlagManager.StringFlag;
import java.util.function.Supplier;

/** A class that reads a flag from a {@link FlagManager} and provides the value as a config. */
final class FlagReader<T> extends AbstractConfigReader<T> {
  private final String flagName;
  private final FlagManager flagManager;
  private final Supplier<T> getter;

  /**
   * Creates a {@link FlagReader} for a string flag.
   *
   * @param flagManager the {@link FlagManager} to use to read the flag
   * @param flagName the name of the flag to read
   */
  public static FlagReader<String> forString(FlagManager flagManager, String flagName) {
    StringFlag flag = StringFlag.create(flagName, "");
    FlagReader<String> reader =
        new FlagReader<>(flagManager, flagName, () -> flagManager.get(flag));
    reader.initialize();
    return reader;
  }

  /**
   * Creates a {@link FlagReader} for a long flag.
   *
   * @param flagManager the {@link FlagManager} to use to read the flag
   * @param flagName the name of the flag to read
   */
  public static FlagReader<Long> forLong(FlagManager flagManager, String flagName) {
    LongFlag flag = LongFlag.create(flagName, 0L);
    FlagReader<Long> reader = new FlagReader<>(flagManager, flagName, () -> flagManager.get(flag));
    reader.initialize();
    return reader;
  }

  @Override
  protected T computeConfig() {
    return getter.get();
  }

  private void initialize() {
    flagManager
        .listenable()
        .addListener(
            flagNames -> {
              if (flagNames.contains(flagName)) {
                refreshConfig();
              }
            });
  }

  private FlagReader(FlagManager flagManager, String flagName, Supplier<T> getter) {
    this.flagManager = flagManager;
    this.flagName = flagName;
    this.getter = getter;
  }
}
