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

package com.google.android.as.oss.attestation;

import com.google.auto.value.AutoValue;
import java.util.Optional;

/** Attestation measurement request builder. */
@AutoValue
public abstract class AttestationMeasurementRequest {
  public static Builder builder() {
    return new AutoValue_AttestationMeasurementRequest.Builder();
  }

  /** This is an optional non empty payload string sent alongside an attestation request. */
  public abstract Optional<String> contentBinding();

  /**
   * This is used to request Device ID attestation alongside the regular key attestation. For more
   * details about ID attestation, see
   * https://source.android.com/docs/security/features/keystore/attestation#id-attestation.
   */
  public abstract Optional<Boolean> includeIdAttestation();

  /**
   * This is used to specify the key algorithm used for the attestation. The supported algorithms
   * are, for example, "EC" for Elliptic Curve and "RSA" for RSA.
   */
  public abstract Optional<String> keyAlgorithm();

  /** Attestation request builder. */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setContentBinding(String value);

    public abstract Builder setIncludeIdAttestation(boolean value);

    public abstract Builder setKeyAlgorithm(String value);

    public abstract AttestationMeasurementRequest build();
  }
}
