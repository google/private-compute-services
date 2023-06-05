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

package com.google.android.as.oss.attestation;

import com.google.android.as.oss.attestation.api.proto.AttestationMeasurementResponse;
import com.google.common.util.concurrent.ListenableFuture;

/** A client interface providing high-level API for Attestation measurement through PCS. */
public interface PccAttestationMeasurementClient {
  String ATTESTATION_FEATURE_NAME = "KEY_ATTESTATION";

  /** Initializes an asynchronous request to generate an {@link AttestationMeasurementResponse}. */
  ListenableFuture<AttestationMeasurementResponse> requestAttestationMeasurement(
      AttestationMeasurementRequest attestationRequest);
}
