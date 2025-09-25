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

package com.google.android.`as`.oss.delegatedui.service.templates.beacon

import android.app.PendingIntent
import com.google.android.`as`.oss.delegatedui.api.integration.templates.beacon.BeaconResponseSource

/** Common utils for the [BeaconTemplateRenderer] */
object BeaconCommonUtils {
  /**
   * Returns the [PendingIntent] for the given [BeaconResponseSource]. If the [BeaconResponseSource]
   * does not have a pending intent index, returns null.
   */
  fun getPendingIntent(pendingIntents: List<PendingIntent>, dataSource: BeaconResponseSource) =
    if (dataSource.hasPendingIntentIndex()) {
      pendingIntents.getOrNull(
        dataSource.pendingIntentIndex - 1
      ) // 1-based index, so -1 to get the true index
    } else {
      null
    }
}
