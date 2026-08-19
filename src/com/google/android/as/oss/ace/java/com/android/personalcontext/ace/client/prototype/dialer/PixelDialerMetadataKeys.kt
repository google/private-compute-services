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

package com.android.personalcontext.ace.client.prototype.dialer

@Deprecated("Use DialerMetadataHint directly")
object PixelDialerMetadataKeys {
  /** The hint type name for the BundleHint containing PixelDialerMetadata proto. */
  const val HINT_TYPE_NAME_PIXEL_DIALER_METADATA = "PixelDialerMetadataHint"
  /** The key for the PixelDialerMetadata proto within a BundleHint. */
  const val KEY_PIXEL_DIALER_METADATA = "pixel_dialer_metadata"
}
