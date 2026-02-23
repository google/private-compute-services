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

package com.google.android.as.oss.pd.processor.impl;

import com.google.android.as.oss.pd.config.IntegrityClientTokenProvider;
import com.google.android.as.oss.pd.processor.ProtectedDownloadProcessor;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/** Provides an implementation of {@link ProtectedDownloadProcessor} injected using Dagger/Hilt. */
@Module
@InstallIn(SingletonComponent.class)
abstract class ProtectedDownloadProcessorModule {
  @Binds
  abstract ProtectedDownloadProcessor bindImpl(ProtectedDownloadProcessorImpl impl);

  @BindsOptionalOf
  abstract IntegrityClientTokenProvider bindOptionalIntegrityClientTokenProvider();
}
