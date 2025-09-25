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

package com.google.android.as.oss.grpc.impl;

import static com.google.android.as.oss.grpc.impl.PcsSecurityPolicies.allowlistedOnly;
import static com.google.android.as.oss.grpc.impl.PcsSecurityPolicies.buildServerSecurityPolicy;
import static com.google.android.as.oss.grpc.impl.PcsSecurityPolicies.untrustedPolicy;

import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.security.api.PackageSecurityInfo;
import com.google.android.as.oss.common.security.config.PccSecurityConfig;
import com.google.android.apps.miphone.pcs.grpc.GrpcServerEndpointConfiguration;
import com.google.android.apps.miphone.pcs.grpc.GrpcServerEndpointConfigurator;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import io.grpc.BindableService;
import io.grpc.CompressorRegistry;
import io.grpc.DecompressorRegistry;
import io.grpc.Server;
import io.grpc.binder.AndroidComponentAddress;
import io.grpc.binder.BinderServerBuilder;
import io.grpc.binder.IBinderReceiver;
import io.grpc.binder.InboundParcelablePolicy;
import io.grpc.binder.ServerSecurityPolicy;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;

/**
 * Configurator used to build an {@link IBinder} for exposing {@link BindableService}
 * implementations provided via a {@link GrpcServerEndpointConfiguration}.
 *
 * <p>TODO: Pick an executor for {@link BinderServerBuilder}.
 *
 * <p>TODO: Use LifecycleOnDestroyHelper to ensure any ongoing calls are cancelled if the service
 * goes down.
 */
@Module
@InstallIn(SingletonComponent.class)
final class GrpcServerEndpointConfiguratorImpl implements GrpcServerEndpointConfigurator {
  private final ConfigReader<PccSecurityConfig> pccSecurityConfigReader;

  @Inject
  GrpcServerEndpointConfiguratorImpl(ConfigReader<PccSecurityConfig> pccSecurityConfigReader) {
    this.pccSecurityConfigReader = pccSecurityConfigReader;
  }

  @Override
  public Server buildOnDeviceServerEndpoint(
      Context context,
      Class<?> cls,
      GrpcServerEndpointConfiguration configuration,
      IBinderReceiver iBinderReceiver)
      throws IOException {
    return buildAndStartOnDeviceServer(context, cls, configuration, iBinderReceiver);
  }

  private Server buildAndStartOnDeviceServer(
      Context context,
      Class<?> cls,
      GrpcServerEndpointConfiguration configuration,
      IBinderReceiver iBinderReceiver)
      throws IOException {

    PccSecurityConfig pccSecurityConfig = pccSecurityConfigReader.getConfig();
    ServerSecurityPolicy serverSecurityPolicy;
    if (pccSecurityConfig.enableAllowlistedOnly()) {
      List<PackageSecurityInfo> packageSecurityInfos =
          pccSecurityConfig.securityInfoList().getPackageSecurityInfosList();
      serverSecurityPolicy =
          buildServerSecurityPolicy(allowlistedOnly(context, packageSecurityInfos), configuration);
    } else {
      serverSecurityPolicy = buildServerSecurityPolicy(untrustedPolicy(), configuration);
    }

    BinderServerBuilder builder =
        BinderServerBuilder.forAddress(
                AndroidComponentAddress.forLocalComponent(context, cls), iBinderReceiver)
            .securityPolicy(serverSecurityPolicy)
            .inboundParcelablePolicy(buildInboundParcelablePolicy())
            .intercept(new MetadataExtractionServerInterceptor());

    // Disable compression by default, since there's little benefit when all communication
    // is
    // on-device, and it means sending supported-encoding headers with every call.
    builder
        .decompressorRegistry(DecompressorRegistry.emptyInstance())
        .compressorRegistry(CompressorRegistry.newEmptyInstance());

    for (BindableService service : configuration.getServices()) {
      builder.addService(service);
    }
    Server server = builder.build();
    server.start();
    return server;
  }

  private InboundParcelablePolicy buildInboundParcelablePolicy() {
    return InboundParcelablePolicy.newBuilder().setAcceptParcelableMetadataValues(true).build();
  }
}
