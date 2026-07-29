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

package com.google.android.`as`.oss.featurelauncher.service

import android.content.Intent as AndroidIntent
import com.google.android.apps.pixel.relationships.gateway.RelationshipsGatewayConstants
import com.google.android.apps.pixel.relationships.onboarding.removevipconfirmation.RemoveVipConfirmationConstants
import com.google.android.`as`.oss.featurelauncher.api.proto.Feature as ProtoFeature
import com.google.android.`as`.oss.featurelauncher.api.proto.FeatureType
import com.google.android.`as`.oss.featurelauncher.api.proto.RelationshipsParams
import com.google.common.flogger.GoogleLogger
import javax.inject.Inject

/** Factory for constructing launch intents of different features. */
internal class LaunchIntentFactory @Inject constructor() {

  fun createLaunchIntent(protoFeature: ProtoFeature): AndroidIntent? {
    return when (protoFeature.featureType) {
      FeatureType.FEATURE_TYPE_RELATIONSHIPS_ONBOARDING_CONSENT -> {
        AndroidIntent(RelationshipsGatewayConstants.ACTION_ONBOARDING_CONSENT)
      }
      FeatureType.FEATURE_TYPE_RELATIONSHIPS_SETTINGS -> {
        AndroidIntent(RelationshipsGatewayConstants.ACTION_SETTINGS)
      }
      FeatureType.FEATURE_TYPE_RELATIONSHIPS_REMOVE_VIP_CONFIRMATION -> {
        val params = getRequiredRelationshipsParams(protoFeature) ?: return null
        AndroidIntent(RemoveVipConfirmationConstants.ACTION_REMOVE_VIP_CONFIRMATION).apply {
          putExtra(RemoveVipConfirmationConstants.EXTRA_CONTACT_ID, params.contactId)
        }
      }
      else -> {
        logger
          .atWarning()
          .log("Rejecting generic/unknown feature type: %s", protoFeature.featureType)
        null
      }
    }
  }

  private fun getRequiredRelationshipsParams(protoFeature: ProtoFeature): RelationshipsParams? {
    if (!protoFeature.hasRelationshipsParams()) {
      logger.atWarning().log("Missing relationshipsParams")
      return null
    }
    return protoFeature.relationshipsParams
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
  }
}
