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

package com.google.android.as.oss.common.statslog;

import com.google.android.as.oss.asi.common.logging.impl.InternalStatsLogConfigExecutor;
import com.google.android.as.oss.common.ExecutorAnnotations.GeneralExecutorQualifier;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.Executor;

/** */
@Module
@InstallIn(SingletonComponent.class)
abstract class InternalStatsLogConfigExecutorModule {

  @Binds
  @InternalStatsLogConfigExecutor
  abstract Executor bindInternalStatsLogConfigExecutor(@GeneralExecutorQualifier Executor executor);
}
