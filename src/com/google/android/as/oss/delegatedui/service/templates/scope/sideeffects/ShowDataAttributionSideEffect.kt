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

package com.google.android.`as`.oss.delegatedui.service.templates.scope.sideeffects

import android.app.PendingIntent
import com.google.android.`as`.oss.dataattribution.DataAttributionApi
import com.google.android.`as`.oss.dataattribution.proto.AttributionChipData
import com.google.android.`as`.oss.dataattribution.proto.AttributionDialogData
import com.google.android.`as`.oss.delegatedui.api.infra.dataservice.DelegatedUiUsageData.SemanticsType.SEMANTICS_TYPE_SHOW_DATA_ATTRIBUTION
import com.google.android.`as`.oss.delegatedui.service.templates.scope.InteractionScope
import com.google.android.`as`.oss.delegatedui.service.templates.scope.SideEffectHelper

/** A side-effect that displays a data attribution dialog in PCS. */
interface ShowDataAttributionSideEffect {

  /** Displays a data attribution dialog. See [DataAttributionApi.createDataAttributionIntent]. */
  suspend fun InteractionScope.showDataAttribution(
    attributionDialogData: AttributionDialogData,
    attributionChipData: AttributionChipData?,
    sourceDeepLinks: Array<PendingIntent?>?,
  )
}

class ShowDataAttributionSideEffectImpl(private val helper: SideEffectHelper) :
  ShowDataAttributionSideEffect {

  override suspend fun InteractionScope.showDataAttribution(
    attributionDialogData: AttributionDialogData,
    attributionChipData: AttributionChipData?,
    sourceDeepLinks: Array<PendingIntent?>?,
  ) =
    with(helper) {
      invokeSideEffect(SEMANTICS_TYPE_SHOW_DATA_ATTRIBUTION) {
        val intent =
          DataAttributionApi.createDataAttributionIntent(
            context = context,
            attributionDialogData = attributionDialogData,
            attributionChipData = attributionChipData,
            sourceDeepLinks = sourceDeepLinks,
          )
        context.startActivity(intent)
      }
    }
}
