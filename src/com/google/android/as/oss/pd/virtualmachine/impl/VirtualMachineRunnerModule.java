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

package com.google.android.as.oss.pd.virtualmachine.impl;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import com.google.android.as.oss.common.ExecutorAnnotations.VirtualMachineExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.pd.config.ProtectedDownloadConfig;
import com.google.android.as.oss.pd.persistence.PersistentStateManager;
import com.google.android.as.oss.pd.virtualmachine.VirtualMachineRunner;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

/** Provides an implementation of {@link VirtualMachineRunner} on supporting devices. */
@Module
@InstallIn(SingletonComponent.class)
interface VirtualMachineRunnerModule {

  // Nullable binding instead of Optional because this Module isn't installed everywhere and
  // therefore the binding is declared elsewhere with @BindsOptionalOf.
  @Provides
  @Nullable
  @Singleton
  static VirtualMachineRunner provideVirtualMachineRunner(
      ConfigReader<ProtectedDownloadConfig> configReader,
      PersistentStateManager persistenceManager,
      @VirtualMachineExecutorQualifier Executor executor,
      @ApplicationContext Context context) {
    // VirtualMachines are supported on U+.
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
            && configReader.getConfig().enableProtectedDownloadVirtualMachines()
        ? new VirtualMachineRunnerImpl(persistenceManager, executor, context)
        : null;
  }
}
