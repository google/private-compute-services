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

import android.content.Context
import com.google.android.`as`.oss.common.time.TimeSource
import com.google.android.`as`.oss.privateinference.Annotations.AttestationPublisherExecutor
import com.google.android.`as`.oss.privateinference.Annotations.PrivateInferenceEndpointUrl
import com.google.common.flogger.GoogleLogger
import com.google.oak.session.AttestationPublisher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Optional
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface PublisherModule {

  companion object PublisherModule {
    private val ATTESTATION_PUBLISHER_SUBDIR = "attestation_evidence"
    private val logger = GoogleLogger.forEnclosingClass()

    @Provides
    @Singleton
    @AttestationPublisherExecutor
    fun provideAttestationPublisherExecutor(): Executor {
      return Executors.newSingleThreadExecutor()
    }

    /** For more information on the publisher, see [AttestationPublisherFlag] */
    @Provides
    @Singleton
    fun provideAttestationPublisher(
      @ApplicationContext context: Context,
      @PrivateInferenceEndpointUrl endpoint: String,
      attestationPublisherFlag: AttestationPublisherFlag,
      @AttestationPublisherExecutor executor: Executor,
      timeSource: TimeSource,
    ): Optional<AttestationPublisher> {
      logger.atInfo().log("attestationPublisherFlag: %s", attestationPublisherFlag.mode())
      return when (attestationPublisherFlag.mode()) {
        AttestationPublisherFlag.Mode.FILE_PUBLISHER ->
          Optional.of(
            PrivateInferenceFileAttestationPublisher(
              executor,
              context,
              timeSource,
              endpoint,
              ATTESTATION_PUBLISHER_SUBDIR,
            )
          )
        else -> Optional.empty()
      }
    }
  }
}
