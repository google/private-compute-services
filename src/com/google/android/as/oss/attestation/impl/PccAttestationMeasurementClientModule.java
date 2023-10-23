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

package com.google.android.as.oss.attestation.impl;

import static java.util.concurrent.TimeUnit.MINUTES;

import android.content.Context;
import com.google.android.as.oss.attestation.PccAttestationMeasurementClient;
import com.google.android.as.oss.common.ExecutorAnnotations.AttestationExecutorQualifier;
import com.google.android.as.oss.common.time.TimeSource;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.okhttp.OkHttpChannelBuilder;
import io.grpc.stub.MetadataUtils;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.inject.Singleton;

/** Convenience module to provide {@link PccAttestationMeasurementClient}. */
@Module
@InstallIn(SingletonComponent.class)
final class PccAttestationMeasurementClientModule {
  private static final String ATTESTATION_API_HOST =
      "androidattestationvalidation-pa.googleapis.com";
  private static final int ATTESTATION_API_PORT = 443;

  @Provides
  @Singleton
  static PccAttestationMeasurementClient providePccAttestationMeasurementClient(
      @AttestationExecutorQualifier Executor attestationExecutor,
      NetworkUsageLogRepository networkUsageLogRepository,
      TimeSource timeSource,
      PcsStatsLog pcsStatsLogger,
      @ApplicationContext Context context) {
    ManagedChannel managedChannel =
        OkHttpChannelBuilder.forAddress(ATTESTATION_API_HOST, ATTESTATION_API_PORT)
            .executor(Executors.newSingleThreadExecutor())
            .idleTimeout(1, MINUTES)
            .build();
    return new PccAttestationMeasurementClientImpl(
        attestationExecutor,
        managedChannel,
        networkUsageLogRepository,
        timeSource,
        pcsStatsLogger,
        context);
  }

  private PccAttestationMeasurementClientModule() {}
}
