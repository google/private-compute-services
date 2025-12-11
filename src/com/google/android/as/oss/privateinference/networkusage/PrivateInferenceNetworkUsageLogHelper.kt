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

package com.google.android.`as`.oss.privateinference.networkusage

import com.google.errorprone.annotations.ThreadSafe

/**
 * Provides helper methods to check the validity of PI network requests and log them in the network
 * usage log.
 */
@ThreadSafe
interface PrivateInferenceNetworkUsageLogHelper {
  enum class IPProtectionRequestType {
    /* Get the proxy configuration from the server. */
    IPP_GET_PROXY_CONFIG,
    /* Get a proxy or terminal anonymous token from the server. */
    IPP_GET_ANONYMOUS_TOKEN,
  }

  /** Returns true if the given feature name is known to the Private Inference service. */
  fun isKnownFeature(featureName: String): Boolean

  /** Logs a Private Inference request for the given feature name. */
  fun logPrivateInferenceRequest(
    featureName: String,
    callingPackageName: String,
    isSuccess: Boolean,
    requestSizeLong: Long,
    responseSizeLong: Long,
  )

  /** Logs an IP Protection request of the given type. */
  fun logIPProtectionRequest(
    requestType: IPProtectionRequestType,
    isSuccess: Boolean,
    requestSizeLong: Long,
    responseSizeLong: Long,
  )
}
