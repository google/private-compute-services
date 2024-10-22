/*
 * Copyright 2024 Google LLC
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

package com.google.android.as.oss.pd.attestation;

import com.google.auto.value.AutoValue;
import com.google.protobuf.ByteString;

/** Response from the attestation client. */
@AutoValue
public abstract class AttestationResponse {
  /** Status of the attestation response. */
  public enum Status {
    SUCCESS,
    NOT_RUN,
    FAILED,
  }

  /** Creates an attestation response with the given attestation token. */
  public static AttestationResponse create(ByteString attestationToken) {
    return new AutoValue_AttestationResponse(attestationToken, Status.SUCCESS);
  }

  /** Creates an empty attestation response. */
  public static AttestationResponse empty() {
    return new AutoValue_AttestationResponse(ByteString.EMPTY, Status.NOT_RUN);
  }

  /** Creates an failed attestation response. */
  public static AttestationResponse failed() {
    return new AutoValue_AttestationResponse(ByteString.EMPTY, Status.FAILED);
  }

  /** Returns the attestation token. */
  public abstract ByteString attestationToken();

  /** Returns the status of the attestation response. */
  public abstract Status status();
}
