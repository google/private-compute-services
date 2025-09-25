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

package com.google.android.as.oss.common.config;

/** A fake implementation of a {@link ConfigReader} that stores its config as a member field. */
// See [redacted]
@SuppressWarnings("ExtendsObject")
public class FakeConfigReader<ConfigT extends Object> extends AbstractConfigReader<ConfigT> {
  private ConfigT config;

  // See [redacted]
  @SuppressWarnings("ExtendsObject")
  public static <ConfigT extends Object> FakeConfigReader<ConfigT> create(ConfigT config) {
    FakeConfigReader<ConfigT> instance = new FakeConfigReader<>(config);
    instance.getConfig(); // Force an initial config fetch.
    return instance;
  }

  public synchronized void setConfig(ConfigT config) {
    this.config = config;
    refreshConfig();
  }

  @Override
  public synchronized ConfigT computeConfig() {
    return config;
  }

  // TODO: Properly handle initialization issues
  @SuppressWarnings({"initialization", "nullness"})
  private FakeConfigReader(ConfigT config) {
    setConfig(config);
  }
}
