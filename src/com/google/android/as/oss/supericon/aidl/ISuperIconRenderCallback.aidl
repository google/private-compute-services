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

package com.google.android.as.oss.supericon.aidl;

import android.graphics.Bitmap;
import android.view.SurfaceControlViewHost;
import com.google.android.as.oss.supericon.aidl.ConversationData;
import com.google.android.as.oss.supericon.aidl.ISuperIconUi;
import com.google.android.as.oss.supericon.utils.SuperIconErrorCodes;
import com.google.android.as.oss.supericon.utils.SuperIconUiType;

/**
 * Callback interface for SuperIcon rendering events.
 *
 * <p>This interface is used to communicate rendering events from the SuperIcon service to the
 * client. More details in [redacted]
 */
oneway interface ISuperIconRenderCallback {


  /**
   * Called when the SuperIcon content is rendered.
   *
   * <p>This method can be called multiple times. For example, it might be called a second time
   * to display a consent dialog if the user clicks the first rendered view and consent is required.
   *
   * @param content the rendered content as a SurfacePackage
   * @param width the width of the rendered content
   * @param height the height of the rendered content
   * @param uiType the type of UI rendered (e.g. SuperIconUiType.SUPER_ICON or SuperIconUiType.CONSENT_DIALOG)
   */
  void onRendered(in ISuperIconUi ui, in SurfaceControlViewHost.SurfacePackage content, int width,
                  int height, @SuperIconUiType int uiType);

  /**
   * Called when the SuperIcon chip is clicked in a non-interactive state.
   *
   * <p>Interaction depends on the render mode:
   * - Standard Chip: Clicking always fires onClick().
   * - Consent Dialog: onClick() is skipped; onConsentGranted/Denied are used instead.
   *
   * @param conversationData the conversation data associated with the SuperIcon
   */
  void onClick(in ConversationData conversationData);

  /**
   * Called when the user grants consent.
   *
   * @param conversationData the conversation data associated with the SuperIcon
   */
  void onConsentGranted(in ConversationData conversationData);

  /**
   * Called when the user denies consent.
   */
  void onConsentDenied();

  /**
   * Called when there is no conversation content available.
   */
  void onError(@SuperIconErrorCodes int errorCode, in String errorMessage);

  /**
   * Called to log the shown/granted/denied event of consent form
   */
  void onConsentMetricsLogged(int eventType, int totalDisplayCount);

  /**
   * Called when a link is clicked in the UI.
   *
   * @param url the url to open in a separate, non-background process through IPC.
   */
  void onLinkClicked(in String url);

  /**
   * Called when the screenshot is received.
   *
   * @param screenshot the screenshot bitmap
   */
  void onScreenshotReceived(in Bitmap screenshot);
};
