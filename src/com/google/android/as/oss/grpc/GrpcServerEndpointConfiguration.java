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

package com.google.android.apps.miphone.pcs.grpc;

import io.grpc.BindableService;
import io.grpc.binder.SecurityPolicy;
import java.util.Map;
import java.util.Set;

/** Defines configuration necessary for standing up a gRPC server endpoint. */
public interface GrpcServerEndpointConfiguration {
  /** Gets the name of the gRPC server. */
  String getServerName();

  /** Gets a set of the names of the {@link BindableService}s provided by {@link #getServices()}. */
  Set<String> getServiceNames();

  /**
   * Returns a set of actual {@link BindableService} implementations of services to expose via the
   * gRPC server endpoint.
   */
  Set<BindableService> getServices();

  /**
   * Returns a map of service names of the {@link BindableService}s provided by {@link
   * #getServices()}, and a corresponding {@link SecurityPolicy} for each service.
   *
   * <p>It is optional to have a custom security policy for a service. If not present, the default
   * security policy will be used.
   */
  Map<String, SecurityPolicy> getServiceSecurityPolicies();

  /** Returns the set of package names that are allowed to bind to this server. */
  Set<String> getAllowedPackages();
}
