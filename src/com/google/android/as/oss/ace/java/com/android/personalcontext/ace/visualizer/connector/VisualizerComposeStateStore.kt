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

package com.android.personalcontext.ace.visualizer.connector

import android.util.LruCache
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-level store for Compose saved states keyed by session UUID.
 *
 * Scoped as [@Singleton] so that saved Compose states survive [PcsVisualizerService] unbind and
 * rebind cycles. Uses an [LruCache] to prevent unbounded memory growth across long-lived sessions.
 */
@Singleton
class VisualizerComposeStateStore @Inject constructor() {

  private val savedStates = object : LruCache<UUID, Map<String, List<Any?>>>(MAX_CACHED_SESSIONS) {}

  @Synchronized operator fun get(key: UUID): Map<String, List<Any?>>? = savedStates.get(key)

  @Synchronized
  operator fun set(key: UUID, state: Map<String, List<Any?>>) {
    savedStates.put(key, state)
  }

  @Synchronized
  fun remove(key: UUID) {
    savedStates.remove(key)
  }

  companion object {
    private const val MAX_CACHED_SESSIONS = 64
  }
}
