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

import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.http.client.HttpDownloaderClient;
import com.google.android.as.oss.http.config.PcsHttpConfig;
import com.google.android.libraries.net.downloader.DownloaderTransport;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.inject.Inject;

/** {@link DownloaderTransport} for HTTP connections in Pcs. */
public class PcsHttpDownloaderTransport implements DownloaderTransport {

  private final HttpDownloaderClient client;
  private final ConfigReader<PcsHttpConfig> configReader;

  @VisibleForTesting
  @Inject
  public PcsHttpDownloaderTransport(
      HttpDownloaderClient client, ConfigReader<PcsHttpConfig> httpConfigReader) {
    this.client = client;
    this.configReader = httpConfigReader;
  }

  @Override
  public HttpURLConnection createHttpConnection(String url) throws IOException {
    return new PcsHttpConnection(client, new URL(url), configReader);
  }

  /**
   * In S we want to remove internet permission from ASI, and grant it exclusively to Pcs. The
   * Downloader API requires internet permission by default.
   */
  @Override
  public boolean requireInternetPermission() {
    return false;
  }
}
