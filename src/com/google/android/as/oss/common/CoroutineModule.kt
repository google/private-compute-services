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

package com.google.android.`as`.oss.common

import com.google.android.`as`.oss.common.CoroutineQualifiers.ApplicationScope
import com.google.android.`as`.oss.common.CoroutineQualifiers.GeneralDispatcher
import com.google.android.`as`.oss.common.CoroutineQualifiers.IoDispatcher
import com.google.android.`as`.oss.common.ExecutorAnnotations.GeneralExecutorQualifier
import com.google.android.`as`.oss.common.ExecutorAnnotations.IoExecutorQualifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
internal object CoroutineModule {
  @Provides
  @Singleton
  @ApplicationScope
  fun provideApplicationScope(@GeneralDispatcher dispatcher: CoroutineDispatcher): CoroutineScope =
    CoroutineScope(SupervisorJob() + dispatcher)

  @Provides
  @Singleton
  @GeneralDispatcher
  fun provideGeneralDispatcher(@GeneralExecutorQualifier executor: Executor): CoroutineDispatcher =
    executor.asCoroutineDispatcher()

  @Provides
  @Singleton
  @IoDispatcher
  fun provideIoDispatcher(@IoExecutorQualifier executor: Executor): CoroutineDispatcher =
    executor.asCoroutineDispatcher()
}
