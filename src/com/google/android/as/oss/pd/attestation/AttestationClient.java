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

package com.google.android.as.oss.pd.attestation;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;

/** An interface for a class that generates an attestation measurement. */
public interface AttestationClient {

  /**
   * Requests an attestation measurement from the client. The measurement will be passed to the
   * server for validation.
   *
   * <p>Returns opaque attestation token that server will decode for validation.
   */
  ListenableFuture<ByteString> requestMeasurementWithContentBinding(String contentBinding);
}
