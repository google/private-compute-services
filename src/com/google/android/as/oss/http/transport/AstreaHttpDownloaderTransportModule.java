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

package com.google.android.as.oss.http.transport;

import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresExtension;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.http.config.PcsHttpConfig;
import com.google.android.libraries.net.downloader.CronetDownloaderTransport;
import com.google.android.libraries.net.downloader.DownloaderTransport;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.chromium.net.CronetEngine;
import org.chromium.net.CronetProvider;

// TODO: Use a delegating transport to allow us to switch between
// PcsHttpDownloaderTransport and OkHttpDownloaderTransport during the process lifetime.
@Module
@InstallIn(SingletonComponent.class)
abstract class PcsHttpDownloaderTransportModule {

  private static final String HTTP_ENGINE_NATIVE_PROVIDER =
      "org.chromium.net.impl.HttpEngineNativeProvider";

  @Provides
  @Singleton
  @Named("PcsCronet")
  @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
  static CronetEngine provideCronetEngine(@ApplicationContext Context context) {
    for (CronetProvider provider : CronetProvider.getAllProviders(context)) {
      if (provider.getClass().getName().equals(HTTP_ENGINE_NATIVE_PROVIDER)
          && provider.isEnabled()) {
        return provider.createBuilder().enableQuic(true).build();
      }
    }
    throw new IllegalStateException("HttpEngineNativeProvider not found or not enabled");
  }

  @Provides
  static DownloaderTransport provideDownloaderTransport(
      ConfigReader<PcsHttpConfig> configReader,
      @Named("PcsCronet") Provider<CronetEngine> cronetEngineProvider,
      Provider<PcsHttpDownloaderTransport> pcsTransportProvider) {
    if (configReader.getConfig().enableCronetMigration()) {
      return new CronetDownloaderTransport(cronetEngineProvider.get());
    }
    return pcsTransportProvider.get();
  }
}
