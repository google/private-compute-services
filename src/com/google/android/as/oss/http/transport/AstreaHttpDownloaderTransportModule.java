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
import androidx.annotation.Nullable;
import androidx.annotation.RequiresExtension;
import com.google.android.libraries.net.downloader.DownloaderTransport;
import com.google.common.flogger.GoogleLogger;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import javax.inject.Named;
import javax.inject.Singleton;
import org.chromium.net.CronetEngine;
import org.chromium.net.CronetProvider;

// TODO: Use a delegating transport to allow us to switch between
// PcsHttpDownloaderTransport and OkHttpDownloaderTransport during the process lifetime.
@Module
@InstallIn(SingletonComponent.class)
abstract class PcsHttpDownloaderTransportModule {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Provides
  @Singleton
  @Named("PcsCronet")
  @Nullable
  @RequiresExtension(extension = Build.VERSION_CODES.S, version = 7)
  static CronetEngine provideCronetEngine(@ApplicationContext Context context) {
    try {
      for (CronetProvider provider : CronetProvider.getAllProviders(context)) {
        if (provider.getName().equals(CronetProvider.PROVIDER_NAME_HTTPENGINE_NATIVE)
            && provider.isEnabled()) {
          return provider.createBuilder().enableQuic(true).build();
        }
      }
    } catch (Throwable t) {
      logger.atWarning().withCause(t).log("Failed to initialize HttpEngine CronetEngine");
    }
    return null;
  }

  @Binds
  abstract DownloaderTransport bindDownloaderTransport(
      PcsHttpDownloaderTransport downloaderTransport);
}
