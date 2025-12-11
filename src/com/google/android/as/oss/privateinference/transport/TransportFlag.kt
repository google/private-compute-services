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

package com.google.android.`as`.oss.privateinference.transport

/** A flag to control device attestation. */
interface TransportFlag {
  enum class Mode {
    /** The transport is unspecified, a default will be chosen. */
    UNSPECIFIED,

    /** Use the OK HTTP transport. */
    OK_HTTP,

    /** Use Cronet (mainline version) transport. */
    CRONET_MAINLINE,

    /** Use statically linked Cronet. */
    CRONET_STATIC,

    /** Use statically linked Cronet with IP Relay. */
    CRONET_STATIC_IP_RELAY,
  }

  fun mode(): Mode
}

/** Configuration for the proxy used for IP Relay. */
data class ProxyConfiguration(val host: String, val port: Int, val authHeader: String)
