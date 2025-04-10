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

package com.google.android.as.oss.grpc.impl;

import android.content.Context;
import com.google.android.apps.miphone.astrea.grpc.GrpcServerEndpointConfiguration;
import io.grpc.Status;
import io.grpc.binder.SecurityPolicy;
import io.grpc.binder.ServerSecurityPolicy;
import java.util.Map;

final class PcsSecurityPolicies {

  static SecurityPolicy allowlistedOnly(
      Context appContext, GrpcServerEndpointConfiguration configuration) {
    return new SecurityPolicy() {
      @Override
      public Status checkAuthorization(int uid) {
        return isPackageAllowed(appContext, configuration, uid)
            ? Status.OK
            : Status.PERMISSION_DENIED.withDescription("Permission denied by security policy");
      }
    };
  }

  static SecurityPolicy untrustedPolicy() {
    return new SecurityPolicy() {
      @Override
      public Status checkAuthorization(int uid) {
        return Status.OK;
      }
    };
  }

  static ServerSecurityPolicy buildServerSecurityPolicy(
      SecurityPolicy defaultPolicy, GrpcServerEndpointConfiguration configuration) {
    ServerSecurityPolicy.Builder result = ServerSecurityPolicy.newBuilder();
    Map<String, SecurityPolicy> configuredSecurityPolicies =
        configuration.getServiceSecurityPolicies();
    for (String service : configuration.getServiceNames()) {
      result.servicePolicy(
          service, configuredSecurityPolicies.getOrDefault(service, defaultPolicy));
    }
    return result.build();
  }

  private static boolean isPackageAllowed(
      Context appContext, GrpcServerEndpointConfiguration configuration, int uid) {
    String[] packages = appContext.getPackageManager().getPackagesForUid(uid);
    if (packages == null) {
      return false;
    }
    for (String packageName : packages) {
      if (configuration.getAllowedPackages().contains(packageName)) {
        return true;
      }
    }
    return false;
  }

  private PcsSecurityPolicies() {}
}
