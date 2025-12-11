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

import com.google.android.`as`.oss.common.config.AbstractConfigReader
import com.google.android.`as`.oss.common.config.FlagListener
import com.google.android.`as`.oss.common.config.FlagManager

/** Config reader for Delegated UI. */
class DelegatedUiConfigReader(private val flagManager: FlagManager) :
  AbstractConfigReader<DelegatedUiConfig>() {
  init {
    refreshConfig()
    flagManager
      .listenable()
      .addListener(
        FlagListener {
          if (FlagListener.anyHasPrefix(it, DelegatedUiFlags.PREFIX)) {
            refreshConfig()
          }
        }
      )
  }

  override fun computeConfig(): DelegatedUiConfig {
    return DelegatedUiConfig(
      isDelegatedUiEnabled = flagManager.get(DelegatedUiFlags.ENABLE_DUI_SERVICE),
      clientAllowlist = flagManager.get(DelegatedUiFlags.DUI_CLIENT_ALLOWLIST),
      dataServiceConfigList = flagManager.get(DelegatedUiFlags.DUI_DATA_SERVICE_CONFIG_LIST),
      bypassAllSecurityPolicies = flagManager.get(DelegatedUiFlags.BYPASS_ALL_SECURITY_POLICIES),
      enableBugleOutlineAnimationV2 =
        flagManager.get(DelegatedUiFlags.ENABLE_BUGLE_OUTLINE_ANIMATION_V2),
    )
  }
}
