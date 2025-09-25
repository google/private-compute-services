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

package com.google.android.`as`.oss.delegatedui.config

import com.google.android.`as`.oss.common.security.api.packageSecurityInfo
import com.google.android.`as`.oss.common.security.api.packageSecurityInfoList

/** Constants for Delegated UI configs. */
internal object DelegatedUiConfigConstants {

  // The client allowlist for the Delegated UI service.
  val FALLBACK_DUI_CLIENT_ALLOWLIST = packageSecurityInfoList {
    packageSecurityInfos +=
      listOf(
        packageSecurityInfo {
          packageName = "com.google.android.dialer"
          allowedReleaseKeys +=
            listOf("e2d049f3a01192f620b1240615fa8c13badc553c22bc6fddfca45c84d8fc545d")
        },
        packageSecurityInfo {
          packageName = "com.google.android.apps.messaging"
          allowedReleaseKeys +=
            listOf("cc75526d6c0f80ac3b3a84eb44838440dbbe6b124443a7c7d7bd19182ac022c9")
        },
        packageSecurityInfo {
          packageName = "com.google.android.apps.weather"
          allowedReleaseKeys +=
            listOf("7751c458da05ba9100ad73926e3e50aae3ca63183c6aaeb21d225883439f8aa1")
        },
        packageSecurityInfo {
          packageName = "com.google.android.apps.pixel.subzero"
          allowedReleaseKeys +=
            listOf("d801de6b2c6cdfe425f29537b11f3d6ba4195bc7c76d03d23449ad1c1aa78eb4")
        },
      )
  }
}
