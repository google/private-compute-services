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

package com.google.android.`as`.oss.privateinference.config.impl

import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import javax.inject.Inject

/** Use pcs PrivateInferenceConfig config to control Aratea auth mode. */
class PcsConfigArateaAuthFlag
@Inject
internal constructor(val config: ConfigReader<PrivateInferenceConfig>) : ArateaAuthFlag {
  override fun mode(): ArateaAuthFlag.Mode {
    return config.config.arateaAuthMode()
  }

  override fun isCacheEnabled(): Boolean {
    return config.config.enableArateaTokenCache()
  }
}
