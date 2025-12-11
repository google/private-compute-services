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

package com.google.android.as.oss.privateinference.config.impl;

import com.google.android.as.oss.common.ExecutorAnnotations.GeneralExecutorQualifier;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.config.FlagManagerFactory;
import com.google.android.as.oss.common.config.FlagNamespace;
import com.google.android.as.oss.privateinference.Annotations.PrivateInferenceAttachCertificateHeader;
import com.google.android.as.oss.privateinference.Annotations.PrivateInferenceEndpointUrl;
import com.google.android.as.oss.privateinference.Annotations.PrivateInferenceWaitForGrpcChannelReady;
import com.google.android.as.oss.privateinference.Annotations.TokenIssuanceEndpointUrl;
import com.google.android.as.oss.privateinference.config.PrivateInferenceConfig;
import com.google.android.as.oss.privateinference.library.oakutil.AttestationPublisherFlag;
import com.google.android.as.oss.privateinference.library.oakutil.DeviceAttestationFlag;
import com.google.android.as.oss.privateinference.transport.TransportFlag;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import java.util.concurrent.Executor;
import javax.inject.Singleton;

/** Module that provides {@link PrivateInferenceConfig} from DeviceConfig flags. */
@Module
@InstallIn(SingletonComponent.class)
interface PrivateInferenceConfigModule {
  @Binds
  ConfigReader<PrivateInferenceConfig> bindConfigReader(PrivateInferenceConfigReader reader);

  @Binds
  @Singleton
  abstract AttestationPublisherFlag bindsAttestationPublisherFlag(
      PcsConfigAttestationPublisherFlag flag);

  @Binds
  @Singleton
  abstract DeviceAttestationFlag bindsDeviceAttestationFlag(PcsConfigAttestationFlag flag);

  @Binds
  @Singleton
  abstract TransportFlag bindsTransportFlag(PcsConfigTransportFlag flag);

  @Binds
  @Singleton
  abstract ArateaAuthFlag bindsArateaAuthFlag(PcsConfigArateaAuthFlag flag);

  @Binds
  @Singleton
  abstract ProxyAuthFlag bindsProxyAuthFlag(PcsConfigProxyAuthFlag flag);

  @Provides
  @Singleton
  static PrivateInferenceConfigReader provideConfigReader(
      FlagManagerFactory flagManagerFactory, @GeneralExecutorQualifier Executor executor) {
    return PrivateInferenceConfigReader.create(
        flagManagerFactory.create(FlagNamespace.DEVICE_PERSONALIZATION_SERVICES, executor));
  }

  @Provides
  @PrivateInferenceEndpointUrl
  static String providePrivateInferenceEndpointUrl(
      ConfigReader<PrivateInferenceConfig> configReader) {
    return configReader.getConfig().endpointUrl();
  }

  @Provides
  @PrivateInferenceWaitForGrpcChannelReady
  static boolean provideWaitForGrpcChannelReady(ConfigReader<PrivateInferenceConfig> configReader) {
    return configReader.getConfig().waitForGrpcChannelReady();
  }

  @Provides
  @PrivateInferenceAttachCertificateHeader
  static boolean provideAttachCertificateHeader(ConfigReader<PrivateInferenceConfig> configReader) {
    return configReader.getConfig().attachCertificateHeader();
  }

  @Provides
  @TokenIssuanceEndpointUrl
  static String provideTokenIssuanceEndpointUrl(ConfigReader<PrivateInferenceConfig> configReader) {
    return configReader.getConfig().tokenIssuanceEndpointUrl();
  }
}
