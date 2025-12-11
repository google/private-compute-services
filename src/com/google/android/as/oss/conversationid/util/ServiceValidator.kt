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

package com.google.android.`as`.oss.conversationid.util

import android.content.Context
import android.os.Build
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.SecurityPolicyUtils
import com.google.android.`as`.oss.common.security.api.PackageSecurityInfo
import com.google.android.`as`.oss.common.security.api.PackageSecurityInfoList
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.conversationid.config.ConversationIdConfig

/**
 * Validates binder caller's app signature. Only allow Gboard and AiAi to call the ConversationId
 * service at this moment.
 */
class ServiceValidator(
  private val configReader: ConfigReader<ConversationIdConfig>,
  private val securityPolicyConfigReader: ConfigReader<PccSecurityConfig>,
) {
  fun isPixel(): Boolean {
    return Build.BRAND.contains("google", ignoreCase = true)
  }

  fun validateGboardCaller(context: Context, callingUid: Int): Boolean {
    return validateCaller(
      context,
      callingUid,
      securityPolicyConfigReader.config.gboardPackageSecurityInfo(),
    )
  }

  fun validateAiAiCaller(context: Context, callingUid: Int): Boolean {
    return validateCaller(
      context,
      callingUid,
      securityPolicyConfigReader.config.asiPackageSecurityInfo(),
    )
  }

  private fun validateCaller(
    context: Context,
    callingUid: Int,
    packageSecurityInfo: PackageSecurityInfo,
  ): Boolean {
    if (!configReader.config.enableSecurityPolicy) {
      return true // Skip security policy check.
    }
    return SecurityPolicyUtils.isCallerAuthorized(
      PackageSecurityInfoList.newBuilder().addPackageSecurityInfos(packageSecurityInfo).build(),
      context,
      callingUid,
      /* allowTestKeys= */ true,
    )
  }
}
