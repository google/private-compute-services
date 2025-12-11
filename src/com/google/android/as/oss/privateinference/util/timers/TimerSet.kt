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

package com.google.android.`as`.oss.privateinference.util.timers

import com.google.errorprone.annotations.CompileTimeConstant

/**
 * A wrapper around a set of timer implementations starts/stops all of them together.
 *
 * This class is intended to make it easy to add/remove various timer implementations using
 * dependency injection. You can inject a single `TimerSet` instance, and its binding method will be
 * populated by the timers bound into a set.
 */
class TimerSet(private val timers: Set<Timers>) : Timers {
  /**
   * Start a new timer using all of the provided [Timers] implementations.
   *
   * @param name The name of the timer. All of the provided [Timers] will start a new [Timer]
   *   instance with this name.
   * @return A [Timer] that wraps all of the started timers.
   */
  override fun start(@CompileTimeConstant name: String): Timers.Timer {
    val startedTimers = timers.map { it.start(name) }
    return timer {
      for (timer in startedTimers) {
        timer.stop()
      }
    }
  }
}
