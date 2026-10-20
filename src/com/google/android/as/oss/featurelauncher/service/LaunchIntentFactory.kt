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
import com.google.android.`as`.oss.featurelauncher.api.proto.AddToListParams
import com.google.android.`as`.oss.featurelauncher.api.proto.Feature as ProtoFeature
import com.google.android.`as`.oss.featurelauncher.api.proto.FeatureType
import com.google.android.`as`.oss.featurelauncher.api.proto.RelationshipsParams
import com.google.android.`as`.oss.featurelauncher.api.proto.SaveToKeepNoteParams
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
      FeatureType.FEATURE_TYPE_ADD_TO_KEEP_LIST -> {
        val params = getRequiredAddToListParams(protoFeature) ?: return null
        AndroidIntent(KEEP_ADD_TO_LIST_ACTION).apply {
          setPackage(KEEP_PACKAGE_NAME)
          if (params.listName.isNotBlank()) {
            putExtra(KEEP_EXTRA_TITLE, params.listName)
          }
          if (params.itemsList.isNotEmpty()) {
            putExtra(KEEP_EXTRA_ITEMS, params.itemsList.toTypedArray())
          }
          if (params.reminderTimeMillis > 0L) {
            putExtra(KEEP_EXTRA_TIME, params.reminderTimeMillis)
          }
        }
      }
      FeatureType.FEATURE_TYPE_SAVE_TO_KEEP_NOTE -> {
        val params = getRequiredSaveToKeepNoteParams(protoFeature) ?: return null
        if (params.title.isBlank() && params.text.isBlank()) {
          logger.atWarning().log("Both title and text are blank in saveToKeepNoteParams")
          return null
        }
        AndroidIntent(AndroidIntent.ACTION_SEND).apply {
          setPackage(KEEP_PACKAGE_NAME)
          type = TEXT_PLAIN_MIME_TYPE
          if (params.title.isNotBlank()) {
            putExtra(AndroidIntent.EXTRA_SUBJECT, params.title)
          }
          if (params.text.isNotBlank()) {
            putExtra(AndroidIntent.EXTRA_TEXT, params.text)
          }
          if (params.reminderTimeMillis > 0L) {
            putExtra(KEEP_EXTRA_TIME, params.reminderTimeMillis)
          }
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

  private fun getRequiredAddToListParams(protoFeature: ProtoFeature): AddToListParams? {
    if (!protoFeature.hasAddToListParams()) {
      logger.atWarning().log("Missing addToListParams")
      return null
    }
    return protoFeature.addToListParams
  }

  private fun getRequiredSaveToKeepNoteParams(protoFeature: ProtoFeature): SaveToKeepNoteParams? {
    if (!protoFeature.hasSaveToKeepNoteParams()) {
      logger.atWarning().log("Missing saveToKeepNoteParams")
      return null
    }
    return protoFeature.saveToKeepNoteParams
  }

  private companion object {
    val logger = GoogleLogger.forEnclosingClass()
    const val KEEP_PACKAGE_NAME = "com.google.android.keep"
    const val KEEP_ADD_TO_LIST_ACTION = "com.google.android.keep.intent.action.ADD_TO_LIST"
    const val KEEP_EXTRA_TITLE = "com.google.android.keep.intent.extra.TITLE"
    const val KEEP_EXTRA_ITEMS = "com.google.android.keep.intent.extra.ITEMS"
    const val KEEP_EXTRA_TIME = "android.intent.extra.TIME"
    const val TEXT_PLAIN_MIME_TYPE = "text/plain"
  }
}
