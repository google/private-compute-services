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

package com.google.android.as.oss.pd.attestation.impl;

import com.google.android.as.oss.attestation.AttestationMeasurementRequest;
import com.google.android.as.oss.attestation.PccAttestationMeasurementClient;
import com.google.android.as.oss.pd.attestation.AttestationClient;
import com.google.android.as.oss.pd.attestation.AttestationResponse;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

/**
 * An Implementation of {@link AttestationClient}, that is used for generating an attestation
 * measurement for protected downloads.
 */
public class AttestationClientImpl implements AttestationClient {
  private final PccAttestationMeasurementClient attestationMeasurementClient;
  private final Executor executor;

  static AttestationClientImpl create(
      PccAttestationMeasurementClient attestationMeasurementClient, Executor executor) {
    return new AttestationClientImpl(attestationMeasurementClient, executor);
  }

  @Override
  public ListenableFuture<AttestationResponse> requestMeasurementWithContentBinding(
      String contentBinding) {
    AttestationMeasurementRequest request =
        AttestationMeasurementRequest.builder()
            .setContentBinding(contentBinding)
            .setIncludeIdAttestation(true)
            .build();

    return FluentFuture.from(attestationMeasurementClient.requestAttestationMeasurement(request))
        .transform(response -> AttestationResponse.create(response.toByteString()), executor);
  }

  private AttestationClientImpl(
      PccAttestationMeasurementClient attestationMeasurementClient, Executor executor) {
    this.attestationMeasurementClient = attestationMeasurementClient;
    this.executor = executor;
  }
}
