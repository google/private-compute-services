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

package com.google.android.as.oss.privateinference;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Qualifier;

/** Annotations for Private Inference. */
public final class Annotations {

  /** Annotation for the endpoint URL of the Private Inference server. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PrivateInferenceEndpointUrl {}

  /** Annotation for whether to wait for the channel to be ready. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PrivateInferenceWaitForGrpcChannelReady {}

  /** Annotation for whether to attach Android package name and certificate to the request. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PrivateInferenceAttachCertificateHeader {}

  /** Annotation for the gRPC channel to the Private Inference server. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PrivateInferenceServerGrpcChannel {}

  /** Annotation for the proxy configuration. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface PrivateInferenceProxyConfiguration {}

  /** Annotation for the gRPC channel to the token issuance server. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TokenIssuanceServerGrpcChannel {}

  /** Annotation for the endpoint URL of the token issuance server. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface TokenIssuanceEndpointUrl {}

  /** An executor to run tasks for saving attestation evidence. */
  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  public @interface AttestationPublisherExecutor {}

  private Annotations() {}
}
