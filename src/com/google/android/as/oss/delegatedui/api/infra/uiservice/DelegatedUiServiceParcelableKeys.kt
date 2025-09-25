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

package com.google.android.`as`.oss.delegatedui.api.infra.uiservice

import android.content.res.Configuration
import android.view.SurfaceControlViewHost.SurfacePackage
import android.window.InputTransferToken
import com.google.android.`as`.oss.delegatedui.utils.ParcelableOverMetadataServerInterceptor
import com.google.android.`as`.oss.delegatedui.utils.SingleParcelableKey

/** Parcelable keys for the Delegated UI Service. */
object DelegatedUiServiceParcelableKeys {

  /** Key for the input transfer token. */
  val INPUT_TRANSFER_TOKEN_KEY =
    SingleParcelableKey("input_transfer_token", InputTransferToken.CREATOR)

  /** Key for the configuration. */
  val CONFIGURATION_KEY = SingleParcelableKey("configuration", Configuration.CREATOR)

  /** Key for the surface package. */
  val SURFACE_PACKAGE_KEY = SingleParcelableKey("surface_package", SurfacePackage.CREATOR)

  /**
   * Interceptor to be set on the Delegated UI service server for receiving and sending Parcelables
   * over headers.
   */
  val SERVER_INTERCEPTOR =
    ParcelableOverMetadataServerInterceptor(
      INPUT_TRANSFER_TOKEN_KEY,
      SURFACE_PACKAGE_KEY,
      CONFIGURATION_KEY,
    )
}
