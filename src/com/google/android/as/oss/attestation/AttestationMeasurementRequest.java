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

import com.google.auto.value.AutoValue;
import java.time.Duration;
import java.util.Optional;

/** Attestation measurement request builder. */
@AutoValue
public abstract class AttestationMeasurementRequest {
  public static Builder builder() {
    return new AutoValue_AttestationMeasurementRequest.Builder();
  }
  /**
   * The ttl is used to set how long the requested challenge should be valid. After the set ttl, the
   * challenge cannot be used to generate a valid {@link
   * com.google.android.as.oss.attestation.api.proto.AttestationMeasurementResponse}. The ttl must
   * be less than 24 hours and is required.
   */
  public abstract Duration ttl();

  /** This is an optional non empty payload string sent alongside an attestation request. */
  public abstract Optional<String> contentBinding();

  /** Attestation request builder. */
  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTtl(Duration value);

    public abstract Builder setContentBinding(String value);

    public abstract AttestationMeasurementRequest build();
  }
}
