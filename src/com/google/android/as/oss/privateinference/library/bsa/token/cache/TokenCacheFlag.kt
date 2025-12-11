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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache

/** Config flag for controlling the behavior of a token cache. */
interface TokenCacheFlag {
  enum class Mode {
    /** Always perform a full token regeneration flow with each request. */
    NO_CACHE,

    /** Cache tokens in memory to avoid a full token regeneration flow with each request. */
    MEMORY_ONLY,

    /** Cache tokens on disk to avoid a full token regeneration flow with each request. */
    DURABLE_ONLY,

    /** Cache tokens with two layers - memory and disk. */
    DURABLE_AND_MEMORY,
  }

  fun mode(): Mode
}
