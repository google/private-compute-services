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

import com.google.android.`as`.oss.common.config.FlagManager.BooleanFlag
import com.google.android.`as`.oss.common.config.FlagManager.ProtoFlag
import com.google.android.`as`.oss.delegatedui.api.config.DelegatedUiDataServiceConfigList

object DelegatedUiFlags {
  const val PREFIX = "DelegatedUi__"
  const val DEFAULT_ENABLE = false
  val ENABLE_DUI_SERVICE = BooleanFlag.create("${PREFIX}enable_dui_service", DEFAULT_ENABLE)
  val DUI_CLIENT_ALLOWLIST =
    ProtoFlag.create(
      "${PREFIX}dui_client_allowlist",
      DelegatedUiConfigConstants.FALLBACK_DUI_CLIENT_ALLOWLIST,
      /* merge= */ false,
    )
  val DUI_DATA_SERVICE_CONFIG_LIST =
    ProtoFlag.create(
      "${PREFIX}dui_data_service_config_list",
      DelegatedUiDataServiceConfigList.getDefaultInstance(),
      /* merge= */ false,
    )
  val BYPASS_ALL_SECURITY_POLICIES =
    BooleanFlag.create("${PREFIX}bypass_all_security_policies", false)
}
