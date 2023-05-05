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

package com.google.android.as.oss.common.time;

import java.time.Instant;

/** A provider for the current value of "now" for java.time users. */
public interface TimeSource {
  /** Returns the current Instant according to this time source. */
  Instant now();

  /** A time source that returns the current time using the best available system clock. */
  static TimeSource system() {
    return Instant::now;
  }
}
