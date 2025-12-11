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
 * An interface for starting a particular kind of timer.
 *
 * This interface is intentional sparse, leaving most of the implementation control up to the
 * implementor.
 *
 * One [Timers] instance manages named timers for a particular timing implementation. Calling
 * [Timers.start] will start a new timer, and return a [Timer] instance that can be used to stop or
 * cancel it.
 *
 * IF you want to support multiple timing implementations (for example, log output and system traces
 * and Primes), check out [TimerSet], which makes it easy to manage a set of [Timers]
 * implementations all together.
 */
interface Timers {
  /** An active timer created by {@link #start}. */
  interface Timer : AutoCloseable {
    override fun close() {
      // This is a no-op if the timer is already stopped.
      stop()
    }

    /**
     * Stops the previously-started timer.
     *
     * The behavior when stop is called multiple times is implementation-dependent.
     */
    fun stop()
  }

  /** Starts a timer with the given name. */
  fun start(@CompileTimeConstant name: String): Timer

  companion object {
    /** A no-op timer that doesn't do anything. */
    val NOP_TIMER = timer {}
  }
}

/**
 * A convenience method for creating a [Timer] from a lambda for [Timer.stop].
 *
 * Allows you to create a timer with a simpler Kotlin syntax:
 * ```
 * val timer = Timers.Timer { myActualTimerImpl.stop() }
 * ```
 */
fun timer(stop: () -> Unit) =
  object : Timers.Timer {
    override fun stop() {
      stop()
    }
  }
