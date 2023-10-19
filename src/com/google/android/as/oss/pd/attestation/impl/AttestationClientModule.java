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

package com.google.android.as.oss.pd.attestation.impl;

import com.google.android.as.oss.attestation.PccAttestationMeasurementClient;
import com.google.android.as.oss.common.ExecutorAnnotations.AttestationExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.flavor.BuildFlavor;
import com.google.android.as.oss.pd.attestation.AttestationClient;
import com.google.android.as.oss.pd.config.ProtectedDownloadConfig;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

/** Convenience module to provide {@link AttestationClient}. */
@Module
@InstallIn(SingletonComponent.class)
interface AttestationClientModule {
  @Provides
  @Singleton
  static AttestationClient providePdAttestationClient(
      BuildFlavor buildFlavor,
      ConfigReader<ProtectedDownloadConfig> configReader,
      PccAttestationMeasurementClient attestationMeasurementClient,
      @AttestationExecutorQualifier Executor executor) {
    return buildFlavor.isInternal() || configReader.getConfig().enableProtectedDownloadAttestation()
        ? AttestationClientImpl.create(attestationMeasurementClient, executor)
        : new AttestationClientNoopImpl();
  }
}
