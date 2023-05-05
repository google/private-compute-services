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

package com.google.android.as.oss.common.config.noop;

import android.os.Binder;
import com.google.android.as.oss.common.config.AbstractFlagManager;
import com.google.android.as.oss.common.config.FlagListener;
import com.google.android.as.oss.common.config.Listenable;
import com.google.android.as.oss.common.config.MulticastListenable;
import org.checkerframework.checker.nullness.qual.Nullable;

/** No-op implementation of FlagManager. */
class DeviceFlagManagerNoOp extends AbstractFlagManager {

  private final MulticastListenable<FlagListener> listenable;

  public static DeviceFlagManagerNoOp create() {
    Binder.clearCallingIdentity();
    return new DeviceFlagManagerNoOp();
  }

  @Override
  protected @Nullable String getProperty(String name) {
    return null;
  }

  @Override
  public Listenable<FlagListener> listenable() {
    return listenable;
  }

  private DeviceFlagManagerNoOp() {
    listenable = MulticastListenable.create();
  }
}
