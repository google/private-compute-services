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

package com.google.android.`as`.oss.privateinference.logging

/** Implementation of duration bucket logic for Private Inference. */
object DurationBucketLogic {
  private val OFFSET: Double = 10.0

  private val LOG_BASE: Double = Math.log(1.1)

  private val SHIFT: Double = 23.0

  @JvmStatic
  fun snapDurationToBucket(durationMillis: Long): Int {
    return approximateDurationMs(encodeDurationToBucket(durationMillis)).toInt()
  }

  private fun encodeDurationToBucket(durationMillis: Long): Int {
    if (durationMillis < 0) {
      return 0 // Unknown duration.
    }
    val bucketIndex = Math.round(Math.log(OFFSET + durationMillis) / LOG_BASE - SHIFT)
    return if (bucketIndex > 200) {
      200
    } else {
      bucketIndex.toInt()
    }
  }

  private fun approximateDurationMs(durationBucket: Int): Long {
    return Math.round(Math.pow(Math.exp(durationBucket + SHIFT), LOG_BASE) - OFFSET)
  }
}
