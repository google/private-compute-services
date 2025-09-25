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

import com.google.android.apps.miphone.pcs.grpc.Annotations.AllowedPackageName;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcService;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceName;
import com.google.android.apps.miphone.pcs.grpc.Annotations.GrpcServiceSecurityPolicy;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.Multibinds;
import io.grpc.BindableService;
import io.grpc.binder.SecurityPolicy;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
abstract class PcsGrpcModule {
  private static final String SERVER_NAME = "pcs_grpc";

  @Multibinds
  @GrpcService
  abstract Set<BindableService> provideBindableService();

  @Multibinds
  @GrpcServiceName
  abstract Set<String> provideServiceName();

  @Multibinds
  @GrpcServiceSecurityPolicy
  abstract Map<String, SecurityPolicy> provideServiceSecurityPolicies();

  @Multibinds
  @AllowedPackageName
  abstract Set<String> provideAllowedPackageNames();

  @Provides
  @Singleton
  static GrpcServerEndpointConfiguration provideConfiguration(
      @GrpcServiceName Set<String> serviceNames,
      @AllowedPackageName Set<String> allowedPackages,
      @GrpcService Lazy<Set<BindableService>> services,
      @GrpcServiceSecurityPolicy Lazy<Map<String, SecurityPolicy>> serviceSecurityPolicies) {
    return new GrpcServerEndpointConfiguration() {
      @Override
      public String getServerName() {
        return SERVER_NAME;
      }

      @Override
      public Set<String> getServiceNames() {
        return serviceNames;
      }

      @Override
      public Set<BindableService> getServices() {
        return services.get();
      }

      @Override
      public Map<String, SecurityPolicy> getServiceSecurityPolicies() {
        return serviceSecurityPolicies.get();
      }

      @Override
      public Set<String> getAllowedPackages() {
        return allowedPackages;
      }
    };
  }
}
