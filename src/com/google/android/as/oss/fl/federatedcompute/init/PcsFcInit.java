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

package com.google.android.as.oss.fl.federatedcompute.init;

import com.google.android.as.oss.fl.federatedcompute.config.PcsFcFlags;
import com.google.android.as.oss.fl.federatedcompute.logging.FcLogManager;
import com.google.fcp.client.AttestationClient;
import com.google.fcp.client.FCFatSdkConfig;
import com.google.fcp.client.FCInit;
import com.google.fcp.client.DynamicFlags;
import com.google.fcp.client.LogManager;
import com.google.common.flogger.GoogleLogger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Centralizes PCS's federated compute initialization settings. This must be executed on app startup
 * (from {@link android.app.Application#onCreate()}).
 */
public final class PcsFcInit {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final String CLIENT_NAME = "astrea";
  // Name of the custom TensorFlow native lib that contains a selection of regular TensorFlow and
  // custom ops necessary for local computation tasks.
  private static final String TENSORFLOW_NATIVE_LIB = "pcs_tensorflow_jni";

  public static void init(
      PcsFcFlags pcsFcFlags,
      FcLogManager fcLogManager,
      @Nullable AttestationClient fcAttestationClient) {
    logger.atInfo().log("Calling FCInit for PCS.");
    FCInit.setFatSdkConfig(
        new FCFatSdkConfig() {
          @Override
          public boolean loadCustomNativeLibrary() {
            System.loadLibrary(TENSORFLOW_NATIVE_LIB);
            return true;
          }

          @Override
          public String getClientName() {
            return CLIENT_NAME;
          }

          @Override
          public @Nullable DynamicFlags getDynamicFlagsOverride() {
            if (pcsFcFlags.enableDeviceConfigOverrides()) {
              return pcsFcFlags;
            }
            return null;
          }

          @Override
          public @Nullable LogManager getLogManagerOverride() {
            if (pcsFcFlags.enableLoggingOverride()) {
              return fcLogManager;
            }
            return null;
          }

          @Override
          public @Nullable AttestationClient getAttestationClientOverride() {
            return fcAttestationClient;
          }
        });
    FCInit.myAppCanHandleMultipleProcesses();
  }

  private PcsFcInit() {}
}
