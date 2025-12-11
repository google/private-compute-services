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

package com.google.android.`as`.oss.privateinference.library.oakutil

/** A flag to control device attestation. */
interface DeviceAttestationFlag {
  enum class Mode {
    DISABLED,
    ENABLED_NO_PROPERTIES,
    ENABLED_WITH_DEVICE_PROPERTIES,
  }

  fun mode(): Mode

  fun enabled(): Boolean = mode() != Mode.DISABLED

  fun useDeviceProperties(): Boolean = mode() == Mode.ENABLED_WITH_DEVICE_PROPERTIES
}
